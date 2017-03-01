package graphql.sql.core.config.domain.type;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.Scalars;
import graphql.language.Node;
import graphql.language.StringValue;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringTypeUtil extends AbstractTypeUtil {

    public static final StringTypeUtil INSTANCE = new StringTypeUtil();

    private StringTypeUtil() {
        super(Scalars.GraphQLString);
    }

    @Override
    public String getValue(Node value) {
        return ((StringValue)value).getValue();
    }

    @Override
    public String getValue(ResultSet rs, int position) throws SQLException {
        return rs.getString(position);
    }

    @Override
    public QueryPreparer.StaticPlaceHolder createStaticPlaceHolder(Object value, QueryPreparer queryPreparer) {
        return queryPreparer.addStaticPlaceHolder((String)value);
    }
}
