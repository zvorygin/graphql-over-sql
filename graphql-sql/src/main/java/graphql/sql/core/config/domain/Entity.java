package graphql.sql.core.config.domain;

import com.google.common.collect.Iterators;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

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

    @Nullable
    private final DbConstraint primaryKeyConstraint;

    public Entity(@Nonnull String entityName,
                  @Nonnull DbTable table,
                  @Nonnull List<EntityField> entityFields,
                  @Nullable EntityReference parentReference,
                  @Nullable DbConstraint primaryKeyConstraint) {
        this.entityName = entityName;
        this.table = table;
        this.entityFields.addAll(entityFields);
        this.parentReference = parentReference;
        this.primaryKeyConstraint = primaryKeyConstraint;
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

    @Nullable
    public EntityReference getParentReference() {
        return parentReference;
    }

    @Nullable
    public DbConstraint getPrimaryKeyConstraint() {
        return primaryKeyConstraint;
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

    public Optional<EntityField> findField(String name) {
        return entityFields.stream().filter(f -> f.getFieldName().equals(name)).findAny();
    }

    public boolean isParentOf(Entity referencedEntity) {
        return Iterators.contains(referencedEntity.hierarchyIterator(), this);
    }

    public void addReference(EntityReference reference) {
        entityReferences.add(reference);
    }

    @Nonnull
    public List<EntityReference> getEntityReferences() {
        return entityReferences;
    }

    public Optional<EntityReference> findReference(String fieldName) {
        return entityReferences.stream().filter(reference -> reference.getName().equals(fieldName)).findAny();
    }

    @SuppressWarnings("unused") // Used from groovy script
    public EntityField getAt(String name) {
        return findField(name).orElseThrow(() -> new NoSuchElementException(String.format("Entity [%s] doesn't have field [%s]", this, name)));
    }

    @Override
    public int compareTo(@Nonnull Entity o) {
        return entityName.compareTo(o.entityName);
    }

    public EntityField findField(DbColumn column) {
        // TODO(dzvorygin) use HashMap here
        for (EntityField entityField : entityFields) {
            if (entityField.getColumn().equals(column))
                return entityField;
        }

        throw new NoSuchElementException(String.format("Column [%s] not found in entity [%s]", column.getName(), getEntityName()));
    }
}
