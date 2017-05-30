package graphql.sql.core.config;

import javax.annotation.Nonnull;

public class CollectionTypeReference implements TypeReference {
    @Nonnull
    private final TypeReference typeReference;

    public CollectionTypeReference(@Nonnull TypeReference typeReference) {
        this.typeReference = typeReference;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public TypeReference getWrappedType() {
        return typeReference;
    }
}
