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

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import br.com.auster.common.io.IOUtils;
import br.com.auster.common.log.LogFactory;
import br.com.auster.common.xml.DOMUtils;
import br.com.auster.persistence.ConfigurationException;
import br.com.auster.persistence.PersistenceResourceAccessException;

/**
 * Enables Hibernate as the persistence service to load and save data objects. Configures
 * 	and gets a reference to a Hibernate Session Factory which is responsable for managing
 * 	sessions to the specified hibernate file's schema.
 * <P>
 * @author framos
 * @version $Id$
 */
public class LocalHibernatePersistenceService extends HibernatePersistenceService {


	
    // ----------------------
    // Instance variables
    // ----------------------
	
	protected SessionFactory factory;

	private Logger log = LogFactory.getLogger(LocalHibernatePersistenceService.class);
	
	
	
    // ----------------------
    // Public methods
    // ----------------------

	/**
	 * Creates new hibernate sessions out of the pre-defined hibernate session factory. This factory must be initialized
	 * using the <code>init()</code> method.
	 * <P>
	 * Inherited from <code>PersistenceService</code> 
	 */
	public Object openResourceConnection() throws PersistenceResourceAccessException {
		return openResourceConnection(null);
    }    

	/**
	 * Creates new hibernate sessions out of the pre-defined hibernate session factory. This factory must be initialized
	 * using the <code>init()</code> method.
	 * <P>
	 * Inherited from <code>PersistenceService</code> 
	 */
	public Object openResourceConnection(Map _properties) throws PersistenceResourceAccessException {
        if (factory == null) {			
            throw new IllegalStateException("service not initialized");            
        }        
        try {
			log.debug("opening session to persistence resource.");
            return factory.openSession();
        } catch (HibernateException he) {
            throw new PersistenceResourceAccessException("could not open a new session", he);
        }
	}
	

	/**
	 * Closes the hibernate session previously crated. 
	 * <P>
	 * Inherited from <code>PersistenceService</code> 
	 */
	public void closeResourceConnection(Object _connection) throws PersistenceResourceAccessException {
		closeResourceConnection(_connection, null);
	}

	/**
	 * Closes the hibernate session previously crated. 
	 * <P>
	 * Inherited from <code>PersistenceService</code> 
	 */
	public void closeResourceConnection(Object _connection, Map _properties, Object _transaction) throws PersistenceResourceAccessException {
        try {
			Session session = (Session) _connection;
			log.debug("closing session to persistence resource.");
            if (session != null) { 
                session.flush();
				commitTransaction(_transaction);
                if ((session.connection() != null) && (!session.connection().getAutoCommit())) {
                    session.connection().commit();
                }
                session.close(); 
            }
        } catch (SQLException sqle) {
            throw new PersistenceResourceAccessException("could not commit transaction", sqle);
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
        try {
            Configuration cfg = new Configuration();
			log.info("Configuring persistence service");

            Element config = DOMUtils.getElement(_configuration, "hibernate", true);
            String configFile = DOMUtils.getAttribute(config, "file", true);
            String bool = DOMUtils.getAttribute(config, "encrypted", true);
            if ((bool == null) || (bool.trim().length() <= 0)) {
                bool = "true";
            }            
			log.info("hibernate file = " + configFile);
            InputStream inStream = IOUtils.openFileForRead(configFile, Boolean.getBoolean(bool));
            if (inStream == null) {
                throw new ConfigurationException("service configuration file does not exist");
            }
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Document doc = dbf.newDocumentBuilder().parse(inStream);
            cfg.configure(doc);
			log.debug("building hibernate configuration object");
            factory = cfg.buildSessionFactory();
			log.debug("got hibernate session factory");
        } catch (IOException ioe) {
          throw new ConfigurationException("I/O exception when reading configuration file", ioe);
        } catch (ParserConfigurationException pce) {
          throw new ConfigurationException("Parser exception when reading configuration file", pce);
        } catch (SAXException se) {
          throw new ConfigurationException("SAX exception when reading configuration file", se);
        } catch (GeneralSecurityException gse) {
          throw new ConfigurationException("Security exception when reading configuration file", gse);
        } catch (HibernateException he) {
            throw new ConfigurationException("could not load hibernate configuration", he);
        }
    }	
}
