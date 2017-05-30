package graphql.sql.engine.sql;

import graphql.execution.ExecutionContext;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.FieldExecutor;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

public class SqlExecutorBuilder {
    @Nonnull
    private final DataSource dataSource;

    public SqlExecutorBuilder(@Nonnull DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public FieldExecutor build(Field schemaField, TableNode tableNode, ExecutionContext executionContext) {
        return new SqlFieldExecutor(schemaField, tableNode, executionContext, dataSource);
    }
}
