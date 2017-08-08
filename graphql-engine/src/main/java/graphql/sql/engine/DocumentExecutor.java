package graphql.sql.engine;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.InvalidSyntaxError;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.language.SourceLocation;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DocumentExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentExecutor.class);
    private final Parser parser = new Parser();
    private final GraphQLSchema graphQLSchema;
    private final OperationExecutor operationExecutor;
    private final LoadingCache<String, DocumentContext> documentCache;

    public DocumentExecutor(GraphQLSchema graphQLSchema, OperationExecutor operationExecutor, long maximumSize) {
        this.graphQLSchema = graphQLSchema;
        this.operationExecutor = operationExecutor;
        documentCache = CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .removalListener(this::onDocumentContextEvicted)
                .build(CacheLoader.from(this::loadDocumentContext));
    }

    public ExecutionResult execute(@Nonnull String documentStr,
                                   @Nullable String operationName,
                                   @Nonnull Map<String, Object> variables) {
        DocumentContext document;
        try {
            document = documentCache.get(documentStr);
        } catch (ExecutionException e) {
            LOGGER.error(String.format("Failed to compile document for operation [%s]", operationName), e);
            DocumentExecutionException cause = (DocumentExecutionException) e.getCause();
            return new ExecutionResultImpl(cause.getValidationErrors());
        }

        try {
            return execute(document, operationName, variables);
        } catch (Exception e) {
            LOGGER.error(String.format("Failed to execute operation [%s]", operationName), e);
            return new ExecutionResultImpl(Collections.singletonList(new ExceptionWhileDataFetching(e)));
        }
    }

    private ExecutionResult execute(@Nonnull DocumentContext document,
                                   @Nullable String operationName,
                                   @Nonnull Map<String, Object> variables) {
        OperationDefinition operation = document.getOperation(operationName);
        return operationExecutor.execute(document, operation, variables);
    }

    private void onDocumentContextEvicted(RemovalNotification<String, DocumentContext> notification) {
        operationExecutor.onDocumentContextEvicted(notification.getValue());
    }

    private DocumentContext loadDocumentContext(String documentStr) {
        Document document;
        try {
            document = parser.parseDocument(documentStr);
        } catch (ParseCancellationException e) {
            RecognitionException recognitionException = (RecognitionException) e.getCause();
            SourceLocation sourceLocation = new SourceLocation(
                    recognitionException.getOffendingToken().getLine(),
                    recognitionException.getOffendingToken().getCharPositionInLine());
            InvalidSyntaxError invalidSyntaxError = new InvalidSyntaxError(sourceLocation);
            throw new DocumentExecutionException(Collections.singletonList(invalidSyntaxError), e);
        }

        Validator validator = new Validator();
        List<ValidationError> validationErrors = validator.validateDocument(graphQLSchema, document);
        if (!validationErrors.isEmpty()) {
            if (LOGGER.isInfoEnabled()) {
                for (ValidationError validationError : validationErrors) {
                    LOGGER.info(validationError.toString());
                }
            }
            throw new DocumentExecutionException(validationErrors);
        }

        return new DocumentContext(document);
    }
}
