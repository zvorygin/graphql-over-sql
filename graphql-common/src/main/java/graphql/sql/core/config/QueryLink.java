package graphql.sql.core.config;

import java.util.Arrays;
import java.util.Objects;

public class QueryLink {
    private final QueryNode source;
    private final QueryNode target;

    private final Field[] sourceFields;
    private final Field[] targetFields;

    public QueryLink(QueryNode source, QueryNode target, Field[] sourceFields, Field[] targetFields) {
        if (sourceFields.length != targetFields.length) {
            throw new IllegalStateException(
                    String.format("Source [%d] and target [%d] link field lengths should match",
                            sourceFields.length, targetFields.length));
        }

        this.source = source;
        this.target = target;
        this.sourceFields = sourceFields;
        this.targetFields = targetFields;
    }

    public QueryNode getSource() {
        return source;
    }

    public QueryNode getTarget() {
        return target;
    }

    public Field[] getSourceFields() {
        return sourceFields;
    }

    public Field[] getTargetFields() {
        return targetFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueryLink queryLink = (QueryLink) o;

        return source.equals(queryLink.source)
                && target.equals(queryLink.target)
                && Arrays.equals(sourceFields, queryLink.sourceFields)
                && Arrays.equals(targetFields, queryLink.targetFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, sourceFields, targetFields);
    }

    public QueryLink reverse() {
        return new QueryLink(target, source, targetFields, sourceFields);
    }
}
