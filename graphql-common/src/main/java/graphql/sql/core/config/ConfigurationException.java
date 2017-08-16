package graphql.sql.core.config;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(Exception e) {
        super(e);
    }

    public ConfigurationException(String message) {
        super(message);
    }
}
