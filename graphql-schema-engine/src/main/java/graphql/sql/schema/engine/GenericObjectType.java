package graphql.sql.schema.engine;

import graphql.execution.ExecutionContext;
import graphql.language.SelectionSet;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.QueryNode;
import graphql.sql.core.config.domain.Config;
import graphql.sql.schema.engine.querygraph.GenericQueryNode;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class GenericObjectType extends AbstractObjectTypeImpl<Field> {
    public GenericObjectType(@Nonnull String name,
                             @Nonnull Map<String, Field> fields,
                             @Nonnull List<Interface> interfaces) {
        super(name, fields, interfaces);
    }

    @Override
    public QueryNode buildQueryNode(Config config) {
        return new GenericQueryNode(config, this);
    }
}
