package graphql.sql.schema.parser.impl;

abstract class NamedNode extends Node {
    private final String name;

    public NamedNode(String name, Location location) {
        super(location);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
