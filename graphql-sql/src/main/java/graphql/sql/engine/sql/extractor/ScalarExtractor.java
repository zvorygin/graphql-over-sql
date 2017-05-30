package graphql.sql.engine.sql.extractor;

import graphql.sql.core.config.domain.type.TypeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ScalarExtractor<T> {
    private final int position;
    private final String name;
    private final TypeUtil<T> typeUtil;

    public ScalarExtractor(int position, String name, TypeUtil<T> typeUtil) {
        this.position = position;
        this.name = name;
        this.typeUtil = typeUtil;
    }

    public T getValue(ResultSet rs) throws SQLException {
        return typeUtil.getValue(rs, position);
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "ScalarExtractor{" +
                "position=" + position +
                ", name='" + name + '\'' +
                '}';
    }
}
