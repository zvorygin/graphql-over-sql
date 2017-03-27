package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.SchemaCompositeType;
import graphql.sql.schema.parser.SchemaField;
import graphql.sql.schema.parser.SchemaParserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

abstract class CompositeSchemaType extends AbstractSchemaType implements SchemaCompositeType {
    private final Map<String, SchemaFieldImpl> fields;

    public CompositeSchemaType(Map<String, SchemaAnnotationImpl> annotations,
                               Map<String, SchemaFieldImpl> fields,
                               String name,
                               Location location) {
        super(annotations, name, location);
        this.fields = fields;
    }

    public void addField(SchemaFieldImpl field) {
        SchemaFieldImpl existing;
        if ((existing = fields.putIfAbsent(field.getName(), field)) != null) {
            throw new SchemaParserException(
                    String.format("Duplicate field name [%s] at position [%s] and [%s]",
                            field.getName(), existing.getLocation(), field.getLocation()));
        }
    }

    @Nullable
    @Override
    public SchemaField getField(String name) {
        return fields.get(name);
    }

    @Nonnull
    @Override
    public Collection<SchemaFieldImpl> getFields() {
        return fields.values();
    }
}
