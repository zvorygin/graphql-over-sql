package graphql.sql.schema;

public interface TypeReference {
    default boolean isCollection() {
        return false;
    }

    default boolean isNonNull() {
        return false;
    }

    default TypeReference getWrappedType() {
        return null;
    }

    default String getTypeName() {
        return null;
    }
}
