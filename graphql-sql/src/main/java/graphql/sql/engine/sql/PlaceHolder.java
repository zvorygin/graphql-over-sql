package graphql.sql.engine.sql;

import com.healthmarketscience.sqlbuilder.QueryPreparer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public interface PlaceHolder {
    void setValue(PreparedStatement ps, Map<String, Object> variables) throws SQLException;
}
