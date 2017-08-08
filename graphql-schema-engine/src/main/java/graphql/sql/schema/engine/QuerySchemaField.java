package graphql.sql.schema.engine;

import com.google.common.base.MoreObjects;
import graphql.execution.ExecutionContext;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLFieldDefinition;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.QueryNode;
import graphql.sql.core.config.TypeReferenceImpl;
import graphql.sql.core.config.domain.Config;

public class QuerySchemaField extends Field {
    public QuerySchemaField() {
        super("__schema", new TypeReferenceImpl("__Schema"));
    }


    @Override
    public QueryNode fetch(Config config, QueryNode queryNode, ExecutionContext ctx, graphql.language.Field queryField) {
        GenericExecutionStrategy executionStrategy = new GenericExecutionStrategy();

        ExecutionContext copy = new ExecutionContext(
                ctx.getGraphQLSchema(),
                executionStrategy,
                executionStrategy,
                ctx.getFragmentsByName(),
                ctx.getOperationDefinition(),
                ctx.getVariables(),
                ctx.getRoot());

        Object result = executionStrategy.executeField(getGraphQLFieldDefinition(), queryField, copy);

        queryNode.fetchConstant(MoreObjects.firstNonNull(queryField.getAlias(), queryField.getName()), result);

        return null;
    }


    @Override
    public GraphQLFieldDefinition getGraphQLFieldDefinition() {
        return Introspection.SchemaMetaFieldDef;
    }
}
