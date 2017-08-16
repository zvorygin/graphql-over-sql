package graphql.sql.core.config;


import com.google.common.base.MoreObjects;
import graphql.execution.ExecutionContext;
import graphql.language.Argument;
import graphql.language.SelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.sql.core.QueryBuilderException;
import graphql.sql.core.config.domain.Config;

import javax.annotation.Nonnull;
import java.util.*;

public class Field {
    @Nonnull
    private final String name;

    @Nonnull
    private final TypeReference typeReference;

    @Nonnull
    private final Map<String, TypeReference> attributes;

    public Field(@Nonnull String name, @Nonnull TypeReference typeReference) {
        this(name, typeReference, Collections.emptyMap());
    }

    public Field(@Nonnull String name,
                 @Nonnull TypeReference typeReference,
                 @Nonnull Map<String, TypeReference> attributes) {
        this.name = name;
        this.typeReference = typeReference;
        this.attributes = attributes;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public TypeReference getTypeReference() {
        return typeReference;
    }

    public GraphQLFieldDefinition getGraphQLFieldDefinition() {
        GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition()
                .name(name)
                .type(typeReference.getGraphQLOutputTypeReference());

        attributes.forEach((k, v) -> builder.argument(GraphQLArgument.newArgument().name(k).type(v.getGraphQLInputTypeReference())));

        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Field field = (Field) o;

        return name.equals(field.name) && typeReference.equals(field.typeReference);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + typeReference.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", typeReference=" + typeReference +
                '}';
    }

    public QueryNode fetch(Config config, QueryNode queryNode, ExecutionContext ctx, graphql.language.Field queryField) {
        CompositeType type = config.getType(getTypeReference());
        String alias = MoreObjects.firstNonNull(queryField.getAlias(), name);

        if (type != null) {
            QueryNode target = type.buildQueryNode(config);
            target.processSelectionSet(queryField.getSelectionSet(), ctx);

            queryField.getArguments().forEach(target::addArgument);

            queryNode.addReference(alias, buildLink(queryNode, target));

            SelectionSet selectionSet = queryField.getSelectionSet();

            if (selectionSet != null) {
                if (target == null) {
                    throw new QueryBuilderException(
                            String.format("Field [%s] with alias [%s] has selection set, but doesn't have QueryNode",
                                    name, queryField.getAlias()));
                }
            }
            return target;
        }

        Scalar scalar = config.getScalar(getTypeReference());
        if (scalar == null) {
            throw new QueryBuilderException(String.format("Unknown field [%s] with alias [%s] type [%s]",
                    name, queryField.getAlias(), getTypeReference().getTypeName()));
        }

        CompositeType nodeType = queryNode.getType();

        //TODO(dzvorygin) join to find proper queryNode for the field

        Field field = nodeType.getField(name);

        queryNode.addField(alias, field);

        return null;
    }

    @Nonnull
    public QueryLink buildLink(QueryNode source, QueryNode target) {
        Map<String, Field> sourceFields = source.getType().getFields();
        Map<String, Field> targetFields = target.getType().getFields();

        List<Field> sourceLinkFields = new ArrayList<>();
        List<Field> targetLinkFields = new ArrayList<>();

        for (Map.Entry<String, Field> sourceEntry : sourceFields.entrySet()) {
            if (targetFields.containsKey(sourceEntry.getKey())) {
                sourceLinkFields.add(sourceEntry.getValue());
                targetLinkFields.add(targetFields.get(sourceEntry.getKey()));
            }
        }


        return new QueryLink(source,
                target,
                sourceLinkFields.toArray(new Field[sourceLinkFields.size()]),
                targetLinkFields.toArray(new Field[targetLinkFields.size()]));
    }
}
