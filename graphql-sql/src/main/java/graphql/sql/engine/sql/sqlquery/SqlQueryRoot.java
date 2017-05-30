package graphql.sql.engine.sql.sqlquery;

import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SqlQueryRoot extends SqlQueryNode {
    private final List<RejoinTable.RejoinColumn> columnList = new ArrayList<>();

    public SqlQueryRoot(RejoinTable master) {
        super(master);
    }

    public int addColumn(@Nonnull RejoinTable.RejoinColumn column) {
        columnList.add(column);
        return columnList.size();
    }

    public SelectQuery buildSelectQuery() {
        SelectQuery result = new SelectQuery();
        result.addCustomFromTable(this);
        columnList.forEach(result::addColumns);
        return result;
    }
}
