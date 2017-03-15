package graphql.sql.core.config.domain;

import graphql.schema.GraphQLArgument;
import graphql.sql.core.config.domain.impl.SqlEntity;

import javax.annotation.Nonnull;
import java.util.List;

public interface EntityQuery {
    @Nonnull
    String getName();

    @Nonnull
    SqlEntity getEntity();

    List<GraphQLArgument> getArguments();
}
