package graphql.sql.schema.parser;

import java.util.List;

public interface SchemaObjectType extends SchemaCompositeType {
    List<String> getInterfaces();
}
