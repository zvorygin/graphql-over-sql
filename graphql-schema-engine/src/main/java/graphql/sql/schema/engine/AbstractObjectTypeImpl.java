package graphql.sql.schema.engine;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.ObjectType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public abstract class AbstractObjectTypeImpl<F extends Field> extends AbstractCompositeType<F> implements ObjectType {
    public AbstractObjectTypeImpl(@Nonnull String name,
                                  @Nonnull Map<String, F> fields,
                                  @Nonnull List<Interface> interfaces) {
        super(name, fields, interfaces);
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
