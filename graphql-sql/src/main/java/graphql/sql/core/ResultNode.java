package graphql.sql.core;

import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityReference;

import java.util.Map;

public class ResultNode {

    private final ResultKey key;
    private Map<EntityField, Object> entityValues;
    private Map<EntityReference, ResultNode> manyToOneReferences;
    private Map<EntityReference, Map<ResultKey, ResultNode>> oneToManyReferences;


    public ResultNode(ResultKey key) {
        this.key = key;
    }

    public ResultKey getKey() {
        return key;
    }

    public Map<EntityField, Object> getEntityValues() {
        return entityValues;
    }

    public Map<EntityReference, ResultNode> getManyToOneReferences() {
        return manyToOneReferences;
    }

    public Map<EntityReference, Map<ResultKey, ResultNode>> getOneToManyReferences() {
        return oneToManyReferences;
    }
}
