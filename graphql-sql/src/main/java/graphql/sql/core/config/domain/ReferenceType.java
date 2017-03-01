package graphql.sql.core.config.domain;

import com.healthmarketscience.sqlbuilder.SelectQuery;

public enum ReferenceType {
    ONE_TO_MANY(SelectQuery.JoinType.LEFT_OUTER),
    MANY_TO_ONE(SelectQuery.JoinType.INNER);

    private final SelectQuery.JoinType joinType;

    ReferenceType(SelectQuery.JoinType joinType) {
        this.joinType = joinType;
    }

    public SelectQuery.JoinType getJoinType() {
        return joinType;
    }
}
