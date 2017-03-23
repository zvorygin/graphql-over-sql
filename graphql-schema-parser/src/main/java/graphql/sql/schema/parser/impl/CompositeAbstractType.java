package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.CompositeType;
import graphql.sql.schema.parser.Field;
import graphql.sql.schema.parser.SchemaParserException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

class CompositeAbstractType extends AbstractType implements CompositeType {
    private final Map<String, FieldImpl> fields;

    public CompositeAbstractType(Map<String, AnnotationImpl> annotations,
                                 Map<String, FieldImpl> fields,
                                 String name,
                                 Location location) {
        super(annotations, name, location);
        this.fields = fields;
    }

    public void addField(FieldImpl field) {
        FieldImpl existing;
        if ((existing = fields.putIfAbsent(field.getName(), field)) != null) {
            throw new SchemaParserException(
                    String.format("Duplicate field name [%s] at position [%s] and [%s]",
                            field.getName(), existing.getLocation(), field.getLocation()));
        }
    }

    @Nullable
    @Override
    public Field getField(String name) {
        return fields.get(name);
    }

    @Override
    public Collection<FieldImpl> getFields() {
        return fields.values();
    }
}
