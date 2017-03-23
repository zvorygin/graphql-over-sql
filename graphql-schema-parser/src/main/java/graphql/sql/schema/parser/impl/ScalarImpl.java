package graphql.sql.schema.parser.impl;

import graphql.sql.schema.parser.Scalar;

import java.util.Map;

class ScalarImpl extends AbstractType implements Scalar {
    public ScalarImpl(Map<String, AnnotationImpl> annotations, String name, Location location) {
        super(annotations, name, location);
    }
}
