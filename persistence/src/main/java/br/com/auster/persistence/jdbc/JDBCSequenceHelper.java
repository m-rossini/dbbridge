/*
 * Copyright (c) 2004 TTI Tecnologia. All Rights Reserved.
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
 * Created on Jun 13, 2005
 */
package br.com.auster.persistence.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.auster.common.log.LogFactory;

/**
 * This JDBC class helps when working with sequences. It creates part of the sql statement to access sequences based on the 
 * 	database implementation of the current database connection, and also executes those sqls to fetch the next value of such
 * 	sequences.   
 *   
 * @author framos
 * @version $Id: JDBCSequenceHelper.java 158 2005-11-04 13:03:09Z framos $
 */
public abstract class JDBCSequenceHelper {

	
	private static Logger log = LogFactory.getLogger(JDBCSequenceHelper.class);
	protected static Map transalationMap;
	protected static Map nextValueMap;
	
	
	
	static {
		transalationMap = new HashMap();
		transalationMap.put(JDBCHelperConstants.DBNAME_POSTGRESQL, "nextval(''{0}'')");
		transalationMap.put(JDBCHelperConstants.DBNAME_ORACLE, "{0}.nextval");
		
		nextValueMap = new HashMap();
		nextValueMap.put(JDBCHelperConstants.DBNAME_POSTGRESQL, "SELECT nextval(''{0}'')");
		nextValueMap.put(JDBCHelperConstants.DBNAME_ORACLE, "SELECT {0}.nextval FROM dual");
	}
	
	
	/**
	 * Creates a string representation of how to get the next value of a sequence. It uses the connection metadata
	 * 	information to determine the database implemetation in use, and so define the correct SQL statement needed
	 * 	to access the sequence.
	 * <P>
	 * In the end, the string representation formatted with the sequence name will be returned to the calling code. 
	 * <P>
	 * If any SQL exception is raised, then it is propagated back to the calling code. Also, if the database in question 
	 * 	is not implemented by this class, a <code>IllegalArgumentException</code> will be raised; but if the database
	 * 	is implemented and has no means of extracting the next value of a sequence (or does not implement sequences), then
	 * 	an empty string will be returned.
	 * 	
	 * 
	 * @param _conn he current database connection 
	 * @param _sequenceName the name of the sequence object
	 * @return the string represtation for the database implementation on how to get the next value of a sequence
	 * 
	 * @throws SQLException if any SQL exception is raised by the JDBC driver
	 */
	public static String translate(Connection _conn, String _sequenceName) throws SQLException {
		
		DatabaseMetaData meta = _conn.getMetaData();
		String dbName = meta.getDatabaseProductName();
		String pattern = (String) transalationMap.get(dbName);
		
		if (pattern == null) {
			log.warn("JDBC sequence translator not compliant with database " + dbName);
			throw new IllegalArgumentException("JDBC sequence translator not compliant with database " + dbName);
		}
		String result = MessageFormat.format(pattern, new Object[] { _sequenceName } );
		log.debug("sequence statement for " + dbName + " is '" + result + "'");
		return result;
	}
	
	/**
	 * Returns the next value of a sequence. It uses the connection metadata information to determine the  
	 * 	database implemetation in use, and so define the correct SQL statement needed to access the sequence.
	 * <P>
	 * Unlike the <code>translate()</code> method, this one not only finds out the correct SQL statement but also
	 * 	runs it against the connection to determine the next value of the specified sequence.
	 * </P>  
	 * If any SQL exception is raised, then it is propagated back to the calling code. Also, if the database in question 
	 * 	is not implemented by this class, a <code>IllegalArgumentException</code> will be raised; but if the database
	 * 	is implemented and has no means of extracting the next value of a sequence (or does not implement sequences), then
	 * 	<code>-1</code> is returned.
	 * 	
	 * @param _conn he current database connection 
	 * @param _sequenceName the name of the sequence object
	 * @return the next value of the defined sequence 
	 * 
	 * @throws SQLException if any SQL exception is raised by the JDBC driver
	 */
	public static long nextValue(Connection _conn, String _sequenceName) throws SQLException {
		
		DatabaseMetaData meta = _conn.getMetaData();
		String dbName = meta.getDatabaseProductName();
		String pattern = (String) nextValueMap.get(dbName);
		
		if (pattern == null) {
			log.warn("JDBC sequence translator not compliant with database " + dbName);
			throw new IllegalArgumentException("JDBC sequence translator not compliant with database " + dbName);
		}
		String result = MessageFormat.format(pattern, new Object[] { _sequenceName } );
		if (result.length() < 1) {
			return -1;
		}
		Statement stmt = null;
		ResultSet rset = null;		
		try {
			stmt = _conn.createStatement();
			rset = stmt.executeQuery(result);
			if (rset.next()) {
				return rset.getLong(1);
			}
			log.warn("could not get next value for sequence " + _sequenceName + ". Looks like it does not exist");
			return -1;
		} finally {
			if (rset != null) { rset.close(); }
			if (stmt != null) { stmt.close(); }
		}
	}

}
