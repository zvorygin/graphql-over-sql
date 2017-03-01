package graphql.sql.core.introspect.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.JDBCType;

public class Column {
    @Nonnull
    private final String columnName;

    @Nonnull
    private final JDBCType jdbcType;

    @Nullable
    private final String columnComment;

    public Column(@Nonnull String columnName,
                  @Nonnull JDBCType jdbcType,
                  @Nullable String columnComment) {
        this.columnName = columnName;
        this.columnComment = columnComment;
        this.jdbcType = jdbcType;
    }

    @Nonnull
    public String getColumnName() {
        return columnName;
    }

    @Nonnull
    public JDBCType getJdbcType() {
        return jdbcType;
    }

    @Nullable
    public String getColumnComment() {
        return columnComment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column column = (Column) o;

        return columnName.equals(column.columnName);
    }

    @Override
    public int hashCode() {
        return columnName.hashCode();
    }
}
