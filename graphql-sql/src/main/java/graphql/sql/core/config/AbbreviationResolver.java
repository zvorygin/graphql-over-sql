package graphql.sql.core.config;

public interface AbbreviationResolver {
    Iterable<String> resolve(String abbreviation);
}
