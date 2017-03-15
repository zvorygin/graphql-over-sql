package graphql.sql.core;

import graphql.execution.ExecutionContext;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.TypeName;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityReference;
import graphql.sql.core.config.domain.impl.Key;
import graphql.sql.core.config.domain.ReferenceType;
import graphql.sql.core.config.domain.impl.SqlEntityReference;
import graphql.sql.core.config.domain.impl.SqlEntityField;
import graphql.sql.core.config.domain.impl.SqlEntity;
import graphql.sql.core.extractor.FragmentExtractor;
import graphql.sql.core.extractor.NodeExtractor;
import graphql.sql.core.extractor.ScalarExtractor;
import graphql.sql.core.querygraph.QueryBuilderException;
import graphql.sql.core.querygraph.QueryNode;
import graphql.sql.core.querygraph.QueryRoot;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class GraphQLQueryExecutorBuilder {

    private final Config config;
    private final GraphQLTypesProvider typesProvider;

    public GraphQLQueryExecutorBuilder(Config config, GraphQLTypesProvider typesProvider) {
        this.config = config;
        this.typesProvider = typesProvider;
    }

    public GraphQLQueryExecutor build(SqlEntity rootEntity, Field rootField, ExecutionContext executionContext) {
        QueryRoot graph = new QueryRoot(rootEntity);
        NodeExtractor extractor = new NodeExtractor();
        addPrimaryKeysToExtractor(graph, extractor);
        processSelectionSet(executionContext, graph, extractor, rootField.getSelectionSet());

        return new GraphQLQueryExecutor(graph, extractor);
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
        String alias = field.getAlias() == null ? field.getName() : field.getAlias();

        while (true) {
            SqlEntity currentEntity = current.getEntity();
            EntityField entityField = currentEntity.findField(field.getName());
            if (entityField != null) {
                int position = current.fetchField(entityField);
                extractor.addScalarField(alias,
                        new ScalarExtractor<>(position, entityField.getScalarType().getTypeUtil()));
                return;
            }

            Optional<SqlEntityReference> reference = currentEntity.findReference(field.getName());
            if (reference.isPresent()) {
                EntityReference entityReference = reference.get();
                QueryNode referencedNode = current.fetchReference(entityReference);
                NodeExtractor referenceExtractor = new NodeExtractor();
                addPrimaryKeysToExtractor(referencedNode, referenceExtractor);
                if (entityReference.getReferenceType() == ReferenceType.ONE_TO_MANY) {
                    extractor.addNestedCollection(alias, referenceExtractor);
                } else {
                    extractor.addCompositeFieldExtractor(alias, referenceExtractor);
                }

                processSelectionSet(executionContext, referencedNode, referenceExtractor, field.getSelectionSet());
                return;
            }

            if (current.getEntity().getParentReference() == null) {
                throw new QueryBuilderException(String.format("Failed to find field [%s] in hierarchy of entity [%s]",
                        field.getName(),
                        node.getEntity().getEntityName()));

            }

            current = current.fetchParent();
        }
    }

    private void addPrimaryKeysToExtractor(QueryNode node, NodeExtractor extractor) {
        for (EntityField field : getEntityKeyFields(node.getEntity())) {
            ScalarExtractor keyExtractor = new ScalarExtractor<>(
                    node.fetchField(field),
                    field.getScalarType().getTypeUtil());
            extractor.addKeyExtractor(keyExtractor);
        }
    }

    @Nonnull
    private List<SqlEntityField> getEntityKeyFields(SqlEntity entity) {
        List<SqlEntityField> keyFields;
        Key primaryKey = entity.getPrimaryKey();

        if (primaryKey != null) {
            keyFields = primaryKey.getFields();
        } else {
            keyFields = entity.getEntityFields();
        }
        return keyFields;
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

        SqlEntity referencedEntity = config.getEntity(entityName);
        QueryNode referencedNode = node.fetchEntityAtHierarchy(referencedEntity);

        List<SqlEntityField> fields = getEntityKeyFields(referencedEntity);
        int[] primaryKeyIndices = new int[fields.size()];
        int i = 0;
        for (EntityField field : fields) {
            int fieldPosition = referencedNode.fetchField(field);
            int relativePosition = extractor.addKeyExtractor(
                    new ScalarExtractor<>(fieldPosition,
                            field.getScalarType().getTypeUtil()));
            primaryKeyIndices[i++] = relativePosition;
        }

        FragmentExtractor fragmentExtractor = new FragmentExtractor(extractor.getNodeExtractor(), primaryKeyIndices);
        extractor.addFragment(fragmentExtractor);

        processSelectionSet(executionContext, referencedNode, fragmentExtractor, selectionSet);
    }

}
