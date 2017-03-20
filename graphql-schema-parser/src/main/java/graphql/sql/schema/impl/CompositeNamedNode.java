package graphql.sql.schema.impl;

import graphql.sql.schema.CompositeType;
import graphql.sql.schema.Field;
import graphql.sql.schema.SchemaParserException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class CompositeNamedNode extends NamedNode implements CompositeType {
    private final Map<String, FieldImpl> fields = new LinkedHashMap<>();

    public CompositeNamedNode(String name, Location location) {
        super(name, location);
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
