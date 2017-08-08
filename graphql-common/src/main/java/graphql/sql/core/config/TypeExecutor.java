package graphql.sql.core.config;

import java.util.Map;

@FunctionalInterface
public interface TypeExecutor {
    Object execute(Map<String, Object> variables);
}
