package graphql.sql.engine.sql;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import graphql.sql.core.Scalars;
import graphql.sql.core.config.Interface;
import graphql.sql.core.config.ObjectType;
import graphql.sql.core.config.TypeReference;
import graphql.sql.engine.sql.introspect.JDBCIntrospector;
import graphql.sql.schema.engine.ConfigurationException;
import graphql.sql.schema.engine.TypeProvider;
import graphql.sql.schema.parser.SchemaAnnotation;
import graphql.sql.schema.parser.SchemaField;
import graphql.sql.schema.parser.SchemaInterface;
import graphql.sql.schema.parser.SchemaObjectType;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SqlTypeProvider implements TypeProvider {
    private final JDBCIntrospector introspector;
    private final NameProvider nameProvider;
    private final SqlExecutorBuilder executorBuilder;

    public SqlTypeProvider(@Nonnull JDBCIntrospector introspector,
                           @Nonnull NameProvider nameProvider,
                           @Nonnull SqlExecutorBuilder executorBuilder) {
        this.introspector = introspector;
        this.nameProvider = nameProvider;
        this.executorBuilder = executorBuilder;
    }

    @Override
    public Interface buildInterface(SchemaInterface schemaInterface, Map<String, Interface> interfaces) {
        Map<String, ? extends SchemaAnnotation> annotations = schemaInterface.getAnnotations();

        DbTable table = getTableName(annotations);

        if (table == null) {
            throw new ConfigurationException(
                    String.format("Interface [%s] should have @Table annotation with String \"name\" attribute",
                            schemaInterface.getName()));
        }

        boolean discover = getDiscover(annotations);
        Map<String, SqlField> fields = buildFields(table, schemaInterface.getFields(), discover);

        return new TableInterface(table, schemaInterface.getName(), fields, executorBuilder);
    }

    @Override
    public ObjectType buildObjectType(SchemaObjectType objectType, Map<String, Interface> interfaces) {
        Map<String, ? extends SchemaAnnotation> annotations = objectType.getAnnotations();

        DbTable dbTable = getTableName(annotations);

        if (dbTable == null) {
            throw new ConfigurationException(
                    String.format("Interface [%s] should have @Table annotation with String \"name\" attribute",
                            objectType.getName()));
        }

        boolean discover = getDiscover(annotations);
        Map<String, SqlField> fields = buildFields(dbTable, objectType.getFields(), discover);

        List<Interface> implementedInterfaces =
                objectType.getInterfaces().stream().map(interfaces::get).collect(Collectors.toList());

        return new TableObjectType(dbTable, objectType.getName(), fields, implementedInterfaces, executorBuilder);
    }

    @Nonnull
    private Map<String, SqlField> buildFields(DbTable dbTable,
                                              Collection<? extends SchemaField> fields,
                                              boolean discover) {
        Map<String, DbColumn> columns =
                dbTable.getColumns().stream().collect(Collectors.toMap(DbColumn::getColumnNameSQL, Function.identity()));

        Set<String> mappedColumns = new HashSet<>();

        HashMap<String, SqlField> sqlFields = fields.stream().map(field -> {
            DbColumn dbColumn = columns.get(field.getName());
            if (dbColumn == null) {
                throw new ConfigurationException(
                        String.format("Unable to find column [%s] in table [%s]", field.getName(), dbTable.getName()));
            }

            mappedColumns.add(dbColumn.getColumnNameSQL());

            TypeReference typeReference = Scalars.getByColumn(dbColumn);
            if (!TypeReference.equals(typeReference, field.getType())) {
                throw new ConfigurationException(
                        String.format("Type reference mismatch for field [%s] in table [%s]. " +
                                        "Expected type in GraphQL schema \"%s\", actual in database \"%s\"",
                                field.getName(), dbTable.getName(),
                                field.getType().toString(), typeReference.toString()));
            }

            return new SqlField(field.getName(), Scalars.getByColumn(dbColumn), dbColumn);
        }).collect(Collectors.toMap(SqlField::getName, Function.identity(), null, HashMap::new));

        if (discover) {
            for (DbColumn dbColumn : columns.values()) {
                if (mappedColumns.contains(dbColumn.getColumnNameSQL())) {
                    continue;
                }
                String fieldName = nameProvider.getFieldName(dbColumn.getColumnNameSQL());
                sqlFields.put(fieldName, new SqlField(fieldName, Scalars.getByColumn(dbColumn), dbColumn));
            }
        }

        return sqlFields;
    }

    private static boolean getDiscover(Map<String, ? extends SchemaAnnotation> annotations) {
        SchemaAnnotation tableAnnotation = annotations.get("Table");
        if (tableAnnotation == null) {
            return false;
        }

        Object tableName = tableAnnotation.getAttribute("discover");

        return tableName instanceof Boolean && (boolean) tableName;
    }

    private DbTable getTableName(Map<String, ? extends SchemaAnnotation> annotations) {
        SchemaAnnotation tableAnnotation = annotations.get("Table");
        if (tableAnnotation == null) {
            return null;
        }

        Object tableName = tableAnnotation.getAttribute("name");

        if (!(tableName instanceof String)) {
            return null;
        }

        Optional<DbTable> table = introspector.getTable((String) tableName);
        return table.orElseThrow(() -> new ConfigurationException(
                String.format("Table [%s] not found in database, or current user isn't permitted to use it",
                        tableName)));
    }

}
