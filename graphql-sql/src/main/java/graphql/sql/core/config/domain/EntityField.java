package graphql.sql.core.config.domain;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

import javax.annotation.Nonnull;

public class EntityField {

    @Nonnull
    private final String fieldName;

    @Nonnull
    private final DbColumn column;

    @Nonnull
    private final ScalarType scalarType;

    private final boolean nullable;

    public EntityField(@Nonnull String fieldName,
                       @Nonnull DbColumn column,
                       @Nonnull ScalarType scalarType,
                       boolean nullable) {
        this.fieldName = fieldName;
        this.column = column;
        this.scalarType = scalarType;
        this.nullable = nullable;
    }

    @Nonnull
    public String getFieldName() {
        return fieldName;
    }

    @Nonnull
    public DbColumn getColumn() {
        return column;
    }

    @Nonnull
    public ScalarType getScalarType() {
        return scalarType;
    }

    public boolean isNullable() {
        return nullable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EntityField entityField = (EntityField) o;

        return fieldName.equals(entityField.fieldName);
    }

    @Override
    public int hashCode() {
        return fieldName.hashCode();
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
