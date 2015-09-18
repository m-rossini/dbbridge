/**
 * 
 */
package br.com.auster.persistence.jdbc;

import java.sql.Connection;

import junit.framework.TestCase;

/**
 * @author framos
 * 
 */
public class JDBCSequenceHelperPostgresqlTest extends TestCase {

	private Connection conn;

	private static final String CORRECT_TEST_SEQUENCE_SQL = "nextval('test_sequence')";

	protected void setUp() throws Exception {
		// Class.forName("org.postgresql.Driver");
		// this.conn = DriverManager.getConnection(
		// "jdbc:postgresql://lisa:5432/testdb", "test", "test");
	}

	protected void tearDown() throws Exception {
		// this.conn.close();
	}

	public void testSOP() {
		System.out.println("Teste");
	}

	// public void testPostgreString() {
	// try {
	// String sql = JDBCSequenceHelper.translate(this.conn,
	// "test_sequence");
	// assertNotNull(sql);
	// assertTrue(CORRECT_TEST_SEQUENCE_SQL.equalsIgnoreCase(sql));
	// } catch (SQLException sqle) {
	// sqle.printStackTrace();
	// fail();
	// }
	// }
	//
	// public void testPostgreValue() {
	// try {
	// long id = JDBCSequenceHelper.nextValue(this.conn, "test_sequence");
	// assertTrue(id > 0);
	// } catch (SQLException sqle) {
	// sqle.printStackTrace();
	// fail();
	// }
	// }
	//
	// public void testPostgreInvalidSequence() {
	// try {
	// JDBCSequenceHelper.nextValue(this.conn, "test_sequence2");
	// fail();
	// } catch (SQLException sqle) {
	// assertTrue(true);
	// }
	// }
}
