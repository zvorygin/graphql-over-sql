package graphql.sql.schema.engine;

import graphql.sql.core.config.ConfigProvider;
import graphql.sql.core.config.domain.Config;
import graphql.sql.schema.parser.Parser;
import graphql.sql.schema.parser.SchemaDocument;

import java.io.IOException;
import java.io.InputStream;

public class SchemaConfigProvider implements ConfigProvider {
    private static final Parser SCHEMA_PARSER = new Parser();
    private final Config config;

    public SchemaConfigProvider(InputStream schemaStream) throws IOException {
            SchemaDocument schemaDocument = SCHEMA_PARSER.parse(schemaStream);
            config = buildConfig(schemaDocument);

    }

    @Override
    public Config getConfig() {
        return config;
    }

    private Config buildConfig(SchemaDocument schemaDocument) {
        return null;
    }
}
