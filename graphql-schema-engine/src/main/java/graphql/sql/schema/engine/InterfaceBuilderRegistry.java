package graphql.sql.schema.engine;

@FunctionalInterface
public interface InterfaceBuilderRegistry {
    InterfaceBuilder getInterfaceWrapper(String name);
}
