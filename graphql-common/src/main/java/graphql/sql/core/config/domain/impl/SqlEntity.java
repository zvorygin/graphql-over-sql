package graphql.sql.core.config.domain.impl;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import graphql.sql.core.config.domain.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlEntity implements Entity {

    @Nonnull
    private final String entityName;

    @Nonnull
    private final DbTable table;

    @Nonnull
    private final List<SqlEntityField> entityFields = new ArrayList<>();

    @Nonnull
    private final List<SqlEntityReference> entityReferences = new ArrayList<>();

    @Nullable
    private final SqlEntityReference parentReference;

    @Nullable
    private final Key primaryKey;

    @Nonnull
    private final String description;

    public SqlEntity(@Nonnull String entityName,
                     @Nonnull DbTable table,
                     @Nonnull List<SqlEntityField> entityFields,
                     @Nullable SqlEntityReference parentReference,
                     @Nullable Key primaryKey) {
        this.entityName = entityName;
        this.table = table;
        this.entityFields.addAll(entityFields);
        this.parentReference = parentReference;
        this.primaryKey = primaryKey;
        description = String.format("Entity [%s] for table [%s]", entityName, table.getAbsoluteName());
    }

    @Override
    @Nonnull
    public String getEntityName() {
        return entityName;
    }

    @Nonnull
    public DbTable getTable() {
        return table;
    }

    @Override
    @Nonnull
    public List<SqlEntityField> getEntityFields() {
        return entityFields;
    }

    @Override
    @Nullable
    public SqlEntityReference getParentReference() {
        return parentReference;
    }

    @Nullable
    public Key getPrimaryKey() {
        return primaryKey;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return description;
    }

    public void addReference(SqlEntityReference reference) {
        entityReferences.add(reference);
    }

    @Override
    @Nonnull
    public List<SqlEntityReference> getEntityReferences() {
        return entityReferences;
    }

    public Optional<SqlEntityReference> findReference(String fieldName) {
        return entityReferences.stream().filter(reference -> reference.getName().equals(fieldName)).findAny();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SqlEntity entity = (SqlEntity) o;

        return entityName.equals(entity.entityName);
    }

    @Override
    public int hashCode() {
        return entityName.hashCode();
    }

    @Override
    public String toString() {
        return entityName;
    }
}
