package graphql.sql.schema.engine.querygraph;

import graphql.execution.ExecutionContext;
import graphql.language.Argument;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.TypeName;
import graphql.sql.core.QueryBuilderException;
import graphql.sql.core.config.*;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.graph.BfsFinder;

import java.util.*;

public abstract class AbstractQueryNode<T extends CompositeType> implements QueryNode {
    private final Config config;

    private final T type;

    protected final Map<String, FieldLink> fieldsToQuery = new LinkedHashMap<>();

    protected final Map<String, QueryLink> references = new LinkedHashMap<>();

    protected final Collection<QueryLink> children = new ArrayList<>();

    protected final Collection<QueryLink> parents = new ArrayList<>();

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
        QueryNode currentNode = this;

        List<CompositeType> path = BfsFinder.findPath(currentNode.getType(),
                (c) -> c.getFields().containsKey(queryField.getName()),
                CompositeType::getInterfaces);

        if (path == null) {
            throw new IllegalStateException(String.format("Can't fetch field [%s] from type [%s]: field not found",
                    queryField.getName(), currentNode.getType().getName()));
        }

        for (CompositeType compositeType : path) {
            currentNode = currentNode.fetchParent(compositeType);
        }

        Field field = currentNode.getType().getField(queryField.getName());
        return field.fetch(config, this, currentNode, ctx, queryField);
    }

    @Override
    public void fetchConstant(String alias, Object result) {
        constants.put(alias, result);
    }

    @Override
    public void addReference(String alias, QueryLink node) {
        references.put(alias, node);
    }

    @Override
    public void addChild(QueryLink link) {
        children.add(link);
    }

    @Override
    public void addField(FieldLink fieldLink) {
        fieldsToQuery.put(fieldLink.getAlias(), fieldLink);
    }

    @Override
    public void addArgument(Argument argument) {
        arguments.add(argument);
    }

    @Override
    public List<Argument> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public Map<String, FieldLink> getFieldsToQuery() {
        return fieldsToQuery;
    }

    @Override
    public Collection<QueryLink> getChildren() {
        return children;
    }

    @Override
    public void addParent(QueryLink link) {
        parents.add(link);
    }

    public Collection<QueryLink> getParents() {
        return parents;
    }

    public Map<String, Object> getConstants() {
        return constants;
    }

    public Map<String, QueryLink> getReferences() {
        return references;
    }

    @Override
    public void processSelectionSet(SelectionSet selectionSet, ExecutionContext executionContext) {
        for (Selection selection : selectionSet.getSelections()) {
            processSelection(selection, executionContext);
        }
    }

    private void processSelection(Selection selection, ExecutionContext executionContext) {
        if (selection instanceof graphql.language.Field) {
            processField((graphql.language.Field) selection, executionContext);
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

    private void processField(graphql.language.Field queryField, ExecutionContext ctx) {


        fetchField(config, queryField, ctx);
    }

    @Override
    public QueryNode fetchParent(CompositeType compositeType) {
        for (QueryLink parent : parents) {
            if (parent.getTarget().getType().equals(compositeType)) {
                return parent.getTarget();
            }
        }

        Field[] sourceFields = new Field[]{};
        Field[] targetFields = new Field[]{};
        QueryNode target = compositeType.buildQueryNode(config);
        QueryLink link = new QueryLink(this, target, sourceFields, targetFields);
        parents.add(link);
        target.addChild(link.reverse());
        return link.getTarget();
    }

    @Override
    public QueryNode fetchChild(CompositeType compositeType) {
        for (QueryLink child : children) {
            if (child.getTarget().getType().equals(compositeType)) {
                return child.getTarget();
            }
        }

        Field[] sourceFields = new Field[]{};
        Field[] targetFields = new Field[]{};
        QueryNode target = compositeType.buildQueryNode(config);
        QueryLink link = new QueryLink(this, target, sourceFields, targetFields);
        children.add(link);
        target.addParent(link.reverse());
        return link.getTarget();
    }

    private void processFragment(ExecutionContext executionContext,
                                 String typeName,
                                 SelectionSet selectionSet) {
        CompositeType referencedEntity = config.getType(typeName);
        if (referencedEntity == null) {
            throw new QueryBuilderException(
                    String.format("Invalid fragment name [%s] doesn't match any type", typeName));
        }

        List<CompositeType> path = BfsFinder.findPath(getType(),
                referencedEntity::equals,
                config::getJoinableTypes);

        if (path == null) {
            throw new QueryBuilderException(
                    String.format("Unable to join [%s] with [%s]",
                            getType().getName(), referencedEntity.getName()));
        }


        QueryNode current = this;

        for (CompositeType compositeType : path) {
            current = current.fetchChild(compositeType);
        }

        current.processSelectionSet(selectionSet, executionContext);
    }

}
