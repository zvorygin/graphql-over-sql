package graphql.sql.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.healthmarketscience.sqlbuilder.InCondition;
import com.healthmarketscience.sqlbuilder.QueryPreparer;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.execution.ExecutionContext;
import graphql.execution.FieldCollector;
import graphql.language.*;
import graphql.schema.*;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityQuery;
import graphql.sql.core.config.domain.ScalarType;
import graphql.sql.core.config.domain.type.TypeUtil;
import graphql.sql.core.querygraph.QueryNode;
import graphql.sql.core.querygraph.QueryRoot;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static graphql.introspection.Introspection.SchemaMetaFieldDef;
import static graphql.introspection.Introspection.TypeMetaFieldDef;

public class OperationExecutor {
    private final GraphQLTypesProvider typesProvider;
    private final Config config;
    private final GraphQLQueryExecutorBuilder graphQLQueryExecutorBuilder;
    private final DataSource dataSource;

    private final FieldCollector fieldCollector = new FieldCollector();
    private final LoadingCache<DocumentContext, Cache<OperationKey, Map<Field, FieldExecutor>>> cache;
    private final long maxOperationsPerDocument;


    public OperationExecutor(GraphQLTypesProvider typesProvider,
                             Config config,
                             GraphQLQueryExecutorBuilder graphQLQueryExecutorBuilder,
                             DataSource dataSource,
                             long maxOperationsPerDocument,
                             int maximumCacheSize) {
        this.typesProvider = typesProvider;
        this.config = config;
        this.graphQLQueryExecutorBuilder = graphQLQueryExecutorBuilder;
        this.dataSource = dataSource;
        this.maxOperationsPerDocument = maxOperationsPerDocument;
        cache = CacheBuilder.newBuilder()
                .maximumSize(maximumCacheSize)
                .build(CacheLoader.from(this::buildOperationCache));
    }

    public ExecutionResult execute(DocumentContext documentContext,
                                   OperationDefinition operationDefinition,
                                   Map<String, Object> variables) {
        OperationKey operationKey = buildOperationKey(documentContext, operationDefinition, variables);

        Cache<OperationKey, Map<Field, FieldExecutor>> documentOperationCache = cache.getUnchecked(documentContext);

        Map<Field, FieldExecutor> queryFields;

        try {
            queryFields = documentOperationCache.get(operationKey,
                    () -> buildOperationContext(documentContext, operationDefinition, variables));
        } catch (ExecutionException e) {
            return new ExecutionResultImpl(Collections.singletonList(new ExceptionWhileDataFetching(e.getCause())));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            for (Map.Entry<Field, FieldExecutor> entry : queryFields.entrySet()) {
                Field field = entry.getKey();
                FieldExecutor fieldExecutor = entry.getValue();
                String name = field.getAlias() == null ? field.getName() : field.getAlias();
                result.put(name, fieldExecutor.execute(conn, variables));

            }
            return new ExecutionResultImpl(result, Collections.emptyList());
        } catch (SQLException e) {
            return new ExecutionResultImpl(Collections.singletonList(new ExceptionWhileDataFetching(e)));
        }
    }

    private Map<Field, FieldExecutor> buildOperationContext(DocumentContext documentContext,
                                                            OperationDefinition operationDefinition,
                                                            Map<String, Object> variables) {
        GraphQLSchema schema = typesProvider.getSchema();
        ExecutionContext executionContext = buildExecutionContext(documentContext, operationDefinition, variables, new FieldExecutionStrategy());

        LinkedHashMap<String, List<Field>> fields = new LinkedHashMap<>();
        fieldCollector.collectFields(executionContext, schema.getQueryType(), operationDefinition.getSelectionSet(), new ArrayList<>(), fields);

        Map<Field, FieldExecutor> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<Field>> entry : fields.entrySet()) {
            FieldExecutor fieldExecutor;
            Field field = Iterables.getOnlyElement(entry.getValue());

            if (field.getName().equals(SchemaMetaFieldDef.getName())) {
                fieldExecutor = getStaticFieldExecutor(SchemaMetaFieldDef, field, documentContext, variables, operationDefinition);
            } else if (field.getName().equals(TypeMetaFieldDef.getName())) {
                fieldExecutor = getStaticFieldExecutor(TypeMetaFieldDef, field, documentContext, variables, operationDefinition);
            } else {
                fieldExecutor = getSqlFieldExecutor(schema, executionContext, field);
            }

            result.put(field, fieldExecutor);
        }

