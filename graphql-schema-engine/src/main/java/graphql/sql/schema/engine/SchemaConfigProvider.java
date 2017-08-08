package graphql.sql.schema.engine;

import graphql.sql.core.Scalars;
import graphql.sql.core.graph.TopologicalIterator;
import graphql.sql.core.config.ConfigProvider;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.ObjectType;
import graphql.sql.core.config.Scalar;
import graphql.sql.core.config.TypeReference;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.domain.impl.ConfigImpl;
import graphql.sql.schema.parser.SchemaAnnotation;
import graphql.sql.schema.parser.SchemaCompositeType;
import graphql.sql.schema.parser.SchemaDocument;
import graphql.sql.schema.parser.SchemaField;
import graphql.sql.schema.parser.SchemaInterface;
import graphql.sql.schema.parser.SchemaObjectType;
import graphql.sql.schema.parser.SchemaParser;
import graphql.sql.schema.parser.SchemaScalar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SchemaConfigProvider implements ConfigProvider {
    private static final SchemaParser SCHEMA_PARSER = new SchemaParser();
    private final Config config;
    private final TypeProviderRegistry typeProviderRegistry;

    public SchemaConfigProvider(InputStream schemaStream, TypeProviderRegistry typeProviderRegistry)
            throws IOException {
        this.typeProviderRegistry = typeProviderRegistry;
        SchemaDocument schemaDocument = SCHEMA_PARSER.parse(schemaStream);
        config = buildConfig(schemaDocument);
    }

    @Override
    public Config getConfig() {
        return config;
    }

    private Config buildConfig(SchemaDocument schemaDocument) {

        Map<String, Scalar> scalars = buildScalars(schemaDocument);

        Map<String, Interface> interfaces = buildInterfaces(schemaDocument.getInterfaces());

        TypeReference queryTypeReference = schemaDocument.getSchema().getQueryType();

        Map<String, ObjectType> types = buildObjects(schemaDocument.getTypes(), interfaces, queryTypeReference.getTypeName());

        return new ConfigImpl(interfaces, types, scalars, queryTypeReference.getTypeName());
    }

    private Map<String, Scalar> buildScalars(SchemaDocument schemaDocument) {
        return schemaDocument.getScalars().values()
                .stream()
                .map(SchemaScalar::getName)
                .map(Scalars::getScalar)
                .collect(Collectors.toMap(Scalar::getName, Function.identity()));
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
            String providerName = getProviderName(schemaInterface);
            if (providerName == null) {
                throw new ConfigurationException(
                        String.format("Provider annotation not found on interface [%s]", schemaInterface.getName()));
            }

            TypeProvider typeProvider = typeProviderRegistry.getTypeBuilder(providerName);
            result.put(schemaInterface.getName(), typeProvider.buildInterface(schemaInterface, result));
        }

        return result;
    }

    private Map<String, ObjectType> buildObjects(Map<String, ? extends SchemaObjectType> types,
                                                 Map<String, Interface> interfaces,
                                                 String queryTypeName) {

        Map<String, ObjectType> result = new HashMap<>();
        for (SchemaObjectType schemaObjectType : types.values()) {
            String providerName = getProviderName(schemaObjectType);
            TypeProvider typeProvider;

            if (providerName != null) {
                typeProvider = typeProviderRegistry.getTypeBuilder(providerName);
            } else {
                typeProvider = typeProviderRegistry.getDefaultTypeProvider();
            }

            boolean isQueryType = schemaObjectType.getName().equals(queryTypeName);
            ObjectType objectType = typeProvider.buildObjectType(schemaObjectType, interfaces, isQueryType);

            result.put(schemaObjectType.getName(), objectType);
        }
        return result;
    }

    @Nullable
    private static String getProviderName(SchemaCompositeType schemaType) {
        SchemaAnnotation providerAnnotation = schemaType.getAnnotations().get("Provider");
        if (providerAnnotation == null) {
            return null;
        }
        Object name = providerAnnotation.getAttribute("name");
        if (!(name instanceof String)) {
            throw new ConfigurationException(
                    String.format("Provider annotation on interface [%s] doesn't have String [name] attribute",
                            schemaType.getName()));
        }
        return (String) name;
    }
}
