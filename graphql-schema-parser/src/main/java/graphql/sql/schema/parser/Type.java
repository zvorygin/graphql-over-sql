package graphql.sql.schema.parser;

import java.util.Map;

public interface Type {
    Map<String, ? extends Annotation> getAnnotations();
}
