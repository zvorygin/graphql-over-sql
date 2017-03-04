package graphql.sql.core.querygraph;

import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityReference;

import graphql.execution.ExecutionContext;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.TypeName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

public class QueryGraphBuilder {

    private final Config config;
    private final GraphQLTypesProvider typesProvider;

    public QueryGraphBuilder(Config config, GraphQLTypesProvider typesProvider) {

        this.config = config;
        this.typesProvider = typesProvider;
    }

    public QueryRoot build(Entity rootEntity, Field rootField, ExecutionContext executionContext) {
        QueryRoot graph = new QueryRoot(rootEntity);

        processSelectionSet(executionContext, graph, rootField.getSelectionSet());

        return graph;
    }

    private void processSelectionSet(ExecutionContext executionContext, QueryNode node, SelectionSet selectionSet) {
        for (Selection selection : selectionSet.getSelections()) {
            processSelection(executionContext, node, selection);
        }
    }

    private void processSelection(ExecutionContext executionContext, QueryNode node, Selection selection) {
        if (selection instanceof Field) {
            processField(executionContext, node, (Field) selection);
        } else if (selection instanceof InlineFragment) {
            InlineFragment fragment = (InlineFragment) selection;
            TypeName typeCondition = fragment.getTypeCondition();

            processFragment(executionContext, node, typeCondition.getName(), fragment.getSelectionSet());

        } else if (selection instanceof FragmentSpread) {
            FragmentSpread fragmentSpread = (FragmentSpread) selection;
            FragmentDefinition fragment = executionContext.getFragment(fragmentSpread.getName());
            processFragment(executionContext, node, fragment.getTypeCondition().getName(), fragment.getSelectionSet());
        }
    }

    private void processField(ExecutionContext executionContext, QueryNode node, Field field) {
        QueryNode current = node;
        String fieldName = field.getName();
        while (current != null) {
            Entity currentEntity = current.getEntity();
            Optional<EntityField> entityField = currentEntity.findField(fieldName);
            if (entityField.isPresent()) {
                current.fetchField(entityField.get());
                return;
            }

            Optional<EntityReference> reference = currentEntity.findReference(fieldName);
            if (reference.isPresent()) {
                QueryNode fieldNode = current.fetchReference(reference.get());
                processSelectionSet(executionContext, fieldNode, field.getSelectionSet());
                return;
            }

            current = current.fetchParent();
        }
        throw new QueryBuilderException(String.format("Failed to find field [%s] in hierarchy of entity [%s]",
                field.getName(),
                node.getEntity().getEntityName()));
    }

    private void processFragment(ExecutionContext executionContext,
                                 QueryNode node,
                                 String typeConditionName,
                                 SelectionSet selectionSet) {
        String entityName;
        if (typesProvider.isInterfaceType(typeConditionName)) {
            entityName = typesProvider.getEntityNameForInterface(typeConditionName);
        } else {
            entityName = typeConditionName;
        }

        Entity referencedEntity = config.getEntity(entityName);

        QueryNode targetEntity = fetchEntityAtHierarchy(node, referencedEntity);
        if (targetEntity != null) {
            // E.g. this node can never represent that entity
            processSelectionSet(executionContext, targetEntity, selectionSet);
        }
    }

    private static QueryNode fetchEntityAtHierarchy(QueryNode node, Entity entity) {

        QueryNode masterNode = node.getHierarchyMaster();

        Entity masterEntity = masterNode.getEntity();

        if (entity.isParentOf(masterEntity)) {
            QueryNode currentNode = masterNode;

            while (currentNode != null && !currentNode.getEntity().equals(entity)) {
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

        return null;
    }
}
