package graphql.sql.core;

import com.google.common.cache.*;
import graphql.ExecutionResult;
import graphql.InvalidSyntaxError;
import graphql.execution.FieldCollector;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.language.SourceLocation;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DocumentExecutor {
    private final Parser parser = new Parser();
    private final FieldCollector collector = new FieldCollector();
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
        DocumentContext document = documentCache.getUnchecked(documentStr);
        return execute(document, operationName, variables);
    }

    public ExecutionResult execute(@Nonnull DocumentContext document,
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
            throw new DocumentExecutionException(Collections.singletonList(invalidSyntaxError));
        }

        Validator validator = new Validator();
        List<ValidationError> validationErrors = validator.validateDocument(graphQLSchema, document);
        if (validationErrors.size() > 0) {
            throw new DocumentExecutionException(validationErrors);
        }

        return new DocumentContext(document);
    }
}
