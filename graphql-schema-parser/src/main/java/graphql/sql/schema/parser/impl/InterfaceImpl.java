package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.Interface;

import java.util.Map;

class InterfaceImpl extends CompositeAbstractType implements Interface {
    public InterfaceImpl(Map<String, AnnotationImpl> annotations,
                         Map<String, FieldImpl> fields,
                         String name,
                         Location location) {
        super(annotations, fields, name, location);
    }
}
