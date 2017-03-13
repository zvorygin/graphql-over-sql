package graphql.sql.core;

import graphql.language.OperationDefinition;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class OperationKey {
    @Nonnull
    private final OperationDefinition operationDefinition;

    @Nonnull
    private final boolean[] queryAffectingFlags;

    private final int hashCode;

    public OperationKey(@Nonnull OperationDefinition operationDefinition, @Nonnull boolean[] queryAffectingFlags) {
        this.operationDefinition = operationDefinition;
        this.queryAffectingFlags = queryAffectingFlags;
        hashCode = 31 * operationDefinition.hashCode()
                + Arrays.hashCode(queryAffectingFlags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OperationKey that = (OperationKey) o;

        return operationDefinition.equals(that.operationDefinition) &&
                Arrays.equals(queryAffectingFlags, that.queryAffectingFlags);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
