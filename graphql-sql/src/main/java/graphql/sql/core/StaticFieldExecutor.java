package graphql.sql.core;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

class StaticFieldExecutor implements FieldExecutor {
    private final Object value;

    public StaticFieldExecutor(Object value) {
        this.value = value;
    }

    @Nonnull
    @Override
    public Object execute(Connection conn, Map<String, Object> variables) throws SQLException {
        return value;
    }
}
