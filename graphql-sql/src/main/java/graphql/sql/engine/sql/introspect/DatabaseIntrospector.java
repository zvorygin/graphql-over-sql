package graphql.sql.engine.sql.introspect;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbForeignKeyConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

public interface DatabaseIntrospector {

    String DEFAULT_SCHEMA = "PUBLIC";

    @Nonnull
    DbSpec getSchema();

    @Nonnull
    Optional<DbTable> getTable(@Nonnull String schemaName, @Nonnull String tableName);

    @Nonnull
    Collection<DbForeignKeyConstraint> getForeignKeyConstraints(DbTable table);

    @Nonnull
    DbJoin getJoin(DbForeignKeyConstraint constraint);

    @Nonnull
    DbJoin getReverseJoin(DbForeignKeyConstraint constraint);

    @Nullable
    DbConstraint getPrimaryKeyConstraint(DbTable table);

    default Optional<DbTable> getTable(String tableName) {
        int dotIndex = tableName.indexOf('.');

        if (dotIndex == -1) {
            return getTable(DEFAULT_SCHEMA, tableName);
        }

        return getTable(tableName.substring(0, dotIndex), tableName.substring(dotIndex + 1));
    }
}
