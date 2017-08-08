package graphql.sql.schema.parser;

import graphql.sql.core.config.TypeReference;

public interface SchemaFieldArgument {
    String getArgumentName();

    TypeReference getTypeReference();
}
