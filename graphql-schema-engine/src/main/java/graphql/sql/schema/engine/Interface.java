package graphql.sql.schema.engine;

import java.util.Map;

public interface Interface {
    Map<String, Field> getFields();

    String getName();
}
