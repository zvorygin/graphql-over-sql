package graphql.sql.schema.parser;

import java.util.Map;

public interface SchemaType {
    Map<String, SchemaAnnotation> getAnnotations();

    String getName();
}
