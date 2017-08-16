package graphql.sql.engine.sql;

import graphql.execution.ExecutionContext;
import graphql.sql.core.config.TypeExecutor;
import graphql.sql.core.config.QueryNode;
import graphql.sql.core.config.domain.Config;
import graphql.sql.schema.engine.querygraph.AbstractQueryNode;

public class TableNode extends AbstractQueryNode<AbstractTableCompositeType> implements QueryNode {
    // TODO(dzvorygin) consider extracting interface and pushing to base class
    private final SqlExecutorBuilder sqlExecutorBuilder;

    public TableNode(Config config,
                     AbstractTableCompositeType type,
                     SqlExecutorBuilder sqlExecutorBuilder) {
        super(config, type);
        this.sqlExecutorBuilder = sqlExecutorBuilder;
    }

    @Override
    public TypeExecutor buildExecutor(ExecutionContext executionContext) {
        return sqlExecutorBuilder.build(this, executionContext);
    }
}
