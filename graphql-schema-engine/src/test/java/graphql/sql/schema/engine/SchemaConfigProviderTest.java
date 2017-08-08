package graphql.sql.schema.engine;

import com.google.common.collect.ImmutableMap;
import graphql.execution.ExecutionContext;
import graphql.language.SelectionSet;
import graphql.schema.GraphQLType;
import graphql.sql.core.config.Field;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.ObjectType;
import graphql.sql.core.config.QueryNode;
import graphql.sql.core.config.domain.Config;
import graphql.sql.schema.engine.querygraph.GenericQueryNode;
import graphql.sql.schema.parser.SchemaField;
import graphql.sql.schema.parser.SchemaInterface;
import graphql.sql.schema.parser.SchemaObjectType;
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
                        new MapBasedTypeProviderRegistry(new DefaultTypeProvider(), ImmutableMap.of(
                                "sql", new TestTypeProvider(),
                                "activeDirectory", new TestTypeProvider())));
    }

    private static class TestTypeProvider implements TypeProvider {
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
                public QueryNode buildQueryNode(Config config, SelectionSet selectionSet, ExecutionContext executionContext) {
                    return new GenericQueryNode(config, this);
                }

                @Override
                public Collection<Interface> getInterfaces() {
                    return Collections.emptyList();
                }

                @Override
                public String getName() {
                    return schemaInterface.getName();
                }

                @Override
                public GraphQLType getGraphQLType(Map<String, GraphQLType> dictionary) {
                    return null;
                }
            };
        }

        @Override
        public ObjectType buildObjectType(SchemaObjectType objectType, Map<String, Interface> interfaces, boolean isQueryType) {
            return null;
        }
    }
}
