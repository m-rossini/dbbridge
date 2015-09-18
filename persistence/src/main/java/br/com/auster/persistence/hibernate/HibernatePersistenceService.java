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
package br.com.auster.persistence.hibernate;

import java.util.Map;

import org.apache.log4j.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import br.com.auster.common.log.LogFactory;
import br.com.auster.persistence.PersistenceResourceAccessException;
import br.com.auster.persistence.PersistenceService;


/**
 * Defines the common methods to all hibernate persistence services.
 * 
 * @author framos
 * @version $Id: HibernatePersistenceService.java 61 2005-05-21 14:03:24Z framos $
 */
public abstract class HibernatePersistenceService implements PersistenceService {

	
	
    // -------------------------
    // Instance variables
    // -------------------------
	
	private Logger log = LogFactory.getLogger(HibernatePersistenceService.class);

	
	
    // -------------------------
    // Public methods
    // -------------------------
	
	/**
	 * Inherited from <code>PersistenceService</code> 
	 */
	public void commitTransaction(Object _transaction) throws PersistenceResourceAccessException {
		try {
			if (_transaction == null) {
				return;
			}
			Transaction transaction = (Transaction) _transaction;
			if ((transaction.wasCommitted()) || (transaction.wasRolledBack())) {
				log.warn("transaction object was already commited or rolledback");
				return;
			}
			transaction.commit();
			log.debug("transaction commited");
		} catch (HibernateException he) {
			log.warn("could not commit transaction. Rolling it back...", he); 
			rollbackTransaction(_transaction);
		}
	}

	/**
	 * Inherited from <code>PersistenceService</code> 
	 */
	public void rollbackTransaction(Object _transaction) throws PersistenceResourceAccessException {
		try {
			if (_transaction == null) {
				return;
			}
			Transaction transaction = (Transaction) _transaction;
			if ((transaction.wasCommitted()) || (transaction.wasRolledBack())) {
				log.warn("transaction was already commited or rolledback");
				return;
			}
			transaction.rollback();
			log.debug("transaction rolled back");
		} catch (HibernateException he) {
			log.error("could not rollback transaction", he);
			throw new PersistenceResourceAccessException("could not execute rollback operation", he);
		}
	}
	
	/**
	 * Inherited from <code>PersistenceService</code> 
	 */
	public Object beginTransaction(Object _connection) throws PersistenceResourceAccessException {
		try {			
			Session session = (Session) _connection;
			log.debug("starting new transaction");
			return session.beginTransaction();
		} catch (HibernateException he) {
			log.error("could not start a new transaction", he);
            throw new PersistenceResourceAccessException("caught hibernate exception", he);
		}
	}
	
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
	
}
