package graphql.sql.schema.engine;

import graphql.sql.core.config.Field;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.ObjectType;
import graphql.sql.core.config.TypeReference;
import graphql.sql.schema.parser.SchemaField;
import graphql.sql.schema.parser.SchemaInterface;
import graphql.sql.schema.parser.SchemaObjectType;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultTypeProvider implements TypeProvider {

    @Override
    public Interface buildInterface(SchemaInterface schemaInterface, Map<String, Interface> interfaces) {
        throw new IllegalStateException("Default type provider can build types for object only, not interfaces");
    }

    @Override
    public ObjectType buildObjectType(SchemaObjectType schemaObjectType, Map<String, Interface> interfaces, boolean isQueryType) {
        if (!schemaObjectType.getFields().isEmpty()) {
            if (!schemaObjectType.getInterfaces().isEmpty()) {
                throw new ConfigurationException(
                        String.format("Object type [%s] without @Provider annotation should have either " +
                                "interfaces or fields but not both", schemaObjectType.getName()));
            }
            return buildFromFields(schemaObjectType, isQueryType);
        } else {
            return buildFromInterfaces(schemaObjectType, interfaces, isQueryType);
        }
    }


    @Nonnull
    private ObjectType buildFromInterfaces(SchemaObjectType objectType, Map<String, Interface> interfaces, boolean isQueryType) {
        Map<String, Field> fields = new LinkedHashMap<>();
        Map<String, Interface> fieldSource = new HashMap<>();

        for (String interfaceName : objectType.getInterfaces()) {
            Interface iface = interfaces.get(interfaceName);

            Map<String, Field> interfaceFields = iface.getFields();
            for (Map.Entry<String, Field> fieldEntry : interfaceFields.entrySet()) {
                Field existing = fields.putIfAbsent(fieldEntry.getKey(), fieldEntry.getValue());
                if (existing != null && !existing.equals(fieldEntry.getValue())) {
                    Interface existingIface = fieldSource.get(existing.getName());
                    throw new ConfigurationException(
                            String.format(
                                    "CompositeType [%s] has conflict in field [%s] defined at [%s] as [%s] and [%s] as [%s]",
                                    objectType.getName(),
                                    existing.getName(),
                                    iface.getName(),
                                    fieldEntry.getValue().getTypeReference(),
                                    existingIface.getName(),
                                    existing.getTypeReference()));
                }
                fieldSource.put(fieldEntry.getKey(), iface);
            }
        }

        if (isQueryType) {
            Field schemaField = buildSchemaField();
            fields.put(schemaField.getName(), schemaField);
        }

        List<Interface> implementedInterfaces = objectType.getInterfaces().stream().map(interfaces::get).collect(Collectors.toList());

        return new GenericObjectType(objectType.getName(), fields, implementedInterfaces);
    }

    private ObjectType buildFromFields(SchemaObjectType objectType, boolean isQueryType) {
        Map<String, Field> fields = new LinkedHashMap<>();
        for (SchemaField schemaField : objectType.getFields()) {

            Map<String, TypeReference> arguments =
                    schemaField.getArguments().entrySet().stream().collect(
                            Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getTypeReference()));

            fields.put(schemaField.getName(), new Field(schemaField.getName(), schemaField.getType(), arguments));
        }

        if (isQueryType) {
            Field schemaField = buildSchemaField();
            fields.put(schemaField.getName(), schemaField);
        }

        return new GenericObjectType(objectType.getName(), fields, Collections.emptyList());
    }

    private Field buildSchemaField() {
        return new QuerySchemaField();
    }
}
