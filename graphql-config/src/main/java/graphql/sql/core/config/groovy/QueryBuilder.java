package graphql.sql.core.config.groovy;

import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityQuery;
import graphql.sql.core.config.groovy.context.ExecutionContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class QueryBuilder {

    private final ExecutionContext executionContext;

    public QueryBuilder(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public void call(Map<String, Object> parameters) {
        String name = Objects.requireNonNull((String) parameters.get("name"), "Query name not specified");
        List<EntityField> fields = Objects.requireNonNull((List<EntityField>) parameters.get("fields"),
                "Query fields were not specified");
        Entity entity = Objects.requireNonNull((Entity) parameters.get("entity"), "Entity not specified");

        if (fields.isEmpty()) {
            throw new IllegalStateException(String.format("Fields of query [%s] shouldn't be empty", name));
        }

        // TODO(dzvorygin) validate that field comes from appropriate entity somehow.

        executionContext.registerQuery(new EntityQuery(name, entity, fields));
    }
}
