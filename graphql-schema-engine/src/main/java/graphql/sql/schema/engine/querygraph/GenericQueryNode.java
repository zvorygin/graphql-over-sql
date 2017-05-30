package graphql.sql.schema.engine.querygraph;

import graphql.execution.ExecutionContext;
import graphql.sql.core.config.CompositeType;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.FieldExecutor;
import graphql.sql.core.config.QueryNode;

import java.util.Map;

public class GenericQueryNode extends AbstractQueryNode {
    public GenericQueryNode(CompositeType type) {
        super(type);
    }

    @Override
    public FieldExecutor buildExecutor(Field schemaField, ExecutionContext executionContext) {
        return new FieldExecutor() {
            @Override
            public Object execute(Map<String, Object> variables) {
                return null;
            }
        };
    }
}
