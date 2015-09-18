/*
 * Copyright (c) 2004 Auster Solutions. All Rights Reserved.
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
 * Created on Feb 28, 2005
 */
package br.com.auster.persistence.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.auster.common.log.LogFactory;
import br.com.auster.persistence.PersistenceResourceAccessException;
import br.com.auster.persistence.PersistenceService;


/**
 * Defines the common methods to all jdbc persistence services.
 * 
 * @author framos
 * @version $Id: JDBCPersistenceService.java 158 2005-11-04 13:03:09Z framos $
 */
public abstract class JDBCPersistenceService implements PersistenceService {

	
	
    // -------------------------
    // Instance variables
    // -------------------------
	
	private static final Logger log = LogFactory.getLogger(JDBCPersistenceService.class);

	protected String txName;
	

	
    // -------------------------
    // Public methods
    // -------------------------
	
	/**
	 * Inherited from <code>PersistenceService</code> 
	 */
	public void closeResourceConnection(Object _connection) throws PersistenceResourceAccessException {
		closeResourceConnection(_connection, null, null);
	}
	
	/**
	 * Inherited from <code>PersistenceService</code> 
	 */
	public void closeResourceConnection(Object _connection, Object _transaction) throws PersistenceResourceAccessException {
		closeResourceConnection(_connection, null, _transaction);
	}

	/**
	 * Inherited from <code>PersistenceService</code> 
	 */
	public void closeResourceConnection(Object _connection, Map _properties) throws PersistenceResourceAccessException {
		closeResourceConnection(_connection, _properties, null);
	}

    public void closeResourceConnection(Object _connection, Map _properties, Object _transaction) throws PersistenceResourceAccessException {
        
        log.debug("closing connection to persistence resource");
        try {
            Connection conn = (Connection) _connection;
            if (conn != null) {
//				int status = ((Transaction)_transaction).getStatus(); 
                commitTransaction(_transaction);
//				if (status == Status.STATUS_ACTIVE) {
//					conn.commit();
//				}
                conn.close();
            }
        } catch (SQLException sqle) {
            throw new PersistenceResourceAccessException("JDBC exception when opening new connection", sqle);
//        } catch (SystemException se) {
//			throw new PersistenceResourceAccessException("caught system exception when rolling back a JDBC transaction", se);
		}
    }
    
}
