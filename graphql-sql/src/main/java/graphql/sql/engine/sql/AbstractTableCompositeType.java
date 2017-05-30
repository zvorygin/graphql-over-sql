package graphql.sql.engine.sql;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.QueryNode;
import graphql.sql.schema.engine.AbstractCompositeType;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

public abstract class AbstractTableCompositeType extends AbstractCompositeType<SqlField> {
    @Nonnull
    private final DbTable dbTable;

    @Nonnull
    private final SqlExecutorBuilder sqlExecutorBuilder;

    public AbstractTableCompositeType(@Nonnull DbTable dbTable,
                                      @Nonnull String name,
                                      @Nonnull Map<String, SqlField> fields,
                                      @Nonnull Collection<Interface> interfaces,
                                      @Nonnull SqlExecutorBuilder sqlExecutorBuilder) {
        super(name, fields, interfaces);
        this.dbTable = dbTable;
        this.sqlExecutorBuilder = sqlExecutorBuilder;
    }

    @Nonnull
    public DbTable getDbTable() {
        return dbTable;
    }

    /*
    @Override
    public FieldExecutor buildExecutor(Field schemaField,
                                       graphql.language.Field queryDocumentField,
                                       ExecutionContext executionContext) {

        this.buildQueryNode(null);

        SqlFieldExecutor fieldExecutor;

        GraphQLQueryExecutor executor = QueryGraphBuilder.build(this, queryDocumentField, executionContext);

        AbstractQueryNode queryGraph = executor.getQueryGraph();
        SelectQuery selectQuery = queryGraph.getSqlQueryNode().buildSelectQuery();

        List<EntityField> rootFieldConditions = entityQuery.getEntityFields();
        if (rootFieldConditions.size() != queryDocumentField.getArguments().size()) {
            throw new IllegalArgumentException(String.format(
                    "Expected [%d] input parameters but got [%d] instead",
                    rootFieldConditions.size(), queryDocumentField.getArguments().size()));
        }

        if (rootFieldConditions.size() != 1) {
            throw new IllegalArgumentException("Not yet implemented");
        }

        EntityField entityField = Iterables.getOnlyElement(entityQuery.getEntityFields());
        Argument argument = Iterables.getOnlyElement(queryDocumentField.getArguments());
        GraphQLArgument argumentType = Iterables.getOnlyElement(schema.getQueryType().getFieldDefinition(queryDocumentField.getName()).getArguments());
        GraphQLInputType argumentValueType = unwrapNonNull(argumentType.getType());

        if (!(argumentValueType instanceof GraphQLList)) {
            throw new IllegalArgumentException("Not yet implemented");
        }

        Map<String, QueryPreparer.PlaceHolder> placeholders = new LinkedHashMap<>();
        Collection<QueryPreparer.StaticPlaceHolder> staticPlaceHolders = new ArrayList<>();

        Value argumentValue = argument.getValue();

        ScalarType scalarType = entityField.getScalarType();
        AbstractQueryNode queryFieldOwner = queryGraph.fetchFieldOwner(entityField);
        RejoinTable queryFieldTable = queryFieldOwner.getTable();
        QueryPreparer queryPreparer = new QueryPreparer();

        TypeUtil typeUtil = scalarType.getTypeUtil();

        QueryPreparer.PlaceHolder placeHolder;

        if (argumentValue instanceof VariableReference) {
            placeHolder = typeUtil.createArrayPlaceholder(queryPreparer);
            placeholders.put(((VariableReference) argumentValue).getName(), placeHolder);
        } else {
            QueryPreparer.StaticPlaceHolder staticArrayPlaceholder = typeUtil.createStaticArrayPlaceholder(queryPreparer, argumentValue);
            staticPlaceHolders.add(staticArrayPlaceholder);
            placeHolder = staticArrayPlaceholder;
        }

        // TODO(dzvorygin) eliminate class cast below!
        selectQuery.addCondition(new InCondition(queryFieldTable.findColumn(((SqlEntityField) entityField).getColumn()), placeHolder));

        String queryString = selectQuery.toString();

        try {
            selectQuery.validate();
        } catch (ValidationException ve) {
            throw new IllegalStateException(String.format("Failed to validate query [%s]", queryString), ve);
        }

        LOGGER.info("Created query {}", queryString);

        fieldExecutor = new SqlFieldExecutor(queryString, executor.getNodeExtractor(), placeholders, staticPlaceHolders);
        return fieldExecutor;
    }*/


    @SuppressWarnings("unchecked")
    private static <T extends GraphQLType> T unwrapNonNull(T onlyElement) {
        if (onlyElement instanceof GraphQLNonNull) {
            return (T) ((GraphQLNonNull) onlyElement).getWrappedType();
        }
        return onlyElement;
    }

    @Override
    public QueryNode buildQueryNode() {
        return new TableNode(this, sqlExecutorBuilder);
    }
}
