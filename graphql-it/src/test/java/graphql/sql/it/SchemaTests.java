package graphql.sql.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import graphql.sql.web.RelayRequest;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class SchemaTests {

    private static final Path TEST_DATA = Paths.get("test-data");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final ObjectWriter WRITER = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
    private static final JsonComparator JSON_COMPARATOR = new JsonComparator(WRITER);
    private static URI URI;
    private static Server SERVER;

    private final Path documentPath;
    private final Path requestPath;
    private final Path expectedResponsePath;

    public SchemaTests(String testName) {
        documentPath = TEST_DATA.resolve(testName + ".document.graphql");
        requestPath = TEST_DATA.resolve(testName + ".request.json");
        expectedResponsePath = TEST_DATA.resolve(testName + ".response.expected.json");
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        SERVER = new Server();

        ServerConnector connector = new ServerConnector(SERVER);
        SERVER.setConnectors(new Connector[]{connector});

        XmlWebApplicationContext applicationContext = new XmlWebApplicationContext();
        applicationContext.setConfigLocation("classpath:spring-test-context.xml");


        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.setContextConfigLocation("classpath:spring-test-webapp-context.xml");

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/graphql");

        webAppContext.addEventListener(new ContextLoaderListener(applicationContext));
        webAppContext.addServlet(new ServletHolder(dispatcherServlet), "/");
        webAppContext.setResourceBase("");

        SERVER.setHandler(webAppContext);
        SERVER.start();

        int port = connector.getLocalPort();
        URI = new URL("http", "localhost", port, "/graphql/").toURI();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        SERVER.stop();
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object> generateParameters() throws IOException {
        return Files.walk(TEST_DATA)
                .map(TEST_DATA::relativize)
                .map(Path::toString)
                .filter(name -> name.endsWith(".request.json"))
                .map(name -> name.substring(0, name.length() - ".request.json".length()))
                .collect(Collectors.toList());
    }

    @Test
    public void testExecution() throws IOException {
        RelayRequest request = buildRequest();
        request.setQuery(new String(Files.readAllBytes(documentPath)));
        ResponseEntity<JsonNode> entity = REST_TEMPLATE
                .exchange(URI, HttpMethod.POST, new HttpEntity<>(request), JsonNode.class);

        Assert.assertEquals(entity.getStatusCode(), HttpStatus.OK);

        JsonNode response = entity.getBody();
        JsonNode expected = OBJECT_MAPPER.readTree(expectedResponsePath.toFile());
        sortArrays(response);
        compareNodes(expected, response);
    }

    private void sortArrays(JsonNode node) {
        if (node instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) node;
            ArrayList<JsonNode> children = Lists.newArrayList(arrayNode.iterator());
            children.iterator().forEachRemaining(this::sortArrays);
            children.sort(JSON_COMPARATOR);
            for (int i = 0; i < children.size(); i++) {
                arrayNode.set(i, children.get(i));
            }
        } else if (node instanceof ObjectNode) {
            node.iterator().forEachRemaining(this::sortArrays);
        }
    }

    private static void compareNodes(JsonNode expected, JsonNode response) throws JsonProcessingException {
        Assert.assertEquals(WRITER.writeValueAsString(expected), WRITER.writeValueAsString(response));
    }

    private RelayRequest buildRequest() throws IOException {
        if (Files.isRegularFile(requestPath)) {
            return OBJECT_MAPPER.readValue(Files.readAllBytes(requestPath), RelayRequest.class);
        }
        return new RelayRequest();
    }
}
