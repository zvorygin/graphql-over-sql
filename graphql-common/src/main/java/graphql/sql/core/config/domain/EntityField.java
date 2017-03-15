package graphql.sql.core.config.domain;

import javax.annotation.Nonnull;

public interface EntityField {
    @Nonnull
    String getFieldName();

    @Nonnull
    ScalarType getScalarType();

    boolean isNullable();

    String getDescription();
}
