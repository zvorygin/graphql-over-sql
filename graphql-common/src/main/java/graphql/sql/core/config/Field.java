package graphql.sql.core.config;


import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

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

}
