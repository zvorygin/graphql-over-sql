package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaDocument;

import javax.annotation.Nonnull;
import java.util.Map;

class SchemaDocumentImpl implements SchemaDocument {
    @Nonnull
    private final SchemaImpl schema;

    @Nonnull
    private final Map<String, SchemaObjectTypeImpl> types;

    @Nonnull
    private final Map<String, SchemaInterfaceImpl> interfaces;

    @Nonnull
    private final Map<String, SchemaScalarImpl> scalars;

    @Nonnull
    private final Map<String, SchemaInputImpl> inputs;

    public SchemaDocumentImpl(@Nonnull SchemaImpl schema,
                              @Nonnull Map<String, SchemaObjectTypeImpl> types,
                              @Nonnull Map<String, SchemaInterfaceImpl> interfaces,
                              @Nonnull Map<String, SchemaScalarImpl> scalars,
                              @Nonnull Map<String, SchemaInputImpl> inputs) {
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
    public Map<String, SchemaObjectTypeImpl> getTypes() {
        return types;
    }

    @Override
    @Nonnull
    public Map<String, SchemaInterfaceImpl> getInterfaces() {
        return interfaces;
    }

    @Override
    @Nonnull
    public Map<String, SchemaScalarImpl> getScalars() {
        return scalars;
    }

    @Override
    @Nonnull
    public Map<String, SchemaInputImpl> getInputs() {
        return inputs;
    }
}
