package graphql.sql.core;

import graphql.ExecutionResult;
import graphql.execution.ExecutionContext;
import graphql.execution.SimpleExecutionStrategy;
import graphql.language.Field;
import graphql.schema.GraphQLObjectType;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.querygraph.QueryGraphBuilder;
import graphql.sql.core.sqlquery.SqlQueryBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
        this.graphBuilder = new QueryGraphBuilder(config, typesProvider);
        this.sqlQueryBuilder = new SqlQueryBuilder(config, typesProvider, graphBuilder);
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
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        System.out.println("----");
                        for (int i = 0; i < columnCount; i++) {
                            System.out.print(metaData.getColumnName(i + 1));
                            System.out.print(" ; ");
                        }
                        System.out.println();
                        System.out.println("----");
                        while (rs.next()) {
                            for (int i = 0; i < columnCount; i++) {
                                System.out.print(rs.getString(i + 1));
                                System.out.print(" ; ");
                            }
                            System.out.println();
                        }
                        System.out.println();
                        System.out.println("----");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return super.execute(executionContext, parentType, source, fields);
        }

        //return new ExecutionResultImpl(result, Collections.emptyList());

    }
}
