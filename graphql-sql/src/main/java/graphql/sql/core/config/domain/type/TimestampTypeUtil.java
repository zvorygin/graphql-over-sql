package graphql.sql.core.config.domain.type;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.Scalars;
import graphql.language.Value;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TimestampTypeUtil extends AbstractTypeUtil<Timestamp> {

    public static final TimestampTypeUtil INSTANCE = new TimestampTypeUtil();

    private TimestampTypeUtil() {
        super(Scalars.GraphQLString);
    }

    @Override
    public Timestamp getValue(ResultSet rs, int position) throws SQLException {
        return rs.getTimestamp(position);
    }

    @Override
    public QueryPreparer.StaticPlaceHolder createStaticPlaceHolder(Timestamp value, QueryPreparer queryPreparer) {
        return queryPreparer.addStaticPlaceHolder(new QueryPreparer.StaticPlaceHolder(queryPreparer) {
            @Override
            public void setValue(PreparedStatement ps) throws SQLException {
                ps.setTimestamp(getIndex(), value);
            }

            @Override
            public String displayToString() {
                return "'" + value + "'";
            }
        });
    }

    @Override
    protected Timestamp getRawValue(Value value) {
        throw new IllegalStateException("Not implemented yet.");
    }

    @Override
    protected String getSqlType() {
        return "TIMESTAMP";
    }
}
