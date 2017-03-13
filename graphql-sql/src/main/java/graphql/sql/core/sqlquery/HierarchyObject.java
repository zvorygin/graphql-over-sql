package graphql.sql.core.sqlquery;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.SqlObject;
import com.healthmarketscience.sqlbuilder.ValidationContext;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HierarchyObject extends SqlObject {
    private final RejoinTable master;

    private final List<JoinWithTable> parents = new ArrayList<>();

    private final List<JoinWithTable> children = new ArrayList<>();

    public HierarchyObject(RejoinTable master) {
        this.master = master;
    }

    public boolean addParent(JoinWithTable joinWithTable) {
        return parents.add(joinWithTable);
    }

    public boolean addChild(JoinWithTable joinWithTable) {
        return children.add(joinWithTable);
    }

    protected boolean requiresParentheses() {
        return !parents.isEmpty() || !children.isEmpty();
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
        vContext.addTable(master);
        parents.forEach(joinWithTable -> joinWithTable.collectSchemaObjects(vContext));
        children.forEach(joinWithTable -> joinWithTable.collectSchemaObjects(vContext));
    }

    @Override
    public void appendTo(AppendableExt a) throws IOException {
        a.append(master.getTableNameSQL());
        String alias = master.getAlias();
        if (alias != null) {
            a.append(" ").append(alias);
        }
        for (JoinWithTable parent : parents) {
            a.append(" ");
            parent.appendTo(SelectQuery.JoinType.INNER, a);
        }
        for (JoinWithTable child : children) {
            a.append(" ");
            child.appendTo(SelectQuery.JoinType.LEFT_OUTER, a);
        }
    }
}
