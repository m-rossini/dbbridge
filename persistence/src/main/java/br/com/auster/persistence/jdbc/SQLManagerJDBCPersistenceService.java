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
 * Created on Jun 11, 2005
 */
package br.com.auster.persistence.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import br.com.auster.common.log.LogFactory;
import br.com.auster.common.sql.SQLConnectionManager;
import br.com.auster.common.xml.DOMUtils;
import br.com.auster.persistence.ConfigurationException;
import br.com.auster.persistence.PersistenceResourceAccessException;

/**
 * @author framos
 * @version $Id: JNDIJDBCPersistenceService.java 231 2006-03-10 14:02:10Z framos $
 */
public class SQLManagerJDBCPersistenceService extends JDBCPersistenceService {

	
	
	private Logger log = LogFactory.getLogger(SQLManagerJDBCPersistenceService.class);	

	private String poolName;
	
	
	
    /**
     * Inherited from <code>PersistenceService</code> 
     */
    public void commitTransaction(Object _transaction) throws PersistenceResourceAccessException {
    	if ((_transaction != null) && (_transaction instanceof Connection)) {
    		Connection conn = (Connection) _transaction;
    		try {
	    		if (!conn.isClosed()) {
	    			conn.commit();
	    		}
    		} catch (SQLException sqle) {
    			throw new PersistenceResourceAccessException("Could not commit transaction", sqle);
    		}
    	}
    }

    /**
     * Inherited from <code>PersistenceService</code> 
     */
    public void rollbackTransaction(Object _transaction) throws PersistenceResourceAccessException {
    	if ((_transaction != null) && (_transaction instanceof Connection)) {
    		Connection conn = (Connection) _transaction;
    		try {
	    		if (!conn.isClosed()) {
	    			conn.rollback();
	    		}
    		} catch (SQLException sqle) {
    			throw new PersistenceResourceAccessException("Could not rollback transaction", sqle);
    		}
    	}
    }
    
    /**
     * Inherited from <code>PersistenceService</code> 
     */
    public Object beginTransaction(Object _connection) throws PersistenceResourceAccessException {
    	return _connection;
    }
    
	
	public Object openResourceConnection() throws PersistenceResourceAccessException {
		try {
			SQLConnectionManager sqlManager = SQLConnectionManager.getInstance(this.poolName);
			if (sqlManager == null) {
				throw new PersistenceResourceAccessException("Could not locate pool named " + this.poolName);
			}
			return sqlManager.getConnection();
		} catch (SQLException sqle) {
			throw new PersistenceResourceAccessException("JDBC exception when opening new connection", sqle);
		} catch (NamingException ne) {
			throw new PersistenceResourceAccessException("could not locate jdbc datasource : " + this.poolName, ne);
		}
	}


	public Object openResourceConnection(Map _properties) throws PersistenceResourceAccessException {
		return openResourceConnection();
	}

	
	/**
	 * Loads the configuration file from filesystem and configures a hibernate session factory
	 * to be used as factory for new sessions.
	 * <P>
	 * This method is called automatically by the <strong>Facelift Service Locator</strong> before
	 * returning the service reference to the client application.
	 * <P>
	 * Inherited from <code>Service</code> 
	 */
    public void init(Element _configuration) throws ConfigurationException {    	
		log.info("Configuring persistence service");
		// reading jndi name 
        this.poolName = DOMUtils.getAttribute(_configuration, "pool-name", true);
		log.info("pool-name = '" + this.poolName + "'");
    }
}
