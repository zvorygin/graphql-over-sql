package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaField;
import graphql.sql.schema.parser.SchemaTypeReference;

class SchemaFieldImpl extends NamedSchemaNode implements SchemaField {
    private final SchemaTypeReference typeReference;

    public SchemaFieldImpl(String name, Location location, SchemaTypeReference typeReference) {
        super(name, location);
        this.typeReference = typeReference;
    }

    @Override
    public SchemaTypeReference getType() {
        return typeReference;
    }
}
