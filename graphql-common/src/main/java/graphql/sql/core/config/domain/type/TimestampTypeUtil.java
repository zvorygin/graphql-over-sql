package graphql.sql.core.config.domain.type;

import graphql.Scalars;
import graphql.language.Value;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TimestampTypeUtil extends AbstractTypeUtil<Timestamp> {

    public static final TimestampTypeUtil INSTANCE = new TimestampTypeUtil();

    private TimestampTypeUtil() {
        super(Scalars.GraphQLString, Timestamp.class);
    }

    @Override
    public Timestamp getValue(ResultSet rs, int position) throws SQLException {
        return rs.getTimestamp(position);
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
