package graphql.sql.core.config;

import graphql.execution.ExecutionContext;
import graphql.language.Argument;
import graphql.language.SelectionSet;
import graphql.sql.core.config.domain.Config;

import java.util.Collection;
import java.util.List;

public interface QueryNode {
    CompositeType getType();

    QueryNode fetchField(Config config, graphql.language.Field queryField, ExecutionContext ctx);

    TypeExecutor buildExecutor(ExecutionContext executionContext);

    void fetchConstant(String alias, Object result);

    void addReference(String s, QueryLink result);

    void addField(FieldLink fieldLink);

    void addArgument(Argument argument);

    List<Argument> getArguments();

    void processSelectionSet(SelectionSet selectionSet, ExecutionContext executionContext);

    void addChild(QueryLink link);

    Collection<QueryLink> getChildren();

    void addParent(QueryLink link);

    QueryNode fetchChild(CompositeType compositeType);

    QueryNode fetchParent(CompositeType compositeType);
}
