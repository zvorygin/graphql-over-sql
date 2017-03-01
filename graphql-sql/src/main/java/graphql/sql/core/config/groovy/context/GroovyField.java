package graphql.sql.core.config.groovy.context;

import graphql.sql.core.introspect.domain.Column;

import java.util.Objects;
import javax.annotation.Nonnull;

public class GroovyField {
    @Nonnull
    private final String name;

    @Nonnull
    private final Column column;

    public GroovyField(@Nonnull String name, @Nonnull Column column) {
        this.name = name;
        this.column = column;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Column getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroovyField that = (GroovyField) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(column, that.column);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, column);
    }
}
