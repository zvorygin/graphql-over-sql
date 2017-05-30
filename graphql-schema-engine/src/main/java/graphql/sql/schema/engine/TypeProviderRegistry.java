package graphql.sql.schema.engine;

@FunctionalInterface
public interface TypeProviderRegistry {
    TypeProvider getTypeBuilder(String name);
}
