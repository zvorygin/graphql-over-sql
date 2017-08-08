package graphql.sql.core.config.domain.type;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.language.Node;
import graphql.language.Value;
import graphql.schema.GraphQLScalarType;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeUtil<T> {

    GraphQLScalarType getGraphQLScalarType();

    T getValue(Node child);

    T getValue(ResultSet rs, int position) throws SQLException;

    T getValue(Object raw);
}
