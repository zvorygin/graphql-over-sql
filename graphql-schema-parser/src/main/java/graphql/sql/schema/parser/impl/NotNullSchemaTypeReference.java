package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaTypeReference;

import javax.annotation.Nonnull;

class NotNullSchemaTypeReference extends SchemaNode implements SchemaTypeReference {
    @Nonnull
    private final SchemaTypeReference wrappedType;

    public NotNullSchemaTypeReference(@Nonnull SchemaTypeReference type, Location location) {
        super(location);
        wrappedType = type;
    }

    @Nonnull
    @Override
    public SchemaTypeReference getWrappedType() {
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

        NotNullSchemaTypeReference that = (NotNullSchemaTypeReference) o;

        return wrappedType.equals(that.wrappedType);
    }

    @Override
    public int hashCode() {
        return wrappedType.hashCode();
    }
}
