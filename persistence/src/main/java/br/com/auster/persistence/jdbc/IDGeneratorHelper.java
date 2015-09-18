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
 * Created on 02/06/2006
 */
package br.com.auster.persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import br.com.auster.common.log.LogFactory;

/**
 * @author framos
 * @Id $Id$
 *
 */
public abstract class IDGeneratorHelper {

	
	private static final Logger log = LogFactory.getLogger(IDGeneratorHelper.class);
	
	
	public static final String IDGENERATOR_TABLE    = "table";
	public static final String IDGENERATOR_SEQUENCE = "sequence";
	
	
	protected static final String GENERATOR_TABLE_NAME = "id_generator";
	protected static final String GENERATOR_DEFAULT_KEY = "default";
	protected static final String GENERATOR_TABLE_COL_KEY = "generator_key";
	protected static final String GENERATOR_TABLE_COL_SEQ = "current_id";
	
	protected static final String GENERATOR_UPDATE_SQL = 
		"update " + GENERATOR_TABLE_NAME + 
			" set " + GENERATOR_TABLE_COL_SEQ + " = " + GENERATOR_TABLE_COL_SEQ + "+1" +
			" where " + GENERATOR_TABLE_COL_KEY + " = ?";

	protected static final String GENERATOR_SELECT_SQL = 
		"select " + GENERATOR_TABLE_COL_SEQ +
		   " from " + GENERATOR_TABLE_NAME + 
		   " where " + GENERATOR_TABLE_COL_KEY + " = ?";

	
	/**
	 * Generates a new long id using the id generation table with the default key {@value #GENERATOR_DEFAULT_KEY}.
	 * <p>
	 * If there is any problems with the database connection, the id generator table or the specified key, then an
	 * 	exception will be raised. Otherwise, the next long number for such key will be returned.
	 * 
	 * @param _conn a database connection
	 * 
	 * @return the next id for the default key {@value #GENERATOR_DEFAULT_KEY}.
	 * 
	 * @throws SQLException If any database-related problems where detected
	 */
	public static long nextId(Connection _conn) throws SQLException {
		return nextId(_conn, GENERATOR_DEFAULT_KEY, IDGENERATOR_TABLE);
	}
	
	/**
	 * Works exaclty as the {@link #nextId(Connection)} version, but instead of using the default key value it uses
	 * 	the one specified in the <code>_key</code> parameter.
	 * 
	 * @param _conn a database connection
	 * @param _key the key for id generation
	 * 
	 * @return the next id for the specified key 
	 * 
	 * @throws SQLException If any database-related problems where detected
	 */
	public static long nextId(Connection _conn, String _key) throws SQLException {
		return nextId(_conn, _key, IDGENERATOR_TABLE);
	}

	/**
	 * Generates a new long id either using a database table or sequence. This selection is based on the 
	 * 	<code>_type</code> parameter. To select the type of geration , use the {@value #IDGENERATOR_SEQUENCE} 
	 *  or {@value #IDGENERATOR_TABLE} constant.
	 * <p>
	 * In case the table method is selected, this method will behavior exactly as {@link #nextId(Connection, String)}.
	 * <p>
	 * In case its the sequence method, the <code>_key</code> parameter is the name of the sequence. 
	 * <p>
	 * If there is any problems with the database connection, the id generator table or the specified key, then an
	 * 	exception will be raised. Otherwise, the next long number for such key will be returned.
	 * 
	 * @param _conn a database connection
	 * @param _key the key for id generation
	 * @param _type which type of id generation to use
	 * 
	 * @return the next id for the specified key 
	 * 
	 * @throws SQLException If any database-related problems where detected
	 */
	public static long nextId(Connection _conn, String _key, String _type) throws SQLException {
		// must have a database connection
		if (_conn == null) {
			throw new IllegalArgumentException("cannot generate id without database connection");
		}
		// validating _key parameter
		if ((_key == null) || (_key.trim().length() <= 0)) {
			log.warn("KEY for id generation was null. Defaulting to " + GENERATOR_DEFAULT_KEY);
			_key = GENERATOR_DEFAULT_KEY;
		}
		// validating id generator type
		if (IDGENERATOR_SEQUENCE.equals(_type)) {
			return nextIdBySequence(_conn, _key);
		} else if (IDGENERATOR_TABLE.equals(_type)) {
			return nextIdByTable(_conn, _key);
		} 
		throw new IllegalArgumentException("specified TYPE '" + _type + "' not defined");
	}
	
	
	private static final long nextIdBySequence(Connection _conn, String _sequenceName) throws SQLException {
		return JDBCSequenceHelper.nextValue(_conn, _sequenceName);
	}

	private synchronized static final long nextIdByTable(Connection _conn, String _keyName) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			// updates sequence
			stmt = _conn.prepareStatement(GENERATOR_UPDATE_SQL);
			stmt.setString(1, _keyName);
			if (stmt.executeUpdate() != 1) {
				throw new SQLException("KEY " + _keyName + " does not point to a single entry in the id generator table");
			}
			stmt.close();
			// select new value
			stmt = _conn.prepareStatement(GENERATOR_SELECT_SQL);
			stmt.setString(1, _keyName);
			rset = stmt.executeQuery();
			if (!rset.next()) {
				throw new SQLException("Could not select next id for KEY " + _keyName);
			}
			return rset.getLong(1);
		} finally {
			if (rset != null) { rset.close(); }
			if (stmt != null) { stmt.close(); }
		}
	}
	
}
