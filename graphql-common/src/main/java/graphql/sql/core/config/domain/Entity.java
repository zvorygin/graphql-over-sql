package graphql.sql.core.config.domain;

import com.google.common.collect.Iterators;
import graphql.sql.core.config.domain.impl.SqlEntityReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * {@link graphql.schema.GraphQLCompositeType GraphQLCompositeType} type implementation
 */
public interface Entity extends Comparable<Entity> {
    /**
     * @return name of this entity
     */
    @Nonnull
    String getEntityName();

    /**
     * @return list of scalar fields in this entity
     */
    @Nonnull
    List<? extends EntityField> getEntityFields();

    /**
     * @return list of references of this entity (excluding parent reference if any)
     */
    @Nonnull
    List<? extends EntityReference> getEntityReferences();

    /**
     * @return parent reference of this entity if any
     */
    @Nullable
    SqlEntityReference getParentReference();

    /**
     * @return iterator to the root of this entity hierarchy
     */
    @Nonnull
    default Iterator<Entity> hierarchyIterator() {
        return new Iterator<Entity>() {
            Entity current = Entity.this;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Entity next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                Entity result = current;
                EntityReference parent = current.getParentReference();
                current = parent == null ? null : parent.getTargetEntity();
                return result;
            }
        };
    }

    /**
     * @return description for this entity
     */
    String getDescription();

    /**
     * @param name field name to look for
     * @return field with specified name
     */
    default EntityField findField(String name) {
        return getEntityFields().stream().filter(f -> f.getFieldName().equals(name)).findAny().orElse(null);
    }

    /**
     * @param referencedEntity potential child
     * @return <code>true</code> if current entity is equal or parent of referencedEntity
     */
    default boolean isParentOf(Entity referencedEntity) {
        return Iterators.contains(referencedEntity.hierarchyIterator(), this);
    }

    /**
     * @param other entity to compare with
     * @return result of entity name comparison
     */
    @Override
    default int compareTo(@Nonnull Entity other) {
        return getEntityName().compareTo(other.getEntityName());
    }
}
