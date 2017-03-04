package graphql.sql.it;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-test-context.xml")
public class HsqldbArrayTest {

    private static final String QUERY =
            "SELECT FIRST_NAME FROM PERSON WHERE PERSON_NUMBER IN (UNNEST(?)) ORDER BY FIRST_NAME";

    @Autowired
    private DataSource dataSource;

    @Test
    public void testArraysSupported() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(QUERY)) {
            Array array = conn.createArrayOf("INTEGER", new Object[]{1, 2, 3});
            try {
                ps.setArray(1, array);
                try (ResultSet rs = ps.executeQuery()) {
                    Assert.assertTrue(rs.next());
                    Assert.assertEquals("Akiko", rs.getString("FIRST_NAME"));
                    Assert.assertTrue(rs.next());
                    Assert.assertEquals("Alejandra", rs.getString("FIRST_NAME"));
                    Assert.assertTrue(rs.next());
                    Assert.assertEquals("Alexander", rs.getString("FIRST_NAME"));
                    Assert.assertFalse(rs.next());
                }
            } finally {
                array.free();
            }
        }
    }
}
