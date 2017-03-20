package graphql.sql.schema.impl;

import graphql.sql.schema.TypeReference;

class NotNullType extends Node implements TypeReference {
    private final TypeReference wrappedType;

    public NotNullType(TypeReference type, Location location) {
        super(location);
        wrappedType = type;
    }

    @Override
    public TypeReference getWrappedType() {
        return wrappedType;
    }

    @Override
    public boolean isNonNull() {
        return true;
    }
}
