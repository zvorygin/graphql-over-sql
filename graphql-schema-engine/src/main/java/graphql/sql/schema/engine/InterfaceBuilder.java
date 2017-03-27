package graphql.sql.schema.engine;

import graphql.sql.schema.parser.SchemaInterface;

import java.util.Map;

public interface InterfaceBuilder {
    Interface buildInterface(SchemaInterface schemaInterface, Map<String, Interface> interfaces);
}
