package graphql.sql.engine.sql.introspect;

import graphql.sql.core.config.impl.IdentityAbbreviationResolver;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class IdentityAbbreviationResolverTest {
    @Test
    public void resolve() {
        IdentityAbbreviationResolver resolver = new IdentityAbbreviationResolver();
        assertThat(resolver.resolve("test"), Matchers.contains("test"));
    }

}