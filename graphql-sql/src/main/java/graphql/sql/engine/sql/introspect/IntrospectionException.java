package graphql.sql.engine.sql.introspect;

public class IntrospectionException extends RuntimeException {

    public IntrospectionException(String message) {
        super(message);
    }

    public IntrospectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
