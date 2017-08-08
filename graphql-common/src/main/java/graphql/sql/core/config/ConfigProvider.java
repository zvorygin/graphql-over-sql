package graphql.sql.core.config;

import graphql.sql.core.config.domain.Config;

@FunctionalInterface
public interface ConfigProvider {

    Config getConfig();
}
