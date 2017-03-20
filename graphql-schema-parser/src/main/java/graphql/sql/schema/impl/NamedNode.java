package graphql.sql.schema.impl;

import graphql.sql.schema.Type;

abstract class NamedNode extends Node implements Type {
    private final String name;

    public NamedNode(String name, Location location) {
        super(location);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
