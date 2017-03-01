package graphql.sql.core.config;

import graphql.sql.core.config.impl.IdentityAbbreviationResolver;
import graphql.sql.core.config.impl.UnderscoreToCamelcaseNameProvider;
import org.junit.Test;

import static org.junit.Assert.*;

public class OracleNameProviderTest {

    @Test
    public void getEntityName() throws Exception {
        UnderscoreToCamelcaseNameProvider builder = new UnderscoreToCamelcaseNameProvider(new IdentityAbbreviationResolver());

        assertEquals("User", builder.getEntityName("USER"));
        assertEquals("UserData", builder.getEntityName("USER_DATA"));
        assertEquals("UserData", builder.getEntityName("_USER__DATA"));
    }

}