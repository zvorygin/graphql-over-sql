package graphql.sql.core.config.domain.type;

import graphql.language.Node;
import graphql.schema.GraphQLInputType;

public abstract class AbstractTypeUtil<T> implements TypeUtil<T> {

    private final GraphQLInputType graphQLScalarType;

    protected AbstractTypeUtil(GraphQLInputType graphQLScalarType) {
        this.graphQLScalarType = graphQLScalarType;
    }

    @Override
    public GraphQLInputType getGraphQLScalarType() {
        return graphQLScalarType;
    }

    public T getValue(Node value) {
        throw new IllegalStateException("Not implemented yet");
    }
}
