package graphql.sql.core.config.groovy;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.sql.core.config.NameProvider;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.impl.SqlEntity;
import graphql.sql.core.config.domain.impl.SqlEntityField;
import graphql.sql.core.config.domain.impl.SqlEntityQuery;
import graphql.sql.core.config.groovy.context.ExecutionContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class QueryBuilder {

    private final ExecutionContext executionContext;

    public QueryBuilder(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public void call(Map<String, Object> parameters) {
        String name = Objects.requireNonNull((String) parameters.get("name"), "Query name not specified");
        List<String> fieldNames = Objects.requireNonNull((List<String>) parameters.get("fields"),
                "Query fields were not specified");
        SqlEntity entity = Objects.requireNonNull((SqlEntity) parameters.get("entity"), "Entity not specified");

        if (fieldNames.isEmpty()) {
            throw new IllegalStateException(String.format("Fields of query [%s] shouldn't be empty", name));
        }

        // TODO(dzvorygin) validate that field comes from appropriate entity somehow.

        List<SqlEntityField> fields =
                entity.getEntityFields().stream().
                filter(f -> fieldNames.contains(f.getFieldName())).collect(Collectors.toList());

        executionContext.registerQuery(
                new SqlEntityQuery(name, entity, fields,
                        buildArguments(fields, name, executionContext.getNameProvider())));
    }

    private List<GraphQLArgument> buildArguments(List<SqlEntityField> fields, String name, NameProvider nameProvider) {
        if (fields.size() <= 1) {
            EntityField entityField = fields.get(0);

            return Collections.singletonList(GraphQLArgument.newArgument()
                    .name(nameProvider.getFieldListName(entityField))
                    .type(new GraphQLNonNull(new GraphQLList(new GraphQLNonNull(
                            entityField.getScalarType().getTypeUtil().getGraphQLScalarType()))))
                    .build());
        }
        GraphQLInputObjectType.Builder elementTypeBuilder = GraphQLInputObjectType.newInputObject()
                .name(name + "Query")
                .description("Query parameters for " + name);

        fields.stream()
                .map(field -> GraphQLInputObjectField.newInputObjectField()
                        .name(field.getFieldName())
                        .type(new GraphQLNonNull(field.getScalarType().getTypeUtil().getGraphQLScalarType()))
                )
                .forEach(elementTypeBuilder::field);
        return Collections.singletonList(new GraphQLArgument("query",
                new GraphQLNonNull(new GraphQLList(new GraphQLNonNull(elementTypeBuilder.build())))));
    }
}
