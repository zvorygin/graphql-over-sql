package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.Input;

class InputImpl extends CompositeNamedNode implements Input {
    public InputImpl(String name, Location location) {
        super(name, location);
    }
}
