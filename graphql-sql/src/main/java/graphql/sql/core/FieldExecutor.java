package graphql.sql.core;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public interface FieldExecutor {
    @Nonnull
    Object execute(Connection conn, Map<String, Object> variables) throws SQLException;
}
