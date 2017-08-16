package graphql.sql.engine.sql.sqlquery;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.SqlObject;
import com.healthmarketscience.sqlbuilder.ValidationContext;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HierarchyObject extends SqlObject {
    private final RejoinTable table;

    private final List<JoinWithSqlQueryNode> parents = new ArrayList<>();

    private final List<JoinWithSqlQueryNode> children = new ArrayList<>();

    public HierarchyObject(RejoinTable table) {
        this.table = table;
    }

    public RejoinTable getTable() {
        return table;
    }

    public boolean addParent(JoinWithSqlQueryNode joinWithTable) {
        return parents.add(joinWithTable);
    }

    public boolean addChild(JoinWithSqlQueryNode joinWithTable) {
        return children.add(joinWithTable);
    }

    protected boolean requiresParentheses() {
        return !parents.isEmpty() || !children.isEmpty();
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
        vContext.addTable(table);
        parents.forEach(joinWithTable -> joinWithTable.collectSchemaObjects(vContext));
        children.forEach(joinWithTable -> joinWithTable.collectSchemaObjects(vContext));

    }

    @Override
    public void appendTo(AppendableExt a) throws IOException {
        a.append(table.getTableNameSQL());
        String alias = table.getAlias();
        if (alias != null) {
            a.append(" ").append(alias);
        }
        for (JoinWithSqlQueryNode parent : parents) {
            a.append(" ");
            parent.appendTo(SelectQuery.JoinType.INNER, a);
        }
        for (JoinWithSqlQueryNode child : children) {
            a.append(" ");
            child.appendTo(SelectQuery.JoinType.LEFT_OUTER, a);
        }
    }
}
