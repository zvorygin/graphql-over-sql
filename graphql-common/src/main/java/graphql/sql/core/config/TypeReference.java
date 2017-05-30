package graphql.sql.core.config;

import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

import java.util.Objects;

public interface TypeReference {
    default boolean isCollection() {
        return false;
    }

    default boolean isNonNull() {
        return false;
    }

    default TypeReference getWrappedType() {
        return null;
    }

    default String getTypeName() {
        return getWrappedType().getTypeName();
    }

    default GraphQLInputType getGraphQLInputTypeReference() {
        GraphQLInputType result;
        if (getWrappedType() != null) {
            result = getWrappedType().getGraphQLInputTypeReference();
        } else {
            result = new GraphQLTypeReference(getTypeName());
        }

        if (isCollection()) {
            result = new GraphQLList(result);
        }

        if (isNonNull()) {
            result = new GraphQLNonNull(result);
        }

        return result;
    }

    default GraphQLOutputType getGraphQLOutputTypeReference() {
        GraphQLOutputType result;
        if (getWrappedType() != null) {
            result = getWrappedType().getGraphQLOutputTypeReference();
        } else {
            result = new GraphQLTypeReference(getTypeName());
        }

        if (isCollection()) {
            result = new GraphQLList(result);
        }

        if (isNonNull()) {
            result = new GraphQLNonNull(result);
        }

        return result;
    }

    static boolean equals(TypeReference first, TypeReference second) {
        if (first == null ^ second == null) {
            return false;
        }

        if (first == null) {
            return true;
        }

        if (first.isCollection() == second.isCollection() &&
                first.isNonNull() == second.isNonNull()) {
            return true;
        }

        if (first.getWrappedType() == null && second.getWrappedType() == null) {
            return Objects.equals(first.getTypeName(), second.getTypeName());
        }

        return equals(first.getWrappedType(), second.getWrappedType());
    }

    default String toGraphQLString() {
        String result;
        if (getWrappedType() != null) {
            result = getWrappedType().toGraphQLString();
        } else {
            result = getTypeName();
        }
        if (isCollection()) {
            result = "[" + result + "]";
        }

        if (isNonNull()) {
            result = result + "!";
        }

        return result;
    }
}
