package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaDocument;
import graphql.sql.schema.parser.SchemaDocument;

import javax.annotation.Nonnull;
import java.util.Map;

class SchemaDocumentImpl implements SchemaDocument {
    @Nonnull
    private final SchemaImpl schema;

    @Nonnull
    private final Map<String, ObjectTypeImpl> types;

    @Nonnull
    private final Map<String, InterfaceImpl> interfaces;

    @Nonnull
    private final Map<String, ScalarImpl> scalars;

    @Nonnull
    private final Map<String, InputImpl> inputs;

    public SchemaDocumentImpl(@Nonnull SchemaImpl schema,
                              @Nonnull Map<String, ObjectTypeImpl> types,
                              @Nonnull Map<String, InterfaceImpl> interfaces,
                              @Nonnull Map<String, ScalarImpl> scalars,
                              @Nonnull Map<String, InputImpl> inputs) {
        this.schema = schema;
        this.types = types;
        this.interfaces = interfaces;
        this.scalars = scalars;
        this.inputs = inputs;
    }

    @Override
    @Nonnull
    public SchemaImpl getSchema() {
        return schema;
    }

    @Override
    @Nonnull
    public Map<String, ObjectTypeImpl> getTypes() {
        return types;
    }

    @Override
    @Nonnull
    public Map<String, InterfaceImpl> getInterfaces() {
        return interfaces;
    }

    @Override
    @Nonnull
    public Map<String, ScalarImpl> getScalars() {
        return scalars;
    }

    @Override
    @Nonnull
    public Map<String, InputImpl> getInputs() {
        return inputs;
    }
}
