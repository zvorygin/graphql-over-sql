package graphql.sql.core.config.domain;

import graphql.sql.core.config.domain.type.DateTypeUtil;
import graphql.sql.core.config.domain.type.DoubleTypeUtil;
import graphql.sql.core.config.domain.type.IntegerTypeUtil;
import graphql.sql.core.config.domain.type.StringTypeUtil;
import graphql.sql.core.config.domain.type.TimestampTypeUtil;
import graphql.sql.core.config.domain.type.TypeUtil;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public enum EntityType {
    STRING(StringTypeUtil.INSTANCE, "VARCHAR", "CLOB"),
    INTEGER(IntegerTypeUtil.INSTANCE, "INTEGER", "SMALLINT"),
    DOUBLE(DoubleTypeUtil.INSTANCE, "DOUBLE"),
    DATE(DateTypeUtil.INSTANCE),
    TIMESTAMP(TimestampTypeUtil.INSTANCE, "TIMESTAMP");

    private static final Map<String, EntityType> SQL_TYPE_NAME_TO_ENTITY_TYPE = new HashMap<>();
    private static final Set<String> NUMERIC_TYPES = new HashSet<>(Arrays.asList("NUMERIC", "DECIMAL"));

    static {
        for (EntityType entityType : EntityType.values()) {
            for (String sqlTypesName : entityType.sqlTypesNames) {
                SQL_TYPE_NAME_TO_ENTITY_TYPE.put(sqlTypesName, entityType);
            }
        }
    }

    private final TypeUtil typeUtil;
    private final String[] sqlTypesNames;

    EntityType(TypeUtil typeUtil, String... sqlTypesNames) {
        this.typeUtil = typeUtil;
        this.sqlTypesNames = sqlTypesNames;
    }

    public TypeUtil getTypeUtil() {
        return typeUtil;
    }

    public static EntityType getByColumn(DbColumn column) {
        EntityType entityType = SQL_TYPE_NAME_TO_ENTITY_TYPE.get(column.getTypeNameSQL());
        if (entityType != null) {
            return entityType;
        }

        if (NUMERIC_TYPES.contains(column.getTypeNameSQL())) {
            if (column.getTypeQualifiers().size() < 2)
                return DOUBLE;
            Object qualifier = column.getTypeQualifiers().get(1);
            if (qualifier instanceof Integer && 0 == (Integer)qualifier)
                return INTEGER;
            return DOUBLE;
        }

        throw new NoSuchElementException(
                String.format("Failed to find EntityType for sql type name [%s]", column.getTypeNameSQL()));
    }
}
