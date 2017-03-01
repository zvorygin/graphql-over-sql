package graphql.sql.core.introspect;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbForeignKeyConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

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
}
