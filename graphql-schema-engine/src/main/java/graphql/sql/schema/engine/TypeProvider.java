package graphql.sql.schema.engine;

import graphql.sql.core.config.Interface;
import graphql.sql.core.config.ObjectType;
import graphql.sql.schema.parser.SchemaInterface;
import graphql.sql.schema.parser.SchemaObjectType;

import java.util.Map;

public interface TypeProvider {
    Interface buildInterface(SchemaInterface schemaInterface, Map<String, Interface> interfaces);

    ObjectType buildObjectType(SchemaObjectType objectType, Map<String, Interface> interfaces, boolean isQueryType);
}
