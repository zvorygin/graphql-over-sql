package graphql.sql.core.config.domain;

import graphql.sql.core.config.domain.impl.SqlEntity;

import javax.annotation.Nonnull;

public interface EntityReference {
    @Nonnull
    String getName();

    @Nonnull
    SqlEntity getTargetEntity();

    @Nonnull
    ReferenceType getReferenceType();

    boolean isNullable();

    String getDescription();
}
