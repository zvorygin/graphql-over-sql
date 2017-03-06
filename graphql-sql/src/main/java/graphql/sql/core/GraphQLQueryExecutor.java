package graphql.sql.core;

import graphql.sql.core.extractor.NodeExtractor;
import graphql.sql.core.querygraph.QueryRoot;

public class GraphQLQueryExecutor {
    private final QueryRoot queryGraph;
    private final NodeExtractor nodeExtractor;

    public GraphQLQueryExecutor(QueryRoot queryGraph, NodeExtractor nodeExtractor) {
        this.queryGraph = queryGraph;
        this.nodeExtractor = nodeExtractor;
    }

    public QueryRoot getQueryGraph() {
        return queryGraph;
    }

    public NodeExtractor getNodeExtractor() {
        return nodeExtractor;
    }
}
