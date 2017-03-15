package graphql.sql.core.config.groovy.context;

import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbForeignKeyConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbObject;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import graphql.sql.core.config.ConfigurationException;
import graphql.sql.core.config.NameProvider;
import graphql.sql.core.config.domain.impl.Key;
import graphql.sql.core.config.domain.ReferenceType;
import graphql.sql.core.config.domain.ScalarType;
import graphql.sql.core.config.domain.impl.SqlEntityReference;
import graphql.sql.core.config.domain.impl.SqlEntityField;
import graphql.sql.core.config.domain.impl.SqlEntity;
import graphql.sql.core.introspect.DatabaseIntrospector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GroovyEntityBuilder {

    private String entityName;
    private String schemaName;
    private String tableName;
    private SqlEntity parent;

    public GroovyEntityBuilder(String schemaName) {
        this.schemaName = schemaName;
    }

    public GroovyEntityBuilder name(@Nonnull String entityName) {
        this.entityName = entityName;
        return this;
    }

    public GroovyEntityBuilder schema(@Nonnull String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public GroovyEntityBuilder table(@Nonnull String tableName) {
        this.tableName = tableName;
        return this;
    }

    public GroovyEntityBuilder parent(SqlEntity parent) {
        this.parent = parent;
        return this;
    }

    public GroovyEntityBuilder isAbstract(boolean isAbstract) {
        return this;
    }

    public SqlEntity build(DatabaseIntrospector introspector, NameProvider nameProvider) {

        if (tableName == null) {
            throw new IllegalArgumentException("Mandatory parameter \"table\" not found");
        }

        if (entityName == null) {
            entityName = nameProvider.getEntityName(tableName);
        }

        DbTable table = introspector.getTable(schemaName, tableName)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                String.format("Table [%s] wasn't found in schema [%s]", tableName, schemaName)));

        List<SqlEntityField> entityFields = table.getColumns()
                .stream()
                .map(column -> new SqlEntityField(
                        nameProvider.getFieldName(column.getName()),
                        column,
                        ScalarType.getByColumn(column),
                        column.getConstraints().stream().map(DbConstraint::getType)
                                .anyMatch(Predicate.isEqual(Constraint.Type.UNIQUE))))
                .collect(Collectors.toList());

        SqlEntityReference parentReference = null;

        if (parent != null) {
            DbTable parentTable = parent.getTable();
            Collection<DbForeignKeyConstraint> foreignKeyConstraints = introspector.getForeignKeyConstraints(table);
            List<DbForeignKeyConstraint> constraints = foreignKeyConstraints.stream()
                    .filter(constraint -> constraint.getReferencedTable().equals(parentTable))
                    .collect(Collectors.toList());

            if (constraints.isEmpty()) {
                throw new IllegalArgumentException(
                        String.format("Unable to find FK constraint from [%s] to [%s]",
                                table.getAbsoluteName(), parentTable.getAbsoluteName()));
            }

            if (constraints.size() > 1) {
                throw new IllegalArgumentException(
                        String.format("Ambiguous FK constraints from [%s] to [%s]",
                                table.getAbsoluteName(), parentTable.getAbsoluteName()));
            }


            DbForeignKeyConstraint constraint = constraints.get(0);

            // If any column is missing NOT_NULL constraint, then this reference is nullable
            boolean nullable = constraint.getColumns().stream().map(
                    dbColumn -> dbColumn.getConstraints().stream().map(DbConstraint::getType)
                            .anyMatch(Predicate.isEqual(Constraint.Type.NOT_NULL))
            ).anyMatch(Predicate.isEqual(false));

            parentReference = new SqlEntityReference(constraint.getName(),
                    introspector.getJoin(constraint), parent, ReferenceType.MANY_TO_ONE, nullable);
        }

        table.getConstraints().stream().filter(dbConstraint -> dbConstraint.getType() == Constraint.Type.PRIMARY_KEY).findAny().orElseThrow(() -> new ConfigurationException(String.format("Primary constraint not found for table [%s]", table.getAbsoluteName())));

        Key primaryKey = buildPrimaryKey(introspector, table, entityFields);
        return new SqlEntity(entityName, table, entityFields, parentReference, primaryKey);
    }

    @Nullable
    private Key buildPrimaryKey(DatabaseIntrospector introspector, DbTable table, List<SqlEntityField> entityFields) {
        DbConstraint constraint = introspector.getPrimaryKeyConstraint(table);

        if (constraint == null) {
            return null;
        }
        Map<String, SqlEntityField> columnNameToEntity = entityFields.stream().collect(Collectors.toMap(field -> field.getColumn().getName(), Function.identity()));

        List<SqlEntityField> keyFields = constraint.getColumns().stream().map(DbObject::getName).map(columnNameToEntity::get).collect(Collectors.toList());

        return new Key(keyFields, constraint.getName(), constraint);
    }
}
