package graphql.sql.schema.parser.impl;

import graphql.sql.core.config.TypeReference;

public class SchemaFieldArgumentImpl {
    private final String argumentName;
    private final TypeReference typeReference;

    public SchemaFieldArgumentImpl(String argumentName, TypeReference typeReference) {
        this.argumentName = argumentName;
        this.typeReference = typeReference;
    }

    public String getArgumentName() {
        return argumentName;
    }

    public TypeReference getTypeReference() {
        return typeReference;
    }
}
