package graphql.sql.schema;

import com.sun.istack.internal.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;

public interface CompositeType extends Type {
    @Nullable
    Field getField(String name);

    @NotNull
    Collection<? extends Field> getFields();
}
