package graphql.sql.schema.parser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface SchemaCompositeType extends SchemaType {
    @Nullable
    SchemaField getField(String name);

    @Nonnull
    Collection<? extends SchemaField> getFields();
}
