package graphql.sql.engine.sql.introspect;

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

import static org.junit.Assert.*;

public class JDBCIntrospectorTest {

    private static JDBCIntrospector INTROSPECTOR;
    private static EmbeddedDatabase EMBEDDED_DATABASE;

    @BeforeClass
    public static void setUp() throws Exception {
        EMBEDDED_DATABASE = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:data/00_create.sql")
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
        DbTable table = INTROSPECTOR.getTable("PUBLIC", "PERSON").orElseThrow(NoSuchElementException::new);
        assertNotNull(table);
        assertEquals(table.getTableNameSQL(), "PUBLIC.PERSON");
        assertEquals(table.getName(), "PERSON");
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
                INTROSPECTOR.getTable("PUBLIC", "PERSON").orElseThrow(NoSuchElementException::new).getColumns();
        assertEquals(3, tableFields.size());
        assertArrayEquals(tableFields.stream().map(DbColumn::getName).toArray(String[]::new),
                new String[]{"PERSON_NUMBER", "FIRST_NAME", "LAST_NAME"});

        assertArrayEquals(tableFields.stream().map(DbColumn::getTypeNameSQL).toArray(String[]::new),
                new String[]{"INTEGER", "VARCHAR", "VARCHAR"});
    }

    @Test
    public void getReferences() {
        DbTable table = INTROSPECTOR.getTable("PUBLIC", "EMPLOYEE").orElseThrow(NoSuchElementException::new);
        List<DbForeignKeyConstraint> references = new ArrayList<>(INTROSPECTOR.getForeignKeyConstraints(table));
        assertThat(references, IsCollectionWithSize.hasSize(3));

        references.sort(Comparator.comparing(DbForeignKeyConstraint::getName));
        DbForeignKeyConstraint officeReference = references.get(0);
        assertEquals("EMPLOYEE_OFFICE_CODE_FK", officeReference.getName());
        assertThat(officeReference.getColumns(), IsCollectionWithSize.hasSize(1));
        assertEquals("OFFICE_CODE", officeReference.getColumns().get(0).getName());
        assertEquals("EMPLOYEE", officeReference.getColumns().get(0).getTable().getName());
        assertThat(officeReference.getReferencedColumns(), IsCollectionWithSize.hasSize(1));
        assertEquals("OFFICE_CODE", officeReference.getReferencedColumns().get(0).getName());
        assertEquals("OFFICE", officeReference.getReferencedTable().getName());
    }
}