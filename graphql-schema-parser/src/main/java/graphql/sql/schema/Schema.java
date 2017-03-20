package graphql.sql.schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Schema {

    @Nonnull
    TypeReference getQueryType();

    @Nullable
    TypeReference getMutationType();
}
