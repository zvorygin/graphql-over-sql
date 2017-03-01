package graphql.sql.core.config.domain;

import java.util.List;
import java.util.Objects;

public class Key {
    private final String name;

    private final List<String> columns;

    public Key(String name, List<String> columns) {
        this.name = name;
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public List<String> getColumns() {
        return columns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Key key = (Key) o;
        return Objects.equals(name, key.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
