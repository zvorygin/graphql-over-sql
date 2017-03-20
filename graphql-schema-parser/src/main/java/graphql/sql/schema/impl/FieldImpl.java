package graphql.sql.schema.impl;

import graphql.sql.schema.Field;
import graphql.sql.schema.TypeReference;

class FieldImpl extends NamedNode implements Field {
    private final TypeReference type;

    public FieldImpl(String name, Location location, TypeReference type) {
        super(name, location);
        this.type = type;
    }

    public TypeReference getType() {
        return type;
    }
}
