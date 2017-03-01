package graphql.sql.core;

import graphql.sql.core.query.QueryNode;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import java.util.HashMap;
import java.util.Map;

public class QueryGenerationContext {
    private final Map<QueryNode, RejoinTable> queryNodeToRejoinTable = new HashMap<>();
    private final QueryPreparer queryPreparer;

    public QueryGenerationContext(QueryPreparer queryPreparer) {
        this.queryPreparer = queryPreparer;
    }

    public RejoinTable getTable(QueryNode node) {
        DbTable table = node.getEntity().getTable();
        return queryNodeToRejoinTable
                .computeIfAbsent(node, n -> table.rejoin(table.getName() + "_" + n.getNodeNumber()));
    }

    public QueryPreparer getQueryPreparer() {
        return queryPreparer;
    }
}
