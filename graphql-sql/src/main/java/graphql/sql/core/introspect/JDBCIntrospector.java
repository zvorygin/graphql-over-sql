package graphql.sql.core.introspect;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbForeignKeyConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

public class JDBCIntrospector implements DatabaseIntrospector {

    private final DataSource dataSource;
    private final DbSpec dbSpec = new DbSpec();
    private final Map<DbTable, DbSchema> tableToSchema = new HashMap<>();
    private final Map<DbTable, Collection<DbForeignKeyConstraint>> tableToForeignKeyConstraint = new HashMap<>();
    private final Map<DbTable, DbConstraint> tableToPrimaryKeyConstraint = new HashMap<>();
    private final Map<DbForeignKeyConstraint, DbJoin> foreignKeyConstraintToDbJoin = new HashMap<>();
    private final Map<DbForeignKeyConstraint, DbJoin> reverseForeignKeyConstraintToDbJoin = new HashMap<>();

    public JDBCIntrospector(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Nonnull
    @Override
    public DbSpec getSchema() {
        return dbSpec;
    }

    @Nonnull
    @Override
    public Optional<DbTable> getTable(@Nonnull String schemaName, @Nonnull String tableName) {
        DbSchema schema = getSchema(schemaName);

        DbTable table = schema.findTable(tableName);

        if (table != null) {
            return Optional.of(table);
        }

        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, schemaName, tableName, null)) {
            if (rs.next()) {
                // TODO(dzvorygin) remove this useless existence check
                // If this table was found in database
                return Optional.of(buildTable(tableName, schema));
            }
        } catch (SQLException e) {
            throw new IntrospectionException(String.format("Failed to list tables of schema [%s]", schemaName), e);
        }
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Collection<DbForeignKeyConstraint> getForeignKeyConstraints(DbTable table) {
        return tableToForeignKeyConstraint.computeIfAbsent(table, this::buildForeignKeyConstraints);
    }

    @SuppressWarnings("Duplicates")
    @Nonnull
    @Override
    public DbJoin getJoin(DbForeignKeyConstraint constraint) {
        return foreignKeyConstraintToDbJoin.computeIfAbsent(constraint,
                dbForeignKeyConstraint -> {
                    List<DbColumn> columns = constraint.getColumns();
                    List<DbColumn> referencedColumns = constraint.getReferencedColumns();
                    // Dirty hack since there's no way to get table from constraint.
                    DbTable table = columns.get(0).getTable();
                    DbTable referencedTable = constraint.getReferencedTable();
                    DbJoin dbJoin = new DbJoin(dbSpec,
                            table,
                            referencedTable,
                            columns.toArray(new DbColumn[columns.size()]),
                            referencedColumns.toArray(new DbColumn[referencedColumns.size()]));
                    return dbSpec.addJoin(dbJoin);
                });
    }

    @SuppressWarnings("Duplicates")
    @Nonnull
    @Override
    public DbJoin getReverseJoin(DbForeignKeyConstraint constraint) {

        return reverseForeignKeyConstraintToDbJoin.computeIfAbsent(constraint,
                dbForeignKeyConstraint -> {
                    DbJoin forward = getJoin(constraint);
                    DbJoin dbJoin = new DbJoin(forward.getSpec(),
                            forward.getToTable(),
                            forward.getFromTable(),
                            forward.getToColumns().toArray(new DbColumn[forward.getToColumns().size()]),
                            forward.getFromColumns().toArray(new DbColumn[forward.getFromColumns().size()]));
                    return dbSpec.addJoin(dbJoin);
                });
    }

    private Collection<DbForeignKeyConstraint> buildForeignKeyConstraints(DbTable table) {
        Collection<DbForeignKeyConstraint> result = new ArrayList<>();
        DbSchema schema = tableToSchema.get(table);

        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getImportedKeys(null, schema.getName(), table.getName())) {
            List<DbColumn> columns = new ArrayList<>();
            List<DbColumn> referencedColumns = new ArrayList<>();
            String constraintName = null;
            DbTable referencedTable = null;
            int keySec = Integer.MIN_VALUE;
            while (rs.next()) {
                int nextKeySec = rs.getInt("KEY_SEQ");
                if (nextKeySec != keySec + 1) {
                    if (keySec != Integer.MIN_VALUE) {
                        result.add(table.foreignKey(constraintName,
                                columns.toArray(new DbColumn[columns.size()]),
                                referencedTable,
                                referencedColumns.toArray(new DbColumn[referencedColumns.size()])));
                    }
                    constraintName = rs.getString("FK_NAME");
                    String foreignTableSchema = rs.getString("PKTABLE_SCHEM");
                    String foreignTableName = rs.getString("PKTABLE_NAME");
                    referencedTable = getTable(foreignTableSchema, foreignTableName).orElseThrow(
                            () -> new IntrospectionException(
                                    String.format("Failed to find table [%s].[%s] referenced by table [%s].[%s]",
                                            foreignTableSchema,
                                            foreignTableName,
                                            schema.getName(),
                                            table.getName()))
                    );
                    referencedColumns.clear();
                    columns.clear();
                }

                keySec = nextKeySec;

                columns.add(table.findColumn(rs.getString("FKCOLUMN_NAME")));
                referencedColumns.add(referencedTable.findColumn(rs.getString("PKCOLUMN_NAME")));
            }
            if (keySec != Integer.MIN_VALUE) {
                // Add last constraint if any.
                result.add(table.foreignKey(constraintName,
                        columns.toArray(new DbColumn[columns.size()]),
                        referencedTable,
                        referencedColumns.toArray(new DbColumn[referencedColumns.size()])));
            }
        } catch (SQLException e) {
            throw new IntrospectionException(
                    String.format("Failed to list foreign key constraints of [%s]", table.getName()));
        }

        return result;
    }

    private DbTable buildTable(String tableName, DbSchema schema) {
        DbTable table = dbSpec.createTable(schema, tableName);

        try (Connection conn = dataSource.getConnection()) {
            try (ResultSet rs = conn.getMetaData().getColumns(null, schema.getName(), tableName, null)) {
                while (rs.next()) {
                    table.addColumn(
                            rs.getString("COLUMN_NAME"),
                            rs.getInt("DATA_TYPE"),
                            rs.getInt("COLUMN_SIZE"),
                            rs.getInt("DECIMAL_DIGITS"));
                }
            }
            try (ResultSet rs = conn.getMetaData().getPrimaryKeys(null, schema.getName(), tableName)) {
                String primaryKeyName = null;
                List<DbColumn> primaryKeyColumns = new ArrayList<>();
                while (rs.next()) {
                    primaryKeyName = rs.getString("PK_NAME");
                    String columnName = rs.getString("COLUMN_NAME");
                    DbColumn column = table.findColumn(columnName);

                    if (column == null) {
                        throw new IntrospectionException(
                                String.format("Unable to resolve column [%s] as part of PK [%s] of table [%s]",
                                        columnName, primaryKeyName, table));
                    }
                    primaryKeyColumns.add(column);
                }

                tableToPrimaryKeyConstraint.put(table, table.primaryKey(primaryKeyName,
                        primaryKeyColumns.stream().map(DbColumn::getName).toArray(String[]::new)));
            }
        } catch (SQLException e) {
            throw new IntrospectionException(String.format("Failed to list columns of table [%s]", table), e);
        }

        schema.addTable(table);
        tableToSchema.put(table, schema);

        return table;
    }

    private DbSchema getSchema(String schemaName) {
        return Optional.ofNullable(dbSpec.findSchema(schemaName)).orElseGet(() -> dbSpec.addSchema(schemaName));
    }
}
