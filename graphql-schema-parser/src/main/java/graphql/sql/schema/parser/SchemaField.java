package graphql.sql.schema.parser;

import graphql.sql.core.config.TypeReference;

public interface SchemaField {

    String getName();

    TypeReference getType();
}
