package graphql.sql.schema.engine;

import graphql.execution.ExecutionContext;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.FieldExecutor;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.QueryNode;
import graphql.sql.schema.engine.querygraph.AbstractQueryNode;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class ObjectTypeImpl extends AbstractObjectTypeImpl<Field> {
    public ObjectTypeImpl(@Nonnull String name,
                          @Nonnull Map<String, Field> fields,
                          @Nonnull List<Interface> interfaces) {
        super(name, fields, interfaces);
    }

    @Override
    public QueryNode buildQueryNode() {
        return new AbstractQueryNode(this) {
            @Override
            public FieldExecutor buildExecutor(Field schemaField, ExecutionContext executionContext) {
                return null;
            }
        };
    }
}
