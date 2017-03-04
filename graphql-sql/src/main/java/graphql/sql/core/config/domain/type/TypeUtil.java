package graphql.sql.core.config.domain.type;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.language.Node;
import graphql.schema.GraphQLInputType;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeUtil<T> {

    GraphQLInputType getGraphQLScalarType();

    T getValue(Node child);

    T getValue(ResultSet rs, int position) throws SQLException;

    QueryPreparer.StaticPlaceHolder createStaticPlaceHolder(T value, QueryPreparer queryPreparer);
}
