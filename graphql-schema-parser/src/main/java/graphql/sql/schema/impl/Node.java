package graphql.sql.schema.impl;

class Node {
    private final Location location;

    public Node(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
