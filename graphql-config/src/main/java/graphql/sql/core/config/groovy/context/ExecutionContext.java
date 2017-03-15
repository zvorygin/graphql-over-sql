package graphql.sql.core.config.groovy.context;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbObject;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import graphql.sql.core.config.NameProvider;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.domain.impl.SqlConfig;
import graphql.sql.core.config.domain.impl.SqlEntity;
import graphql.sql.core.config.domain.impl.SqlEntityQuery;
import graphql.sql.core.introspect.DatabaseIntrospector;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExecutionContext {

    private final Map<DbTable, SqlEntity> entities = new TreeMap<>(Comparator.comparing(DbObject::getAbsoluteName));

    private final Map<String, SqlEntityQuery> queries = new TreeMap<>();

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

    public void registerEntity(SqlEntity entity) {
        if (entities.putIfAbsent(entity.getTable(), entity) != null) {
            throw new IllegalArgumentException(
                    String.format("Entity for table [%s] is already registered", entity.getTable().getAbsoluteName()));
        }
    }

    public Config buildConfig() {
        Map<String, SqlEntity> entityMap =
                entities.values().stream().collect(Collectors.toMap(SqlEntity::getEntityName, Function.identity()));
        return new SqlConfig(entityMap, queries);
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

    public Optional<SqlEntity> findEntity(DbTable referencedTable) {
        return Optional.ofNullable(entities.get(referencedTable));
    }

    public void registerQuery(SqlEntityQuery entityQuery) {
        if (queries.putIfAbsent(entityQuery.getName(), entityQuery) != null) {
            throw new IllegalStateException(
                    String.format("Query with name [%s] is already registered", entityQuery.getName()));
        }
    }
}
