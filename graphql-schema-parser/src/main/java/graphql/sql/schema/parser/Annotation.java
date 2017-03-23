package graphql.sql.schema.parser;

import java.util.Map;

public interface Annotation {
    String getName();

    Map<String, Object> getAttributes();
}
