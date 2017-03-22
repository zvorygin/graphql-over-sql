package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.Type;

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
