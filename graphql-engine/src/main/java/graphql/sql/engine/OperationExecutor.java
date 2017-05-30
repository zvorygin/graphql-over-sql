package graphql.sql.engine;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.execution.ExecutionContext;
import graphql.execution.FieldCollector;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import graphql.sql.core.config.CompositeType;
import graphql.sql.core.config.FieldExecutor;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.QueryNode;
import graphql.sql.core.config.TypeReference;
import graphql.sql.core.config.domain.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static graphql.introspection.Introspection.SchemaMetaFieldDef;
import static graphql.introspection.Introspection.TypeMetaFieldDef;

public class OperationExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationExecutor.class);
    private final GraphQLTypesProvider typesProvider;
    private final Config config;
    private final QueryGraphBuilder queryGraphBuilder;

    private final FieldCollector fieldCollector = new FieldCollector();
    private final LoadingCache<DocumentContext, Cache<OperationKey, Map<Field, FieldExecutor>>> cache;
    private final long maxOperationsPerDocument;


    public OperationExecutor(GraphQLTypesProvider typesProvider,
                             Config config,
                             QueryGraphBuilder queryGraphBuilder,
                             long maxOperationsPerDocument,
                             int maximumCacheSize) {
        this.typesProvider = typesProvider;
        this.config = config;
        this.queryGraphBuilder = queryGraphBuilder;
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
            LOGGER.error("Failed to build query fields executors", e);
            return new ExecutionResultImpl(Collections.singletonList(new ExceptionWhileDataFetching(e.getCause())));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<Field, FieldExecutor> entry : queryFields.entrySet()) {
            Field field = entry.getKey();
            FieldExecutor fieldExecutor = entry.getValue();
            String name = field.getAlias() == null ? field.getName() : field.getAlias();
            result.put(name, fieldExecutor.execute(variables));

        }
        return new ExecutionResultImpl(result, Collections.emptyList());
    }

    private Map<Field, FieldExecutor> buildOperationContext(DocumentContext documentContext,
                                                            OperationDefinition operationDefinition,
                                                            Map<String, Object> variables) {
        GraphQLSchema schema = typesProvider.getSchema();
        ExecutionContext executionContext = buildExecutionContext(documentContext, operationDefinition, variables, new GenericExecutionStrategy());

        LinkedHashMap<String, List<Field>> fields = new LinkedHashMap<>();
        fieldCollector.collectFields(executionContext, schema.getQueryType(), operationDefinition.getSelectionSet(), new ArrayList<>(), fields);

        Map<Field, FieldExecutor> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<Field>> entry : fields.entrySet()) {
            FieldExecutor fieldExecutor;
            Field field = Iterables.getOnlyElement(entry.getValue());

            if (field.getName().equals(SchemaMetaFieldDef.getName())) {
                fieldExecutor = getGenericFieldExecutor(SchemaMetaFieldDef, field, documentContext, variables, operationDefinition);
            } else if (field.getName().equals(TypeMetaFieldDef.getName())) {
                fieldExecutor = getGenericFieldExecutor(TypeMetaFieldDef, field, documentContext, variables, operationDefinition);
            } else {
                fieldExecutor = getFieldExecutor(executionContext, field);
            }

            result.put(field, fieldExecutor);
        }

        return result;
    }

    private StaticFieldExecutor getGenericFieldExecutor(GraphQLFieldDefinition fieldDefinition, Field field, DocumentContext documentContext, Map<String, Object> variables, OperationDefinition operationDefinition) {
        GenericExecutionStrategy genericExecutionStrategy = new GenericExecutionStrategy();
        Object value = genericExecutionStrategy.executeField(fieldDefinition, field,
                buildExecutionContext(documentContext, operationDefinition, variables, genericExecutionStrategy));

        return new StaticFieldExecutor(value);
    }

    @Nonnull
    private ExecutionContext buildExecutionContext(DocumentContext documentContext,
                                                   OperationDefinition operationDefinition,
                                                   Map<String, Object> variables,
                                                   GenericExecutionStrategy queryStrategy) {
        return new ExecutionContext(
                typesProvider.getSchema(),
                queryStrategy,
                null,
                documentContext.getFragmentsByName(),
                operationDefinition,
                variables,
                null);
    }

    @Nonnull
    private FieldExecutor getFieldExecutor(ExecutionContext executionContext, Field queryDocumentField) {

        String queryTypeName = config.getQueryTypeName();

        CompositeType queryType = config.getType(queryTypeName);
        graphql.sql.core.config.Field schemaField = queryType.getField(queryDocumentField.getName());
        TypeReference typeReference = schemaField.getTypeReference();

        CompositeType resultType = config.getType(typeReference);

        QueryNode rootNode = queryGraphBuilder.build(resultType, queryDocumentField, executionContext);

        return rootNode.buildExecutor(schemaField, executionContext);
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
