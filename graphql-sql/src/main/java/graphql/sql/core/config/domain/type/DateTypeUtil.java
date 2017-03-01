package graphql.sql.core.config.domain.type;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.Scalars;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DateTypeUtil extends AbstractTypeUtil {

    public static final DateTypeUtil INSTANCE = new DateTypeUtil();

    private DateTypeUtil() {
        super(Scalars.GraphQLString);
    }

    @Override
    public Date getValue(ResultSet rs, int position) throws SQLException {
        return rs.getDate(position);
    }

    @Override
    public QueryPreparer.StaticPlaceHolder createStaticPlaceHolder(Object value, QueryPreparer queryPreparer) {
        return queryPreparer.addStaticPlaceHolder(new QueryPreparer.StaticPlaceHolder(queryPreparer) {
            @Override
            public void setValue(PreparedStatement ps) throws SQLException {
                ps.setDate(getIndex(), (Date) value);
            }

            @Override
            public String displayToString() {
                return "'" + value + "'";
            }
        });
    }
}
