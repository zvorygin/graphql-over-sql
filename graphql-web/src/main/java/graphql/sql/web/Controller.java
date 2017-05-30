package graphql.sql.web;

import graphql.ExecutionResult;
import graphql.sql.engine.DocumentExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class Controller {

    private final DocumentExecutor documentExecutor;

    public Controller(DocumentExecutor documentExecutor) {
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
        if (!executionResult.getErrors().isEmpty()) {
            result.put("errors", executionResult.getErrors());
        }
        result.put("data", executionResult.getData());
        return result;
    }

    private ExecutionResult getExecutionResult(@RequestBody RelayRequest request) {
        String query = request.getQuery();

        String operationName = request.getOperationName();

        Map<String, Object> variables = request.getVariables();

        if (variables == null) {
            variables = Collections.emptyMap();
        }

        return documentExecutor.execute(query, operationName, variables);
    }

}
