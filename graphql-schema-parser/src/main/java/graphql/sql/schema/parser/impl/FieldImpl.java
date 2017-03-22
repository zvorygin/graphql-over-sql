package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.Field;
import graphql.sql.schema.parser.TypeReference;

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
