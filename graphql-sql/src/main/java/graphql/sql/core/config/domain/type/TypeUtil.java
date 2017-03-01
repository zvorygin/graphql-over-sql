package graphql.sql.core.config.domain.type;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.language.Node;
import graphql.language.Value;
import graphql.schema.GraphQLInputType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface TypeUtil {

    GraphQLInputType getGraphQLScalarType();

    String getArrayTypeName();

    Object getValue(Node child);

    Object getValue(ResultSet rs, int position) throws SQLException;

    QueryPreparer.StaticPlaceHolder createStaticPlaceHolder(Object value, QueryPreparer queryPreparer);
}
