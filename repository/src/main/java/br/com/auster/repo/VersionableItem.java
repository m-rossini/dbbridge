package br.com.auster.repo;


import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;

/**
 *
 */
public abstract class VersionableItem extends AbstractItem {

	/**
	 * Property names for this node type.
	 */
	public static final String DESCRIPTION = "auster:description";

	public static final String LAST_MODIFIED = "auster:lastModified";

	public static final String CHECKIN_COMMENT = "auster:checkinComment";

	public static final String VERSION_NUMBER = "auster:versionNumber";

	public static final String CREATION_DATE = "auster:createdDate";

	/** this is what is referred to when reading content from a versioned node */
	private Node contentNode = null;

	/**
	 * Sets this object's node attribute to the specified node
	 * 
	 * @param rulesRepository the RulesRepository object that this object is being created from
	 * @param node the node in the repository that this item corresponds to
	 */
	public VersionableItem(RulesRepository rulesRepository, Node node) {
		super(rulesRepository, node);
	}

	/**
	 * @return A unique identifier for this items content node.
	 */
	public String getUUID() {
		try {
			return this.getVersionContentNode().getUUID();
		} catch (RepositoryException e) {
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * This will return true if the current entity is actually a
	 * historical version (which means is effectively read only).
	 */
	public boolean isHistoricalVersion() throws RepositoryException {
		return this.node.getPrimaryNodeType().getName().equals("nt:version")
				|| node.getPrimaryNodeType().getName().equals("nt:frozenNode");
	}

	/**
	 * @return the predessor node of this node in the version history, or null if no predecessor version exists
	 * @throws RulesRepositoryException
	 */
	protected Node getPrecedingVersionNode() throws RulesRepositoryException {
		try {
			Node versionNode;
			if (this.node.getPrimaryNodeType().getName().equals("nt:version")) {
				versionNode = this.node;
			} else {
				versionNode = this.node.getBaseVersion();
			}

			Property predecessorsProperty = versionNode
					.getProperty("jcr:predecessors");
			Value[] predecessorValues = predecessorsProperty.getValues();

			if (predecessorValues.length > 0) {
				Node predecessorNode = this.node.getSession().getNodeByUUID(
						predecessorValues[0].getString());

				//we don't want to return the root node - it isn't a true predecessor
				if (predecessorNode.getName().equals("jcr:rootVersion")) {
					return null;
				}

				return predecessorNode;
			}
		} catch (PathNotFoundException e) {
			//do nothing - this will happen if no predecessors exits
		} catch (Exception e) {
			log.error("Caught exception", e);
			throw new RulesRepositoryException(e);
		}
		return null;
	}

	/**
	 * @return the successor node of this node in the version history
	 * @throws RulesRepositoryException
	 */
	protected Node getSucceedingVersionNode() throws RulesRepositoryException {
		try {
			Property successorsProperty = this.node
					.getProperty("jcr:successors");
			Value[] successorValues = successorsProperty.getValues();

			if (successorValues.length > 0) {
				Node successorNode = this.node.getSession().getNodeByUUID(
						successorValues[0].getString());
				return successorNode;
			}
		} catch (PathNotFoundException e) {
			//do nothing - this will happen if no successors exist
		} catch (Exception e) {
			log.error("Caught exception", e);
			throw new RulesRepositoryException(e);
		}
		return null;
	}

	/**
	 * Clients of this method can cast the resulting object to the type of object they are 
	 * calling the method on (e.g. 
	 *         <pre>
	 *           RuleItem item;
	 *           ...
	 *           RuleItem predcessor = (RuleItem) item.getPrecedingVersion();
	 *         </pre>
	 * @return a VersionableItem object encapsulating the predessor node of this node in the 
	 *         version history, or null if no predecessor version exists
	 * @throws RulesRepositoryException
	 */
	public abstract VersionableItem getPrecedingVersion()
			throws RulesRepositoryException;

	/**
	 * Clients of this method can cast the resulting object to the type of object they are 
	 * calling the method on (e.g. 
	 *         <pre>
	 *           RuleItem item;
	 *           ...
	 *           RuleItem successor = (RuleItem) item.getSucceedingVersion();
	 *         </pre>
	 *         
	 * @return a VersionableItem object encapsulating the successor node of this node in the 
	 *         version history. 
	 * @throws RulesRepositoryException
	 */
	public abstract VersionableItem getSucceedingVersion()
			throws RulesRepositoryException;

	/**
	 * update a text field. This is a convenience method that just
	 * uses the JCR node to set a property.
	 * This will also update the timestamp.
	 */
	protected void updateStringProperty(String value, String prop) {
		try {
			checkIsUpdateable();

			node.checkout();
			node.setProperty(prop, value);
			Calendar lastModified = Calendar.getInstance();
			this.node.setProperty(LAST_MODIFIED, lastModified);

		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * See the Dublin Core documentation for more
	 * explanation: http://dublincore.org/documents/dces/
	 * 
	 * @return the description of this object's node.
	 * @throws RulesRepositoryException
	 */
	public String getDescription() throws RulesRepositoryException {
		return getStringProperty(DESCRIPTION);
	}

	/**
	 * get this version number (default is incrementing integer, but you
	 * can provide an implementation of VersionNumberGenerator if needed).
	 */
	public String getVersionNumber() {

		return getStringProperty(VERSION_NUMBER);
	}

	/**
	 * This will return the checkin comment for the latest revision.
	 */
	public String getCheckinComment() throws RulesRepositoryException {
		try {
			Property data = getVersionContentNode()
					.getProperty(CHECKIN_COMMENT);
			return data.getValue().getString();
		} catch (Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * @return the date the function node (this version) was last modified
	 * @throws RulesRepositoryException
	 */
	public Calendar getLastModified() throws RulesRepositoryException {
		try {
			Property lastModifiedProperty = getVersionContentNode()
					.getProperty(LAST_MODIFIED);
			return lastModifiedProperty.getDate();
		} catch (Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * Creates a new version of this object's node, updating the description content 
	 * for the node.
	 * <br>
	 * See the Dublin Core documentation for more
	 * explanation: http://dublincore.org/documents/dces/ 
	 * 
	 * @param newDescriptionContent the new description content for the rule
	 * @throws RulesRepositoryException
	 */
	public void updateDescription(String newDescriptionContent)
			throws RulesRepositoryException {
		try {
			this.node.checkout();

			this.node.setProperty(DESCRIPTION, newDescriptionContent);

			Calendar lastModified = Calendar.getInstance();
			this.node.setProperty(LAST_MODIFIED, lastModified);

		} catch (Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * When retrieving content, if we are dealing with a version in the history, 
	 * we need to get the actual content node to retrieve values.
	 * 
	 */
	public Node getVersionContentNode() throws RepositoryException,
			PathNotFoundException {
		if (this.contentNode == null) {
			this.contentNode = getRealContentFromVersion(this.node);
		}
		return contentNode;
	}

	/**
	 * This deals with a node which *may* be a version, if it is, it grabs the frozen copy.
	 */
	protected Node getRealContentFromVersion(Node node)
			throws RepositoryException, PathNotFoundException {
		if (node.getPrimaryNodeType().getName().equals("nt:version")) {
			return node.getNode("jcr:frozenNode");
		} else {
			return node;
		}
	}

	/** 
	 * Need to get the name from the content node, not the version node
	 * if it is in fact a version ! 
	 */
	public String getName() {
		try {
			return getVersionContentNode().getName();
		} catch (RepositoryException e) {
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * This will check out the node prior to editing.
	 */
	public void checkout() {

		try {
			this.node.checkout();
		} catch (UnsupportedRepositoryOperationException e) {
			String message = "";
			try {
				message = "Error: Caught UnsupportedRepositoryOperationException when attempting to checkout rule: "
						+ this.node.getName()
						+ ". Are you sure your JCR repository supports versioning? ";
				log.error(message, e);
			} catch (RepositoryException e1) {
				log.error("Caught Exception", e);
				throw new RulesRepositoryException(e1);
			}
			throw new RulesRepositoryException(message, e);
		} catch (Exception e) {
			log.error("Caught Exception", e);
			throw new RulesRepositoryException(e);
		}
	}

	/** 
	 * This will save the content (if it hasn't been already) and 
	 * then check it in to create a new version.
	 * It will also set the last modified property.
	 */
	public void checkin(String comment) {
		try {
			this.node.setProperty(LAST_MODIFIED, Calendar.getInstance());
			this.node.setProperty(CHECKIN_COMMENT, comment);
			String nextVersion = this.getRulesRepository().calculateNextVersion(getVersionNumber(), this);
			this.node.setProperty(VERSION_NUMBER, nextVersion);
			this.node.getSession().save();
			this.node.checkin();
		} catch (RepositoryException e) {
			throw new RulesRepositoryException("Unable to checkin.", e);
		}
	}

	/**
	 * This will check to see if the node is the "head" and
	 * so can be updated (you can't update historical nodes ).
	 * @throws RulesRepositoryException if it is not allowed
	 * (means a programming error !).
	 */
	protected void checkIsUpdateable() {
		try {
			if (this.node.getPrimaryNodeType().getName().equals("nt:version")) {
				String message = "Error. Tags can only be added to the head version of a rule node";
				log.error(message);
				throw new RulesRepositoryException(message);
			}
		} catch (RepositoryException e) {
			throw new RulesRepositoryException(e);
		}
	}

	/**
	 * This returns the date/time on which the asset was "ORIGINALLY CREATED".
	 * Kinda handy if you want to know how old something is.
	 */
	public Calendar getCreatedDate() {
		Property prop;
		try {
			prop = getVersionContentNode().getProperty(CREATION_DATE);
			return prop.getDate();
		} catch (RepositoryException e) {
			throw new RulesRepositoryException(e);
		}

	}

	protected String getStringProperty(String property) {
		try {
			Node theNode = getVersionContentNode();
			if (theNode.hasProperty(property)) {
				Property data = theNode.getProperty(property);
				return data.getValue().getString();
			} else {
				return "";
			}
		} catch (RepositoryException e) {
			throw new RulesRepositoryException(e);
		}
	}

}
