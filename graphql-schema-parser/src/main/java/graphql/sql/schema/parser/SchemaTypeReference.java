package graphql.sql.schema.parser;

public interface SchemaTypeReference {
    default boolean isCollection() {
        return false;
    }

    default boolean isNonNull() {
        return false;
    }

    default SchemaTypeReference getWrappedType() {
        return null;
    }

    default String getTypeName() {
        return null;
    }
}
