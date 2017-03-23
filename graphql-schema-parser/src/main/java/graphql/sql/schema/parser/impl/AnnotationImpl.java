package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.Annotation;

import java.util.Map;

public class AnnotationImpl extends NamedNode implements Annotation {
    private final Map<String, Object> attributes;

    public AnnotationImpl(Map<String, Object> attributes, String name, Location location) {
        super(name, location);
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
