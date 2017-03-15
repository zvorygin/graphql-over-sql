package graphql.sql.core.config;

import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.groovy.GroovyConfigProvider;
import graphql.sql.core.config.impl.IdentityAbbreviationResolver;
import graphql.sql.core.config.impl.UnderscoreToCamelcaseNameProvider;
import graphql.sql.core.introspect.JDBCIntrospector;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.Collection;

public class GroovyConfigProviderTest {

    private static JDBCIntrospector INTROSPECTOR;
    private static EmbeddedDatabase EMBEDDED_DATABASE;

    @BeforeClass
    public static void setUp() throws Exception {
        EMBEDDED_DATABASE = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:test.sql")
                .build();
        INTROSPECTOR = new JDBCIntrospector(EMBEDDED_DATABASE);
    }

    @AfterClass
    public static void tearDown() {
        EMBEDDED_DATABASE.shutdown();
        EMBEDDED_DATABASE = null;
        INTROSPECTOR = null;
    }

    @Test
    public void testEntityCreated() {
        GroovyConfigProvider configProvider =
                new GroovyConfigProvider(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream("test.graphqlsql"),
                        new UnderscoreToCamelcaseNameProvider(new IdentityAbbreviationResolver()),
                        INTROSPECTOR);

        Config config = configProvider.getConfig();
        Collection<Entity> entities = config.getEntities();

        Assert.assertArrayEquals(new String[]{"Message", "Moderator", "Thread", "User"},
                entities.stream().map(Entity::getEntityName).sorted().toArray(String[]::new));
    }
}
