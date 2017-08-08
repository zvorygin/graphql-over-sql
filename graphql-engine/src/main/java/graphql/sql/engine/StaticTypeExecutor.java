package graphql.sql.engine;

import graphql.sql.core.config.TypeExecutor;

import javax.annotation.Nonnull;
import java.util.Map;

class StaticTypeExecutor implements TypeExecutor {
    private final Object value;

    public StaticTypeExecutor(Object value) {
        this.value = value;
    }

    @Nonnull
    @Override
    public Object execute(Map<String, Object> variables) {
        return value;
    }
}
