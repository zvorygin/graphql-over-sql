package graphql.sql.schema.parser.impl;

import graphql.sql.core.config.TypeReference;
import graphql.sql.schema.parser.SchemaField;

import java.util.Collections;
import java.util.Map;

class SchemaFieldImpl extends NamedSchemaNode implements SchemaField {
    private final TypeReference typeReference;
    private final Map<String, SchemaFieldArgumentImpl> arguments;

    public SchemaFieldImpl(String name, Location location, TypeReference typeReference) {
        this(name, location, typeReference, Collections.emptyMap());
    }

    public SchemaFieldImpl(String name,
                           Location location,
                           TypeReference typeReference,
                           Map<String, SchemaFieldArgumentImpl> arguments) {
        super(name, location);
        this.typeReference = typeReference;
        this.arguments = arguments;
    }

    @Override
    public TypeReference getType() {
        return typeReference;
    }
}
