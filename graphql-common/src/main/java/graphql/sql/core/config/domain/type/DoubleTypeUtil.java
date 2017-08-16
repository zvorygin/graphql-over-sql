package graphql.sql.core.config.domain.type;

import graphql.Scalars;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.Node;
import graphql.language.Value;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleTypeUtil extends AbstractTypeUtil<Double> {

    public static final DoubleTypeUtil INSTANCE = new DoubleTypeUtil();

    private DoubleTypeUtil() {
        super(Scalars.GraphQLFloat, Double.class);
    }

    @Override
    public Double getValue(Node value) {
        return ((FloatValue) value).getValue().doubleValue();
    }

    @Override
    protected Double getRawValue(Value value) {
        if (value instanceof IntValue) {
            return ((IntValue) value).getValue().doubleValue();
        }
        return ((FloatValue) value).getValue().doubleValue();
    }

    @Override
    protected String getSqlType() {
        return "DOUBLE";
    }

    @Override
    public Double getValue(ResultSet rs, int position) throws SQLException {
        double result = rs.getDouble(position);
        return rs.wasNull() ? null : result;
    }
}
