package graphql.sql.core.config.domain.impl;

import graphql.schema.GraphQLArgument;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityQuery;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SqlEntityQuery implements Comparable<EntityQuery>, EntityQuery {

    @Nonnull
    private final String name;

    @Nonnull
    private final SqlEntity entity;

    @Nonnull
    private final List<SqlEntityField> entityFields;

    @Nonnull
    private final List<GraphQLArgument> arguments;

    public SqlEntityQuery(@Nonnull String name,
                          @Nonnull SqlEntity entity,
                          @Nonnull List<SqlEntityField> entityFields,
                          @Nonnull List<GraphQLArgument> arguments) {
        this.name = name;
        this.entity = entity;
        this.entityFields = entityFields;
        this.arguments = arguments;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nonnull
    public SqlEntity getEntity() {
        return entity;
    }

    @Nonnull
    @Override
    public List<GraphQLArgument> getArguments() {
        return arguments;
    }

    @Nonnull
    public List<EntityField> getEntityFields() {
        return Collections.unmodifiableList(entityFields);
    }

    @Override
    public int compareTo(@Nonnull EntityQuery o) {
        return name.compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SqlEntityQuery that = (SqlEntityQuery) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
