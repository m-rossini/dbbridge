package br.com.auster.repo;

import javax.jcr.Node;

import org.apache.log4j.Logger;


/**
 * The item class is used to abstract away the underlying details of the JCR repository.
 * 
 * @author btruitt
 */
public abstract class AbstractItem {

	public static final String CHARSET_ENCODING = "UTF-8";

    Logger log = Logger.getLogger(AbstractItem.class);

    /**
     * The node within the repository that this item corresponds to
     */
    protected Node node;
    
    /**
     * The RulesRepository object that this object was created from
     */
    protected RulesRepository rulesRepository;

    /**
     * Sets the item object's node attribute to the specified node
     * 
     * @param rulesRepository the RulesRepository object that this object is being created from
     * @param node the node in the repository that this item corresponds to
     */
    public AbstractItem(RulesRepository rulesRepository, Node node) {
        this.rulesRepository = rulesRepository;
        this.node = node;
    }

    /**
     * gets the node in the repository that this item is associated with
     * 
     * @return the node in the repository that this item is associated with
     */
    public Node getNode() {
        return node;
    }    
    
    /**
     * gets the name of this item (unless overridden in a subclass, this just returns the
     * name of the node that this Item encapsulates.
     * 
     * @return the name of the node that this item encapsultes
     * @throws RulesRepositoryException 
     */
    public String getName() throws RulesRepositoryException {
        try {
            return this.node.getName();
        }
        catch(Exception e) {
            log.error("Caught exception: " + e);
            throw new RulesRepositoryException(e);
        }
    }

    /**
     * @return the RulesRepository object that this object was instantiated from
     */
    public RulesRepository getRulesRepository() {
        return rulesRepository;
    }
    
    public boolean equals(Object obj) {
        if(!(obj instanceof AbstractItem)) {
            return false;
        }
        else {
            AbstractItem rhs = (AbstractItem)obj;
            return this.node.equals(rhs.getNode());
        }
    }

    public int hashCode() {
        return this.node.hashCode();
    }        
    

}
