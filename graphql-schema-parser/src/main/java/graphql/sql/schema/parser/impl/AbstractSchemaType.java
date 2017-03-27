package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaType;

import java.util.Map;

public class AbstractSchemaType extends NamedSchemaNode implements SchemaType {
    private final Map<String, SchemaAnnotationImpl> annotations;

    public AbstractSchemaType(Map<String, SchemaAnnotationImpl> annotations, String name, Location location) {
        super(name, location);
        this.annotations = annotations;
    }

    @Override
    public Map<String, SchemaAnnotationImpl> getAnnotations() {
        return annotations;
    }
}
