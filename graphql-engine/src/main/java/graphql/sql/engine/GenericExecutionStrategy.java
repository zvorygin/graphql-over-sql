package graphql.sql.engine;

import graphql.ExecutionResult;
import graphql.execution.ExecutionContext;
import graphql.execution.SimpleExecutionStrategy;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GenericExecutionStrategy extends SimpleExecutionStrategy {
    public Object executeField(GraphQLFieldDefinition fieldDef, Field field, ExecutionContext executionContext) {
        List<Field> fields = Collections.singletonList(field);

        Map<String, Object> argumentValues = valuesResolver.getArgumentValues(
                fieldDef.getArguments(),
                field.getArguments(),
                executionContext.getVariables());

        DataFetchingEnvironment environment = new DataFetchingEnvironment(
                null,
                argumentValues,
                executionContext.getRoot(),
                fields,
                fieldDef.getType(),
                executionContext.getGraphQLSchema().getQueryType(),
                executionContext.getGraphQLSchema());

        Object resolvedValue = fieldDef.getDataFetcher().get(environment);

        ExecutionResult executionResult = completeValue(executionContext, fieldDef.getType(), fields, resolvedValue);
        return executionResult.getData();
    }
}
