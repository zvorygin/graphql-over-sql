package graphql.sql.schema.engine.querygraph;

import com.google.common.base.MoreObjects;
import graphql.execution.ExecutionContext;
import graphql.sql.core.config.CompositeType;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.QueryNode;
import graphql.sql.core.config.TypeExecutor;
import graphql.sql.core.config.domain.Config;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericQueryNode extends AbstractQueryNode<CompositeType> {

    public GenericQueryNode(Config config, CompositeType type) {
        super(config, type);
    }

    @Override
    public TypeExecutor buildExecutor(ExecutionContext executionContext) {
        Map<String, Object> constants = new LinkedHashMap<>();

        getConstants().forEach(constants::put);

        Map<String, TypeExecutor> executors = new LinkedHashMap<>();

        getReferences().forEach((k, v) -> executors.put(k, v.buildExecutor(executionContext)));

        if (!getChildren().isEmpty()) {
            throw new IllegalStateException("Children of generic query node should be empty");
        }

        if (!getFieldsToQuery().isEmpty()) {
            throw new IllegalStateException("Fields of generic query node should be empty");
        }

        if (!getParents().isEmpty()) {
            throw new IllegalStateException("Parents of generic query should be empty");
        }

        return variables -> {
            Map<String, Object> result = new LinkedHashMap<>(constants);
            executors.forEach((k, v) -> result.put(k, v.execute(variables)));
            return result;
        };
    }

    @Override
    public QueryNode fetchField(Config config, graphql.language.Field queryField, ExecutionContext ctx) {
        String fieldName = queryField.getName();

        Field field = getType().getField(fieldName);

        return field.fetch(config, this, ctx, queryField);
    }
}
