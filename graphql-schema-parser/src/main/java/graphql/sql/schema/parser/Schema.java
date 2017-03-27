package graphql.sql.schema.parser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Schema {

    @Nonnull
    SchemaTypeReference getQueryType();

    @Nullable
    SchemaTypeReference getMutationType();
}
