package graphql.sql.core.config.domain;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class Key {
    @Nonnull
    private final List<EntityField> fields;

    @Nullable
    private final String name;

    @Nullable
    private final DbConstraint constraint;

    public Key(@Nonnull List<EntityField> fields,
               @Nullable String name,
               @Nullable DbConstraint constraint) {
        this.fields = fields;
        this.name = name;
        this.constraint = constraint;
    }

    @Nonnull
    public List<EntityField> getFields() {
        return fields;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public DbConstraint getConstraint() {
        return constraint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Key key = (Key) o;

        return fields.equals(key.fields) &&
                (name != null ? name.equals(key.name) : key.name == null) &&
                (constraint != null ? constraint.equals(key.constraint) : key.constraint == null);
    }

    @Override
    public int hashCode() {
        int result = fields.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (constraint != null ? constraint.hashCode() : 0);
        return result;
    }
}
