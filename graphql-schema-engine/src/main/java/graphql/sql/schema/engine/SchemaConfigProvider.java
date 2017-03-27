package graphql.sql.schema.engine;

import graphql.sql.core.TopologicalIterator;
import graphql.sql.core.config.ConfigProvider;
import graphql.sql.core.config.domain.Config;
import graphql.sql.schema.parser.SchemaAnnotation;
import graphql.sql.schema.parser.SchemaDocument;
import graphql.sql.schema.parser.SchemaField;
import graphql.sql.schema.parser.SchemaInterface;
import graphql.sql.schema.parser.SchemaObjectType;
import graphql.sql.schema.parser.SchemaParser;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SchemaConfigProvider implements ConfigProvider {
    private static final SchemaParser SCHEMA_PARSER = new SchemaParser();
    private final Config config;
    private final InterfaceBuilderRegistry interfaceBuilderRegistry;

    public SchemaConfigProvider(InputStream schemaStream, InterfaceBuilderRegistry interfaceBuilderRegistry)
            throws IOException {
        this.interfaceBuilderRegistry = interfaceBuilderRegistry;
        SchemaDocument schemaDocument = SCHEMA_PARSER.parse(schemaStream);
        config = buildConfig(schemaDocument);
    }

    @Override
    public Config getConfig() {
        return config;
    }

    private Config buildConfig(SchemaDocument schemaDocument) {

        Map<String, Interface> interfaces = buildInterfaces(schemaDocument.getInterfaces());

        Map<String, Type> types = buildObjects(schemaDocument.getTypes(), interfaces);

        return null;
    }

    @Nonnull
    private Map<String, Interface> buildInterfaces(Map<String, ? extends SchemaInterface> interfaces) {
        TopologicalIterator<SchemaInterface> iterator =
                new TopologicalIterator<>(interfaces.values(), iface -> {
                    SchemaAnnotation parent = iface.getAnnotations().get("Parent");
                    if (parent != null) {
                        Object name = parent.getAttribute("name");
                        if (!(name instanceof String)) {
                            throw new ConfigurationException("Parent annotation on interface [%s] doesn't have String [name] attribute");
                        }
                        return Collections.singleton(interfaces.get(name));
                    }

                    return Collections.emptyList();
                });

        Map<String, Interface> result = new HashMap<>();

        while (iterator.hasNext()) {
            SchemaInterface schemaInterface = iterator.next();

            SchemaAnnotation providerAnnotation = schemaInterface.getAnnotations().get("Provider");
            if (providerAnnotation == null) {
                throw new ConfigurationException(
                        String.format("Provider annotation not found on interface [%s]", schemaInterface.getName()));
            }
            Object name = providerAnnotation.getAttribute("name");
            if (!(name instanceof String)) {
                throw new ConfigurationException(
                        String.format("Provider annotation on interface [%s] doesn't have String [name] attribute",
                                schemaInterface.getName()));
            }

            InterfaceBuilder interfaceBuilder = interfaceBuilderRegistry.getInterfaceWrapper((String) name);
            result.put(schemaInterface.getName(), interfaceBuilder.buildInterface(schemaInterface, result));
        }

        return result;
    }

    private Map<String, Type> buildObjects(Map<String, ? extends SchemaObjectType> types,
                                           Map<String, Interface> interfaces) {
        Map<String, Type> result = new HashMap<>();
        for (SchemaObjectType objectType : types.values()) {
            if (!objectType.getAnnotations().isEmpty()) {
                throw new ConfigurationException(
                        String.format("Object type [%s] shouldn't have any annotations", objectType.getName()));
            }

            List<String> implemented = objectType.getInterfaces();
            Collection<? extends SchemaField> fields = objectType.getFields();

            if (!fields.isEmpty() && !implemented.isEmpty()) {
                throw new ConfigurationException(
                        String.format("Object type [%s] should have either fields or interfaces predefined, but not both", objectType.getName()));
            }

            if (!fields.isEmpty()) {
                result.put(objectType.getName(), buildFromFields(objectType, interfaces));
            } else if (!implemented.isEmpty()) {
                result.put(objectType.getName(), buildFromInterfaces(objectType, interfaces));
            }


        }
        return result;
    }

    private Type buildFromInterfaces(SchemaObjectType objectType, Map<String, Interface> interfaces) {
        Map<String, Field> fields = new LinkedHashMap<>();
        Map<String, Interface> fieldSource = new HashMap<>();
        for (Interface iface : interfaces.values()) {
            Map<String, Field> interfaceFields = iface.getFields();
            for (Map.Entry<String, Field> fieldEntry : interfaceFields.entrySet()) {
                Field existing = fields.putIfAbsent(fieldEntry.getKey(), fieldEntry.getValue());
                if (existing != null && !existing.equals(fieldEntry.getValue())) {
                    Interface existingIface = fieldSource.get(existing.getName());
                    throw new ConfigurationException(
                            String.format(
                                    "Type [%s] has conflict in field [%s] defined at [%s] as [%s] and [%s] as [%s]",
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
        return new Type(fields);
    }

    private Type buildFromFields(SchemaObjectType objectType, Map<String, Interface> interfaces) {
        Map<String, Field> fields = new LinkedHashMap<>();
        for (SchemaField schemaField : objectType.getFields()) {
            fields.put(schemaField.getName(), new Field(schemaField.getName(), schemaField.getType()));
        }

        return new Type(fields);
    }
}
