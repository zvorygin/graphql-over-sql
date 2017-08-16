package graphql.sql.core.config.domain.type;

import graphql.language.Node;
import graphql.language.Value;
import graphql.schema.GraphQLScalarType;

public abstract class AbstractTypeUtil<T> implements TypeUtil<T> {

    private final GraphQLScalarType graphQLScalarType;

    private final Class<T> clazz;

    protected AbstractTypeUtil(GraphQLScalarType graphQLScalarType, Class<T> clazz) {
        this.graphQLScalarType = graphQLScalarType;
        this.clazz = clazz;
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
    public T getValue(Object raw) {
        return clazz.cast(raw);
    }

/*    @Override
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
    }*/

    protected abstract T getRawValue(Value value);

    protected abstract String getSqlType();

    /*private static class DelegatingStaticPlaceHolder extends QueryPreparer.StaticPlaceHolder {
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
    }*/
}
