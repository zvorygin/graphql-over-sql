package graphql.sql.core.config.domain;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class Config {
    private final Map<String, Entity> entities;
    private final Map<String, EntityQuery> queries;

    public Config(Map<String, Entity> entities, Map<String, EntityQuery> queries) {
        this.entities = ImmutableMap.copyOf(entities);
        this.queries = ImmutableMap.copyOf(queries);
    }

    public Map<String, Entity> getEntities() {
        return entities;
    }

    public Entity getEntity(String entityName) {
        return entities.get(entityName);
    }

    public Map<String, EntityQuery> getQueries() {
        return queries;
    }

    public EntityQuery getQuery(String queryName) {
        return queries.get(queryName);
    }
}
