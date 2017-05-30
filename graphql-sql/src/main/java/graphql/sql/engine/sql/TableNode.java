package graphql.sql.engine.sql;

import graphql.execution.ExecutionContext;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.FieldExecutor;
import graphql.sql.core.config.QueryNode;
import graphql.sql.schema.engine.querygraph.AbstractQueryNode;

public class TableNode extends AbstractQueryNode<AbstractTableCompositeType> implements QueryNode {
    // TODO(dzvorygin) consider extracting interface and pushing to base class
    private final SqlExecutorBuilder sqlExecutorBuilder;

    public TableNode(AbstractTableCompositeType type,
                     SqlExecutorBuilder sqlExecutorBuilder) {
        super(type);
        this.sqlExecutorBuilder = sqlExecutorBuilder;
    }

    @Override
    public FieldExecutor buildExecutor(Field schemaField, ExecutionContext executionContext) {
        return sqlExecutorBuilder.build(schemaField, this, executionContext);
    }
}
