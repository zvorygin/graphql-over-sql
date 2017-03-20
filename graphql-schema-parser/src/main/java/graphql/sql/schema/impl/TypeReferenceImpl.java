package graphql.sql.schema.impl;

import graphql.sql.schema.TypeReference;

public class TypeReferenceImpl extends Node implements TypeReference {
    private final String typeName;

    public TypeReferenceImpl(String typeName, Location location) {
        super(location);
        this.typeName = typeName;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }
}
