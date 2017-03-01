package graphql.sql.core.config.domain.type;

import graphql.language.Node;
import graphql.schema.GraphQLInputType;

public abstract class AbstractTypeUtil implements TypeUtil {

    private final GraphQLInputType graphQLScalarType;

    protected AbstractTypeUtil(GraphQLInputType graphQLScalarType) {
        this.graphQLScalarType = graphQLScalarType;
    }

    @Override
    public GraphQLInputType getGraphQLScalarType() {
        return graphQLScalarType;
    }

    @Override
    public String getArrayTypeName() {
        return "DZVORYGIN.VARCHAR_TABLE";
    }

    public Object getValue(Node value) {
        throw new IllegalStateException("Not implemented yet");
    }
}
