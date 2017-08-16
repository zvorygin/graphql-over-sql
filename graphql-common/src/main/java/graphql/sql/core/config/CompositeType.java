package graphql.sql.core.config;

import graphql.sql.core.config.domain.Config;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public interface CompositeType extends Type {
    Map<String, Field> getFields();

    @Nonnull
    default Field getField(String name) {
        return Objects.requireNonNull(
                getFields().get(name),
                () -> String.format("Field [%s] not found in type [%s].", name, getName()));
    }

    QueryNode buildQueryNode(Config config);

    Collection<Interface> getInterfaces();
}
