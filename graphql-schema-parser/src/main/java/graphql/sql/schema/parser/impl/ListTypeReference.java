package graphql.sql.schema.parser.impl;

import graphql.sql.core.config.TypeReference;

import javax.annotation.Nonnull;

class ListTypeReference extends SchemaNode implements TypeReference {
    @Nonnull
    private final TypeReference wrappedType;

    public ListTypeReference(@Nonnull TypeReference typeReference, Location location) {
        super(location);
        wrappedType = typeReference;
    }

    @Nonnull
    @Override
    public TypeReference getWrappedType() {
        return wrappedType;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public String toString() {
        return "[" + wrappedType.toString() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ListTypeReference that = (ListTypeReference) o;

        return wrappedType.equals(that.wrappedType);
    }

    @Override
    public int hashCode() {
        return wrappedType.hashCode();
    }
}
