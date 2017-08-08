package graphql.sql.schema.engine;

public interface TypeProviderRegistry {
    TypeProvider getTypeBuilder(String name);
    TypeProvider getDefaultTypeProvider();
}
