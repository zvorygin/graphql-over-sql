package graphql.sql.schema.engine;

import com.google.common.collect.ImmutableMap;
import graphql.sql.schema.parser.SchemaField;
import graphql.sql.schema.parser.SchemaInterface;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
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
                    Function<SchemaField, String> getName = SchemaField::getName;
                    Function<SchemaField, Field> buildField = f -> new Field(f.getName(), f.getType());
                    Collection<SchemaField> fields = Collections.unmodifiableCollection(schemaInterface.getFields());
                    return fields.stream().collect(Collectors.toMap(getName, buildField));
                }

                @Override
                public String getName() {
                    return schemaInterface.getName();
                }
            };
        }
    }
}
