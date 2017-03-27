package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaTypeReference;

import javax.annotation.Nonnull;

class ListSchemaTypeReference extends SchemaNode implements SchemaTypeReference {
    @Nonnull
    private final SchemaTypeReference wrappedType;

    public ListSchemaTypeReference(@Nonnull SchemaTypeReference typeReference, Location location) {
        super(location);
        wrappedType = typeReference;
    }

    @Nonnull
    @Override
    public SchemaTypeReference getWrappedType() {
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

        ListSchemaTypeReference that = (ListSchemaTypeReference) o;

        return wrappedType.equals(that.wrappedType);
    }

    @Override
    public int hashCode() {
        return wrappedType.hashCode();
    }
}
