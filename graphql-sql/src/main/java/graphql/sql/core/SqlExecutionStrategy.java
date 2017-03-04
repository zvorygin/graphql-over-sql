package graphql.sql.core;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.execution.ExecutionContext;
import graphql.execution.SimpleExecutionStrategy;
import graphql.language.Field;
import graphql.schema.GraphQLObjectType;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.extractor.*;
import graphql.sql.core.extractor.ResultNode;
import graphql.sql.core.querygraph.QueryGraphBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SqlExecutionStrategy extends SimpleExecutionStrategy {

    private final boolean processingSystemField = false;
    private final DataSource dataSource;
    private final QueryGraphBuilder graphBuilder;
    private final SqlQueryBuilder sqlQueryBuilder;
    private Config config;
    private GraphQLTypesProvider typesProvider;

    public SqlExecutionStrategy(Config config, GraphQLTypesProvider typesProvider, DataSource dataSource) {
        this.dataSource = dataSource;
        graphBuilder = new QueryGraphBuilder(config, typesProvider);
        sqlQueryBuilder = new SqlQueryBuilder(config, typesProvider, graphBuilder);
        this.config = config;
        this.typesProvider = typesProvider;
    }

    @Override
    public ExecutionResult execute(ExecutionContext executionContext, GraphQLObjectType parentType, Object
            source, Map<String, List<Field>> fields) {

        try {
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();

            for (Map.Entry<String, List<Field>> queryRoot : fields.entrySet()) {
                try (Connection conn = dataSource.getConnection();
                     PreparedStatement ps = sqlQueryBuilder.createPreparedStatement(conn, queryRoot, executionContext)) {
                    try (ResultSet rs = ps.executeQuery()) {
                        NodeExtractor extractor = sqlQueryBuilder.getExtractor();
                        Map<ArrayKey, ResultNode> response = new LinkedHashMap<>();
                        while (rs.next()) {
                            ArrayKey key = extractor.getKey(rs);
                            extractor.extractTo(rs, response, key);
                        }

                        result.put(queryRoot.getKey(), response.values());
                    }
                }
            }

            return new ExecutionResultImpl(result, Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
            return super.execute(executionContext, parentType, source, fields);
        }
    }
}
