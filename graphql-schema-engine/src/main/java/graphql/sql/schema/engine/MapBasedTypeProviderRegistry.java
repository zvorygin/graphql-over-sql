package graphql.sql.schema.engine;

import java.util.Map;

public class MapBasedTypeProviderRegistry implements TypeProviderRegistry {
    private final Map<String, TypeProvider> registry;

    public MapBasedTypeProviderRegistry(Map<String, TypeProvider> registry) {
        this.registry = registry;
    }

    @Override
    public TypeProvider getTypeBuilder(String name) {
        return registry.get(name);
    }
}
