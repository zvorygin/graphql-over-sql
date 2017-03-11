package graphql.sql.core;

import graphql.GraphQLError;

import javax.annotation.Nonnull;
import java.util.List;

public class DocumentExecutionException extends RuntimeException {
    @Nonnull
    private final List<? extends GraphQLError> validationErrors;

    public DocumentExecutionException(@Nonnull List<? extends GraphQLError> validationErrors) {
        this(validationErrors, null);
    }

    public DocumentExecutionException(@Nonnull List<? extends GraphQLError> validationErrors, Exception e) {
        super(e);
        if (validationErrors.isEmpty()) {
            throw new IllegalStateException("Validation errors should not be empty");
        }
        this.validationErrors = validationErrors;
    }

    @Nonnull
    public List<? extends GraphQLError> getValidationErrors() {
        return validationErrors;
    }
}
