package graphql.sql.schema.engine;

import java.util.Map;

public class MapBasedInterfaceBuilderRegistry implements InterfaceBuilderRegistry {
    private final Map<String, InterfaceBuilder> registry;

    public MapBasedInterfaceBuilderRegistry(Map<String, InterfaceBuilder> registry) {
        this.registry = registry;
    }

    @Override
    public InterfaceBuilder getInterfaceWrapper(String name) {
        return registry.get(name);
    }
}
