package graphql.sql.schema.engine.querygraph;

import com.google.common.collect.Sets;
import graphql.sql.core.QueryBuilderException;
import graphql.sql.core.config.CompositeType;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.QueryNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractQueryNode<T extends CompositeType> implements QueryNode {
    private final T type;

    private final Map<String, Field> fieldsToQuery = new LinkedHashMap<>();

    private final Map<String, QueryNode> references = new LinkedHashMap<>();

    private final Collection<QueryNode> children = new ArrayList<>();

    private final Collection<QueryNode> parents = new ArrayList<>();

    public AbstractQueryNode(T type) {
        this.type = type;
    }

    @Override
    public T getType() {
        return type;
    }

    @Override
    public void fetchField(String name, String alias) {
        Field field = type.getField(name);
        if (field == null) {
            throw new QueryBuilderException(String.format("Type [%s] doesn't have field [%s]", type.getName(), name));
        }
        Field existing = fieldsToQuery.putIfAbsent(alias, field);
        if (existing != null && !existing.getName().equals(name)) {
            throw new QueryBuilderException(
                    String.format("Can't fetch field [%s]. Field [%s] was already fetched same alias [%s]",
                            name, existing.getName(), alias));
        }
    }

    @Override
    public QueryNode fetchChild(CompositeType type) {
        return fetchType(type, children);
    }

    @Override
    public QueryNode fetchParent(CompositeType type) {
        return fetchType(type, parents);
    }

    private QueryNode fetchType(CompositeType type, Collection<QueryNode> nodes) {
        Optional<QueryNode> fetched = nodes.stream().filter(c -> c.getType().equals(type)).findAny();

        if (fetched.isPresent()) {
            return fetched.get();
        }

        Set<String> commonFields = Sets.intersection(type.getFields().keySet(), this.type.getFields().keySet());
        if (commonFields.isEmpty()) {
            throw new QueryBuilderException(
                    String.format("Type [%s] can't be joined directly to type [%s]",
                            this.type.getName(), type.getName()));
        }

        QueryNode result = type.buildQueryNode();

        nodes.add(result);

        return result;
    }

    public Map<String, Field> getFieldsToQuery() {
        return fieldsToQuery;
    }

    public Collection<QueryNode> getChildren() {
        return children;
    }

    public Collection<QueryNode> getParents() {
        return parents;
    }

    /*public AbstractQueryNode fetchEntityAtHierarchy(Entity entity) {

        AbstractQueryNode masterNode = getHierarchyMaster();

        Entity masterEntity = masterNode.getEntity();

        if (entity.isParentOf(masterEntity)) {
            AbstractQueryNode currentNode = masterNode;

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
            AbstractQueryNode currentNode = masterNode;
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
    }*/

    /*@Nonnull
    public AbstractQueryNode fetchParent() {
        if (parent == null) {
            if (entity.getParentReference() == null) {
                throw new QueryBuilderException(String.format("Entity [%s] doesn't have parent", entity.getEntityName()));
            }
            SqlEntityReference ref = entity.getParentReference();

            RejoinTable parentTable = rejoin(ref.getTargetEntity(), graph.nextNodeNumber());
            parent = new AbstractQueryNode<>(ref.getTargetEntity(), sqlQueryNode, this, graph, parentTable);
            sqlQueryNode.addParent(new JoinWithTable(parentTable,
                    remapColumns(ref.getJoin().getFromColumns(), table).collect(Collectors.toList()),
                    remapColumns(ref.getJoin().getToColumns(), parentTable).collect(Collectors.toList())));
            parent.children.put(entity, this);
        }

        return parent;
    }*/

    /*public AbstractQueryNode fetchChild(Entity e) {
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
            AbstractQueryNode result = new AbstractQueryNode<>(child, sqlQueryNode, this, graph, table);
            result.parent = this;
            sqlQueryNode.addChild(new JoinWithTable(table,
                    remapColumns(parentReference.getJoin().getToColumns(), this.table).collect(Collectors.toList()),
                    remapColumns(parentReference.getJoin().getFromColumns(), table).collect(Collectors.toList())));

            return result;
        });
    }*/

    /*public AbstractQueryNode fetchReference(EntityReference ref) {
        // TODO(dzvorygin) remove cast below
        SqlEntityReference reference = (SqlEntityReference) ref;
        return references.computeIfAbsent(reference.getName(),
                (name) -> {
                    RejoinTable referencedTable = rejoin(reference.getTargetEntity(), graph.nextNodeNumber());
                    SqlQueryNode referencedNode = new SqlQueryNode(referencedTable);
                    AbstractQueryNode<SqlQueryNode> result = new AbstractQueryNode<>(reference.getTargetEntity(), referencedNode, null, graph, referencedTable);
                    sqlQueryNode.addNestedNode(new JoinWithSqlQueryNode(referencedNode,
                            remapColumns(reference.getJoin().getFromColumns(), table).collect(Collectors.toList()),
                            remapColumns(reference.getJoin().getToColumns(), referencedTable).collect(Collectors.toList())));
                    return result;
                });
    }*/

    /*public int fetchField(EntityField f) {
        SqlEntityField entityField = (SqlEntityField) f;
        return fieldsToQuery.computeIfAbsent(entityField,
                (field) -> graph.getSqlQueryNode().addColumn(table.findColumn(entityField.getColumn())));
    }*/

    /*public SqlEntity getEntity() {
        return entity;
    }*/

    /*public AbstractQueryNode getHierarchyMaster() {
        return hierarchyMaster;
    }*/

    /*public T getSqlQueryNode() {
        return sqlQueryNode;
    }*/

    /*public AbstractQueryNode fetchFieldOwner(EntityField entityField) {
        AbstractQueryNode current = this;
        while (true) {
            if (current.getEntity().getEntityFields().contains(entityField)) {
                return current;
            }
            current = current.fetchParent();
        }
    }*/

    /*private static RejoinTable rejoin(SqlEntity entity, int i) {
        return entity.getTable().rejoin("t" + i);
    }*/

    /*private static Stream<RejoinTable.RejoinColumn> remapColumns(List<DbColumn> fromColumns, RejoinTable table) {
        return fromColumns.stream().map(table::findColumn);
    }*/


    /*public RejoinTable getTable() {
        return table;
    }*/

}
