package graphql.sql.core.config.domain.impl;

import com.google.common.collect.ImmutableMap;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class SqlConfig implements Config {
    private final Map<String, SqlEntity> entities;
    private final Map<String, SqlEntityQuery> queries;

    public SqlConfig(Map<String, SqlEntity> entities, Map<String, SqlEntityQuery> queries) {
        this.entities = ImmutableMap.copyOf(entities);
        this.queries = ImmutableMap.copyOf(queries);
    }

    @Override
    public Collection<Entity> getEntities() {
        return Collections.unmodifiableCollection(entities.values());
    }

    @Override
    public SqlEntity getEntity(String entityName) {
        return entities.get(entityName);
    }

    public Collection<EntityQuery> getQueries() {
        return Collections.unmodifiableCollection(queries.values());
    }

    @Override
    public SqlEntityQuery getQuery(String queryName) {
        return queries.get(queryName);
    }
}
