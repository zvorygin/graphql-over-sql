package graphql.sql.core.query;

import graphql.sql.core.config.domain.Entity;

public class QueryGraph extends QueryNode {

    private int nodeNumber = 0;

    public QueryGraph(Entity root) {
        super(root, true, null, 0);
    }

    public int nextNodeNumber() {
        return nodeNumber++;
    }
}
