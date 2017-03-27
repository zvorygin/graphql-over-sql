package graphql.sql.schema.parser.impl;

abstract class NamedSchemaNode extends SchemaNode {
    private final String name;

    public NamedSchemaNode(String name, Location location) {
        super(location);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
