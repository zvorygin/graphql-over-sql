package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaInterface;

import java.util.Map;

class SchemaInterfaceImpl extends CompositeSchemaType implements SchemaInterface {
    public SchemaInterfaceImpl(Map<String, SchemaAnnotationImpl> annotations,
                               Map<String, SchemaFieldImpl> fields,
                               String name,
                               Location location) {
        super(annotations, fields, name, location);
    }
}
