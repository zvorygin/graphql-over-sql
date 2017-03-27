package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaTypeReference;

import javax.annotation.Nonnull;

public class TypeReferenceImpl extends SchemaNode implements SchemaTypeReference {
    @Nonnull
    private final String typeName;

    public TypeReferenceImpl(@Nonnull String typeName, Location location) {
        super(location);
        this.typeName = typeName;
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypeReferenceImpl that = (TypeReferenceImpl) o;

        return typeName.equals(that.typeName);
    }

    @Override
    public int hashCode() {
        return typeName.hashCode();
    }

    @Override
    public String toString() {
        return typeName;
    }
}
