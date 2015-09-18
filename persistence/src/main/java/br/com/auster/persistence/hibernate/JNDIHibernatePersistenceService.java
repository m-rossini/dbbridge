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
 * Created on Mai 10, 2005
 */
package br.com.auster.persistence.hibernate;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import br.com.auster.common.log.LogFactory;
import br.com.auster.common.xml.DOMUtils;
import br.com.auster.persistence.ConfigurationException;
import br.com.auster.persistence.PersistenceResourceAccessException;

/**
 * Enables Hibernate as the persistence service to load and save data objects. Also, it 
 * 	locates the Hibernate Session Factory in a JNDI environment, so this factory can be
 * 	deployed in a J2EE application server and the sessions be managed by it. 
 * <P>
 * @author framos
 * @version $Id$
 */
public class JNDIHibernatePersistenceService extends HibernatePersistenceService {


	
    // ----------------------
    // Class variables
    // ----------------------
	
    public static final String HIBERNATEFACTORY_JNDI_PROPERTY = "factory.jndiName";

	
	
    // ----------------------
    // Instance variables
    // ----------------------
	
	private String jndiName;
	private Hashtable jndiProperties;
	
	private Logger log = LogFactory.getLogger(JNDIHibernatePersistenceService.class);
	
	
    // ----------------------
    // public methods
    // ----------------------

	/**
	 * Creates new hibernate sessions out of the pre-defined hibernate session factory. The factory is configured by the
	 * JBoss hibernate context service at deployment time.
	 * <P>
	 * The jndi path to the hibernate factory is specified as a configuration parameter for the service. If not defined, this
	 * method searches the property service for the key <code>HIBERNATEFACTORY_JNDI_PROPERTY</code>, looking up first for the
	 * property realm configured. Again, the realm may not be specified and if so, the default realm will be used.
	 * <P>
	 * If no value is found, then an exception will be raised.
	 * <P>
	 * Inherited from <code>PersistenceService</code> 
	 */
	public Object openResourceConnection() throws PersistenceResourceAccessException {
		Properties property = System.getProperties();
		Map configuredProperties = new HashMap();
		if (jndiName == null) {
			log.debug("no jndi-name defined for opening connection to persistence resource. Checking for property");
				configuredProperties.put(HIBERNATEFACTORY_JNDI_PROPERTY, 
						                 property.getProperty(HIBERNATEFACTORY_JNDI_PROPERTY));
		} else {
			log.debug("requesting connection to persistence resource located at " + jndiName);
			configuredProperties.put(HIBERNATEFACTORY_JNDI_PROPERTY, jndiName);
		}
		return openResourceConnection(configuredProperties);
    }    

	/**
	 * Inherited from <code>PersistenceService</code> 
	 */
	public Object openResourceConnection(Map _properties) throws PersistenceResourceAccessException {
		if (_properties.get(HIBERNATEFACTORY_JNDI_PROPERTY) == null) {
			throw new IllegalStateException("resource path not defined");
		}
		String jndiName = (String)_properties.get(HIBERNATEFACTORY_JNDI_PROPERTY);
		try {
			Context context = new InitialContext(jndiProperties);
			SessionFactory factory = (SessionFactory) context.lookup(jndiName);
			log.debug("got Hibernate Session Factory from jndi environment");
			return factory.openSession();
		} catch (HibernateException he) {
			throw new PersistenceResourceAccessException("hibernate exception when opening new session", he);
		} catch (NamingException ne) {
			throw new PersistenceResourceAccessException("could not locate hibernate factory : " + jndiName, ne);
		}
	}

	/**
	 * Inherited from <code>PersistenceService</code> 
	 */
	public void closeResourceConnection(Object _connection, Map _properties, Object _transaction) throws PersistenceResourceAccessException {
		log.debug("closing connection to persistence resource");
		try {
			Session session = (Session) _connection;
            if (session != null) { 
                session.flush();
				commitTransaction(_transaction);
				session.close();
            }
        } catch (HibernateException he) {
            throw new PersistenceResourceAccessException("caught hibernate exception", he);
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
    }
	
}


