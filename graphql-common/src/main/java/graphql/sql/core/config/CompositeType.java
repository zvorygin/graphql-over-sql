package graphql.sql.core.config;

import java.util.Collection;
import java.util.Map;

public interface CompositeType extends Type {
    Map<String, Field> getFields();

    default Field getField(String name) {
        return getFields().get(name);
    }

    QueryNode buildQueryNode();

    Collection<Interface> getInterfaces();
}
