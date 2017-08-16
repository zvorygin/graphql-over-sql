package graphql.sql.core.config;

public class FieldLink {
    private final QueryNode targetNode;
    private final Field targetField;
    private final String alias;

    public FieldLink(QueryNode targetNode, Field targetField, String alias) {
        this.targetNode = targetNode;
        this.targetField = targetField;
        this.alias = alias;
    }

    public QueryNode getTargetNode() {
        return targetNode;
    }

    public Field getTargetField() {
        return targetField;
    }

    public String getAlias() {
        return alias;
    }
}
