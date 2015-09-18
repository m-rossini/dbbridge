package br.com.auster.repo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.log4j.Logger;

/**
 * RulesRepository is the class that defines the bahavior for the rule repository
 * based upon the JCR specification (JSR-170).  
 */
public class RulesRepository {

	public static final String REPOSITORY_HOME = "repository";

	public static final String AUSTER_NS = "auster";

	public static final String AUSTER_URI = "http://www.auster.com.br/auster-repository/1.0";

	private static final Logger log = Logger.getLogger(RulesRepository.class);

	private Map<String, Node> areaNodeCache = new HashMap<String, Node>();

	/**
	 * The name of the rules area of the repository
	 */
	public final static String DRL_AREA = "auster:drl_area";

    /**
     * The name of the DSL area of the repository
     */
    public final static String DSL_AREA = "auster:dsl_area";

    /**
	 * The name of the rules repository within the JCR repository
	 */
	public final static String RULES_REPOSITORY_NAME = "auster:repository";

	private Session session;

	/**
	 * This requires a JCR session be setup, and the repository be configured.
	 */
	public RulesRepository(Session session) {
		this.session = session;
	}

	/**
	 * Will add a node named 'nodeName' of type 'type' to 'parent' if such a node does not already
	 * exist.
	 * 
	 * @param parent the parent node to add the new node to
	 * @param nodeName the name of the new node
	 * @param type the type of the new node
	 * @return a reference to the Node object that is created by the addition, or, if the node already
	 *         existed, a reference to the pre-existant node.
	 * @throws RulesRepositoryException
	 */
	protected static Node addNodeIfNew(Node parent, String nodeName, String type) throws RulesRepositoryException {              
		Node node;
		try {
			node = parent.getNode(nodeName);                
		}
		catch(PathNotFoundException e) {
			//it doesn't exist yet, so create it                       
			try {
				log.debug("Adding new node of type: " + type + " named: " + nodeName + " to parent node named " + parent.getName());

				node = parent.addNode(nodeName, type);
			}
			catch (Exception e1) {                
				log.error("Caught Exception", e);
				throw new RulesRepositoryException(e1);
			}
		}
		catch(Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
		return node;
	}

	/**
	 * Explicitly logout of the underlying JCR repository.  If this is the last session to that
	 * repository, the repository will automatically be shutdown.
	 */
	public void logout() {
		this.session.logout();
	}

	/**
	 * Recursively outputs the contents of the workspace starting from root. The large subtree
	 * called jcr:system is skipped.  This method is just here for programmatic debugging 
	 * purposes, and should be removed.
	 * 
	 * @throws RulesRepositoryException
	 */
	public void dumpRepository() throws RulesRepositoryException {
		try {
			this.dump(this.session.getRootNode().getNode(RULES_REPOSITORY_NAME));
		}
		catch(Exception e) {
			log.error("Caught exception: " + e);
			throw new RulesRepositoryException(e);
		}
	}

	/** 
	 * Recursively outputs the contents of the given node. Used for debugging purposes. 
	 */
	private void dump(final Node node) throws RulesRepositoryException {
		try {
			// First output the node path
			System.out.println(node.getPath());
			// Skip the virtual (and large!) jcr:system subtree
			/*if (node.getName().equals("jcr:system")) {
                return;
            }*/

			// Then output the properties
			PropertyIterator properties = node.getProperties();
			while (properties.hasNext()) {
				Property property = properties.nextProperty();
				if (property.getDefinition().isMultiple()) {
					// A multi-valued property, print all values
					Value[] values = property.getValues();
					for (int i = 0; i < values.length; i++) {
						System.out.println(
								property.getPath() + " = " + values[i].getString());
					}
				} else {
					// A single-valued property
					System.out.println(
							property.getPath() + " = " + property.getString());
				}
			}

			// Finally output all the child nodes recursively
			NodeIterator nodes = node.getNodes();
			while (nodes.hasNext()) {
				dump(nodes.nextNode());
			}
		}
		catch(Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}                

	private Node getAreaNode(String areaName) throws RulesRepositoryException {
		if (areaNodeCache.containsKey(areaName)) {
			return areaNodeCache.get(areaName);
		} else {
			Node folderNode = null;
			int tries = 0;
			while(folderNode == null && tries < 2) {
				try {
					tries++;                                                
					folderNode = this.session.getRootNode().getNode(RULES_REPOSITORY_NAME + "/" + areaName);
				}
				catch(PathNotFoundException e) {
					if(tries == 1) {
						//hmm..repository must have gotten screwed up.  set it up again                
						log.warn("The repository appears to have become corrupted. It will be re-setup now.");
						throw new RulesRepositoryException("Unable to get the main rule repo node. Repository is not setup correctly.", e);
					}
					else {
						log.error("Unable to correct repository corruption");
					}
				}
				catch(Exception e) {
					log.error("Caught Exception", e);
					throw new RulesRepositoryException("Caught exception " + e.getClass().getName(), e);
				}
			}
			if(folderNode == null) {
				String message = "Could not get a reference to a node for " + RULES_REPOSITORY_NAME + "/" + areaName;
				log.error(message);
				throw new RulesRepositoryException(message);
			}
			areaNodeCache.put(areaName, folderNode);

			return folderNode;
		}
	}

	/**
	 * Loads a DRL for the specified DRL name. Will throw
	 * an exception if the specified rule package does not exist.
	 * @param name the name of the package to load 
	 * @return a RulePackageItem object
	 */
	public RuleItem loadRule(String name) throws RulesRepositoryException {
		try {
			Node folderNode = this.getAreaNode(DRL_AREA);
			Node rulePackageNode = folderNode.getNode(name);

			return new RuleItem(this, rulePackageNode);
		} catch(Exception e) {
			log.debug("Unable to load a rule. ", e);
			return null;
		}
	}    

	/**
	 * Loads a Node for the specified uuid. 
	 * @param uuid the uuid of the node to load 
	 * @return a Node object
	 * @throws RulesRepositoryException
	 */
	protected Node loadNodeByUUID(String uuid) throws RulesRepositoryException {

		try {
			return this.session.getNodeByUUID(uuid);
		} catch (Exception e) {
			log.error("Unable to load node by UUID. ", e);
			if (e instanceof RuntimeException ) {
				throw (RuntimeException) e;                
			} else {
				throw new RulesRepositoryException("Unable to load a rule. ", e);
			}
		}
	}    

	/**
	 * Loads a RuleItem for the specified uuid. 
	 * @param uuid the uuid of the package to load 
	 * @return a RulePackageItem object
	 * @throws RulesRepositoryException
	 */
	public RuleItem loadRuleByUUID(String uuid) throws RulesRepositoryException {

		Node node = this.loadNodeByUUID(uuid);

		return new RuleItem(this, node);
	}

	/**
	 * Loads a DSL for the specified uuid. 
	 * @param uuid the uuid of the package to load 
	 * @return a RulePackageItem object
	 * @throws RulesRepositoryException
	 */
	public ExpanderItem loadExpanderByUUID(String uuid) throws RulesRepositoryException {

		Node node = this.loadNodeByUUID(uuid);

		return new ExpanderItem(this, node);
	}

	/**
	 * Loads a DSL for the specified DSL. Will throw
	 * an exception if the specified rule package does not exist.
	 * @param name the name of the package to load 
	 * @return a RulePackageItem object
	 */
	public ExpanderItem loadExpander(String name) throws RulesRepositoryException {
		try {
			Node folderNode = this.getAreaNode(DSL_AREA);
			Node rulePackageNode = folderNode.getNode(name);

			return new ExpanderItem(this, rulePackageNode);
		} catch(Exception e) {
			log.debug("Unable to load a expander. ", e);
			return null;
		}
	}    

	/**
	 * Creates a expander file in the repository. 
	 *   
	 * @param packageName what to name the node added
	 * @param name what description to use for the node
	 * @param content what description to use for the node
	 * @return a ExpanderItem, encapsulating the created node
	 * @throws RulesRepositoryException
	 */
	public ExpanderItem createExpander(String name, String content) throws RulesRepositoryException {

		Node folderNode = this.getAreaNode(DSL_AREA);

		try {
			Node ruleNode = folderNode.addNode(name, ExpanderItem.EXPANDER_NODE_TYPE_NAME);

			ruleNode.setProperty(ExpanderItem.CONTENT, content);

			Calendar lastModified = Calendar.getInstance();
			ruleNode.setProperty(ExpanderItem.LAST_MODIFIED, lastModified);

			ruleNode.setProperty(ExpanderItem.CREATION_DATE, lastModified);

			this.session.save();
			ruleNode.checkin();

			return new ExpanderItem(this, ruleNode);

		} catch (ItemExistsException e) {
			throw new RulesRepositoryException("A DRL name must be unique.", e);
		} catch (RepositoryException e) {
			log.error("Error when creating a new rule package", e);
			throw new RulesRepositoryException(e);
		}

	}

	public ExpanderItem createOrUpdateExpander(String name, String content) throws RulesRepositoryException {
		
		ExpanderItem expander = null;
		if (this.loadExpander(name) == null) {
			expander = this.createExpander(name, content);
		} else {
			expander = this.updateExpander(name, content);
		}

		return expander;
	}

	public RuleItem createOrUpdateRule(String packageName, String name, String content, 
			String expanderName, String expanderContent) throws RulesRepositoryException {

		ExpanderItem expander = this.createOrUpdateExpander(expanderName, expanderContent);

		this.createOrUpdateRule(packageName, name, content, expander);
		
		return null;
	}

	public RuleItem createOrUpdateRule(String packageName, String name, String content) {

		return createOrUpdateRule(packageName, name, content, null);
	}

	/**
	 * Creates or update a rule in the repository. 
	 *   
	 * @param packageName 
	 * @param name 
	 * @param content 
	 * @param dsl 
	 * @return a RuleItem, encapsulating the created node
	 * @throws RulesRepositoryException
	 */
	public RuleItem createOrUpdateRule(String packageName, String name, String content, ExpanderItem expander) {

		RuleItem rule = null;
		if (this.loadRule(name) == null) {
			rule = this.createRule(packageName, name, content, expander);
		} else {
			rule = this.updateRule(name, content, expander);
		}

		return rule;
	}

	/**
	 * Creates a rule in the repository. 
	 *   
	 * @param packageName what to name the node added
	 * @param name what description to use for the node
	 * @param content what description to use for the node
	 * @return a RuleItem, encapsulating the created node
	 * @throws RulesRepositoryException
	 */
	public RuleItem createRule(String packageName, String name, String content) throws RulesRepositoryException {
		
		return this.createRule(packageName, name, content, null);
	}

	/**
	 * Creates a rule in the repository. 
	 *   
	 * @param packageName 
	 * @param name 
	 * @param content 
	 * @param dsl 
	 * @return a RuleItem, encapsulating the created node
	 * @throws RulesRepositoryException
	 */
	public RuleItem createRule(String packageName, String name, String content,
			String expanderName, String expanderContent)
			throws RulesRepositoryException {

		ExpanderItem expander = this.createExpander(expanderName, expanderContent);

		return this.createRule(packageName, name, content, expander);
	}

	/**
	 * Creates a rule in the repository. 
	 *   
	 * @param packageName 
	 * @param name 
	 * @param content 
	 * @param dsl 
	 * @return a RuleItem, encapsulating the created node
	 * @throws RulesRepositoryException
	 */
	public RuleItem createRule(String packageName, String name, String content, ExpanderItem dsl) throws RulesRepositoryException {
		Node folderNode = this.getAreaNode(DRL_AREA);

		try {

			//create the node - see section 6.7.22.6 of the spec
			Node ruleNode = folderNode.addNode(name, RuleItem.RULE_NODE_TYPE_NAME);

			ruleNode.setProperty(RuleItem.PACKAGE_NAME, packageName);
			ruleNode.setProperty(RuleItem.CONTENT, content);

//			rulePackageNode.setProperty(RuleItem.TITLE_PROPERTY_NAME, name);

//			ruleNode.setProperty(RuleItem.DESCRIPTION, "rule");
//			rulePackageNode.setProperty(AssetItem.FORMAT_PROPDrlItem, PackageItem.PACKAGE_FORMAT);

			Calendar lastModified = Calendar.getInstance();
			ruleNode.setProperty(RuleItem.LAST_MODIFIED, lastModified);

			ruleNode.setProperty(RuleItem.CREATION_DATE, lastModified);

			if (dsl != null) {
				Node dslNode = this.loadExpander(dsl.getName()).getNode();
				ruleNode.setProperty(RuleItem.DSL_REFERENCE, dslNode);
			}

			this.session.save();
			ruleNode.checkin();

			return new RuleItem(this, ruleNode);

		} catch (ItemExistsException e) {
			throw new RulesRepositoryException("A DRL name must be unique.", e);
		} catch (RepositoryException e) {
			log.error("Error when creating a new rule package", e);
			throw new RulesRepositoryException(e);
		}

	}
	
	public RuleItem updateRule(String name, String content) throws RulesRepositoryException {

		return updateRule(name, content, null);
	}

	public RuleItem updateRule(String name, String content, 
			String expanderName, String expanderContent) throws RulesRepositoryException {

		ExpanderItem expander = this.createOrUpdateExpander(expanderName, expanderContent);

		return updateRule(name, content, expander);
	}

	public RuleItem updateRule(String name, String content, ExpanderItem dsl) throws RulesRepositoryException {

		RuleItem rule = null;

		try {
			rule = this.loadRule(name);
			rule.updateContent(content);
			rule.updateExpander(dsl);

			Calendar lastModified = Calendar.getInstance();
			rule.getNode().setProperty(RuleItem.LAST_MODIFIED, lastModified);

			this.session.save();
			rule.checkin("Rule update.");

		} catch (Exception e) {
			log.error("Error updating rule: " + name + ".", e);
		}

		return rule;
	}
	
	public ExpanderItem updateExpander(String name, String content) throws RulesRepositoryException {
		
		ExpanderItem expander = null;
		try {
			expander = this.loadExpander(name);
			expander.updateContent(content);

			Calendar lastModified = Calendar.getInstance();
			expander.getNode().setProperty(RuleItem.LAST_MODIFIED, lastModified);

			this.session.save();
			expander.checkin("Expander update.");

		} catch (Exception e) {
			log.error("Error updating expander: " + name + ".", e);
		}
		
		return expander;
	}

	/**
	 * @return an Iterator which will provide RuleItem's.
	 * This will show ALL the packages, only returning latest versions, by default.
	 */
	public List<RuleItem> listRuleItems() {

		ArrayList<RuleItem> list = new ArrayList<RuleItem>();
	
		Node folderNode = this.getAreaNode(DRL_AREA);

		try {
			Iterator it = folderNode.getNodes();
			while (it.hasNext()) {
				Node node = (Node) it.next();
				if (node.getPrimaryNodeType().getName().equals(RuleItem.RULE_NODE_TYPE_NAME)) {
					list.add(this.loadRuleByUUID(node.getUUID()));
				}
			}
		} catch ( RepositoryException e ) {
			throw new RulesRepositoryException(e);
		}

		return list;
	}

	/**
	 * @return an Iterator which will provide ExpanderItem's.
	 * This will show ALL the packages, only returning latest versions, by default.
	 */
	public List<ExpanderItem> listExpanderItems() {

		ArrayList<ExpanderItem> list = new ArrayList<ExpanderItem>();
	
		Node folderNode = this.getAreaNode(DSL_AREA);

		try {
			Iterator it = folderNode.getNodes();
			while (it.hasNext()) {
				Node node = (Node) it.next();
				if (node.getPrimaryNodeType().getName().equals(ExpanderItem.EXPANDER_NODE_TYPE_NAME)) {
					list.add(this.loadExpanderByUUID(node.getUUID()));
				}
			}

		} catch ( RepositoryException e ) {
			throw new RulesRepositoryException(e);
		}

		return list;
	}

	/**
	 * @return The JCR session that this repository is using.
	 */
	public Session getSession() {
		return this.session;
	}

	/**
	 * Save any pending changes.
	 */
	public void save() {
		try {
			this.session.save();
		} catch ( Exception e ) {
			if (e instanceof RuntimeException ) {
				throw (RuntimeException) e;
			} else {
				throw new RulesRepositoryException(e);
			}
		}
	}

	public String calculateNextVersion(String currentVersionLabel, VersionableItem item) {

		if (currentVersionLabel == null || currentVersionLabel.trim().equals("")) {
			return "1";
		}
		try {
			int current = Integer.parseInt(currentVersionLabel);
			return Integer.toString(++current);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Unable to calculate next version number for version: " + currentVersionLabel);
		}
	}
}