package graphql.sql.schema.parser;

public interface SchemaField {

    String getName();

    SchemaTypeReference getType();
}
