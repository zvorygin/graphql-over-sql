package graphql.sql.core;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.QueryPreparer;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArrayToTablePlaceHolder extends QueryPreparer.PlaceHolder {

    private final String ARRAY_TYPE;

    public ArrayToTablePlaceHolder(QueryPreparer outer, String array_type) {
        super(outer);
        ARRAY_TYPE = array_type;
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
        app.append("SELECT COLUMN_VALUE FROM TABLE(");
        super.appendTo(app);
        app.append(")");
    }

    public void setArray(Object[] payload, PreparedStatement preparedStatement) throws SQLException {
        OraclePreparedStatement ops = preparedStatement.unwrap(OraclePreparedStatement.class);
        Connection conn = preparedStatement.getConnection();
        OracleConnection oc = conn.unwrap(OracleConnection.class);

        ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor(ARRAY_TYPE, oc);

        ARRAY array = new ARRAY(descriptor, oc, payload);
        ops.setARRAY(getIndex(), array);
    }
}
