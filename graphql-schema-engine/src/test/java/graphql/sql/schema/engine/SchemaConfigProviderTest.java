package graphql.sql.schema.engine;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

public class SchemaConfigProviderTest {
    @Test
    public void testSchemaConfigProvider() throws IOException {
        SchemaConfigProvider provider = new SchemaConfigProvider(new FileInputStream("src/test/data/classic_models.graphqls"));
    }
}
