package graphql.sql.schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface CompositeType extends Type {
    @Nullable
    Field getField(String name);

    @Nonnull
    Collection<? extends Field> getFields();
}
