package graphql.sql.core.config.impl;

import graphql.sql.core.config.AbbreviationResolver;
import com.sun.istack.internal.NotNull;

import java.util.Collections;

public class IdentityAbbreviationResolver implements AbbreviationResolver {
    @NotNull
    @Override
    public Iterable<String> resolve(@NotNull String abbreviation) {
        return Collections.singleton(abbreviation);
    }
}
