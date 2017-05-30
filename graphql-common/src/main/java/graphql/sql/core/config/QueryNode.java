package graphql.sql.core.config;

import graphql.execution.ExecutionContext;

public interface QueryNode {
    CompositeType getType();

    void fetchField(String name, String alias);

    QueryNode fetchChild(CompositeType type);

    QueryNode fetchParent(CompositeType type);

    FieldExecutor buildExecutor(Field schemaField, ExecutionContext executionContext);
}
