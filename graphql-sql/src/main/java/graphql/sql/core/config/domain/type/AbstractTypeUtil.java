package graphql.sql.core.config.domain.type;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.language.Node;
import graphql.schema.GraphQLInputType;
import graphql.sql.core.HsqldbArrayPlaceholder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractTypeUtil<T> implements TypeUtil<T> {

    private final GraphQLInputType graphQLScalarType;

    protected AbstractTypeUtil(GraphQLInputType graphQLScalarType) {
        this.graphQLScalarType = graphQLScalarType;
    }

    @Override
    public GraphQLInputType getGraphQLScalarType() {
        return graphQLScalarType;
    }

    public T getValue(Node value) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public QueryPreparer.PlaceHolder createArrayPlaceholder(QueryPreparer preparer) {
        return new HsqldbArrayPlaceholder(preparer, getSqlType());
    }

    @Override
    public QueryPreparer.StaticPlaceHolder createStaticArrayPlaceholder(QueryPreparer preparer, Object[] data) {
        return new DelegatingStaticPlaceHolder(preparer, new HsqldbArrayPlaceholder(preparer, getSqlType()), data );
    }

    protected abstract String getSqlType();

    private static class DelegatingStaticPlaceHolder extends QueryPreparer.StaticPlaceHolder {
        private final Object[] data;
        private final HsqldbArrayPlaceholder arrayPlaceHolder;

        public DelegatingStaticPlaceHolder(QueryPreparer outer, HsqldbArrayPlaceholder arrayPlaceholder, Object[] data) {
            super(outer);
            arrayPlaceHolder = arrayPlaceholder;
            this.data = data;

        }

        @Override
        public void setValue(PreparedStatement ps) throws SQLException {
            arrayPlaceHolder.setArray(data, ps, getIndex());
        }

        @Override
        public String displayToString() {
            return null;
        }
    }
}
