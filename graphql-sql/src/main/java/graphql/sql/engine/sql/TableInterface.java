package graphql.sql.engine.sql;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLType;
import graphql.schema.TypeResolverProxy;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.Interface;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class TableInterface extends AbstractTableCompositeType implements Interface {
    public TableInterface(@Nonnull DbTable dbTable,
                          @Nonnull String name,
                          @Nonnull Map<String, Field> fields,
                          @Nonnull SqlExecutorBuilder executorBuilder) {
        super(dbTable, name, fields, Collections.emptyList(), executorBuilder);
    }

    @Override
    public GraphQLType getGraphQLType(Map<String, GraphQLType> dictionary) {
        return GraphQLInterfaceType
                .newInterface()
                .name(getName())
                .fields(getFieldDefinitions())
                .typeResolver(new TypeResolverProxy())
                .build();
    }
}
