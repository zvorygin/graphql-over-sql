package graphql.sql.schema.impl;

import graphql.sql.schema.Input;

class InputImpl extends CompositeNamedNode implements Input {
    public InputImpl(String name, Location location) {
        super(name, location);
    }
}
