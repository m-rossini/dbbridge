package br.com.auster.repo;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;

/**
 * The RuleItem class is used to abstract away the details of the underlying JCR repository.
 * It is used to pass information about rules stored in the repository.
 * 
 * @author btruitt
 */
public class ExpanderItem extends VersionableItem {
	private Logger log = Logger.getLogger(ExpanderItem.class);

	/**
	 * The name of the rule node type
	 */
	public static final String EXPANDER_NODE_TYPE_NAME = "auster:dslNodeType";

	public static final String CONTENT = "auster:dslContent";

	/**
	 * Constructs a RuleItem object, setting its node attribute to the specified node.
	 * 
	 * @param rulesRepository the rulesRepository that instantiated this object
	 * @param node the node in the repository that this RuleItem corresponds to
	 * @throws RulesRepositoryException 
	 */
	public ExpanderItem(RulesRepository rulesRepository, Node node)
			throws RulesRepositoryException {
		super(rulesRepository, node);

		try {
			//make sure this node is a rule node       
			if (!(this.node.getPrimaryNodeType().getName().equals(
					EXPANDER_NODE_TYPE_NAME) || isHistoricalVersion())) {
				String message = this.node.getName()
						+ " is not a node of type " + EXPANDER_NODE_TYPE_NAME
						+ " nor nt:version. It is a node of type: "
						+ this.node.getPrimaryNodeType().getName();
				log.error(message);
				throw new RulesRepositoryException(message);
			}
		} catch (Exception e) {
			log.error("Caught exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * returns the contents of the rule node.
	 * It there is a URI, this may need to access the external resource
	 * to grab/sync the latest, but in any case, it should be the real content.
	 */
	public String getContent() throws RulesRepositoryException {
		try {
			Node ruleNode = getVersionContentNode();
			if (ruleNode.hasProperty(CONTENT)) {
				Property data = ruleNode.getProperty(CONTENT);
				return data.getValue().getString();

			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * This will update the rules content (checking it out if it is not already).
	 * This will not save the session or create a new version of the node 
	 * (this has to be done seperately, as several properties may change as part of one edit).
	 */
	public ExpanderItem updateContent(String newContent)
			throws RulesRepositoryException {
		checkout();
		try {
			this.node.setProperty(CONTENT, newContent);
			return this;
		} catch (RepositoryException e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * This updates a user defined property (not one of the intrinsic ones).
	 */
	public void updateUserProperty(String propertyName, String value) {
		if (propertyName.startsWith("auster:")) {
			throw new IllegalArgumentException(
					"Can only set the pre defined fields using the appropriate methods.");
		}
		updateStringProperty(value, propertyName);

	}

	/**
	 * Nicely formats the information contained by the node that this object encapsulates
	 */
	public String toString() {
		try {
			StringBuffer returnString = new StringBuffer();
			returnString.append("Content of dsl item named '" + this.getName()
					+ "':\n");
			returnString.append("Content: " + this.getContent() + "\n");
			returnString.append("------\n");

			returnString.append("------\n");
			returnString.append("--------------\n");
			return returnString.toString();
		} catch (Exception e) {
			throw new RulesRepositoryException(e);
		}
	}

	public VersionableItem getPrecedingVersion()
			throws RulesRepositoryException {
		try {
			Node precedingVersionNode = this.getPrecedingVersionNode();
			if (precedingVersionNode != null) {
				return new ExpanderItem(this.rulesRepository, precedingVersionNode);
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("Caught exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	public VersionableItem getSucceedingVersion()
			throws RulesRepositoryException {
		try {
			Node succeedingVersionNode = this.getSucceedingVersionNode();
			if (succeedingVersionNode != null) {
				return new ExpanderItem(this.rulesRepository, succeedingVersionNode);
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("Caught exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * @return A property value (for a user defined property).
	 */
	public String getUserProperty(String property) {
		return getStringProperty(property);
	}

	public Reader getContentAsReader() {

		try {
			Node ruleNode = getVersionContentNode();
			if (ruleNode.hasProperty(CONTENT)) {
				Property data = ruleNode.getProperty(CONTENT);

				return new InputStreamReader(data.getStream(), CHARSET_ENCODING);
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}
}