package graphql.sql.core.sqlquery;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import graphql.execution.ExecutionContext;
import graphql.language.*;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.domain.*;
import graphql.sql.core.config.domain.type.TypeUtil;
import graphql.sql.core.querygraph.QueryRoot;
import graphql.sql.core.querygraph.QueryGraphBuilder;
import graphql.sql.core.querygraph.QueryNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO(dzvorygin):
public class SqlQueryBuilder {

    private final Config config;
    private final GraphQLTypesProvider typesProvider;
    private final QueryGraphBuilder graphBuilder;

    public SqlQueryBuilder(Config config, GraphQLTypesProvider typesProvider, QueryGraphBuilder graphBuilder) {
        this.config = config;
        this.typesProvider = typesProvider;
        this.graphBuilder = graphBuilder;
    }

    public PreparedStatement createPreparedStatement(Connection conn,
                                                     Map.Entry<String, List<Field>> queryRoot,
                                                     ExecutionContext executionContext) throws SQLException {
        EntityQuery entityQuery = config.getQuery(queryRoot.getKey());
        Field queryField = queryRoot.getValue().get(0);
        QueryRoot queryGraph = graphBuilder.build(entityQuery.getEntity(), queryField, executionContext);

        SelectQuery query = queryGraph.getSqlQueryNode().buildSelectQuery();

        List<EntityField> rootFieldConditions = entityQuery.getEntityFields();
        if (rootFieldConditions.size() != queryField.getArguments().size()) {
            throw new IllegalArgumentException(String.format(
                    "Expected [%d] input parameters but got [%d] instead",
                    rootFieldConditions.size(), queryField.getArguments().size()));
        }

        if (rootFieldConditions.size() != 1) {
            throw new IllegalArgumentException("Not yet implemented");
        } else {
            EntityField entityField = entityQuery.getEntityFields().get(0);
            Argument argument = queryField.getArguments().get(0);
            ScalarType scalarType = entityField.getScalarType();
            Object[] values = getArrayValue(scalarType, argument, executionContext);
            QueryNode queryFieldOwner = queryGraph.fetchFieldOwner(entityField);
            RejoinTable queryFieldTable = queryFieldOwner.getTable();
            QueryPreparer queryPreparer = new QueryPreparer();

            TypeUtil typeUtil = scalarType.getTypeUtil();
            List<QueryPreparer.StaticPlaceHolder> placeHolders = new ArrayList<>();
            for (Object value : values) {
                placeHolders.add(typeUtil.createStaticPlaceHolder(value, queryPreparer));
            }
            query.addCondition(new InCondition(queryFieldTable.findColumn(entityField.getColumn()),
                    new SqlObjectList<>(SqlObjectList.DEFAULT_DELIMITER, placeHolders)));

            String sql = query.toString();

            System.out.println(sql);

            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            for (QueryPreparer.StaticPlaceHolder placeHolder : placeHolders) {
                placeHolder.setValue(preparedStatement);
            }
            return preparedStatement;
        }
    }

    private Object[] getArrayValue(ScalarType scalarType, Argument argument, ExecutionContext executionContext) {
        Value value = argument.getValue();
        if (value instanceof ArrayValue) {
            List<Value> children = ((ArrayValue) value).getValues();
            Object[] result = new Object[children.size()];
            for (int i = 0; i < children.size(); i++) {
                Value child = children.get(i);
                result[i] = scalarType.getTypeUtil().getValue(child);
            }
            return result;
        }
        if (value instanceof VariableReference) {
            List referencedValue =
                    (List) executionContext.getVariables().get(((VariableReference) value).getName());
            return referencedValue.toArray(new Object[referencedValue.size()]);
        }
        throw new IllegalStateException(String.format("Argument [%s] value is not collection", argument.getName()));
    }
}
