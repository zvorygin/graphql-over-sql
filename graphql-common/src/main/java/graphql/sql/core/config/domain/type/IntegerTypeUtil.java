package graphql.sql.core.config.domain.type;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.Scalars;
import graphql.language.IntValue;
import graphql.language.Node;
import graphql.language.Value;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerTypeUtil extends AbstractTypeUtil<Integer> {

    public static final IntegerTypeUtil INSTANCE = new IntegerTypeUtil();

    private IntegerTypeUtil() {
        super(Scalars.GraphQLInt);
    }

    @Override
    public Integer getValue(Node value) {
        //TODO(dzvorygin) check range
        return ((IntValue) value).getValue().intValue();
    }

    @Override
    protected Integer getRawValue(Value value) {
        return ((IntValue) value).getValue().intValue();
    }

    @Override
    protected String getSqlType() {
        return "INTEGER";
    }

    @Override
    public Integer getValue(ResultSet rs, int position) throws SQLException {
        int result = rs.getInt(position);
        return rs.wasNull() ? null : result;
    }

    @Override
    public QueryPreparer.StaticPlaceHolder createStaticPlaceHolder(Integer value, QueryPreparer queryPreparer) {
        return queryPreparer.addStaticPlaceHolder(value);
    }
}
