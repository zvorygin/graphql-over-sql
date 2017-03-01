package graphql.sql.core.config.domain.type;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.Scalars;
import graphql.language.IntValue;
import graphql.language.Node;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerTypeUtil extends AbstractTypeUtil {

    public static final IntegerTypeUtil INSTANCE = new IntegerTypeUtil();

    private IntegerTypeUtil() {
        super(Scalars.GraphQLInt);
    }

    @Override
    public Integer getValue(Node value) {
        //TODO(dzvorygin) check range
        return ((IntValue)value).getValue().intValue();
    }

    @Override
    public Integer getValue(ResultSet rs, int position) throws SQLException {
        int result = rs.getInt(position);
        return rs.wasNull() ? null : result;
    }

    @Override
    public QueryPreparer.StaticPlaceHolder createStaticPlaceHolder(Object value, QueryPreparer queryPreparer) {
        return queryPreparer.addStaticPlaceHolder((Integer) value);
    }
}
