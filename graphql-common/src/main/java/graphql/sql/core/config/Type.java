package graphql.sql.core.config;

import graphql.schema.GraphQLType;

import java.util.Map;

public interface Type {
    String getName();

    GraphQLType getGraphQLType(Map<String, GraphQLType> dictionary);
}
