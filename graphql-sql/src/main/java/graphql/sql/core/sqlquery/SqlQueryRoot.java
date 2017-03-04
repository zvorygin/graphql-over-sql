package graphql.sql.core.sqlquery;

import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;

import java.util.ArrayList;
import java.util.List;

public class SqlQueryRoot extends SqlQueryNode {
    private final List<RejoinTable.RejoinColumn> columnList = new ArrayList<>();

    public SqlQueryRoot(RejoinTable master) {
        super(master);
    }

    public boolean addColumn(RejoinTable.RejoinColumn column) {
        return columnList.add(column);
    }

    public SelectQuery buildSelectQuery() {
        SelectQuery result = new SelectQuery();
        result.addCustomFromTable(this);
        columnList.forEach(result::addColumns);
        return result;
    }
}
