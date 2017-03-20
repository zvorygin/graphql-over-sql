package graphql.sql.schema.impl;

import graphql.sql.schema.ObjectType;

class ObjectTypeImpl extends CompositeNamedNode implements ObjectType {
    public ObjectTypeImpl(String name, Location location) {
        super(name, location);
    }
}
