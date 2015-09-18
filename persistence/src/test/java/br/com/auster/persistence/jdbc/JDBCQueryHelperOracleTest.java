/*
 * Copyright (c) 2004-2006 Auster Solutions. All Rights Reserved.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on 11/09/2006
 */
package br.com.auster.persistence.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;
import br.com.auster.persistence.FetchCriteria;

/**
 * @author framos
 * @version $Id$
 * 
 */
public class JDBCQueryHelperOracleTest extends TestCase {

	protected static final String TEST_SQL = "select sysdate from dual";

	protected Connection conn;

	protected void setUp() throws Exception {
		Class.forName("oracle.jdbc.OracleDriver");
		this.conn = DriverManager.getConnection(
				"jdbc:oracle:thin:@lisa:1521:TEST01", "test", "test");
	}

	protected void tearDown() throws Exception {
		this.conn.close();
	}

	public void testOraclePaging() {
		FetchCriteria criteria = new FetchCriteria();
		criteria.setOffset(10);
		criteria.setSize(20);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			String sql = JDBCQueryHelper.applyFetchParameters(conn, TEST_SQL,
					criteria);
			System.out.println(sql);
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			rset.close();
			stmt.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			fail();
		}
	}

	public void testOracleSorting() {
		FetchCriteria criteria = new FetchCriteria();
		criteria.addOrder("1", false);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			String sql = JDBCQueryHelper.applyFetchParameters(conn, TEST_SQL,
					criteria);
			System.out.println(sql);
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			rset.close();
			stmt.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			fail();
		}
	}

	public void testOracleNull() {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			String sql = JDBCQueryHelper.applyFetchParameters(conn, TEST_SQL,
					null);
			System.out.println(sql);
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			rset.close();
			stmt.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			fail();
		}

	}

	public void testOracleNothing() {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			String sql = JDBCQueryHelper.applyFetchParameters(conn, TEST_SQL,
					new FetchCriteria());
			System.out.println(sql);
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			rset.close();
			stmt.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			fail();
		}

	}

	public void testOracleSortAndPaging() {
		FetchCriteria criteria = new FetchCriteria();
		criteria.setOffset(10);
		criteria.setSize(20);
		criteria.addOrder("1", true);
		Statement stmt = null;
		ResultSet rset = null;
		try {
			String sql = JDBCQueryHelper.applyFetchParameters(conn, TEST_SQL,
					criteria);
			System.out.println(sql);
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			rset.close();
			stmt.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			fail();
		}

	}
}
