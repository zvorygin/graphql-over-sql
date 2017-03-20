package graphql.sql.schema.impl;

import graphql.sql.schema.TypeReference;

class ListType extends Node implements TypeReference {
    private final TypeReference wrappedType;

    public ListType(TypeReference typeReference, Location location) {
        super(location);
        wrappedType = typeReference;
    }

    @Override
    public TypeReference getWrappedType() {
        return wrappedType;
    }

    @Override
    public boolean isCollection() {
        return true;
    }
}
