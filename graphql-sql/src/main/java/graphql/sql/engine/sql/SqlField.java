package graphql.sql.engine.sql;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.TypeReference;

import javax.annotation.Nonnull;

public class SqlField extends Field {
    @Nonnull
    private final DbColumn dbColumn;

    public SqlField(@Nonnull String name, @Nonnull TypeReference typeReference, @Nonnull DbColumn dbColumn) {
        super(name, typeReference);
        this.dbColumn = dbColumn;
    }

    @Nonnull
    public DbColumn getDbColumn() {
        return dbColumn;
    }
}
