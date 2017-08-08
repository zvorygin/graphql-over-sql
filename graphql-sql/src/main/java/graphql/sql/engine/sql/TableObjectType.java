package graphql.sql.engine.sql;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.ObjectType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class TableObjectType extends AbstractTableCompositeType implements ObjectType {
    public TableObjectType(@Nonnull DbTable dbTable,
                           @Nonnull String name,
                           @Nonnull Map<String, Field> fields,
                           @Nonnull List<Interface> interfaces,
                           @Nonnull SqlExecutorBuilder executorBuilder) {
        super(dbTable, name, fields, interfaces, executorBuilder);
    }

    @Override
    public GraphQLType getGraphQLType(Map<String, GraphQLType> dictionary) {
        List<GraphQLFieldDefinition> fieldDefinitions = getFieldDefinitions();

        return GraphQLObjectType
                .newObject()
                .name(getName())
                .fields(fieldDefinitions)
                .withInterfaces(getInterfaceDefinitions(dictionary))
                .build();
    }
}
