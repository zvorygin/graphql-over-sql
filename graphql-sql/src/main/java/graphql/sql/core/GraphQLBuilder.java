package graphql.sql.core;

import graphql.GraphQL;
import graphql.sql.core.config.GraphQLTypesProvider;

public class GraphQLBuilder {
    private final GraphQLTypesProvider typesProvider;

    public GraphQLBuilder(GraphQLTypesProvider typesProvider) {
        this.typesProvider = typesProvider;
    }

    public GraphQL build() {
        return new GraphQL(typesProvider.getSchema());
    }
}
