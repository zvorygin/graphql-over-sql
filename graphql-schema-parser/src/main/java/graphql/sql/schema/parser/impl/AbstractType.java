package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.Type;

import java.util.Map;

public class AbstractType extends NamedNode implements Type {
    private final Map<String, AnnotationImpl> annotations;

    public AbstractType(Map<String, AnnotationImpl> annotations, String name, Location location) {
        super(name, location);
        this.annotations = annotations;
    }

    @Override
    public Map<String, AnnotationImpl> getAnnotations() {
        return annotations;
    }
}
