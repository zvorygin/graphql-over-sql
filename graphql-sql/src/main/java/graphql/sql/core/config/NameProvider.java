package graphql.sql.core.config;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbForeignKeyConstraint;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.ReferenceType;

public interface NameProvider {
    String getEntityName(String tableName);

    String getFieldName(String columnName);

    String getLinkName(DbForeignKeyConstraint constraint, Entity from, Entity to, ReferenceType type);

    String getFieldListName(EntityField entityField);
}
