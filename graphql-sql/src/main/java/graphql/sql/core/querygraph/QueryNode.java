package graphql.sql.core.querygraph;

import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityReference;
import graphql.sql.core.config.domain.impl.SqlEntityReference;
import graphql.sql.core.config.domain.impl.SqlEntityField;
import graphql.sql.core.config.domain.impl.SqlEntity;
import graphql.sql.core.sqlquery.JoinWithSqlQueryNode;
import graphql.sql.core.sqlquery.JoinWithTable;
import graphql.sql.core.sqlquery.SqlQueryNode;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryNode<T extends SqlQueryNode> {

    private final QueryRoot graph;

    private final QueryNode<? extends SqlQueryNode> hierarchyMaster;

    private final T sqlQueryNode;

    private final SqlEntity entity;

    private final RejoinTable table;

    private final HashMap<EntityField, Integer> fieldsToQuery = new LinkedHashMap<>();

    private final Map<String, QueryNode<SqlQueryNode>> references = new LinkedHashMap<>();

    private QueryNode<SqlQueryNode> parent;

    private final Map<Entity, QueryNode<? extends SqlQueryNode>> children = new LinkedHashMap<>();

    protected QueryNode(SqlEntity entity, T sqlQueryNode, QueryNode<? extends SqlQueryNode> hierarchyMaster, QueryRoot graph, RejoinTable table) {
        this.entity = entity;
        this.sqlQueryNode = sqlQueryNode;
        this.hierarchyMaster = hierarchyMaster == null ? this : hierarchyMaster;
        this.graph = graph == null ? (QueryRoot) this : graph;
        this.table = table;
    }

    public QueryNode fetchEntityAtHierarchy(Entity entity) {

        QueryNode masterNode = getHierarchyMaster();

        Entity masterEntity = masterNode.getEntity();

        if (entity.isParentOf(masterEntity)) {
            QueryNode currentNode = masterNode;

            while (!currentNode.getEntity().equals(entity)) {
                currentNode = currentNode.fetchParent();
            }

            return currentNode;

        } else if (masterEntity.isParentOf(entity)) {
            List<Entity> path = new ArrayList<>();
            Iterator<Entity> hierarchyIterator = entity.hierarchyIterator();
            while (hierarchyIterator.hasNext()) {
                Entity next = hierarchyIterator.next();
                if (next.equals(masterEntity)) {
                    break;
                }
                path.add(next);
            }

            ListIterator<Entity> reverseIterator = path.listIterator(path.size());
            QueryNode currentNode = masterNode;
            while (reverseIterator.hasPrevious()) {
                currentNode = currentNode.fetchChild(reverseIterator.previous());
            }
            return currentNode;
        }

        throw new IllegalStateException(
                String.format("There's no entity [%s] in hierarchy of node [%s] with master [%s]",
                        entity.getEntityName(),
                        getEntity().getEntityName(),
                        masterNode.getEntity().getEntityName()));
    }

    @Nonnull
    public QueryNode fetchParent() {
        if (parent == null) {
            if (entity.getParentReference() == null) {
                throw new QueryBuilderException(String.format("Entity [%s] doesn't have parent", entity.getEntityName()));
            }
            SqlEntityReference ref = entity.getParentReference();

            RejoinTable parentTable = rejoin(ref.getTargetEntity(), graph.nextNodeNumber());
            parent = new QueryNode<>(ref.getTargetEntity(), sqlQueryNode, this, graph, parentTable);
            sqlQueryNode.addParent(new JoinWithTable(parentTable,
                    remapColumns(ref.getJoin().getFromColumns(), table).collect(Collectors.toList()),
                    remapColumns(ref.getJoin().getToColumns(), parentTable).collect(Collectors.toList())));
            parent.children.put(entity, this);
        }

        return parent;
    }

    public QueryNode fetchChild(Entity e) {
        // TODO(dzvorygin) remove cast below.
        SqlEntity child = (SqlEntity) e;
        SqlEntityReference parentReference = child.getParentReference();
        if (parentReference == null) {
            throw new QueryBuilderException(
                    String.format("Entity [%s] doesn't have parent", child.getEntityName()));
        }
        SqlEntity childParent = parentReference.getTargetEntity();

        if (!childParent.equals(entity)) {
            throw new QueryBuilderException(
                    String.format("Expected [%s] parent to be [%s], but got [%s]",
                            child.getEntityName(),
                            childParent.getEntityName(),
                            entity.getEntityName()));
        }

        return children.computeIfAbsent(child, entity -> {
            RejoinTable table = child.getTable().rejoin("t" + graph.nextNodeNumber());
            QueryNode result = new QueryNode<>(child, sqlQueryNode, this, graph, table);
            result.parent = this;
            sqlQueryNode.addChild(new JoinWithTable(table,
                    remapColumns(parentReference.getJoin().getToColumns(), this.table).collect(Collectors.toList()),
                    remapColumns(parentReference.getJoin().getFromColumns(), table).collect(Collectors.toList())));

            return result;
        });
    }

    public QueryNode fetchReference(EntityReference ref) {
        // TODO(dzvorygin) remove cast below
        SqlEntityReference reference = (SqlEntityReference) ref;
        return references.computeIfAbsent(reference.getName(),
                (name) -> {
                    RejoinTable referencedTable = rejoin(reference.getTargetEntity(), graph.nextNodeNumber());
                    SqlQueryNode referencedNode = new SqlQueryNode(referencedTable);
                    QueryNode<SqlQueryNode> result = new QueryNode<>(reference.getTargetEntity(), referencedNode, null, graph, referencedTable);
                    sqlQueryNode.addNestedNode(new JoinWithSqlQueryNode(referencedNode,
                            remapColumns(reference.getJoin().getFromColumns(), table).collect(Collectors.toList()),
                            remapColumns(reference.getJoin().getToColumns(), referencedTable).collect(Collectors.toList())));
                    return result;
                });
    }

    public int fetchField(EntityField f) {
        SqlEntityField entityField = (SqlEntityField) f;
        return fieldsToQuery.computeIfAbsent(entityField,
                (field) -> graph.getSqlQueryNode().addColumn(table.findColumn(entityField.getColumn())));
    }

    public SqlEntity getEntity() {
        return entity;
    }

    public QueryNode getHierarchyMaster() {
        return hierarchyMaster;
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

    private static RejoinTable rejoin(SqlEntity entity, int i) {
        return entity.getTable().rejoin("t" + i);
    }

    private static Stream<RejoinTable.RejoinColumn> remapColumns(List<DbColumn> fromColumns, RejoinTable table) {
        return fromColumns.stream().map(table::findColumn);
    }


    public RejoinTable getTable() {
        return table;
    }
}
