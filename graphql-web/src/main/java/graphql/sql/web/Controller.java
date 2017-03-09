package graphql.sql.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.language.Value;
import graphql.sql.core.DocumentExecutor;
import graphql.sql.core.FieldExecutionStrategy;
import graphql.sql.core.config.ConfigProvider;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.NameProvider;
import graphql.sql.core.config.domain.Config;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class Controller {

    private final GraphQL graphql;
    private final Config config;
    private final GraphQLTypesProvider typesProvider;

    private final DataSource dataSource;
    private final ObjectMapper mapper = new ObjectMapper();

    private final DocumentExecutor documentExecutor;

    public Controller(ConfigProvider configProvider, DataSource dataSource, NameProvider nameProvider, DocumentExecutor documentExecutor) {
        this.dataSource = dataSource;
        config = configProvider.getConfig();
        typesProvider = new GraphQLTypesProvider(config, nameProvider);
        graphql = new GraphQL(typesProvider.getSchema(), new FieldExecutionStrategy());
        this.documentExecutor = documentExecutor;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public Object executeOperation() throws IOException {
        return "Hello World";
    }

    @RequestMapping(value = "/",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object executeOperation(@RequestBody RelayRequest request) throws IOException {
        ExecutionResult executionResult = getExecutionResult(request);

        Map<String, Object> result = new LinkedHashMap<>();
        if (executionResult.getErrors().size() > 0) {
            result.put("errors", executionResult.getErrors());
        }
        result.put("data", executionResult.getData());
        return result;
    }

    private ExecutionResult getExecutionResult(@RequestBody RelayRequest request) {
        String query = request.getQuery();

        String operationName = request.getOperationName();

        Map<String, Object> variables = request.getVariables();

        return documentExecutor.execute(query, operationName, variables);
    }

}
