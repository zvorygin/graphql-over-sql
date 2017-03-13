package graphql.sql.core.config.domain;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import graphql.sql.core.config.domain.type.*;

import java.util.*;

public enum ScalarType {
    STRING(StringTypeUtil.INSTANCE, "VARCHAR", "CLOB"),
    INTEGER(IntegerTypeUtil.INSTANCE, "INTEGER", "SMALLINT"),
    DOUBLE(DoubleTypeUtil.INSTANCE, "DOUBLE"),
    DATE(DateTypeUtil.INSTANCE, "DATE"),
    TIMESTAMP(TimestampTypeUtil.INSTANCE, "TIMESTAMP");

    private static final Map<String, ScalarType> SQL_TYPE_NAME_TO_ENTITY_TYPE = new HashMap<>();
    private static final Set<String> NUMERIC_TYPES = new HashSet<>(Arrays.asList("NUMERIC", "DECIMAL"));

    static {
        for (ScalarType scalarType : ScalarType.values()) {
            for (String sqlTypesName : scalarType.sqlTypesNames) {
                SQL_TYPE_NAME_TO_ENTITY_TYPE.put(sqlTypesName, scalarType);
            }
        }
    }

    private final TypeUtil<?> typeUtil;
    private final String[] sqlTypesNames;

    ScalarType(TypeUtil<?> typeUtil, String... sqlTypesNames) {
        this.typeUtil = typeUtil;
        this.sqlTypesNames = sqlTypesNames;
    }

    public TypeUtil<?> getTypeUtil() {
        return typeUtil;
    }

    public static ScalarType getByColumn(DbColumn column) {
        ScalarType scalarType = SQL_TYPE_NAME_TO_ENTITY_TYPE.get(column.getTypeNameSQL());
        if (scalarType != null) {
            return scalarType;
        }

        if (NUMERIC_TYPES.contains(column.getTypeNameSQL())) {
            if (column.getTypeQualifiers().size() < 2) {
                return DOUBLE;
            }
            Object qualifier = column.getTypeQualifiers().get(1);
            if (qualifier instanceof Integer && 0 == (Integer) qualifier) {
                return INTEGER;
            }
            return DOUBLE;
        }

        throw new NoSuchElementException(
                String.format("Failed to find ScalarType for sql type name [%s]", column.getTypeNameSQL()));
    }
}
