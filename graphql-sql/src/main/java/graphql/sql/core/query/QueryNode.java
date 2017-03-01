package graphql.sql.core.query;

import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityReference;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class QueryNode {

    private final QueryGraph graph;

    private final int nodeNumber;

    private final boolean hierarchyMaster;

    private final Entity entity;

    private Collection<EntityField> fieldsToQuery = new LinkedHashSet<>();

    private Map<String, QueryNode> references = new LinkedHashMap<>();

    private QueryNode parent;

    private Map<Entity, QueryNode> children = new LinkedHashMap<>();

    protected QueryNode(Entity entity, boolean hierarchyMaster, QueryGraph graph, int nodeNumber) {
        this.entity = entity;
        this.hierarchyMaster = hierarchyMaster;
        // TODO(dzvorygin) fix this hacky code one day.
        if (graph != null) {
            this.graph = graph;
        } else {
            this.graph = (QueryGraph) this;
        }
        this.nodeNumber = nodeNumber;
    }

    public QueryNode fetchParent() {
        if (parent == null) {

            entity.getParentReference().ifPresent(ref -> {
                        parent = new QueryNode(ref.getTargetEntity(), false, graph, graph.nextNodeNumber());
                        parent.children.put(entity, this);
                    }
            );
        }

        return parent;
    }

    public QueryNode fetchChild(Entity child) {
        Entity childParent = child.getParentReference()
                .orElseThrow(() -> new QueryBuilderException(
                        String.format("Entity [%s] doesn't have parent", child.getEntityName())))
                .getTargetEntity();

        if (!childParent.equals(entity)) {
            throw new QueryBuilderException(
                    String.format("Expected [%s] parent to be [%s], but got [%s]",
                            child.getEntityName(),
                            childParent.getEntityName(),
                            entity.getEntityName()));
        }

        return children.computeIfAbsent(child, entity -> {
            QueryNode result = new QueryNode(child, false, graph, graph.nextNodeNumber());
            result.parent = this;
            return result;
        });
    }

    public QueryNode fetchReference(EntityReference reference) {
        return references.computeIfAbsent(reference.getName(),
                (ref) -> new QueryNode(reference.getTargetEntity(), true, graph, graph.nextNodeNumber()));
    }

    public void fetchField(EntityField entityField) {
        if (!fieldsToQuery.contains(entityField)) {
            fieldsToQuery.add(entityField);
        }
    }

    public Entity getEntity() {
        return entity;
    }

    @Nonnull
    public QueryNode findHierarchyMaster() {
        return Stream.<Supplier<Optional<QueryNode>>>of(this::findHierarchyMasterUp,
                this::findHierarchyMasterDown)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findAny()
                .orElseThrow(IllegalStateException::new)
                .orElseThrow(IllegalStateException::new);
    }

    private Optional<QueryNode> findHierarchyMasterUp() {
        QueryNode current = this;
        while (current != null) {
            if (current.hierarchyMaster) {
                return Optional.of(current);
            }
            current = current.parent;
        }
        return Optional.empty();
    }

    private Optional<QueryNode> findHierarchyMasterDown() {
        if (hierarchyMaster) {
            return Optional.of(this);
        }

        return children.values().stream()
                .map(QueryNode::findHierarchyMasterDown)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    public Collection<EntityField> getFieldsToQuery() {
        return fieldsToQuery;
    }

    public QueryNode getParent() {
        return parent;
    }

    public Map<Entity, QueryNode> getChildren() {
        return children;
    }

    public Map<String, QueryNode> getReferences() {
        return references;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public QueryNode findFieldInHierarchy(EntityField entityField) {
        QueryNode current = this;
        while (current != null) {
            if (current.getFieldsToQuery().contains(entityField))
                return current;
            current = current.getParent();
        }
        return null;
    }
}
