package graphql.sql.core.config.domain;

import graphql.sql.core.config.domain.impl.SqlEntity;
import graphql.sql.core.config.domain.impl.SqlEntityQuery;

import java.util.Collection;

public interface Config {
    Collection<Entity> getEntities();

    SqlEntity getEntity(String entityName);

    Collection<EntityQuery> getQueries();

    SqlEntityQuery getQuery(String queryName);
}
