/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ContextRelationship
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a parent, child or sibling {@link Relationship}.
 *
 * @author MEME Group
 */

public interface ContextRelationship extends Relationship {

  /**
   * Returns the hierarchical code.
   * @return the hierarchical code
   */
  public String getHierarchicalCode();

  /**
   * Sets the hierarchical code.
   * @param hierarchical_code the hierarchical code
   */
  public void setHierarchicalCode(String hierarchical_code);

  /**
   * Returns the {@link ContextPath} from the parent to the root.
   * @return the {@link ContextPath} from the parent to the root
   */
  public ContextPath getParentTreenum();

  /**
   * Sets the {@link ContextPath} from the parent to the root.
   * @param parent_treenum the {@link ContextPath} from the parent to the root
   */
  public void setParentTreenum(ContextPath parent_treenum);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link ContextRelationship} interface.
   */
  public class Default extends Relationship.Default implements
      ContextRelationship {

    //
    // Fields
    //

    private String hierarchical_code = null;
    private ContextPath parent_treenum = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty {@link ContextRelationship}.
     */
    public Default() {
      super();
    }

    /**
     * Instantiates a {@link ContextRelationship}
     * with the specified relationship id.
     * @param relationship_id the <code>int</code> relationship id
     */
    public Default(int relationship_id) {
      super();
      setIdentifier(new Identifier.Default(relationship_id));
    }

    //
    // Implementation of ContextRelationship interface
    //

    /**
     * Implements {@link ContextRelationship#getHierarchicalCode()}.
     */
    public String getHierarchicalCode() {
      return hierarchical_code;
    }

    /**
     * Implements {@link ContextRelationship#setHierarchicalCode(String)}.
     */
    public void setHierarchicalCode(String hierarchical_code) {
      this.hierarchical_code = hierarchical_code;
    }

    /**
     * Implements {@link ContextRelationship#getParentTreenum()}.
     */
    public ContextPath getParentTreenum() {
      return parent_treenum;
    }

    /**
     * Implements {@link ContextRelationship#setParentTreenum(ContextPath)}.
     */
    public void setParentTreenum(ContextPath parent_treenum) {
      this.parent_treenum = parent_treenum;
    }

  }
}
