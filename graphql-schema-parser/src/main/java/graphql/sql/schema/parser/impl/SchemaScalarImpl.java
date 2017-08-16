package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaScalar;

import java.util.Map;

class SchemaScalarImpl extends AbstractSchemaType implements SchemaScalar {
    public SchemaScalarImpl(Map<String, SchemaAnnotationImpl> annotations, String name, Location location) {
        super(annotations, name, location);
    }
}
