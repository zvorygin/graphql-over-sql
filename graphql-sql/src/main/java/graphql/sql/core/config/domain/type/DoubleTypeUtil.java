package graphql.sql.core.config.domain.type;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.Scalars;
import graphql.language.FloatValue;
import graphql.language.Node;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleTypeUtil extends AbstractTypeUtil {

    public static final DoubleTypeUtil INSTANCE = new DoubleTypeUtil();

    private DoubleTypeUtil() {
        super(Scalars.GraphQLFloat);
    }

    @Override
    public Double getValue(Node value) {
        return ((FloatValue) value).getValue().doubleValue();
    }

    @Override
    public Double getValue(ResultSet rs, int position) throws SQLException {
        double result = rs.getDouble(position);
        return rs.wasNull() ? null : result;
    }

    @Override
    public QueryPreparer.StaticPlaceHolder createStaticPlaceHolder(Object value, QueryPreparer queryPreparer) {
        return queryPreparer.addStaticPlaceHolder(new QueryPreparer.StaticPlaceHolder(queryPreparer) {
            @Override
            public void setValue(PreparedStatement ps) throws SQLException {
                ps.setDouble(getIndex(), (Double) value);
            }

            @Override
            public String displayToString() {
                return "'" + value + "'";
            }
        });
    }
}
