package graphql.sql.schema.engine;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLType;
import graphql.sql.core.config.CompositeType;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.TypeReference;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractCompositeType<F extends Field> implements CompositeType {
    @Nonnull
    private final String name;

    @Nonnull
    private final Map<String, F> fields;

    @Nonnull
    private Collection<Interface> interfaces;

    public AbstractCompositeType(@Nonnull String name,
                                 @Nonnull Map<String, F> fields,
                                 @Nonnull Collection<Interface> interfaces) {
        this.name = name;
        this.fields = fields;
        this.interfaces = interfaces;
    }

    @Nonnull
    @Override
    public Map<String, Field> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    @Nonnull
    @Override
    public Collection<Interface> getInterfaces() {
        return interfaces;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    protected GraphQLInterfaceType[] getInterfaceDefinitions(Map<String, GraphQLType> dictionary) {
        return interfaces
                .stream()
                .map(i -> dictionary.get(i.getName()))
                .toArray(GraphQLInterfaceType[]::new);
    }

    protected List<GraphQLFieldDefinition> getFieldDefinitions() {
        Map<String, Field> allFields = new LinkedHashMap<>(fields);

        interfaces.stream()
                .map(Interface::getFields)
                .map(Map::values)
                .flatMap(Collection::stream)
                .forEach(field -> {
                    Field existing = allFields.get(field.getName());
                    if (existing == null) {
                        allFields.put(field.getName(), field);
                    } else if (!TypeReference.equals(existing.getTypeReference(), field.getTypeReference())) {
                        throw new graphql.sql.core.config.ConfigurationException(
                                String.format("Object [%s] has type conflict in field [%s] - [%s] and [%s] ",
                                        getName(),
                                        field.getName(),
                                        existing.getTypeReference().toGraphQLString(),
                                        field.getTypeReference().toGraphQLString()));
                    }
                });

        return allFields.values().stream().map(Field::getGraphQLFieldDefinition)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
