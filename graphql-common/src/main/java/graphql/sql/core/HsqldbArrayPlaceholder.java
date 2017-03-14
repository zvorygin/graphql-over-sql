package graphql.sql.core;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.QueryPreparer;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HsqldbArrayPlaceholder extends QueryPreparer.PlaceHolder {

    private final String arrayType;

    public HsqldbArrayPlaceholder(QueryPreparer outer, String arrayType) {
        super(outer);
        this.arrayType = arrayType;
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
}
