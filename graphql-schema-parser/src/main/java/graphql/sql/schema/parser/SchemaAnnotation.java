package graphql.sql.schema.parser;

import java.util.Map;

public interface SchemaAnnotation {
    String getName();

    Map<String, Object> getAttributes();

    default Object getAttribute(String name) {
        return getAttributes().get(name);
    }

}
