package graphql.sql.schema.parser;

import java.util.Map;

public interface SchemaType {
    Map<String, ? extends SchemaAnnotation> getAnnotations();

    String getName();
}
