package graphql.sql.web;

import java.util.Collections;
import java.util.Map;

public class RelayRequest {
    private String query;

    private String operationName;

    private Map<String, Object> variables = Collections.emptyMap();

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}