package graphql.sql.schema.parser.impl;

class SchemaNode {
    private final Location location;

    public SchemaNode(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
