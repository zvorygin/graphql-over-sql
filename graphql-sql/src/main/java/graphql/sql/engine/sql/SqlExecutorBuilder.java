package graphql.sql.engine.sql;

import graphql.execution.ExecutionContext;
import graphql.language.SelectionSet;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.TypeExecutor;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

public class SqlExecutorBuilder {
    @Nonnull
    private final DataSource dataSource;

    public SqlExecutorBuilder(@Nonnull DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public TypeExecutor build(TableNode tableNode, ExecutionContext executionContext) {
        return new SqlTypeExecutor(tableNode, executionContext, dataSource);
    }
}
