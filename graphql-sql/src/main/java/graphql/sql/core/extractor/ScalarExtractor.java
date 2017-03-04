package graphql.sql.core.extractor;

import graphql.sql.core.config.domain.type.TypeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ScalarExtractor<T> {
    private final int position;
    private final TypeUtil<T> typeUtil;

    public ScalarExtractor(int position, TypeUtil typeUtil) {
        this.position = position;
        this.typeUtil = typeUtil;
    }

    public T getValue(ResultSet rs) throws SQLException {
        return typeUtil.getValue(rs, position);
    }

    public int getPosition() {
        return position;
    }
}
