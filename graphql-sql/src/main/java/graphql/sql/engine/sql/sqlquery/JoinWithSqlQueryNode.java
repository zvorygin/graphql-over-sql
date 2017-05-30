package graphql.sql.engine.sql.sqlquery;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.ValidationContext;
import com.healthmarketscience.sqlbuilder.dbspec.Column;

import java.io.IOException;
import java.util.List;

public class JoinWithSqlQueryNode extends JoinWith<SqlQueryNode> {

    public JoinWithSqlQueryNode(SqlQueryNode with,
                                List<? extends Column> fromColumns,
                                List<? extends Column> toColumns) {
        super(with, fromColumns, toColumns);
    }

    @Override
    protected void collectWith(ValidationContext vContext, SqlQueryNode with) {
        with.collectSchemaObjects(vContext);

    }

    @Override
    protected void appendWith(AppendableExt app, SqlQueryNode with) throws IOException {
        if (with.requiresParentheses()) {
            app.append(" ( ").append(with).append(" ) ");
        } else {
            app.append(with);
        }
    }
}
