package graphql.sql.schema.parser.impl;

import graphql.sql.core.config.TypeReference;
import graphql.sql.schema.parser.SchemaFieldArgument;

public class SchemaFieldArgumentImpl implements SchemaFieldArgument {
    private final String argumentName;
    private final TypeReference typeReference;

    public SchemaFieldArgumentImpl(String argumentName, TypeReference typeReference) {
        this.argumentName = argumentName;
        this.typeReference = typeReference;
    }

    @Override
    public String getArgumentName() {
        return argumentName;
    }

    @Override
    public TypeReference getTypeReference() {
        return typeReference;
    }
}
