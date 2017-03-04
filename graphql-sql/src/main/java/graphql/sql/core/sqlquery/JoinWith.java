package graphql.sql.core.sqlquery;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.ValidationContext;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.RejoinTable;

import java.io.IOException;
import java.util.List;

public abstract class JoinWith<T> {
    private final T with;
    private final ComboCondition onCondition;
    private final List<? extends Column> fromColumns;
    private final List<? extends Column> toColumns;

    protected JoinWith(T with, List<? extends Column> fromColumns, List<? extends Column> toColumns) {
        assert fromColumns.size() == toColumns.size() : "fromColumns size should match toColumn size.";

        this.with = with;
        this.fromColumns = fromColumns;
        this.toColumns = toColumns;

        onCondition = ComboCondition.and();
        // create join condition
        for (int i = 0; i < fromColumns.size(); ++i) {
            onCondition.addCondition(BinaryCondition.equalTo(fromColumns.get(i), toColumns.get(i)));
        }

    }

    void collectSchemaObjects(ValidationContext vContext) {
        collectWith(vContext, with);
        fromColumns.forEach(vContext::addColumn);
        toColumns.forEach(vContext::addColumn);
    }

    void appendTo(SelectQuery.JoinType joinType, AppendableExt app) throws IOException {
        app.append(joinType);
        appendWith(app, with);
        app.append(" ON ").append(onCondition);
    }

    protected abstract void collectWith(ValidationContext vContext, T with);

    protected abstract void appendWith(AppendableExt app, T with) throws IOException;
}
