package graphql.sql.core.config.domain.impl;

import graphql.sql.core.config.Interface;
import graphql.sql.core.config.ObjectType;
import graphql.sql.core.config.domain.Config;
import graphql.sql.core.config.Scalar;

import java.util.Map;

public class ConfigImpl implements Config {

    private final Map<String, Interface> interfaces;
    private final Map<String, ObjectType> types;
    private final Map<String, Scalar> scalars;
    private final String queryTypeName;

    public ConfigImpl(Map<String, Interface> interfaces,
                      Map<String, ObjectType> types,
                      Map<String, Scalar> scalars,
                      String queryTypeName) {
        this.interfaces = interfaces;
        this.types = types;
        this.scalars = scalars;
        this.queryTypeName = queryTypeName;
    }

    @Override
    public Map<String, Interface> getInterfaces() {
        return interfaces;
    }

    @Override
    public Map<String, ObjectType> getTypes() {
        return types;
    }

    @Override
    public Map<String, Scalar> getScalars() {
        return scalars;
    }

    @Override
    public String getQueryTypeName() {
        return queryTypeName;
    }
}
