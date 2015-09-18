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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import br.com.auster.common.log.LogFactory;
import br.com.auster.common.xml.DOMUtils;
import br.com.auster.persistence.ConfigurationException;
import br.com.auster.persistence.PersistenceResourceAccessException;

/**
 * @author framos
 * @version $Id: JNDIJDBCPersistenceService.java 231 2006-03-10 14:02:10Z framos $
 */
public class JNDIJDBCPersistenceService extends JDBCPersistenceService {

	
	
	private String jndiName;
	private Hashtable jndiProperties;

	private Logger log = LogFactory.getLogger(JNDIJDBCPersistenceService.class);
	
	public static final String JDBCFACTORY_JNDI_PROPERTY = "factory.jndiName";
	
	
	
	
    /**
     * Inherited from <code>PersistenceService</code> 
     */
    public void commitTransaction(Object _transaction) throws PersistenceResourceAccessException {
		if (_transaction == null) {
			return;
		}
        Transaction tx = (Transaction) _transaction;
        try {
            if (tx.getStatus() == Status.STATUS_ACTIVE) {
                tx.commit();
            } else {
                log.warn("not a commitable status " + tx.getStatus());
            }
        } catch (SystemException se) {
            throw new PersistenceResourceAccessException("caught system exception when rolling back a JDBC transaction", se);
        } catch (SecurityException se) {
            throw new PersistenceResourceAccessException("violated security constraints when commintg a JDBC transaction", se);
        } catch (RollbackException re) {
            throw new PersistenceResourceAccessException("rollback exception when commiting a JDBC transaction", re);
        } catch (HeuristicMixedException hme) {
            throw new PersistenceResourceAccessException("heuristic mixed exception when commiting a JDBC transaction", hme);
        } catch (HeuristicRollbackException hre) {
            throw new PersistenceResourceAccessException("heuristic rollback exception when commiting a JDBC transaction", hre);
        }
    }

    /**
     * Inherited from <code>PersistenceService</code> 
     */
    public void rollbackTransaction(Object _transaction) throws PersistenceResourceAccessException {
		if (_transaction == null) {
			return;
		}
        Transaction tx = (Transaction) _transaction;
        try {
            if (tx.getStatus() == Status.STATUS_ACTIVE) {
                tx.rollback();
            } else {
                log.warn("not a rollbackable status " + tx.getStatus());
            }
            
        } catch (IllegalStateException ise) {
            throw new PersistenceResourceAccessException("Illegal state for rolling back current JDBC transaction", ise);
        } catch (SystemException se) {
            throw new PersistenceResourceAccessException("caught system exception when rolling back a JDBC transaction", se);
        }
    }
    
    /**
     * Inherited from <code>PersistenceService</code> 
     */
    public Object beginTransaction(Object _connection) throws PersistenceResourceAccessException {
    	if (this.txName == null) {
    		log.warn("No transaction JNDI defined.");
    		return null;
    	}
        try {
            Context ctx = new InitialContext();
            TransactionManager txMgr = (TransactionManager) ctx.lookup(this.txName);
			if ((txMgr.getStatus() == Status.STATUS_NO_TRANSACTION) || (txMgr.getStatus() == Status.STATUS_UNKNOWN)) {
				txMgr.begin();
			}
            return txMgr.getTransaction();
        } catch (NamingException ne) {
            throw new PersistenceResourceAccessException("could not locate the transaction manager", ne);
        } catch (NotSupportedException nse) {
            throw new PersistenceResourceAccessException("operation not supported", nse);
        } catch (SystemException se) {
            throw new PersistenceResourceAccessException("caught system exception", se);
        }
    }
    
	
	public Object openResourceConnection() throws PersistenceResourceAccessException {
		
		Properties property = System.getProperties();
		Map configuredProperties = new HashMap();
		if (jndiName == null) {
			log.debug("no jndi-name defined for opening connection to persistence resource. Checking for property ");
				configuredProperties.put(JDBCFACTORY_JNDI_PROPERTY, 
						                 property.getProperty(JDBCFACTORY_JNDI_PROPERTY));
		} else {
			log.debug("requesting connection to persistence resource located at " + jndiName);
			configuredProperties.put(JDBCFACTORY_JNDI_PROPERTY, jndiName);
		}
		return openResourceConnection(configuredProperties);

	}


	public Object openResourceConnection(Map _properties) throws PersistenceResourceAccessException {
		
		if (_properties.get(JDBCFACTORY_JNDI_PROPERTY) == null) {
			throw new IllegalStateException("resource path not defined");
		}
		String jndiName = (String)_properties.get(JDBCFACTORY_JNDI_PROPERTY);
		try {
			Context context = new InitialContext(jndiProperties);
			DataSource ds = (DataSource) context.lookup(jndiName);
			log.debug("got JDBC datasource from jndi environment");
			Connection conn = ds.getConnection();
            //conn.setAutoCommit(false);
            return conn;
		} catch (SQLException sqle) {
			throw new PersistenceResourceAccessException("JDBC exception when opening new connection", sqle);
		} catch (NamingException ne) {
			throw new PersistenceResourceAccessException("could not locate jdbc datasource : " + jndiName, ne);
		}
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
        jndiName = DOMUtils.getAttribute(_configuration, "jndi-name", false);
		log.info("jndi-name = '" + jndiName + "'");
		// optional configuration attributes for JNDI context creation
		Element propList = DOMUtils.getElement(_configuration, "jndi-properties", false);
		if (propList != null) {
			NodeList props = DOMUtils.getElements(propList, null);
			jndiProperties = new Hashtable(); 
			for (int i=0; i < props.getLength(); i++) {
				Element prop = (Element)props.item(i);
				jndiProperties.put(prop.getNodeName(), DOMUtils.getText(prop).toString());
			}
		}
		// optional Tx JNDI name
        this.txName = DOMUtils.getAttribute(_configuration, "tx-jndi", false);
        if ((this.txName == null) || (this.txName.trim().length() <= 0)) {
        	log.warn("Transaction manager not configured.");
        	this.txName = null;
        } else {
        	log.info("TX JNDI = '" + this.txName + "'");
        }
    }
}
