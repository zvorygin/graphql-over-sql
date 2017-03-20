package graphql.sql.schema;

import graphql.sql.core.TestUtil;
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
public class NegativeParserTest {
    private static final Path TEST_DATA = Paths.get("src/test/negative");
    private static final String SCHEMA_SUFFIX = ".graphqls";
    private final File input;

    public NegativeParserTest(String resourcePath) {
        input = TEST_DATA.resolve(resourcePath + SCHEMA_SUFFIX).toFile();
    }

    @Test(expected = SchemaParserException.class)
    public void parse() throws Exception {
        Parser parser = new Parser();

        try (InputStream is = new FileInputStream(input)) {
            TestUtil.valueToTree(parser.parse(is));
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
}