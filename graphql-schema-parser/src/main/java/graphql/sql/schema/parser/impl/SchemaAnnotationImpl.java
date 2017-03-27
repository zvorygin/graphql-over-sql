package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaAnnotation;

import java.util.Map;

public class SchemaAnnotationImpl extends NamedSchemaNode implements SchemaAnnotation {
    private final Map<String, Object> attributes;

    public SchemaAnnotationImpl(Map<String, Object> attributes, String name, Location location) {
        super(name, location);
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
