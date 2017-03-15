package graphql.sql.core.config.domain.impl;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.ScalarType;

import javax.annotation.Nonnull;

public class SqlEntityField implements EntityField {

    @Nonnull
    private final String fieldName;

    @Nonnull
    private final DbColumn column;

    @Nonnull
    private final ScalarType scalarType;

    private final boolean nullable;

    @Nonnull
    private final String description;

    public SqlEntityField(@Nonnull String fieldName,
                          @Nonnull DbColumn column,
                          @Nonnull ScalarType scalarType,
                          boolean nullable) {
        this.fieldName = fieldName;
        this.column = column;
        this.scalarType = scalarType;
        this.nullable = nullable;
        description = String.format("Field for column [%s]", column.getAbsoluteName());

    }

    @Override
    @Nonnull
    public String getFieldName() {
        return fieldName;
    }

    @Nonnull
    public DbColumn getColumn() {
        return column;
    }

    @Override
    @Nonnull
    public ScalarType getScalarType() {
        return scalarType;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SqlEntityField entityField = (SqlEntityField) o;

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
