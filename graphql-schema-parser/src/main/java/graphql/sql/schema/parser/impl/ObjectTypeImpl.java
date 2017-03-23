package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.ObjectType;

import java.util.Map;

class ObjectTypeImpl extends CompositeAbstractType implements ObjectType {
    public ObjectTypeImpl(Map<String, FieldImpl> fields,
                          Map<String, AnnotationImpl> annotations,
                          String name,
                          Location location) {
        super(annotations, fields, name, location);
    }
}
