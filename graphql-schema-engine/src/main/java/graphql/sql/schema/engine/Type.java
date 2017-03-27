package graphql.sql.schema.engine;

import javax.annotation.Nonnull;
import java.util.Map;

public class Type {
    @Nonnull
    private final Map<String, Field> fields;

    public Type(@Nonnull Map<String, Field> fields) {
        this.fields = fields;
    }
}
