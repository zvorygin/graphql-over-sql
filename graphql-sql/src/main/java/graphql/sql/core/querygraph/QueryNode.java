package graphql.sql.core.querygraph;

import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityReference;
import graphql.sql.core.sqlquery.JoinWithSqlQueryNode;
import graphql.sql.core.sqlquery.JoinWithTable;
import graphql.sql.core.sqlquery.SqlQueryNode;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public class QueryNode<T extends SqlQueryNode> {

    private final QueryRoot graph;

    private final QueryNode<? extends SqlQueryNode> hierarchyMaster;

    private final T sqlQueryNode;

    private final Entity entity;

    private final RejoinTable table;

    private Collection<EntityField> fieldsToQuery = new LinkedHashSet<>();

    private Map<String, QueryNode<SqlQueryNode>> references = new LinkedHashMap<>();

    private QueryNode<SqlQueryNode> parent;

    private Map<Entity, QueryNode<? extends SqlQueryNode>> children = new LinkedHashMap<>();

    protected QueryNode(Entity entity, T sqlQueryNode, QueryNode<? extends SqlQueryNode> hierarchyMaster, QueryRoot graph, RejoinTable table) {
        this.entity = entity;
        this.sqlQueryNode = sqlQueryNode;
        this.hierarchyMaster = hierarchyMaster == null ? this : hierarchyMaster;
        this.graph = graph == null ? (QueryRoot) this : graph;
        this.table = table;
    }

    @Nonnull
    public QueryNode fetchParent() {
        if (parent == null) {
            if (entity.getParentReference() == null) {
                throw new QueryBuilderException(String.format("Entity [%s] doesn't have parent", entity.getEntityName()));
            }
            EntityReference ref = entity.getParentReference();

            RejoinTable parentTable = rejoin(ref.getTargetEntity(), graph.nextNodeNumber());
            parent = new QueryNode<>(ref.getTargetEntity(), sqlQueryNode, this, graph, parentTable);
            sqlQueryNode.addParent(new JoinWithTable(parentTable,
                    remapColumns(ref.getJoin().getFromColumns(), table).collect(Collectors.toList()),
                    remapColumns(ref.getJoin().getToColumns(), parentTable).collect(Collectors.toList())));
            parent.children.put(entity, this);
        }

        return parent;
    }

    public QueryNode fetchChild(Entity child) {
        EntityReference parentReference = child.getParentReference();
        if (parentReference == null) {
            throw new QueryBuilderException(
                    String.format("Entity [%s] doesn't have parent", child.getEntityName()));
        }
        Entity childParent = parentReference.getTargetEntity();

        if (!childParent.equals(entity)) {
            throw new QueryBuilderException(
                    String.format("Expected [%s] parent to be [%s], but got [%s]",
                            child.getEntityName(),
                            childParent.getEntityName(),
                            entity.getEntityName()));
        }

        return children.computeIfAbsent(child, entity -> {
            RejoinTable table = entity.getTable().rejoin("t" + graph.nextNodeNumber());
            QueryNode result = new QueryNode<>(child, sqlQueryNode, this, graph, table);
            result.parent = this;
            sqlQueryNode.addChild(new JoinWithTable(table,
                    remapColumns(parentReference.getJoin().getToColumns(), this.table).collect(Collectors.toList()),
                    remapColumns(parentReference.getJoin().getFromColumns(), table).collect(Collectors.toList())));

            return result;
        });
    }

    public QueryNode fetchReference(EntityReference reference) {
        return references.computeIfAbsent(reference.getName(),
                (ref) -> {
                    RejoinTable referencedTable = rejoin(reference.getTargetEntity(), graph.nextNodeNumber());
                    SqlQueryNode referencedNode = new SqlQueryNode(referencedTable);
                    QueryNode<SqlQueryNode> result = new QueryNode<>(reference.getTargetEntity(), referencedNode, null, graph, referencedTable);
                    sqlQueryNode.addNestedNode(new JoinWithSqlQueryNode(referencedNode,
                            remapColumns(reference.getJoin().getFromColumns(), table).collect(Collectors.toList()),
                            remapColumns(reference.getJoin().getToColumns(), referencedTable).collect(Collectors.toList())));
                    return result;
                });
    }

    public void fetchField(EntityField entityField) {
        if (!fieldsToQuery.contains(entityField)) {
            fieldsToQuery.add(entityField);
            graph.getSqlQueryNode().addColumn(table.findColumn(entityField.getColumn()));
        }
    }

    public Entity getEntity() {
        return entity;
    }

    public QueryNode getHierarchyMaster() {
        return hierarchyMaster;
    }

    public Collection<EntityField> getFieldsToQuery() {
        return fieldsToQuery;
    }

    public QueryNode getParent() {
        return parent;
    }

    public Map<Entity, QueryNode<? extends SqlQueryNode>> getChildren() {
        return children;
    }

    public Map<String, QueryNode<SqlQueryNode>> getReferences() {
        return references;
    }

    public T getSqlQueryNode() {
        return sqlQueryNode;
    }

    public QueryNode fetchFieldOwner(EntityField entityField) {
        QueryNode current = this;
        while (true) {
            if (current.getEntity().getEntityFields().contains(entityField)) {
                return current;
            }
            current = current.fetchParent();
        }
    }

    private static RejoinTable rejoin(Entity entity, int i) {
        return entity.getTable().rejoin("t" + i);
    }

    private static Stream<RejoinTable.RejoinColumn> remapColumns(List<DbColumn> fromColumns, RejoinTable table) {
        return fromColumns.stream().map(table::findColumn);
    }


    public RejoinTable getTable() {
        return table;
    }
}
