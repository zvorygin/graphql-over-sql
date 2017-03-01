package graphql.sql.core.config.groovy;

import graphql.sql.core.config.groovy.context.ExecutionContext;

public class SchemaSetter {
    private ExecutionContext executionContext;

    public SchemaSetter(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @SuppressWarnings("unused")
    public void call(String schemaName) {
        executionContext.setSchemaName(schemaName);
    }
}
