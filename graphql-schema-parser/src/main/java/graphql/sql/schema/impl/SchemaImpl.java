package graphql.sql.schema.impl;

import graphql.sql.schema.Schema;
import graphql.sql.schema.TypeReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class SchemaImpl extends Node implements Schema {
    @Nonnull
    private final TypeReferenceImpl queryType;

    @Nullable
    private final TypeReferenceImpl mutationType;


    public SchemaImpl(@Nonnull TypeReferenceImpl queryType,
                      @Nullable TypeReferenceImpl mutationType,
                      @Nonnull Location location) {
        super(location);
        this.queryType = queryType;
        this.mutationType = mutationType;
    }

    @Nonnull
    public TypeReferenceImpl getQueryType() {
        return queryType;
    }

    @Nullable
    public TypeReferenceImpl getMutationType() {
        return mutationType;
    }
}
