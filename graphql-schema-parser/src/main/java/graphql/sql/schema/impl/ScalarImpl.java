package graphql.sql.schema.impl;

import graphql.sql.schema.Scalar;

class ScalarImpl extends NamedNode implements Scalar {
    public ScalarImpl(String name, Location location) {
        super(name, location);
    }
}
