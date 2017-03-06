package graphql.sql.core.config;

import graphql.schema.*;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityQuery;

import graphql.Scalars;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GraphQLTypesProvider {

    private final Config config;

    private final NameProvider nameProvider;

    private final Map<Entity, GraphQLObjectType> objectCache = new HashMap<>();

    private final Map<Entity, GraphQLInterfaceType> interfaceCache = new HashMap<>();

    private final GraphQLObjectType queryType;
    private final GraphQLSchema schema;

    public GraphQLTypesProvider(Config config, NameProvider nameProvider) {
        this.config = config;
        this.nameProvider = nameProvider;

        // TODO(dzvorygin) use GraphQL builders everywhere.
        GraphQLObjectType.Builder queryTypeBuilder =
                GraphQLObjectType.newObject().name("query").description("Root query object");

        config.getQueries().values().forEach(query -> {
            GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                    .name(getQueryName(query))
                    .type(new GraphQLList(getInterfaceType(query.getEntity())))
                    .argument(getQueryArgument(query));

            queryTypeBuilder.field(fieldBuilder);
        });

        queryType = queryTypeBuilder.build();


        Collection<GraphQLInterfaceType> interfaceTypes = this.config.getEntities().values().stream()
                .map(this::getInterfaceType)
                .collect(Collectors.toList());

        Collection<GraphQLObjectType> objectTypes = this.config.getEntities().values().stream()
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

    private GraphQLArgument getQueryArgument(EntityQuery query) {
        if (query.getEntityFields().size() <= 1) {
            EntityField entityField = query.getEntityFields().get(0);

            return GraphQLArgument.newArgument()
                    .name(nameProvider.getFieldListName(entityField))
                    .type(new GraphQLNonNull(new GraphQLList(new GraphQLNonNull(
                            entityField.getScalarType().getTypeUtil().getGraphQLScalarType()))))
                    .build();
        }
        GraphQLInputObjectType.Builder elementTypeBuilder = GraphQLInputObjectType.newInputObject()
                .name(query.getName() + "Query")
                .description("Query parameters for " + query.getName());

        query.getEntityFields()
                .stream()
                .map(field -> GraphQLInputObjectField.newInputObjectField()
                        .name(field.getFieldName())
                        .type(new GraphQLNonNull(field.getScalarType().getTypeUtil().getGraphQLScalarType()))
                )
                .forEach(elementTypeBuilder::field);
        return new GraphQLArgument("query",
                new GraphQLNonNull(new GraphQLList(new GraphQLNonNull(elementTypeBuilder.build()))));
    }

    private String getQueryName(EntityQuery entity) {
        String entityName = entity.getName();
        return Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
    }

    private GraphQLObjectType getObjectType(Entity entity) {
        return objectCache.computeIfAbsent(entity, k -> new GraphQLObjectType(
                entity.getEntityName(),
                String.format("Object type for table [%s]", entity.getTable()),
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
                String.format("Interface type for table [%s]", entity.getTable()),
                getFieldDefinitions(entity),
                new TypeResolverProxy()));
    }

    private List<GraphQLFieldDefinition> getFieldDefinitions(Entity entity) {
        Stream<GraphQLFieldDefinition> scalarFields =
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(entity.hierarchyIterator(), 0), false)
                        .flatMap(e -> e.getEntityFields().stream())
                        .distinct()
                        .map(field -> new GraphQLFieldDefinition(
                                field.getFieldName(),
                                String.format("Field for column [%s]", field.getColumn().getName()),
                                Scalars.GraphQLString,
                                new StaticDataFetcher(null),
                                Collections.emptyList(),
                                null));

        Stream<GraphQLFieldDefinition> compositeFields =
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(entity.hierarchyIterator(), 0), false)
                        .flatMap(e -> e.getEntityReferences().stream())
                        .map(reference -> new GraphQLFieldDefinition(
                                reference.getName(),
                                String.format("Reference to [%s] via FK [%s]",
                                        reference.getTargetEntity().getTable(),
                                        reference.getJoin().getName()),
                                GraphQLInterfaceType.reference(getInterfaceName(reference.getTargetEntity())),
                                new StaticDataFetcher(null),
                                Collections.emptyList(),
                                null));

        return Stream.concat(scalarFields, compositeFields).collect(Collectors.toList());
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
