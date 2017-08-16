package graphql.sql.engine;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategy;
import graphql.execution.FieldCollector;
import graphql.language.OperationDefinition;
import graphql.sql.core.config.CompositeType;
import graphql.sql.core.config.TypeExecutor;
import graphql.sql.core.config.GraphQLTypesProvider;
import graphql.sql.core.config.QueryNode;
import graphql.sql.core.config.domain.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class OperationExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationExecutor.class);
    private final GraphQLTypesProvider typesProvider;
    private final Config config;
    private final QueryGraphBuilder queryGraphBuilder;

    private final FieldCollector fieldCollector = new FieldCollector();
    private final LoadingCache<DocumentContext, Cache<OperationKey, TypeExecutor>> cache;
    private final long maxOperationsPerDocument;


    public OperationExecutor(GraphQLTypesProvider typesProvider,
                             Config config,
                             QueryGraphBuilder queryGraphBuilder,
                             long maxOperationsPerDocument,
                             int maximumCacheSize) {
        this.typesProvider = typesProvider;
        this.config = config;
        this.queryGraphBuilder = queryGraphBuilder;
        this.maxOperationsPerDocument = maxOperationsPerDocument;
        cache = CacheBuilder.newBuilder()
                .maximumSize(maximumCacheSize)
                .build(CacheLoader.from(this::buildOperationCache));
    }

    public ExecutionResult execute(DocumentContext documentContext,
                                   OperationDefinition operationDefinition,
                                   Map<String, Object> variables) {

        OperationKey operationKey = buildOperationKey(documentContext, operationDefinition, variables);

        Cache<OperationKey, TypeExecutor> documentOperationCache = cache.getUnchecked(documentContext);

        TypeExecutor queryFields;

        try {
            queryFields = documentOperationCache.get(operationKey,
                    () -> buildOperationExecutor(documentContext, operationDefinition, variables));
        } catch (ExecutionException e) {
            LOGGER.error("Failed to build query fields executors", e);
            return new ExecutionResultImpl(Collections.singletonList(new ExceptionWhileDataFetching(e.getCause())));
        }

        Object result = queryFields.execute(variables);

        return new ExecutionResultImpl(result, Collections.emptyList());
    }

    private TypeExecutor buildOperationExecutor(DocumentContext documentContext,
                                                OperationDefinition operationDefinition,
                                                Map<String, Object> variables) {

        if (operationDefinition.getOperation() != OperationDefinition.Operation.QUERY) {
            throw new IllegalStateException("Only QUERY operations are currently supported.");
        }

        CompositeType type = config.getType(config.getQueryTypeName());

        ExecutionContext executionContext = buildExecutionContext(documentContext, operationDefinition, variables, null);

        QueryNode queryNode = queryGraphBuilder.build(type, operationDefinition.getSelectionSet(), executionContext);

        return queryNode.buildExecutor(executionContext);
    }

    @Nonnull
    private ExecutionContext buildExecutionContext(DocumentContext documentContext,
                                                   OperationDefinition operationDefinition,
                                                   Map<String, Object> variables,
                                                   ExecutionStrategy queryStrategy) {
        return new ExecutionContext(
                typesProvider.getSchema(),
                queryStrategy,
                null,
                documentContext.getFragmentsByName(),
                operationDefinition,
                variables,
                null);
    }
/*

    @Nonnull
    private TypeExecutor getFieldExecutor(ExecutionContext executionContext, Field queryDocumentField) {

        String queryTypeName = config.getQueryTypeName();

        CompositeType queryType = config.getType(queryTypeName);
        graphql.sql.core.config.Field schemaField = queryType.getField(queryDocumentField.getName());
        TypeReference typeReference = schemaField.getTypeReference();

        CompositeType resultType = config.getType(typeReference);

        QueryNode rootNode = queryGraphBuilder.build(resultType, queryDocumentField, executionContext);

        return rootNode.buildExecutor(schemaField, executionContext);
    }
*/

    public void onDocumentContextEvicted(DocumentContext value) {
        cache.invalidate(value);
    }

    @Nonnull
    private <K, V> Cache<K, V> buildOperationCache() {
        return CacheBuilder.newBuilder().maximumSize(maxOperationsPerDocument).build();
    }

    private OperationKey buildOperationKey(DocumentContext documentContext,
                                           OperationDefinition operationDefinition,
                                           Map<String, Object> variables) {
        Set<String> queryAffectingFlags = documentContext.getQueryAffectingFlags(operationDefinition);
        boolean[] flagValues = new boolean[queryAffectingFlags.size()];
        int i = 0;
        for (String flag : queryAffectingFlags) {
            flagValues[i++] = (boolean) variables.get(flag);
        }

        return new OperationKey(operationDefinition, flagValues);
    }

}
