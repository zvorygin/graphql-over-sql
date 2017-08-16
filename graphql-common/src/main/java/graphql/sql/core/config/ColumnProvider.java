package graphql.sql.core.config;

import graphql.sql.core.config.domain.EntityField;

public interface ColumnProvider {
    EntityField getColumns(String tableName, String schemaName);
}
