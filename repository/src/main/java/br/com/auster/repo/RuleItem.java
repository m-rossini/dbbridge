package br.com.auster.repo;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;

public class RuleItem extends VersionableItem {
	private Logger log = Logger.getLogger(RuleItem.class);

	/**
	 * The name of the rule node type
	 */
	public static final String RULE_NODE_TYPE_NAME = "auster:drlNodeType";

	public static final String CONTENT = "auster:content";

	/**
	 * The name of the date effective property on the rule node type
	 */
	public static final String DATE_EFFECTIVE = "auster:dateEffective";

	/**
	 * The name of the date expired property on the rule node type
	 */
	public static final String DATE_EXPIRED = "auster:dateExpired";

	public static final String PACKAGE_NAME = "auster:packageName";
	
	public static final String DSL_REFERENCE = "auster:dslReference";

	/**
	 * Constructs a RuleItem object, setting its node attribute to the specified node.
	 * 
	 * @param rulesRepository the rulesRepository that instantiated this object
	 * @param node the node in the repository that this RuleItem corresponds to
	 * @throws RulesRepositoryException 
	 */
	public RuleItem(RulesRepository rulesRepository, Node node)
			throws RulesRepositoryException {
		super(rulesRepository, node);

		try {
			//make sure this node is a rule node       
			if (!(this.node.getPrimaryNodeType().getName().equals(
					RULE_NODE_TYPE_NAME) || isHistoricalVersion())) {
				String message = this.node.getName()
						+ " is not a node of type " + RULE_NODE_TYPE_NAME
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

//	public ExpanderItem getDslReference() throws RulesRepositoryException {
//		try {
//			Node ruleNode = getVersionContentNode();
//			if (ruleNode.hasProperty(DSL_REFERENCE)) {
//				Property data = ruleNode.getProperty(DSL_REFERENCE);
//				
//				return data.getValue().getString();
//
//			} else {
//				return null;
//			}
//		} catch (Exception e) {
//			log.error("Caught Exception", e);
//			throw new RulesRepositoryException(e);
//		}
//	}

	/**
	 * @return the date the rule becomes effective
	 * @throws RulesRepositoryException
	 */
	public Calendar getDateEffective() throws RulesRepositoryException {
		try {
			Node ruleNode = getVersionContentNode();

			Property dateEffectiveProperty = ruleNode
					.getProperty(DATE_EFFECTIVE);
			return dateEffectiveProperty.getDate();
		} catch (PathNotFoundException e) {
			// doesn't have this property
			return null;
		} catch (Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * Creates a new version of this object's rule node, updating the effective date for the
	 * rule node. 
	 *  
	 * @param newDateEffective the new effective date for the rule 
	 * @throws RulesRepositoryException
	 */
	public void updateDateEffective(Calendar newDateEffective)
			throws RulesRepositoryException {
		checkIsUpdateable();
		checkout();
		try {
			this.node.setProperty(DATE_EFFECTIVE, newDateEffective);
		} catch (RepositoryException e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * @return the date the rule becomes expired
	 * @throws RulesRepositoryException
	 */
	public Calendar getDateExpired() throws RulesRepositoryException {
		try {
			Node ruleNode = getVersionContentNode();

			Property dateExpiredProperty = ruleNode.getProperty(DATE_EXPIRED);
			return dateExpiredProperty.getDate();
		} catch (PathNotFoundException e) {
			// doesn't have this property
			return null;
		} catch (Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * Creates a new version of this object's rule node, updating the expired date for the
	 * rule node. 
	 *  
	 * @param newDateExpired the new expired date for the rule 
	 * @throws RulesRepositoryException
	 */
	public void updateDateExpired(Calendar newDateExpired)
			throws RulesRepositoryException {
		checkout();

		try {
			this.node.setProperty(DATE_EXPIRED, newDateExpired);
		} catch (Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	public boolean hasExpander() throws RepositoryException {

		try {
			Node ruleNode = getVersionContentNode();
			Property dslReference = ruleNode.getProperty(DSL_REFERENCE);
		} catch (PathNotFoundException e) {
			// doesn't have this propertyW
			return false;
		}

		return true;
	}

	/**
	 * @return the DSL
	 * @throws RulesRepositoryException
	 */
	public ExpanderItem getExpander() throws RulesRepositoryException {
		try {
			Node ruleNode = getVersionContentNode();

			Property dsReference = ruleNode.getProperty(DSL_REFERENCE);

			ExpanderItem expander = this.getRulesRepository().loadExpanderByUUID(dsReference.getString());

			return expander;

		} catch (PathNotFoundException e) {
			// doesn't have this property
			return null;
		} catch (Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * Creates a new version of this object's rule node, updating the DSL for the
	 * rule node. 
	 *  
	 * @param expanderItem the new expired date for the rule 
	 * @throws RulesRepositoryException
	 */
	public void updateExpander(ExpanderItem expanderItem)
			throws RulesRepositoryException {
		checkout();

		try {
			if (expanderItem == null) {
				if (this.hasExpander()) {
					this.node.getProperty(DSL_REFERENCE).remove();
				}
			} else {
				this.node.setProperty(DSL_REFERENCE, expanderItem.getUUID());
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
	public RuleItem updateContent(String newRuleContent)
			throws RulesRepositoryException {
		checkout();
		try {
			this.node.setProperty(CONTENT, newRuleContent);
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
			returnString.append("Content of rule item named '" + this.getName()
					+ "':\n");
			returnString.append("Content: " + this.getContent() + "\n");
			returnString.append("------\n");

			returnString.append("Date Effective: " + this.getDateEffective()
					+ "\n");
			returnString
					.append("Date Expired: " + this.getDateExpired() + "\n");
			returnString.append("------\n");
			returnString.append("--------------\n");
			return returnString.toString();
		} catch (Exception e) {
			throw new RulesRepositoryException(e);
		}
	}

	public RuleItem getPrecedingVersion()
			throws RulesRepositoryException {
		try {
			Node precedingVersionNode = this.getPrecedingVersionNode();
			if (precedingVersionNode != null) {
				return new RuleItem(this.rulesRepository, precedingVersionNode);
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("Caught exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	public RuleItem getSucceedingVersion()
			throws RulesRepositoryException {
		try {
			Node succeedingVersionNode = this.getSucceedingVersionNode();
			if (succeedingVersionNode != null) {
				return new RuleItem(this.rulesRepository, succeedingVersionNode);
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("Caught exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * Get the name of the enclosing package.
	 * As assets are stored in versionable subfolders, this means walking up 2 levels in the 
	 * hierarchy to get to the enclosing "package" node.
	 */
	public String getPackageName() {
		return super.getStringProperty(PACKAGE_NAME);
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