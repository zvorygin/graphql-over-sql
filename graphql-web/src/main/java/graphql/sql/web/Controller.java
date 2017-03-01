package graphql.sql.web;

import graphql.sql.core.SqlExecutionStrategy;
import graphql.sql.core.config.ConfigProvider;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.NameProvider;
import graphql.sql.core.config.domain.Config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

@RestController
public class Controller {

    private final GraphQL graphql;
    private final Config config;
    private final GraphQLTypesProvider typesProvider;

    private DataSource dataSource;
    private final ObjectMapper mapper = new ObjectMapper();

    public Controller(ConfigProvider configProvider, DataSource dataSource, NameProvider nameProvider) {
        this.dataSource = dataSource;
        config = configProvider.getConfig();
        typesProvider = new GraphQLTypesProvider(config, nameProvider);

        Collection<GraphQLInterfaceType> interfaceTypes = typesProvider.getInterfaceTypes();
        Collection<GraphQLObjectType> objectTypes = typesProvider.getObjectTypes();

        Set<GraphQLType> dictionary = new HashSet<>();
        dictionary.addAll(interfaceTypes);
        dictionary.addAll(objectTypes);

        GraphQLSchema schema =  GraphQLSchema.newSchema().query(typesProvider.getQueryObject()).build(dictionary);
        graphql = new GraphQL(schema, new SqlExecutionStrategy(config, typesProvider, dataSource));
    }

    @RequestMapping(value = "/",
                    method = RequestMethod.POST,
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object executeOperation(@RequestBody JsonNode body) throws IOException {
        String query = body.get("query").asText();
        String variablesStr = body.get("variables").asText();

        Map variables = mapper.readValue(variablesStr, Map.class);
        if (variables == null) {
            variables = Collections.emptyMap();
        }

        ExecutionResult executionResult = graphql.execute(query, (Object) null, variables);
        Map<String, Object> result = new LinkedHashMap<>();
        if (executionResult.getErrors().size() > 0) {
            result.put("errors", executionResult.getErrors());
        }
        result.put("data", executionResult.getData());
        return result;
    }

}
