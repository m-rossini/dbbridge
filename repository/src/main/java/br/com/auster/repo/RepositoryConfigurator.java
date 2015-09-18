package br.com.auster.repo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;

import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
import org.apache.jackrabbit.core.nodetype.NodeTypeDef;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.core.nodetype.compact.CompactNodeTypeDefReader;
import org.apache.log4j.Logger;

/** 
 * This contains code to initialise the repository for jackrabbit.
 * This is mostly a collection of utilities. 
 * Any jackrabbit specific code needs to go in here.
 */
public class RepositoryConfigurator {

	private static final Logger log = Logger.getLogger(RepositoryConfigurator.class);        

	/** 
	 * @return a new Repository instance. 
	 * There should only be one instance of this in an application.
	 * Generally, one repository (which may be bineded to JNDI) can spawn multiple sessions
	 * for each user as needed.
	 * Typically this would be created on application startup.
	 */
	public Repository createRepository() {
		try {
			return new TransientRepository();
		} catch (IOException e) {
			throw new RulesRepositoryException("Unable to create a Repository instance.", e);
		}
	}

	public Repository createRepository(String configFile, String home) {
		try {
			return new TransientRepository(configFile, home);
		} catch (IOException e) {
			throw new RulesRepositoryException("Unable to create a Repository instance.", e);
		}
	}

	/** Create a new user session for the given repository instance. */
	public Session login(Repository repository) throws LoginException, RepositoryException {
		Session session = repository.login(
				new SimpleCredentials("username", "password".toCharArray()));

		if(session == null) {
			log.error("LOGIN FAILED! SESSION IS NULL!");
			throw new RulesRepositoryException("Unable to login to repository.");
		}
		return session;
	}

	/**
	 * Clears out the entire tree below the rules repository node of the JCR repository.
	 */
	public void clearRulesRepository(Session session) {
		try {

			if (session.getRootNode().hasNode(RulesRepository.RULES_REPOSITORY_NAME)) {
				System.out.println("Clearing rules repository");
				Node node = session.getRootNode().getNode(RulesRepository.RULES_REPOSITORY_NAME);
				node.remove();
			} else {
				System.out.println("Repo not setup, ergo not clearing it !");
			}
		}
		catch(PathNotFoundException e) {                
			//doesn't exist yet. no biggie.
		}          
		catch(RepositoryException e) {
			//this will happen on the first setup. no biggie.
		}
	}    

	/**
	 * Attempts to setup the repository.  If the work that it tries to do has already been done, it 
	 * will return without modifying the repository.
	 * This will not erase any data.
	 * 
	 * @throws RulesRepositoryException     
	 */
	public void setupRulesRepository(Session session) throws RulesRepositoryException {
		System.out.println("Setting up the repository, registering node types etc.");
		try {
			Node root = session.getRootNode();
			Workspace ws = session.getWorkspace();

			//no need to set it up again, skip it if it has.
			boolean registered = false;
			String uris[] = ws.getNamespaceRegistry().getURIs();            
			for (int i = 0; i < uris.length; i++) {
				if (RulesRepository.AUSTER_URI.equals(uris[i])) {
					registered = true;
				}
			}

			if (!registered) {
				ws.getNamespaceRegistry().registerNamespace(RulesRepository.AUSTER_NS, RulesRepository.AUSTER_URI);

				//Note, the order in which they are registered actually does matter !
				this.registerNodeTypesFromCndFile("/node_type_definitions/versionable_node_type.cnd", ws);
				this.registerNodeTypesFromCndFile("/node_type_definitions/dsl_node_type.cnd", ws);            
				this.registerNodeTypesFromCndFile("/node_type_definitions/drl_node_type.cnd", ws);

			}

			// Setup the rule repository node
			Node repositoryNode = RulesRepository.addNodeIfNew(root, RulesRepository.RULES_REPOSITORY_NAME, "nt:folder");

			// Setup the RulePackageItem area        
			RulesRepository.addNodeIfNew(repositoryNode, RulesRepository.DRL_AREA, "nt:folder");

			// Setup the DSL area                
			RulesRepository.addNodeIfNew(repositoryNode, RulesRepository.DSL_AREA, "nt:folder");

			session.save();                        
		}
		catch(Exception e) {
			log.error("Caught Exception", e);
			System.err.println(e.getMessage());
			throw new RulesRepositoryException(e);
		}
	}

	private void registerNodeTypesFromCndFile(String cndFileName, Workspace ws) throws RulesRepositoryException, InvalidNodeTypeDefException {
		try {
			//Read in the CND file
			Reader in = new InputStreamReader(this.getClass().getResourceAsStream(cndFileName));

			// Create a CompactNodeTypeDefReader
			CompactNodeTypeDefReader cndReader = new CompactNodeTypeDefReader(in, cndFileName);

			// Get the List of NodeTypeDef objects
			List ntdList = cndReader.getNodeTypeDefs();

			// Get the NodeTypeManager from the Workspace.
			// Note that it must be cast from the generic JCR NodeTypeManager to the
			// Jackrabbit-specific implementation.
			NodeTypeManagerImpl ntmgr = (NodeTypeManagerImpl)ws.getNodeTypeManager();

			// Acquire the NodeTypeRegistry
			NodeTypeRegistry ntreg = ntmgr.getNodeTypeRegistry();

			// Loop through the prepared NodeTypeDefs
			for(Iterator i = ntdList.iterator(); i.hasNext();) {                               
				// Get the NodeTypeDef...
				NodeTypeDef ntd = (NodeTypeDef)i.next();                                        

				log.debug("Attempting to regsiter node type named: " + ntd.getName());

				// ...and register it            
				ntreg.registerNodeType(ntd);
			}
		}
		catch(InvalidNodeTypeDefException e) {
			log.warn("InvalidNodeTypeDefinitionException caught when trying to add node from CND file: " + cndFileName + ". This will happen if the node type was already registered. " + e);
			throw e;
		}
		catch(Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}    


}
