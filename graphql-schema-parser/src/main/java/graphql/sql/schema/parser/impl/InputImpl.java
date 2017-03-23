package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.Input;

import java.util.Map;

class InputImpl extends CompositeAbstractType implements Input {
    public InputImpl(Map<String, AnnotationImpl> annotations,
                     Map<String, FieldImpl> fields,
                     String name,
                     Location location) {
        super(annotations, fields, name, location);
    }
}
