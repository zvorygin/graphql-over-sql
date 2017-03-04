package graphql.sql.core.sqlquery;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SqlQueryNode extends HierarchyObject {
    private final List<JoinWithSqlQueryNode> nestedNodes = new ArrayList<>();

    public SqlQueryNode(RejoinTable master) {
        super(master);
    }

    public boolean addNestedNode(JoinWithSqlQueryNode node) {
        return nestedNodes.add(node);
    }

    @Override
    protected boolean requiresParentheses() {
        return !nestedNodes.isEmpty() || super.requiresParentheses();
    }

    @Override
    public void appendTo(AppendableExt a) throws IOException {
        super.appendTo(a);
        for (JoinWithSqlQueryNode nestedNode : nestedNodes) {
            nestedNode.appendTo(SelectQuery.JoinType.LEFT_OUTER, a);
        }
    }
}
