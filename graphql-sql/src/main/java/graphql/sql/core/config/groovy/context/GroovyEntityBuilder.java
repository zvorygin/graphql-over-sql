package graphql.sql.core.config.groovy.context;

import graphql.sql.core.config.NameProvider;
import graphql.sql.core.config.domain.Entity;
import graphql.sql.core.config.domain.EntityField;
import graphql.sql.core.config.domain.EntityReference;
import graphql.sql.core.config.domain.ScalarType;
import graphql.sql.core.config.domain.ReferenceType;
import graphql.sql.core.introspect.DatabaseIntrospector;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbForeignKeyConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class GroovyEntityBuilder {

    private String entityName;
    private String catalogName;
    private String schemaName;
    private String tableName;
    private boolean isAbstract;
    private Entity parent;

    public GroovyEntityBuilder(String catalogName, String schemaName) {
        this.catalogName = catalogName;
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

    public GroovyEntityBuilder parent(Entity parent) {
        this.parent = parent;
        return this;
    }

    public GroovyEntityBuilder isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    public Entity build(DatabaseIntrospector introspector, NameProvider nameProvider) {

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

        List<EntityField> entityFields = table.getColumns()
                .stream()
                .map(column -> new EntityField(
                        nameProvider.getFieldName(column.getName()),
                        column,
                        ScalarType.getByColumn(column)))
                .collect(Collectors.toList());

        EntityReference parentReference = null;

        if (parent != null) {
            DbTable parentTable = parent.getTable();
            Collection<DbForeignKeyConstraint> foreignKeyConstraints = introspector.getForeignKeyConstraints(table);
            List<DbForeignKeyConstraint> constraints = foreignKeyConstraints.stream()
                    .filter(constraint -> constraint.getReferencedTable().equals(parentTable))
                    .collect(Collectors.toList());

            if (constraints.size() == 0) {
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

            parentReference = new EntityReference(constraint.getName(),
                    introspector.getJoin(constraint),
                    introspector.getReverseJoin(constraint), parent, ReferenceType.MANY_TO_ONE);
        }

        return new Entity(entityName, table, entityFields, parentReference);
    }
}
