package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaObjectType;

import java.util.List;
import java.util.Map;

class SchemaObjectTypeImpl extends CompositeSchemaType implements SchemaObjectType {
    public final List<String> interfaces;

    public SchemaObjectTypeImpl(Map<String, SchemaFieldImpl> fields,
                                Map<String, SchemaAnnotationImpl> annotations,
                                List<String> interfaces,
                                String name,
                                Location location) {
        super(annotations, fields, name, location);
        this.interfaces = interfaces;
    }

    @Override
    public List<String> getInterfaces() {
        return interfaces;
    }
}
