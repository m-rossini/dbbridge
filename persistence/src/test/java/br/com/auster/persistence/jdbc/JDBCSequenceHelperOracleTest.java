/**
 * 
 */
package br.com.auster.persistence.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * @author framos
 * 
 */
public class JDBCSequenceHelperOracleTest extends TestCase {

	private static final String CORRECT_TEST_SEQUENCE_SQL = "TEST_SEQUENCE.NEXTVAL";

	private static final Logger log = Logger
			.getLogger(JDBCSequenceHelperOracleTest.class);

	private Connection conn;

	protected void setUp() throws Exception {
		Class.forName("oracle.jdbc.OracleDriver");
		this.conn = DriverManager.getConnection(
				"jdbc:oracle:thin:@lisa:1521:TEST01", "test", "test");
	}

	protected void tearDown() throws Exception {
		this.conn.close();
	}

	public void testOracleString() {
		try {
			String sql = JDBCSequenceHelper.translate(this.conn,
					"test_sequence");
			assertNotNull(sql);
			assertTrue(CORRECT_TEST_SEQUENCE_SQL.equalsIgnoreCase(sql));
			log.debug("got correct string: " + sql);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			fail();
		}
	}

	public void testOracleValue() {
		try {
			long id = JDBCSequenceHelper.nextValue(this.conn, "test_sequence");
			assertTrue(id > 0);
			log.debug("recovered id is :" + id);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			fail();
		}
	}

	public void testOracleInvalidSequence() {
		try {
			JDBCSequenceHelper.nextValue(this.conn, "test_sequence2");
			fail();
		} catch (SQLException sqle) {
			log.debug(sqle.getMessage());
			assertTrue(true);
		}
	}

}
