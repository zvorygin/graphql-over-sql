package graphql.sql.core.config.groovy;

import graphql.sql.core.config.ConfigProvider;
import graphql.sql.core.config.ConfigurationException;
import graphql.sql.core.config.NameProvider;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.groovy.context.ExecutionContext;
import graphql.sql.core.introspect.DatabaseIntrospector;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;

public class GroovyConfigProvider implements ConfigProvider {

    public static final String DEFAULT_CATALOG_NAME = "PUBLIC";
    private final Resource configResource;
    private final NameProvider nameProvider;
    private final DatabaseIntrospector databaseIntrospector;

    public GroovyConfigProvider(Resource configResource,
                                NameProvider nameProvider,
                                DatabaseIntrospector databaseIntrospector) {
        this.configResource = configResource;
        this.nameProvider = nameProvider;
        this.databaseIntrospector = databaseIntrospector;
    }

    @Override
    public Config getConfig() {
        Binding binding = new Binding();

        GroovyShell gs = new GroovyShell(binding);

        ExecutionContext executionContext = new ExecutionContext(nameProvider, databaseIntrospector);
        executionContext.setCatalogName(DEFAULT_CATALOG_NAME);
        binding.setProperty("entity", new EntityBuilder(executionContext));
        binding.setProperty("schema", new SchemaSetter(executionContext));
        binding.setProperty("query", new QueryBuilder(executionContext));

        try (InputStreamReader reader = new InputStreamReader(configResource.getInputStream())) {
            gs.evaluate(reader);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }

        return executionContext.buildConfig();
    }

}
