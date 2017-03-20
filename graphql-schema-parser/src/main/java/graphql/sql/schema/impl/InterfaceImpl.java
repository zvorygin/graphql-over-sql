package graphql.sql.schema.impl;

import graphql.sql.schema.Interface;

class InterfaceImpl extends CompositeNamedNode implements Interface {
    public InterfaceImpl(String name, Location location) {
        super(name, location);
    }

}
