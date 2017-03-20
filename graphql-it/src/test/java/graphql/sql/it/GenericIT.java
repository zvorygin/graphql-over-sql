package graphql.sql.it;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import graphql.sql.core.TestUtil;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class GenericIT {

    private static final Path TEST_DATA = Paths.get("test-data");
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static URI URI;
    private static Server SERVER;

    private final Path documentPath;
    private final Path requestPath;
    private final Path expectedResponsePath;

    public GenericIT(String testName) {
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
                .filter(name -> name.endsWith(".document.graphql"))
                .map(name -> name.substring(0, name.length() - ".document.graphql".length()))
                .collect(Collectors.toList());
    }

    @Test
    public void testExecution() throws IOException {
        Map<String, Object> request = buildRequest();
        request.put("query", new String(Files.readAllBytes(documentPath)));
        ResponseEntity<JsonNode> entity = REST_TEMPLATE
                .exchange(URI, HttpMethod.POST, new HttpEntity<>(request), JsonNode.class);

        Assert.assertEquals(entity.getStatusCode(), HttpStatus.OK);

        JsonNode response = entity.getBody();
        JsonNode expected = TestUtil.readResource(expectedResponsePath.toFile());
        TestUtil.sortArrays(response);
        TestUtil.assertEquals(expected, response);
    }

    private Map<String, Object> buildRequest() throws IOException {
        if (Files.isRegularFile(requestPath)) {
            TypeReference<Map<String, Object>> typeReference =
                    new TypeReference<Map<String, Object>>() {
                    };
            return TestUtil.readResource(requestPath.toFile(), typeReference);
        }
        return new HashMap<>();
    }
}
