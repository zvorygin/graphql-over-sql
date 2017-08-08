package graphql.sql.schema.engine;

import java.util.Map;

public class MapBasedTypeProviderRegistry implements TypeProviderRegistry {
    private final TypeProvider defaultTypeProvider;
    private final Map<String, TypeProvider> registry;

    public MapBasedTypeProviderRegistry(TypeProvider defaultTypeProvider, Map<String, TypeProvider> registry) {
        this.defaultTypeProvider = defaultTypeProvider;
        this.registry = registry;
    }

    @Override
    public TypeProvider getTypeBuilder(String name) {
        return registry.get(name);
    }

    @Override
    public TypeProvider getDefaultTypeProvider() {
        return defaultTypeProvider;
    }
}
