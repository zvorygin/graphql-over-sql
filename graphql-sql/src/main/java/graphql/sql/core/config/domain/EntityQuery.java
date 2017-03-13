package graphql.sql.core.config.domain;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class EntityQuery implements Comparable<EntityQuery> {

    @Nonnull
    private final String name;

    @Nonnull
    private final Entity entity;

    @Nonnull
    private final List<EntityField> entityFields;

    public EntityQuery(@Nonnull String name,
                       @Nonnull Entity entity,
                       @Nonnull List<EntityField> entityFields) {
        this.name = name;
        this.entity = entity;
        this.entityFields = entityFields;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Entity getEntity() {
        return entity;
    }

    @Nonnull
    public List<EntityField> getEntityFields() {
        return entityFields;
    }

    @Override
    public int compareTo(@Nonnull EntityQuery o) {
        return name.compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityQuery that = (EntityQuery) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
