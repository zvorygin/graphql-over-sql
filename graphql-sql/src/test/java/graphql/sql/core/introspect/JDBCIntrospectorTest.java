package graphql.sql.core.introspect;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbForeignKeyConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

public class JDBCIntrospectorTest {

    private static JDBCIntrospector INTROSPECTOR;
    private static EmbeddedDatabase EMBEDDED_DATABASE;

    @BeforeClass
    public static void setUp() throws Exception {
        EMBEDDED_DATABASE = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:test.sql")
                .build();
        INTROSPECTOR = new JDBCIntrospector(EMBEDDED_DATABASE);
    }

    @AfterClass
    public static void tearDown() {
        EMBEDDED_DATABASE.shutdown();
        EMBEDDED_DATABASE = null;
        INTROSPECTOR = null;
    }

    @Test
    public void findTable() {
        DbTable table = INTROSPECTOR.getTable("PUBLIC", "USER").orElseThrow(NoSuchElementException::new);
        assertNotNull(table);
        assertEquals(table.getTableNameSQL(), "PUBLIC.USER");
        assertEquals(table.getName(), "USER");
    }

    @Test
    public void findNonExistingTable() {
        assertFalse(INTROSPECTOR.getTable("PUBLIC", "xxx").isPresent());
    }

    @Test
    public void findInNonExistingSchema() {
        assertFalse(INTROSPECTOR.getTable("xxx", "USER").isPresent());
    }

    @Test
    public void getColumns() throws Exception {
        List<DbColumn> tableFields =
                INTROSPECTOR.getTable("PUBLIC", "MESSAGE").orElseThrow(NoSuchElementException::new).getColumns();
        assertEquals(5, tableFields.size());
        assertArrayEquals(tableFields.stream().map(DbColumn::getName).toArray(String[]::new),
                new String[] {"ID", "BODY", "CREATED_AT", "AUTHOR", "THREAD"});

        assertArrayEquals(tableFields.stream().map(DbColumn::getTypeNameSQL).toArray(String[]::new),
                new String[] {"INTEGER", "VARCHAR", "TIMESTAMP", "INTEGER", "INTEGER"});
    }

    @Test
    public void getReferences() {
        DbTable table = INTROSPECTOR.getTable("PUBLIC", "MESSAGE").orElseThrow(NoSuchElementException::new);
        List<DbForeignKeyConstraint> references = new ArrayList<>(INTROSPECTOR.getForeignKeyConstraints(table));
        assertThat(references, IsCollectionWithSize.hasSize(2));

        references.sort(Comparator.comparing(DbForeignKeyConstraint::getName));
        DbForeignKeyConstraint userReference = references.get(0);
        assertEquals("MESSAGE_AUTHOR", userReference.getName());
        assertThat(userReference.getColumns(), IsCollectionWithSize.hasSize(1));
        assertEquals("AUTHOR", userReference.getColumns().get(0).getName());
        assertEquals("MESSAGE", userReference.getColumns().get(0).getTable().getName());
        assertThat(userReference.getReferencedColumns(), IsCollectionWithSize.hasSize(1));
        assertEquals("USER", userReference.getReferencedTable().getName());
        assertEquals("ID", userReference.getReferencedColumns().get(0).getName());
    }
}