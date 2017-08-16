package graphql.sql.schema.parser.impl;

import graphql.sql.core.config.TypeReference;

import javax.annotation.Nonnull;

class NotNullTypeReference extends SchemaNode implements TypeReference {
    @Nonnull
    private final TypeReference wrappedType;

    public NotNullTypeReference(@Nonnull TypeReference type, Location location) {
        super(location);
        wrappedType = type;
    }

    @Nonnull
    @Override
    public TypeReference getWrappedType() {
        return wrappedType;
    }

    @Override
    public boolean isNonNull() {
        return true;
    }

    @Override
    public String toString() {
        return wrappedType.toString() + "!";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NotNullTypeReference that = (NotNullTypeReference) o;

        return wrappedType.equals(that.wrappedType);
    }

    @Override
    public int hashCode() {
        return wrappedType.hashCode();
    }
}
