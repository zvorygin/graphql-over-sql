package graphql.sql.engine;

import graphql.execution.ExecutionContext;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.TypeName;
import graphql.sql.core.QueryBuilderException;
import graphql.sql.core.config.CompositeType;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.QueryNode;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.graph.BfsFinder;

import java.util.List;

public class QueryGraphBuilder {

    private final Config config;

    public QueryGraphBuilder(Config config) {
        this.config = config;
    }

    public QueryNode build(CompositeType rootEntity, Field rootField, ExecutionContext executionContext) {
        QueryNode queryNode = rootEntity.buildQueryNode();
        processSelectionSet(executionContext, queryNode, rootField.getSelectionSet());
        return queryNode;
    }

    private void processSelectionSet(ExecutionContext executionContext, QueryNode queryNode, SelectionSet selectionSet) {
        for (Selection selection : selectionSet.getSelections()) {
            processSelection(executionContext, queryNode, selection);
        }
    }

    private void processSelection(ExecutionContext executionContext, QueryNode queryNode, Selection selection) {
        if (selection instanceof Field) {
            processField(queryNode, (Field) selection);
        } else if (selection instanceof InlineFragment) {
            InlineFragment fragment = (InlineFragment) selection;
            TypeName typeCondition = fragment.getTypeCondition();

            processFragment(executionContext, queryNode, typeCondition.getName(), fragment.getSelectionSet());

        } else if (selection instanceof FragmentSpread) {
            FragmentSpread fragmentSpread = (FragmentSpread) selection;
            FragmentDefinition fragment = executionContext.getFragment(fragmentSpread.getName());
            processFragment(executionContext, queryNode, fragment.getTypeCondition().getName(), fragment.getSelectionSet());
        }
    }

    private void processField(QueryNode queryNode, Field queryField) {
        String fieldName = queryField.getName();
        String fieldAlias = queryField.getAlias() == null ? fieldName : queryField.getAlias();

        CompositeType nodeType = queryNode.getType();
        graphql.sql.core.config.Field field = nodeType.getField(fieldName);

        if (field != null) {
            //TODO(dzvorygin) handle composite fields here somehow
            queryNode.fetchField(fieldName, fieldAlias);
            return;
        }

        for (Interface iface : nodeType.getInterfaces()) {
            graphql.sql.core.config.Field interfaceField = iface.getField(fieldName);
            if (interfaceField != null) {
                QueryNode targetNode = fetchInterface(queryNode, iface);
                targetNode.fetchField(fieldName, fieldAlias);
                return;
            }
        }

        throw new QueryBuilderException(
                String.format("Failed to find field [%s] in hierarchy of type [%s]",
                        fieldName, queryNode.getType().getName()));
    }

    private QueryNode fetchInterface(QueryNode queryNode, Interface iface) {
        CompositeType current = queryNode.getType();

        List<CompositeType> path = BfsFinder.findPath(current, iface, config::getJoinableTypes);
        if (path == null) {
            throw new QueryBuilderException(
                    String.format("There's no join path from type [%s] to type [%s]",
                            queryNode.getType().getName(), iface.getName()));
        }
        QueryNode result = null;

        for (CompositeType compositeType : path) {
            result = queryNode.fetchParent(compositeType);
        }

        return result;
    }

    private void processFragment(ExecutionContext executionContext,
                                 QueryNode queryNode,
                                 String typeName,
                                 SelectionSet selectionSet) {
        CompositeType referencedEntity = config.getType(typeName);
        if (referencedEntity == null) {
            throw new QueryBuilderException(
                    String.format("Invalid fragment name [%s] doesn't match any type", typeName));
        }

        List<CompositeType> path = BfsFinder.findPath(queryNode.getType(),
                referencedEntity,
                config::getJoinableTypes);

        if (path == null) {
            throw new QueryBuilderException(
                    String.format("Unable to join [%s] with [%s]",
                            queryNode.getType().getName(), referencedEntity.getName()));
        }

        QueryNode current = queryNode;

        for (CompositeType compositeType : path) {
            current = current.fetchChild(compositeType);
        }

        current.fetchChild(referencedEntity);

        processSelectionSet(executionContext, current, selectionSet);
    }

}
