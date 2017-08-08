package graphql.sql.engine;

import graphql.execution.ExecutionContext;
import graphql.language.SelectionSet;
import graphql.sql.core.config.CompositeType;
import graphql.sql.core.config.QueryNode;
import graphql.sql.core.config.domain.Config;

public class QueryGraphBuilder {

    private final Config config;

    public QueryGraphBuilder(Config config) {
        this.config = config;
    }

    public QueryNode build(CompositeType rootType, SelectionSet selectionSet, ExecutionContext executionContext) {
        return rootType.buildQueryNode(config, selectionSet, executionContext);
    }
}
