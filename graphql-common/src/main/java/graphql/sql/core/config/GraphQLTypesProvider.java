package graphql.sql.core.config;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.sql.core.config.domain.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GraphQLTypesProvider {

    private final GraphQLSchema schema;

    public GraphQLTypesProvider(Config config) {
        Map<String, GraphQLType> dictionary = new HashMap<>();

        config.getInterfaces().values().stream().map(anInterface -> anInterface.getGraphQLType(dictionary)).forEach(i -> dictionary.put(i.getName(), i));

        config.getScalars().values().stream().map(scalar -> scalar.getGraphQLType(dictionary)).forEach(s -> dictionary.put(s.getName(), s));

        GraphQLType queryType = null;

        for (ObjectType objectType : config.getTypes().values()) {
            GraphQLType graphQLType = objectType.getGraphQLType(dictionary);
            if (graphQLType.getName().equals(config.getQueryTypeName())) {
                queryType = graphQLType;
            }
            dictionary.put(graphQLType.getName(), graphQLType);
        }

        if (queryType == null) {
            throw new ConfigurationException(String.format("Query type [%s] not found", config.getQueryTypeName()));
        }

        schema = GraphQLSchema.newSchema().query((GraphQLObjectType) queryType).build(new HashSet<>(dictionary.values()));
    }

    public GraphQLSchema getSchema() {
        return schema;
    }
}
