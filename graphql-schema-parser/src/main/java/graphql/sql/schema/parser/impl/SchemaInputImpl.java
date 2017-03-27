package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaInput;

import java.util.Map;

class SchemaInputImpl extends CompositeSchemaType implements SchemaInput {
    public SchemaInputImpl(Map<String, SchemaAnnotationImpl> annotations,
                           Map<String, SchemaFieldImpl> fields,
                           String name,
                           Location location) {
        super(annotations, fields, name, location);
    }
}
