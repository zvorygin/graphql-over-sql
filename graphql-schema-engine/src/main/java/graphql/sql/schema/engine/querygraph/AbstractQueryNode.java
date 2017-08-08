package graphql.sql.schema.engine.querygraph;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import graphql.execution.ExecutionContext;
import graphql.language.Argument;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.TypeName;
import graphql.sql.core.QueryBuilderException;
import graphql.sql.core.config.CompositeType;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.QueryNode;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.graph.BfsFinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractQueryNode<T extends CompositeType> implements QueryNode {
    private final Config config;

    private final T type;

    protected final Map<String, Field> fieldsToQuery = new LinkedHashMap<>();

    protected final Map<String, QueryNode> references = new LinkedHashMap<>();

    protected final Collection<QueryNode> children = new ArrayList<>();

    protected final Collection<QueryNode> parents = new ArrayList<>();

    protected final Map<String, Object> constants = new LinkedHashMap<>();
    protected final List<Argument> arguments = new ArrayList<>();

    public AbstractQueryNode(Config config, T type) {
        this.config = config;
        this.type = type;
    }

    @Override
    public T getType() {
        return type;
    }

    @Override
    public QueryNode fetchField(Config config, graphql.language.Field queryField, ExecutionContext ctx) {
        Field field = type.getField(queryField.getName());
        return field.fetch(config, this, ctx, queryField);
        /*if (field == null) {
            throw new QueryBuilderException(String.format("Type [%s] doesn't have field [%s]", type.getName(), name));
        }
        Field existing = fieldsToQuery.putIfAbsent(alias, field);
        if (existing != null && !existing.getName().equals(name)) {
            throw new QueryBuilderException(
                    String.format("Can't fetch field [%s]. Field [%s] was already fetched same alias [%s]",
                            name, existing.getName(), alias));
        }
*/
        /*

        CompositeType nodeType = currentNode.getType();
        graphql.sql.core.config.Field field = nodeType.getField(fieldName);

        CompositeType fieldType = config.getType(field.getTypeReference());

        if (fieldType == null) {
            // If raw field
            currentNode.fetchField(fieldName, fieldAlias);
        } else {
            // If composite field
        }

        currentNode.fetchField(field.getName(), fieldAlias);

        if (field != null) {
            //TODO(dzvorygin) handle composite fields here somehow
            currentNode.fetchField(fieldName, fieldAlias);
            return;
        }

        for (Interface iface : nodeType.getInterfaces()) {
            graphql.sql.core.config.Field interfaceField = iface.getField(fieldName);
            if (interfaceField != null) {
                QueryNode targetNode = fetchInterface(currentNode, iface);
                targetNode.fetchField(fieldName, fieldAlias);
                return;
            }
        }

        throw new QueryBuilderException(
                String.format("Failed to find field [%s] in hierarchy of type [%s]",
                        fieldName, currentNode.getType().getName()));



         */
    }

    /*@Override
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

        QueryNode result = type.buildQueryNode(config, selectionSet, executionContext);

        nodes.add(result);

        return result;
    }
*/

    @Override
    public void fetchConstant(String alias, Object result) {
        constants.put(alias, result);
    }

    @Override
    public void addReference(String alias, QueryNode node) {
        references.put(alias, node);
    }

    @Override
    public void addField(String alias, Field field) {
        fieldsToQuery.put(alias, field);
    }

    @Override
    public void addArgument(Argument argument) {
        arguments.add(argument);
    }

    @Override
    public List<Argument> getArguments() {
        return Collections.unmodifiableList(arguments);
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

    public Map<String, Object> getConstants() {
        return constants;
    }

    public Map<String, QueryNode> getReferences() {
        return references;
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


    public void processSelectionSet(ExecutionContext executionContext, SelectionSet selectionSet) {
        for (Selection selection : selectionSet.getSelections()) {
            processSelection(executionContext, selection);
        }
    }

    private void processSelection(ExecutionContext executionContext, Selection selection) {
        if (selection instanceof graphql.language.Field) {
            processField(this, (graphql.language.Field) selection, executionContext);
        } else if (selection instanceof InlineFragment) {
            InlineFragment fragment = (InlineFragment) selection;
            TypeName typeCondition = fragment.getTypeCondition();

            processFragment(executionContext, typeCondition.getName(), fragment.getSelectionSet());

        } else if (selection instanceof FragmentSpread) {
            FragmentSpread fragmentSpread = (FragmentSpread) selection;
            FragmentDefinition fragment = executionContext.getFragment(fragmentSpread.getName());
            processFragment(executionContext, fragment.getTypeCondition().getName(), fragment.getSelectionSet());
        }
    }

    private void processField(QueryNode currentNode, graphql.language.Field queryField, ExecutionContext ctx) {
        //TODO(dzvorygin) search for field with more logic - in nested objects, or parent objects
        currentNode.fetchField(config, queryField, ctx);
    }

    /*private QueryNode fetchInterface(Interface iface) {
        CompositeType current = this.getType();

        List<CompositeType> path = BfsFinder.findPath(current, iface, config::getJoinableTypes);
        if (path == null) {
            throw new QueryBuilderException(
                    String.format("There's no join path from type [%s] to type [%s]",
                            this.getType().getName(), iface.getName()));
        }
        QueryNode result = null;

        for (CompositeType compositeType : path) {
            result = this.fetchParent(compositeType);
        }

        return result;
    }*/

    private void processFragment(ExecutionContext executionContext,
                                 String typeName,
                                 SelectionSet selectionSet) {
        CompositeType referencedEntity = config.getType(typeName);
        if (referencedEntity == null) {
            throw new QueryBuilderException(
                    String.format("Invalid fragment name [%s] doesn't match any type", typeName));
        }

        List<CompositeType> path = BfsFinder.findPath(this.getType(),
                referencedEntity,
                config::getJoinableTypes);

        if (path == null) {
            throw new QueryBuilderException(
                    String.format("Unable to join [%s] with [%s]",
                            this.getType().getName(), referencedEntity.getName()));
        }

        QueryNode current = this;

        // TODO(dzvorygin) fetch children here somehow

/*
        for (CompositeType compositeType : path) {
            current = current.fetchChild(compositeType);
        }

        current.fetchChild(referencedEntity);*/

        // TODO(dzvorygin) do something with commented line below

        //current.processSelectionSet(executionContext, selectionSet);
    }

}
