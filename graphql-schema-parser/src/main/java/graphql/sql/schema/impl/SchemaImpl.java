package graphql.sql.schema.impl;

import graphql.sql.schema.Schema;

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

    @Override
    @Nonnull
    public TypeReferenceImpl getQueryType() {
        return queryType;
    }

    @Override
    @Nullable
    public TypeReferenceImpl getMutationType() {
        return mutationType;
    }
}
