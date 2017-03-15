package graphql.sql.core.querygraph;

import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;
import graphql.sql.core.config.domain.impl.SqlEntity;
import graphql.sql.core.sqlquery.SqlQueryRoot;

public class QueryRoot extends QueryNode<SqlQueryRoot> {

    private int nodeNumber = 0;

    public QueryRoot(SqlEntity root) {
        this(root, root.getTable().rejoin("t0"));
    }

    private QueryRoot(SqlEntity root, RejoinTable t) {
        super(root, new SqlQueryRoot(t), null, null, t);
    }

    public int nextNodeNumber() {
        return ++nodeNumber;
    }
}
