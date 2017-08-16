package graphql.sql.core.config;

import javax.annotation.Nonnull;

public class NonNullTypeReference implements TypeReference {
    @Nonnull
    private final TypeReference wrappedType;

    public NonNullTypeReference(@Nonnull TypeReference wrappedType) {
        this.wrappedType = wrappedType;
    }

    @Override
    public boolean isNonNull() {
        return true;
    }

    @Nonnull
    @Override
    public TypeReference getWrappedType() {
        return wrappedType;
    }
}
