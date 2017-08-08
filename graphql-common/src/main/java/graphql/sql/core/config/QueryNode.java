package graphql.sql.core.config;

import graphql.execution.ExecutionContext;
import graphql.language.Argument;
import graphql.sql.core.config.domain.Config;

import java.util.List;

public interface QueryNode {
    CompositeType getType();

    QueryNode fetchField(Config config, graphql.language.Field queryField, ExecutionContext ctx);
/*
    QueryNode fetchChild(CompositeType type);

    QueryNode fetchParent(CompositeType type);*/

    TypeExecutor buildExecutor(ExecutionContext executionContext);

    void fetchConstant(String alias, Object result);

    void addReference(String s, QueryNode result);

    void addField(String alias, Field field);

    void addArgument(Argument argument);

    List<Argument> getArguments();
}
