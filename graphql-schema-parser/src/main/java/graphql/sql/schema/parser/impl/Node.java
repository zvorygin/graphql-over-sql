package graphql.sql.schema.parser.impl;

class Node {
    private final Location location;

    public Node(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
