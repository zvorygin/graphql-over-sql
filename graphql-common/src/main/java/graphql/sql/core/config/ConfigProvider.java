package graphql.sql.core.config;

import graphql.sql.core.config.domain.Config;

public interface ConfigProvider {

    Config getConfig();
}
