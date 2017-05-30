package graphql.sql.core.config;

import java.util.Map;

@FunctionalInterface
public interface FieldExecutor {
    Object execute(Map<String, Object> variables);
}
