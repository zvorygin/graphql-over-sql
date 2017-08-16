package graphql.sql.engine.sql;

import graphql.sql.core.config.Field;
import graphql.sql.core.config.TypeReference;
import graphql.sql.schema.parser.SchemaAnnotation;

import java.util.Map;

public class CompositeSqlField extends Field {

    private final Map<String, SchemaAnnotation> annotations;

    public CompositeSqlField(String name, TypeReference type, Map<String, SchemaAnnotation> annotations) {
        super(name, type);
        this.annotations = annotations;
    }
}
