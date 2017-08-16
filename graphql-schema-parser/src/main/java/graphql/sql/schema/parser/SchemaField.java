package graphql.sql.schema.parser;

import graphql.sql.core.config.TypeReference;

import java.util.Map;

public interface SchemaField {

    String getName();

    TypeReference getType();

    Map<String, SchemaFieldArgument> getArguments();

    Map<String, SchemaAnnotation> getAnnotations();
}
