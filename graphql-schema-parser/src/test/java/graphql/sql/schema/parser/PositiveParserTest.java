package graphql.sql.schema.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import graphql.sql.core.TestUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class PositiveParserTest {
    private static final Path TEST_DATA = Paths.get("src/test/positive");
    private static final String SCHEMA_SUFFIX = ".graphqls";
    private static final String EXPECTED_SUFFIX = ".expected.json";
    private final File input;
    private final File expected;

    @BeforeClass
    public static void beforeClass() {
        TestUtil.addMixIn(Type.class, NodeMixIn.class);
        TestUtil.addMixIn(TypeReference.class, NodeMixIn.class);
        TestUtil.addMixIn(Schema.class, NodeMixIn.class);
    }

    public PositiveParserTest(String resourcePath) {
        input = TEST_DATA.resolve(resourcePath + SCHEMA_SUFFIX).toFile();
        expected = TEST_DATA.resolve(resourcePath + EXPECTED_SUFFIX).toFile();
    }

    @Test
    public void parse() throws Exception {
        Parser parser = new Parser();

        try (InputStream is = new FileInputStream(input)) {
            SchemaDocument document = parser.parse(is);
            JsonNode resultTree = TestUtil.valueToTree(document);
            JsonNode expectedTree = TestUtil.readResource(expected);
            TestUtil.assertEquals(expectedTree, resultTree);
        }
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object> generateParameters() throws IOException {
        return Files.walk(TEST_DATA)
                .map(TEST_DATA::relativize)
                .map(Path::toString)
                .filter(name -> name.endsWith(SCHEMA_SUFFIX))
                .map(name -> name.substring(0, name.length() - SCHEMA_SUFFIX.length()))
                .collect(Collectors.toList());
    }

    @JsonPropertyOrder(alphabetic = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public interface NodeMixIn {
        @JsonIgnore
        Object getLocation();
    }
}