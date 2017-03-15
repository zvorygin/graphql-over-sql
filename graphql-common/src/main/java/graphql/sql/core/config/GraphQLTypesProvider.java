package graphql.sql.core.config;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.StaticDataFetcher;
import graphql.schema.TypeResolverProxy;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityQuery;
import graphql.sql.core.config.domain.EntityReference;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GraphQLTypesProvider {

    private final Map<Entity, GraphQLObjectType> objectCache = new HashMap<>();

    private final Map<Entity, GraphQLInterfaceType> interfaceCache = new HashMap<>();

    private final GraphQLSchema schema;

    public GraphQLTypesProvider(Config config) {
        // TODO(dzvorygin) use GraphQL builders everywhere.
        GraphQLObjectType.Builder queryTypeBuilder =
                GraphQLObjectType.newObject().name("query").description("Root query object");

        config.getQueries().forEach(query -> {
            GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                    .name(getQueryName(query))
                    .type(new GraphQLList(getInterfaceType(query.getEntity())))
                    .argument(getQueryArgument(query));

            queryTypeBuilder.field(fieldBuilder);
        });

        GraphQLObjectType queryType = queryTypeBuilder.build();


        Collection<GraphQLInterfaceType> interfaceTypes = config.getEntities().stream()
                .map(this::getInterfaceType)
                .collect(Collectors.toList());

        Collection<GraphQLObjectType> objectTypes = config.getEntities().stream()
                .map(this::getObjectType)
                .collect(Collectors.toList());

        Set<GraphQLType> dictionary = new HashSet<>();
        dictionary.addAll(interfaceTypes);
        dictionary.addAll(objectTypes);

        schema = GraphQLSchema.newSchema().query(queryType).build(dictionary);
    }

    public GraphQLSchema getSchema() {
        return schema;
    }

    private List<GraphQLArgument> getQueryArgument(EntityQuery query) {
        return query.getArguments();
    }

    private String getQueryName(EntityQuery entity) {
        String entityName = entity.getName();
        return Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
    }

    private GraphQLObjectType getObjectType(Entity entity) {
        return objectCache.computeIfAbsent(entity, k -> new GraphQLObjectType(
                entity.getEntityName(),
                entity.getDescription(),
                getFieldDefinitions(entity),
                getInterfaces(entity)));
    }

    private List<GraphQLInterfaceType> getInterfaces(Entity entity) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(entity.hierarchyIterator(), 0), false)
                .map(this::getInterfaceType)
                .collect(Collectors.toList());
    }

    private String getInterfaceName(Entity entity) {
        return "I" + entity.getEntityName();
    }

    private GraphQLInterfaceType getInterfaceType(Entity entity) {
        return interfaceCache.computeIfAbsent(entity, k -> new GraphQLInterfaceType(
                getInterfaceName(entity),
                String.format("Primary interface type for entity [%s]", entity.getEntityName()),
                getFieldDefinitions(entity),
                new TypeResolverProxy()));
    }

    private List<GraphQLFieldDefinition> getFieldDefinitions(Entity entity) {
        Stream<GraphQLFieldDefinition> scalarFields =
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(entity.hierarchyIterator(), 0), false)
                        .flatMap(e -> e.getEntityFields().stream())
                        .distinct()
                        .map(this::getFieldDefinition);

        Stream<GraphQLFieldDefinition> compositeFields =
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(entity.hierarchyIterator(), 0), false)
                        .flatMap(e -> e.getEntityReferences().stream())
                        .map(this::getFieldDefinition);

        return Stream.concat(scalarFields, compositeFields).collect(Collectors.toList());
    }

    @Nonnull
    private GraphQLFieldDefinition getFieldDefinition(EntityReference reference) {
        GraphQLInterfaceType fieldType = GraphQLInterfaceType.reference(getInterfaceName(reference.getTargetEntity()));

        return new GraphQLFieldDefinition(
                reference.getName(),
                reference.getDescription(),
                fieldType,
                new StaticDataFetcher(null),
                Collections.emptyList(),
                null);
    }

    @Nonnull
    private GraphQLFieldDefinition getFieldDefinition(EntityField field) {
        GraphQLOutputType fieldType = field.getScalarType().getTypeUtil().getGraphQLScalarType();

        if (!field.isNullable()) {
            fieldType = new GraphQLNonNull(fieldType);
        }

        return new GraphQLFieldDefinition(
                field.getFieldName(),
                field.getDescription(),
                fieldType,
                new StaticDataFetcher(null),
                Collections.emptyList(),
                null);
    }

    public boolean isInterfaceType(String typeConditionName) {
        return typeConditionName.startsWith("I") && Character.isUpperCase(typeConditionName.charAt(1));
    }

    public String getEntityNameForInterface(String typeConditionName) {
        if (!isInterfaceType(typeConditionName)) {
            throw new IllegalStateException(String.format("[%s] is not interface type", typeConditionName));
        }
        return typeConditionName.substring(1);
    }
}
