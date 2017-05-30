package graphql.sql.engine;

import graphql.sql.core.config.FieldExecutor;

import javax.annotation.Nonnull;
import java.util.Map;

class StaticFieldExecutor implements FieldExecutor {
    private final Object value;

    public StaticFieldExecutor(Object value) {
        this.value = value;
    }

    @Nonnull
    @Override
    public Object execute(Map<String, Object> variables) {
        return value;
    }
}
