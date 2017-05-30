package graphql.sql.engine;

import graphql.GraphQLError;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DocumentExecutionException extends RuntimeException {
    @Nonnull
    private final List<GraphQLError> validationErrors;

    public DocumentExecutionException(@Nonnull List<? extends GraphQLError> validationErrors) {
        this(validationErrors, null);
    }

    public DocumentExecutionException(@Nonnull List<? extends GraphQLError> validationErrors, Exception e) {
        super(e);
        if (validationErrors.isEmpty()) {
            throw new IllegalStateException("Validation errors should not be empty");
        }
        this.validationErrors = new ArrayList<>(validationErrors);
    }

    @Nonnull
    public List<GraphQLError> getValidationErrors() {
        return validationErrors;
    }
}
