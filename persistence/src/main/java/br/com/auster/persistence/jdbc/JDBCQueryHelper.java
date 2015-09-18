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
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import br.com.auster.common.log.LogFactory;
import br.com.auster.persistence.FetchCriteria;
import br.com.auster.persistence.OrderClause;

/**
 * @author framos
 * @version $Id: JDBCQueryHelper.java 141 2005-08-24 12:50:29Z framos $
 */
public abstract class JDBCQueryHelper {

	
	protected static String SQL_OPEN_TOKEN = "${";
	protected static String SQL_END_TOKEN = "}";

	protected static final String SQL_OPEN_TOKEN_SYSPROP = "auster.persistence.sql.openToken";
	protected static final String SQL_END_TOKEN_SYSPROP = "auster.persistence.sql.endToken";
	
	
	protected static Map offsetLimitMap;
	private static Logger log = LogFactory.getLogger(JDBCSequenceHelper.class);
	
	static { 
		offsetLimitMap = new HashMap();
		offsetLimitMap.put(JDBCHelperConstants.DBNAME_POSTGRESQL, "{0} {1} limit {3} offset {2} ");
		offsetLimitMap.put(JDBCHelperConstants.DBNAME_ORACLE,  
				                "select * from ( select origsql_.*, rownum rownbr_ from (" +
				                "{0} {1} ) origsql_ where rownum <= ({3}+{2}) ) where rownbr_ > {2}");
		
		if (System.getProperties().containsKey(SQL_OPEN_TOKEN_SYSPROP)) {
			SQL_OPEN_TOKEN = System.getProperty(SQL_OPEN_TOKEN_SYSPROP);
		}
		if (System.getProperties().containsKey(SQL_END_TOKEN_SYSPROP)) {
			SQL_END_TOKEN = System.getProperty(SQL_END_TOKEN_SYSPROP);
		}
	}
	

	/**
	 * Creates a new SQL statement, adding order clauses and offset/limit parameters to the specified query.  It uses   
	 * 	the connection metadata information to determine the database implemetation in use, and so define the correct
	 *  SQL syntax. 
	 * <P>
	 * The result SQL is the concatenation of the incoming sql plus any order clauses and offset/limit definitions, according
	 * 	to the values in the <code>_fetch</code> parameter. 
	 *  
	 * @param _conn the current database connection
	 * @param _sql the sql statement to be modified
	 * @param _fetch the fetching parameters
	 * 
	 * @return the modified sql, as defined above
	 * 
	 * @throws SQLException if any SQL exception is raised by the JDBC driver
	 */
	public static String applyFetchParameters(Connection _conn, String _sql, FetchCriteria _fetch) throws SQLException {
		
		if (_fetch == null) {
			log.debug("cannot set fetch parameters if fetch criteria is null");
			return _sql;
		}
		
		DatabaseMetaData meta = _conn.getMetaData();
		String dbName = meta.getDatabaseProductName();
		String pattern = (String) offsetLimitMap.get(dbName);
		if (pattern == null) {
			log.warn("JDBC sequence translator not compliant with database " + dbName);
			throw new IllegalArgumentException("JDBC sequence translator not compliant with database " + dbName);
		}
		
		Iterator iterator = _fetch.orderIterator();
		String orderBy = JDBCHelperConstants.ORDERBY_TOKEN;
		while (iterator.hasNext()) {
			OrderClause clause = (OrderClause) iterator.next();
			orderBy += clause.getFieldName() + (clause.isAscending() ? " ASC " : " DESC ");
			orderBy += ",";
		}
		if (!orderBy.equals(JDBCHelperConstants.ORDERBY_TOKEN)) {
			orderBy = orderBy.substring(0, orderBy.length()-1);
		} else {
			orderBy = "";
		}
		
		if ( (_fetch.getOffset() >= 0) && (_fetch.getSize() > 0) ) {
			return MessageFormat.format(pattern, new Object[] { _sql, orderBy, String.valueOf(_fetch.getOffset()), String.valueOf(_fetch.getSize()) } );
		}
		return _sql;
	}	
	
	

	public static String createSQL(String _pattern, ResourceBundle _bundle) {
		return createSQL(_pattern, _bundle, SQL_OPEN_TOKEN, SQL_END_TOKEN);
	}

	public static String createSQL(String _pattern, ResourceBundle _bundle, String _openToken, String _endToken) {
		if ((_pattern == null) || (_openToken == null) || (_endToken == null)) {
			return _pattern;
		}
		String[] keymap = buildKeyMap(_pattern, _openToken, _endToken);
		// there are no keys to replace in selected query 
		if ((keymap == null) || (keymap.length <= 1)) { 
			return _pattern; 
		}
		// replacing tokens with bundled values
		String[] values = new String[keymap.length-1];
		for (int i=0; i < keymap.length-1; i++) {
			try {
				values[i] = _bundle.getString(keymap[i]);
			} catch (MissingResourceException mre) {
				log.warn("No value found for key '" + keymap[i] + "'. Defaulting to key itself.");
				values[i] = keymap[i];
			}
		}
		return MessageFormat.format(keymap[keymap.length-1], values);
	}
	
	
	protected static final String[] buildKeyMap(String _pattern, String _openToken, String _endToken) {
		int keyCount = 0;
		int lastPos = 0;
		StringBuffer sb = new StringBuffer();
		List keymap = new LinkedList();
		// looking up for first key 
		int startPos = _pattern.indexOf(_openToken, lastPos);
		int endPos = _pattern.indexOf(_endToken, startPos);
		while (startPos < endPos) {
			// saving sql portion
			sb.append(_pattern.substring(lastPos, startPos));
			sb.append("{");
			sb.append(keyCount++);
			sb.append("}");
			// adding key to list
			keymap.add(_pattern.substring(startPos+_openToken.length(), endPos));
			// setting new lastPos
			lastPos = endPos+_endToken.length();
			// looking up for next key 
			startPos = _pattern.indexOf(_openToken, lastPos);
			if (startPos < 0) { 
				sb.append(_pattern.substring(endPos+_endToken.length()));
				break; 
			}
			endPos = _pattern.indexOf(_endToken, startPos);
		}
		keymap.add(sb.toString());
		return (String[]) keymap.toArray(new String[] {});
	}	
}
