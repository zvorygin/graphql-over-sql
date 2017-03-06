package graphql.sql.core.config.impl;

import graphql.sql.core.config.AbbreviationResolver;

import javax.annotation.Nonnull;
import java.util.Collections;

public class IdentityAbbreviationResolver implements AbbreviationResolver {
    @Nonnull
    @Override
    public Iterable<String> resolve(@Nonnull String abbreviation) {
        return Collections.singleton(abbreviation);
    }
}
