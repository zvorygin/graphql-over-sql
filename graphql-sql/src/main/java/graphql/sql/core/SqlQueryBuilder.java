package graphql.sql.core;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import graphql.execution.ExecutionContext;
import graphql.language.*;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.domain.*;
import graphql.sql.core.config.domain.type.TypeUtil;
import graphql.sql.core.query.QueryGraph;
import graphql.sql.core.query.QueryGraphBuilder;
import graphql.sql.core.query.QueryNode;

import java.nio.channels.IllegalSelectorException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        QueryGenerationContext context = new QueryGenerationContext(new QueryPreparer());
        EntityQuery entityQuery = config.getQuery(queryRoot.getKey());
        Field queryField = queryRoot.getValue().get(0);
        QueryGraph queryGraph = graphBuilder.build(entityQuery.getEntity(), queryField, executionContext);

        SelectQuery query = new SelectQuery();

        if (queryGraph.getParent() == null &&
                queryGraph.getChildren().isEmpty() &&
                queryGraph.getReferences().isEmpty()) {
            query.addFromTable(context.getTable(queryGraph));
            fetchScalarFields(query, queryGraph, context);
        } else {
            fetchNode(query, queryGraph, context);

            try {
                query.validate();
            } catch (ValidationException ve) {
                ve.printStackTrace();
            }
        }

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
            EntityType entityType = entityField.getEntityType();
            Object[] values = getArrayValue(entityType, argument, executionContext);
            QueryNode queryFieldOwner = queryGraph.findFieldInHierarchy(entityField);
            RejoinTable queryFieldTable = context.getTable(queryFieldOwner);
            QueryPreparer queryPreparer = context.getQueryPreparer();

            TypeUtil typeUtil = entityType.getTypeUtil();
            List<QueryPreparer.StaticPlaceHolder> placeHolders = new ArrayList<>();
            for (Object value : values) {
                placeHolders.add(typeUtil.createStaticPlaceHolder(value, queryPreparer));
            }
            query.addCondition(new InCondition(queryFieldTable.findColumn(entityField.getColumn()),
                    new SqlObjectList<>(SqlObjectList.DEFAULT_DELIMITER, placeHolders)));

            PreparedStatement preparedStatement = conn.prepareStatement(query.toString());

            for (QueryPreparer.StaticPlaceHolder placeHolder : placeHolders) {
                placeHolder.setValue(preparedStatement);
            }
            return preparedStatement;
        }
    }

    private Object[] getArrayValue(EntityType entityType, Argument argument, ExecutionContext executionContext) {
        Value value = argument.getValue();
        if (value instanceof ArrayValue) {
            List<Value> children = ((ArrayValue) value).getValues();
            Object[] result = new Object[children.size()];
            for (int i = 0; i < children.size(); i++) {
                Value child = children.get(i);
                result[i] = entityType.getTypeUtil().getValue(child);
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

    //TODO(dzvorygin) proper join grouping should be used here!
    private void fetchNode(SelectQuery query, QueryNode node, QueryGenerationContext context) {
        QueryNode hierarchyMaster = node.findHierarchyMaster();
        QueryNode current = hierarchyMaster;

        while (current.getParent() != null) {
            QueryNode parent = current.getParent();
            DbJoin parentJoin = current.getEntity().getParentReference()
                    .orElseThrow(IllegalSelectorException::new).getJoin();
            addJoin(query, parent, current, parentJoin, SelectQuery.JoinType.INNER, context);
            fetchFields(query, parent, context);

            current = current.getParent();
        }

        addChildrenHierarchyJoins(query, hierarchyMaster, context);

        fetchFields(query, hierarchyMaster, context);
    }

    private void fetchFields(SelectQuery query, QueryNode node, QueryGenerationContext context) {
        fetchScalarFields(query, node, context);

        node.getReferences().forEach((fieldName, referencedNode) -> {
                    EntityReference entityReference = node.getEntity().findReference(fieldName).get();

                    SelectQuery.JoinType joinType = entityReference.getReferenceType().getJoinType();
                    addJoin(query, referencedNode, node, entityReference.getReverseJoin(), joinType, context);
                    fetchNode(query, referencedNode, context);
                }
        );
    }

    private void fetchScalarFields(SelectQuery query, QueryNode node, QueryGenerationContext context) {
        RejoinTable table = context.getTable(node);
        remapColumns(node.getFieldsToQuery().stream().map(EntityField::getColumn)
                .collect(Collectors.toList()), table).forEach(query::addColumns);
    }

    private void addJoin(SelectQuery query,
                         QueryNode from,
                         QueryNode to,
                         DbJoin join,
                         SelectQuery.JoinType joinType, QueryGenerationContext context) {
        RejoinTable fromTable = context.getTable(from);
        RejoinTable toTable = context.getTable(to);

        // Inverted join here to preserve proper join type
        query.addJoin(joinType,
                toTable,
                fromTable,
                remapColumns(join.getToColumns(), toTable).collect(Collectors.toList()),
                remapColumns(join.getFromColumns(), fromTable).collect(Collectors.toList()));
    }

    private void addChildrenHierarchyJoins(SelectQuery query, QueryNode parent, QueryGenerationContext context) {
        parent.getChildren().values().forEach(child -> {
            addJoin(query, child, parent, child.getEntity().getParentReference().get().getJoin(),
                    SelectQuery.JoinType.LEFT_OUTER, context);
            fetchFields(query, child, context);

            addChildrenHierarchyJoins(query, child, context);
        });
    }

    private Stream<RejoinTable.RejoinColumn> remapColumns(List<DbColumn> fromColumns, RejoinTable table) {
        return fromColumns.stream().map(table::findColumn);
    }
}
