package graphql.sql.core.introspect;

public class IntrospectionException extends RuntimeException {

    public IntrospectionException(String message) {
        super(message);
    }

    public IntrospectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntrospectionException(Throwable cause) {
        super(cause);
    }
}
