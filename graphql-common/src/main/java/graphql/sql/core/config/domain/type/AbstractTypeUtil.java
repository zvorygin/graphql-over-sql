package graphql.sql.core.config.domain.type;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.language.ArrayValue;
import graphql.language.Node;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.schema.GraphQLScalarType;
import graphql.sql.core.HsqldbArrayPlaceholder;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractTypeUtil<T> implements TypeUtil<T> {

    private final GraphQLScalarType graphQLScalarType;

    protected AbstractTypeUtil(GraphQLScalarType graphQLScalarType) {
        this.graphQLScalarType = graphQLScalarType;
    }

    @Override
    public GraphQLScalarType getGraphQLScalarType() {
        return graphQLScalarType;
    }

    @Override
    public T getValue(Node value) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public QueryPreparer.PlaceHolder createArrayPlaceholder(QueryPreparer preparer) {
        return new HsqldbArrayPlaceholder(preparer, getSqlType());
    }

    @Override
    public QueryPreparer.StaticPlaceHolder createStaticArrayPlaceholder(QueryPreparer preparer, Value value) {
        if (!(value instanceof ArrayValue)) {
            throw new IllegalStateException(
                    String.format("Value expected to be ArrayValue, but got [%s] - [%s] instead.",
                            value.getClass(), value.toString()));
        }

        ArrayValue arrayValue = (ArrayValue) value;

        List<Value> values = arrayValue.getValues();

        Object[] data = new Object[values.size()];
        int i = 0;
        for (Value element : values) {
            if (element instanceof VariableReference) {
                throw new IllegalStateException("Nested variable references aren't supported yet");
            }
            data[i++] = getRawValue(element);
        }

        return new DelegatingStaticPlaceHolder(preparer, new HsqldbArrayPlaceholder(preparer, getSqlType()), data);
    }

    protected abstract T getRawValue(Value value);

    protected abstract String getSqlType();

    private static class DelegatingStaticPlaceHolder extends QueryPreparer.StaticPlaceHolder {
        private final Object[] data;
        private final HsqldbArrayPlaceholder arrayPlaceHolder;

        public DelegatingStaticPlaceHolder(QueryPreparer outer, HsqldbArrayPlaceholder arrayPlaceholder, Object[] data) {
            super(null);
            arrayPlaceHolder = arrayPlaceholder;
            this.data = data;
        }

        @Override
        public void appendTo(AppendableExt app) throws IOException {
            arrayPlaceHolder.appendTo(app);
        }


        @Override
        public void setValue(PreparedStatement ps) throws SQLException {
            arrayPlaceHolder.setArray(data, ps);
        }

        @Override
        public String displayToString() {
            return null;
        }
    }
}
