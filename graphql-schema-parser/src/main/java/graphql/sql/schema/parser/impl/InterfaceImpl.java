package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.Interface;

class InterfaceImpl extends CompositeNamedNode implements Interface {
    public InterfaceImpl(String name, Location location) {
        super(name, location);
    }

}
