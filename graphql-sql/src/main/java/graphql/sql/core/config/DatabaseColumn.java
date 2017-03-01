package graphql.sql.core.config;

import javax.annotation.Nonnull;
import java.sql.JDBCType;

public class DatabaseColumn {
    @Nonnull
    private final String name;

    @Nonnull
    private final JDBCType type;

    @Nonnull
    private final String comment;

    public DatabaseColumn(@Nonnull String name, @Nonnull JDBCType type, @Nonnull String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public JDBCType getType() {
        return type;
    }

    @Nonnull
    public String getComment() {
        return comment;
    }
}
