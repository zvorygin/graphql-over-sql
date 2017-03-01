package graphql.sql.core.query;

public class QueryBuilderException extends RuntimeException {

    public QueryBuilderException(String message) {
        super(message);
    }

    public QueryBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
