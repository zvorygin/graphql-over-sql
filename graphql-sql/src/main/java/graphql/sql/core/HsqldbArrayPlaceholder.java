package graphql.sql.core;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.QueryPreparer;
import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.sql.core.config.domain.ScalarType;
import graphql.sql.engine.sql.PlaceHolder;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HsqldbArrayPlaceholder extends QueryPreparer.PlaceHolder implements PlaceHolder {

    private final ScalarType scalarType;
    private final String arrayType;
    private final Argument argument;

    public HsqldbArrayPlaceholder(QueryPreparer outer, ScalarType scalarType, String arrayType, Argument argument) {
        super(outer);
        //TODO(dzvorygin) use scalarType only without arrayType
        this.scalarType = scalarType;
        this.arrayType = arrayType;
        this.argument = argument;
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
        app.append("UNNEST(");
        super.appendTo(app);
        app.append(")");
    }

    public Closeable setArray(Object[] payload, PreparedStatement ps) throws SQLException {
        int index = getIndex();
        return setArray(payload, ps, index);
    }

    @Nonnull
    public Closeable setArray(Object[] payload, PreparedStatement ps, int index) throws SQLException {
        Connection conn = ps.getConnection();
        Array array = conn.createArrayOf(arrayType, payload);
        ps.setArray(index, array);
        return () -> {
            try {
                array.free();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public void setValue(PreparedStatement ps, Map<String, Object> variables) throws SQLException {
        ArrayList<Object> values = new ArrayList<>();
        Value argumentValue = argument.getValue();
        if (argumentValue instanceof ArrayValue) {
            for (Value value : ((ArrayValue) argumentValue).getValues()) {
                if (value instanceof VariableReference) {
                    Object variable = variables.get(((VariableReference) value).getName());
                    values.add(scalarType.getTypeUtil().getValue(variable));
                } else {
                    values.add(scalarType.getTypeUtil().getValue(value));
                }
            }
        } else if (argumentValue instanceof VariableReference) {
            List value = (List) variables.get(((VariableReference) argumentValue).getName());
            for (Object o : value) {
                values.add(scalarType.getTypeUtil().getValue(o));
            }
        }

        setArray(values.toArray(), ps);
    }
}
