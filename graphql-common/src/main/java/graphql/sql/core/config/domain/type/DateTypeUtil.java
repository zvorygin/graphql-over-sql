package graphql.sql.core.config.domain.type;

import graphql.Scalars;
import graphql.language.Value;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DateTypeUtil extends AbstractTypeUtil<Date> {

    public static final DateTypeUtil INSTANCE = new DateTypeUtil();

    private DateTypeUtil() {
        super(Scalars.GraphQLString, Date.class);
    }

    @Override
    public Date getValue(ResultSet rs, int position) throws SQLException {
        return rs.getDate(position);
    }

    @Override
    protected Date getRawValue(Value value) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    protected String getSqlType() {
        return "DATE";
    }
}
