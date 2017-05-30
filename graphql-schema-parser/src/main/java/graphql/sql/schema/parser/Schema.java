package graphql.sql.schema.parser;

import graphql.sql.core.config.TypeReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Schema {

    @Nonnull
    TypeReference getQueryType();

    @Nullable
    TypeReference getMutationType();
}
