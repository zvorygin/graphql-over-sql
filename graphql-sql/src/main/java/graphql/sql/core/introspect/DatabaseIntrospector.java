package graphql.sql.core.introspect;

import com.healthmarketscience.sqlbuilder.dbspec.basic.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

public interface DatabaseIntrospector {

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
}
