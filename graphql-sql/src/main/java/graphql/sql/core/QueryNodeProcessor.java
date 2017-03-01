package graphql.sql.core;

import graphql.sql.core.config.domain.EntityReference;
import graphql.sql.core.config.domain.type.TypeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.Nullable;

public class QueryNodeProcessor {
    private final int[] keyColumnPositions;
    private final TypeUtil[] keyColumnTypeUtils;
    private final List<FieldProcessor> fieldProcessors;
    private final LinkedHashMap<EntityReference, QueryNodeProcessor> referencesProcessors;
    private final QueryNodeProcessor parentProcessor;
    private final List<QueryNodeProcessor> childProcessors;

    public QueryNodeProcessor(int[] keyColumnPositions,
                              TypeUtil[] keyColumnTypeUtils,
                              List<FieldProcessor> fieldProcessors,
                              LinkedHashMap<EntityReference, QueryNodeProcessor> referencesProcessors,
                              QueryNodeProcessor parentProcessor,
                              List<QueryNodeProcessor> childProcessors) {
        this.keyColumnPositions = keyColumnPositions;
        this.keyColumnTypeUtils = keyColumnTypeUtils;
        this.fieldProcessors = fieldProcessors;
        this.referencesProcessors = referencesProcessors;
        this.parentProcessor = parentProcessor;
        this.childProcessors = childProcessors;
    }

    @Nullable
    public ResultKey getKey(ResultSet rs) throws SQLException {
        Object[] key = new Object[keyColumnPositions.length];

        boolean allNulls = true;

        for (int i = 0; i < keyColumnPositions.length; i++) {
            key[i] = keyColumnTypeUtils[i].getValue(rs, keyColumnPositions[i]);
            allNulls &= (key[i] == null);
        }

        return allNulls ? null : new ResultKey(key);
    }

    public List<FieldProcessor> getFieldProcessors() {
        return fieldProcessors;
    }

    public LinkedHashMap<EntityReference, QueryNodeProcessor> getReferencesProcessors() {
        return referencesProcessors;
    }

    public QueryNodeProcessor getParentProcessor() {
        return parentProcessor;
    }

    public List<QueryNodeProcessor> getChildProcessors() {
        return childProcessors;
    }
}
