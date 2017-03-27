package graphql.sql.schema.engine;

import com.google.common.collect.ImmutableMap;
import graphql.sql.schema.parser.SchemaField;
import graphql.sql.schema.parser.SchemaInterface;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class SchemaConfigProviderTest {
    @Test
    public void testSchemaConfigProvider() throws IOException {
        SchemaConfigProvider provider =
                new SchemaConfigProvider(new FileInputStream("src/test/data/classic_models.graphqls"),
                        new MapBasedInterfaceBuilderRegistry(ImmutableMap.of(
                                "sql", new TestInterfaceBuilder(),
                                "activeDirectory", new TestInterfaceBuilder())));
    }

    private static class TestInterfaceBuilder implements InterfaceBuilder {
        @Override
        public Interface buildInterface(SchemaInterface schemaInterface, Map<String, Interface> interfaces) {
            return new Interface() {
                @Override
                public Map<String, Field> getFields() {
                    return schemaInterface.getFields().stream().collect(Collectors.toMap(SchemaField::getName, f -> new Field(f.getName(), f.getType())));
                }

                @Override
                public String getName() {
                    return schemaInterface.getName();
                }
            };
        }
    }
}
