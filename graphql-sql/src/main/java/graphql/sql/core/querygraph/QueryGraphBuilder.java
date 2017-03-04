package graphql.sql.core.querygraph;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import graphql.execution.ExecutionContext;
import graphql.language.*;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityReference;
import graphql.sql.core.extractor.FragmentExtractor;
import graphql.sql.core.extractor.NodeExtractor;
import graphql.sql.core.extractor.ScalarExtractor;

import java.util.*;

public class QueryGraphBuilder {

    private final Config config;
    private final GraphQLTypesProvider typesProvider;
    private QueryRoot graph;
    private NodeExtractor extractor;

    public QueryGraphBuilder(Config config, GraphQLTypesProvider typesProvider) {

        this.config = config;
        this.typesProvider = typesProvider;
    }

    public QueryRoot build(Entity rootEntity, Field rootField, ExecutionContext executionContext) {
        graph = new QueryRoot(rootEntity);
        extractor = new NodeExtractor();
        addPrimaryKeysToExtractor(graph, extractor);
        processSelectionSet(executionContext, graph, extractor, rootField.getSelectionSet());

        return graph;
    }

    public NodeExtractor getExtractor() {
        return extractor;
    }

    private void processSelectionSet(ExecutionContext executionContext, QueryNode node, FragmentExtractor extractor, SelectionSet selectionSet) {
        for (Selection selection : selectionSet.getSelections()) {
            processSelection(executionContext, node, extractor, selection);
        }
    }

    private void processSelection(ExecutionContext executionContext, QueryNode node, FragmentExtractor extractor, Selection selection) {
        if (selection instanceof Field) {
            processField(executionContext, node, extractor, (Field) selection);
        } else if (selection instanceof InlineFragment) {
            InlineFragment fragment = (InlineFragment) selection;
            TypeName typeCondition = fragment.getTypeCondition();

            processFragment(executionContext, node, extractor, typeCondition.getName(), fragment.getSelectionSet());

        } else if (selection instanceof FragmentSpread) {
            FragmentSpread fragmentSpread = (FragmentSpread) selection;
            FragmentDefinition fragment = executionContext.getFragment(fragmentSpread.getName());
            processFragment(executionContext, node, extractor, fragment.getTypeCondition().getName(), fragment.getSelectionSet());
        }
    }

    private void processField(ExecutionContext executionContext, QueryNode node, FragmentExtractor extractor, Field field) {
        QueryNode current = node;
        String fieldName = field.getAlias() == null ? field.getName() : field.getAlias();

        while (current != null) {
            Entity currentEntity = current.getEntity();
            Optional<EntityField> entityField = currentEntity.findField(fieldName);
            if (entityField.isPresent()) {
                int position = current.fetchField(entityField.get());
                extractor.addField(fieldName,
                        new ScalarExtractor(position, entityField.get().getScalarType().getTypeUtil()));
                return;
            }

            Optional<EntityReference> reference = currentEntity.findReference(field.getName());
            if (reference.isPresent()) {
                QueryNode referencedNode = current.fetchReference(reference.get());
                NodeExtractor referenceExtractor = new NodeExtractor();
                extractor.addReference(fieldName, referenceExtractor);
                addPrimaryKeysToExtractor(referencedNode, referenceExtractor);

                processSelectionSet(executionContext, referencedNode, referenceExtractor, field.getSelectionSet());
                return;
            }

            current = current.fetchParent();
        }
        throw new QueryBuilderException(String.format("Failed to find field [%s] in hierarchy of entity [%s]",
                field.getName(),
                node.getEntity().getEntityName()));
    }

    private void addPrimaryKeysToExtractor(QueryNode node, NodeExtractor extractor) {
        Entity entity = node.getEntity();
        // Get primary key constraint of referenced table to use as key
        DbConstraint primaryKeyConstraint = entity.getPrimaryKeyConstraint();
        for (DbColumn column : primaryKeyConstraint.getColumns()) {
            EntityField primaryKeyField = entity.findField(column);
            ScalarExtractor keyExtractor = new ScalarExtractor(node.fetchField(primaryKeyField),
                    primaryKeyField.getScalarType().getTypeUtil());
            extractor.addKeyExtractor(keyExtractor);
        }
    }

    private void processFragment(ExecutionContext executionContext,
                                 QueryNode node,
                                 FragmentExtractor extractor,
                                 String typeConditionName,
                                 SelectionSet selectionSet) {
        String entityName;
        if (typesProvider.isInterfaceType(typeConditionName)) {
            entityName = typesProvider.getEntityNameForInterface(typeConditionName);
        } else {
            entityName = typeConditionName;
        }

        Entity referencedEntity = config.getEntity(entityName);
        QueryNode referencedNode = node.fetchEntityAtHierarchy(referencedEntity);

        List<DbColumn> columns = referencedEntity.getPrimaryKeyConstraint().getColumns();
        int[] primaryKeyIndices = new int[columns.size()];
        int i = 0;
        for (DbColumn column : columns) {
            EntityField referencedEntityPrimaryField = referencedEntity.findField(column);
            int referencedEntityPrimaryColumnPosition = referencedNode.fetchField(referencedEntityPrimaryField);
            int relativePosition = extractor.addKeyExtractor(new ScalarExtractor(referencedEntityPrimaryColumnPosition,
                    referencedEntityPrimaryField.getScalarType().getTypeUtil()));
            primaryKeyIndices[i++] = relativePosition;
        }

        FragmentExtractor fragmentExtractor = new FragmentExtractor(extractor.getNodeExtractor(), primaryKeyIndices);
        extractor.addFragment(fragmentExtractor);

        processSelectionSet(executionContext, referencedNode, fragmentExtractor, selectionSet);
    }

}
