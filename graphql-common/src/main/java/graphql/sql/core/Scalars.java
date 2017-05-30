package graphql.sql.core;

import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.sql.core.config.NonNullTypeReference;
import graphql.sql.core.config.Scalar;
import graphql.sql.core.config.TypeReference;
import graphql.sql.core.config.TypeReferenceImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

public class Scalars {
    private static final Scalar INT = new GraphQLScalarWrapper(graphql.Scalars.GraphQLInt);
    private static final Scalar STRING = new GraphQLScalarWrapper(graphql.Scalars.GraphQLString);
    private static final Scalar FLOAT = new GraphQLScalarWrapper(graphql.Scalars.GraphQLFloat);
    private static final Scalar BOOLEAN = new GraphQLScalarWrapper(graphql.Scalars.GraphQLBoolean);

    private static final Map<String, Scalar> SQL_TYPE_NAME_TO_ENTITY_TYPE = new HashMap<>();
    private static final Set<String> NUMERIC_TYPES = new HashSet<>(Arrays.asList("NUMERIC", "DECIMAL"));

    static {
        // TODO(dzvorygin) hack goes in next two lines
        SQL_TYPE_NAME_TO_ENTITY_TYPE.put("DATE", STRING);
        SQL_TYPE_NAME_TO_ENTITY_TYPE.put("TIMESTAMP", STRING);

        SQL_TYPE_NAME_TO_ENTITY_TYPE.put("VARCHAR", STRING);
        SQL_TYPE_NAME_TO_ENTITY_TYPE.put("CLOB", STRING);
        SQL_TYPE_NAME_TO_ENTITY_TYPE.put("INTEGER", INT);
        SQL_TYPE_NAME_TO_ENTITY_TYPE.put("SMALLINT", INT);
        SQL_TYPE_NAME_TO_ENTITY_TYPE.put("DOUBLE", FLOAT);
    }

    public static Scalar getScalar(String scalarName) {
        if (scalarName.equals(INT.getName())) {
            return INT;
        } else if (scalarName.equals(STRING.getName())) {
            return STRING;
        } else if (scalarName.equals(FLOAT.getName())) {
            return FLOAT;
        } else if (scalarName.equals(BOOLEAN.getName())) {
            return BOOLEAN;
        }
        throw new IllegalStateException(String.format("Unknown built in scalar [%s]", scalarName));
    }

    private static final class GraphQLScalarWrapper implements Scalar {
        private final GraphQLScalarType scalarType;

        private GraphQLScalarWrapper(GraphQLScalarType scalarType) {
            this.scalarType = scalarType;
        }

        @Override
        public String getName() {
            return scalarType.getName();
        }

        @Override
        public GraphQLScalarType getGraphQLType(Map<String, GraphQLType> dictionary) {
            return scalarType;
        }
    }

    public static TypeReference getByColumn(DbColumn column) {
        Scalar scalarType = SQL_TYPE_NAME_TO_ENTITY_TYPE.get(column.getTypeNameSQL());

        if (NUMERIC_TYPES.contains(column.getTypeNameSQL())) {
            if (column.getTypeQualifiers().size() < 2) {
                scalarType = FLOAT;
            } else {
                Object qualifier = column.getTypeQualifiers().get(1);
                if (qualifier instanceof Integer && 0 == (Integer) qualifier) {
                    scalarType = INT;
                } else {
                    scalarType = FLOAT;
                }
            }
        } else if (scalarType == null) {
            throw new NoSuchElementException(
                    String.format("Failed to find ScalarType for sql type name [%s]", column.getTypeNameSQL()));
        }


        TypeReference result = new TypeReferenceImpl(scalarType.getName());

        if (column.getConstraints().stream()
                .map(DbConstraint::getType)
                .anyMatch(Predicate.isEqual(Constraint.Type.NOT_NULL))) {
            return new NonNullTypeReference(result);
        }

        return result;
    }
}
