package graphql.sql.core.sqlquery;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;

import java.io.IOException;
import java.util.List;

public class JoinWithTable extends JoinWith<RejoinTable> {

    public JoinWithTable(RejoinTable withTable,
                         List<? extends Column> fromColumns,
                         List<? extends Column> toColumns) {
        super(withTable, fromColumns, toColumns);
    }

    @Override
    protected void collectWith(ValidationContext vContext, RejoinTable with) {
        vContext.addTable(with);
    }

    @Override
    protected void appendWith(AppendableExt app, RejoinTable with) throws IOException {
        app.append(with.getTableNameSQL());
        String alias = with.getAlias();
        if(alias != null) {
            app.append(" ").append(alias);
        }
    }

}