        return result;
    }

    private FieldExecutor getStaticFieldExecutor(GraphQLFieldDefinition fieldDefinition, Field field, DocumentContext documentContext, Map<String, Object> variables, OperationDefinition operationDefinition) {
        FieldExecutionStrategy fieldExecutionStrategy = new FieldExecutionStrategy();
        Object value = fieldExecutionStrategy.executeField(fieldDefinition, field,
                buildExecutionContext(documentContext, operationDefinition, variables, fieldExecutionStrategy));

        return new StaticFieldExecutor(value);
    }

    @Nonnull
    private ExecutionContext buildExecutionContext(DocumentContext documentContext, OperationDefinition operationDefinition, Map<String, Object> variables, FieldExecutionStrategy queryStrategy) {
        return new ExecutionContext(
                typesProvider.getSchema(),
                queryStrategy,
                null,
                documentContext.getFragmentsByName(),
                operationDefinition,
                variables,
                null
        );
    }

    @Nonnull
    private SqlFieldExecutor getSqlFieldExecutor(GraphQLSchema schema, ExecutionContext executionContext, Field queryField) {
        SqlFieldExecutor fieldExecutor;
        EntityQuery entityQuery = config.getQuery(queryField.getName());
        GraphQLQueryExecutor executor = graphQLQueryExecutorBuilder.build(entityQuery.getEntity(), queryField, executionContext);

        QueryRoot queryGraph = executor.getQueryGraph();
        SelectQuery selectQuery = queryGraph.getSqlQueryNode().buildSelectQuery();

        List<EntityField> rootFieldConditions = entityQuery.getEntityFields();
        if (rootFieldConditions.size() != queryField.getArguments().size()) {
            throw new IllegalArgumentException(String.format(
                    "Expected [%d] input parameters but got [%d] instead",
                    rootFieldConditions.size(), queryField.getArguments().size()));
        }

        if (rootFieldConditions.size() != 1) {
            throw new IllegalArgumentException("Not yet implemented");
        }

        EntityField entityField = Iterables.getOnlyElement(entityQuery.getEntityFields());
        Argument argument = Iterables.getOnlyElement(queryField.getArguments());
        GraphQLArgument argumentType = Iterables.getOnlyElement(schema.getQueryType().getFieldDefinition(queryField.getName()).getArguments());
        GraphQLInputType argumentValueType = unwrapNonNull(argumentType.getType());

        if (!(argumentValueType instanceof GraphQLList)) {
            throw new IllegalArgumentException("Not yet implemented");
        }


        Map<String, QueryPreparer.PlaceHolder> placeholders = new LinkedHashMap<>();
        Collection<QueryPreparer.StaticPlaceHolder> staticPlaceHolders = new ArrayList<>();

        Value argumentValue = argument.getValue();


        ScalarType scalarType = entityField.getScalarType();
        QueryNode queryFieldOwner = queryGraph.fetchFieldOwner(entityField);
        RejoinTable queryFieldTable = queryFieldOwner.getTable();
        QueryPreparer queryPreparer = new QueryPreparer();

        TypeUtil typeUtil = scalarType.getTypeUtil();

        QueryPreparer.PlaceHolder placeHolder;

        if (argumentValue instanceof VariableReference) {
            placeHolder = typeUtil.createArrayPlaceholder(queryPreparer);
            placeholders.put(((VariableReference) argumentValue).getName(), placeHolder);
        } else {
            QueryPreparer.StaticPlaceHolder staticArrayPlaceholder = typeUtil.createStaticArrayPlaceholder(queryPreparer, new Object[]{});
            staticPlaceHolders.add(staticArrayPlaceholder);
            placeHolder = staticArrayPlaceholder;
        }


        selectQuery.addCondition(new InCondition(queryFieldTable.findColumn(entityField.getColumn()), placeHolder));


        fieldExecutor = new SqlFieldExecutor(selectQuery.toString(), executor.getNodeExtractor(), placeholders, staticPlaceHolders);
        return fieldExecutor;
    }

    @SuppressWarnings("unchecked")
    private static <T extends GraphQLType> T unwrapNonNull(T onlyElement) {
        if (onlyElement instanceof GraphQLNonNull) {
            return (T) ((GraphQLNonNull) onlyElement).getWrappedType();
        }
        return onlyElement;
    }

    public void onDocumentContextEvicted(DocumentContext value) {
        cache.invalidate(value);
    }

    @Nonnull
    private <K, V> Cache<K, V> buildOperationCache() {
        return CacheBuilder.newBuilder().maximumSize(maxOperationsPerDocument).build();
    }

    private OperationKey buildOperationKey(DocumentContext documentContext,
                                           OperationDefinition operationDefinition,
                                           Map<String, Object> variables) {
        Set<String> queryAffectingFlags = documentContext.getQueryAffectingFlags(operationDefinition);
        boolean[] flagValues = new boolean[queryAffectingFlags.size()];
        int i = 0;
        for (String flag : queryAffectingFlags) {
            flagValues[i++] = (boolean) variables.get(flag);
        }

        return new OperationKey(operationDefinition, flagValues);
    }

}
