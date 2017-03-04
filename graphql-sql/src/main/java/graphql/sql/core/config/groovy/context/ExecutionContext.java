package graphql.sql.core.config.groovy.context;

import graphql.sql.core.config.NameProvider;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityQuery;
import graphql.sql.core.introspect.DatabaseIntrospector;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class ExecutionContext {

    private final Map<String, Entity> entities = new TreeMap<>();

    private final Map<String, EntityQuery> queries = new TreeMap<>();

    private final NameProvider nameProvider;

    private final DatabaseIntrospector introspector;
    private String schemaName;
    private String catalogName;

    public ExecutionContext(NameProvider nameProvider, DatabaseIntrospector introspector) {
        this.nameProvider = nameProvider;
        this.introspector = introspector;
    }

    public NameProvider getNameProvider() {
        return nameProvider;
    }

    public DatabaseIntrospector getIntrospector() {
        return introspector;
    }

    public void registerEntity(Entity entity) {
        if (entities.putIfAbsent(entity.getEntityName(), entity) != null) {
            throw new IllegalArgumentException(
                    String.format("Entity with name [%s] is already registered", entity.getEntityName()));
        }
    }

    public Config buildConfig() {
        return new Config(entities, queries);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schema) {
        schemaName = schema;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public Optional<Entity> findEntity(DbTable referencedTable) {
        return entities.values().stream().filter(entity -> entity.getTable().equals(referencedTable)).findAny();
    }

    public void registerQuery(EntityQuery entityQuery) {
        if (queries.putIfAbsent(entityQuery.getName(), entityQuery) != null) {
            throw new IllegalStateException(
                    String.format("Query with name [%s] is already registered", entityQuery.getName()));
        }
    }
}
