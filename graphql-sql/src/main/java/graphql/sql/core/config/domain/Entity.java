package graphql.sql.core.config.domain;

import com.google.common.collect.Iterators;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Entity implements Comparable<Entity> {

    @Nonnull
    private final String entityName;

    @Nonnull
    private final DbTable table;

    @Nonnull
    private final List<EntityField> entityFields = new ArrayList<>();

    @Nonnull
    private final List<EntityReference> entityReferences = new ArrayList<>();

    @Nullable
    private final EntityReference parentReference;

    public Entity(@Nonnull String entityName,
                  @Nonnull DbTable table,
                  @Nonnull List<EntityField> entityFields,
                  @Nullable EntityReference parentReference) {
        this.entityName = entityName;
        this.table = table;
        this.entityFields.addAll(entityFields);
        this.parentReference = parentReference;
    }

    @Nonnull
    public String getEntityName() {
        return entityName;
    }

    @Nonnull
    public DbTable getTable() {
        return table;
    }

    @Nonnull
    public List<EntityField> getEntityFields() {
        return entityFields;
    }

    public EntityReference getParentReference() {
        return parentReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Entity entity = (Entity) o;

        return entityName.equals(entity.entityName);
    }

    public Iterator<Entity> hierarchyIterator() {
        return new Iterator<Entity>() {
            Entity current = Entity.this;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Entity next() {
                Entity result = current;
                EntityReference parentReference = current.getParentReference();
                current = parentReference == null ? null : parentReference.getTargetEntity();
                return result;
            }
        };
    }

    @Override
    public int hashCode() {
        return entityName.hashCode();
    }

    @Override
    public String toString() {
        return entityName;
    }

    public EntityField getField(String name) {
        // TODO (dzvorygin) use cache here
        Optional<EntityField> field = findField(name);
        if (!field.isPresent()) {
            throw new IllegalStateException(String.format("Field [%s] not found on entity [%s]", name, entityName));
        }
        return field.get();
    }

    public Optional<EntityField> findField(String name) {
        return entityFields.stream().filter(f -> f.getFieldName().equals(name)).findAny();
    }

    public boolean isParentOf(Entity referencedEntity) {
        return Iterators.contains(referencedEntity.hierarchyIterator(), this);
    }

    public void addReference(EntityReference reference) {
        this.entityReferences.add(reference);
    }

    @Nonnull
    public List<EntityReference> getEntityReferences() {
        return entityReferences;
    }

    public Optional<EntityReference> findReference(String fieldName) {
        return entityReferences.stream().filter(reference -> reference.getName().equals(fieldName)).findAny();
    }

    public EntityField getAt(String name) {
        return findField(name).orElseThrow(() -> new NoSuchElementException(String.format("Entity [%s] doesn't have field [%s]", this, name)));
    }

    @Override
    public int compareTo(Entity o) {
        return entityName.compareTo(o.entityName);
    }
}
