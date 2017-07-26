/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Relationship
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a relationship.
 *
 * @author MEME Group
 */

public interface Relationship extends AtomElement {

  /**
   * Returns the {@link Atom}.
   * @return the {@link Atom}
   */
  public Atom getAtom();

  /**
   * Sets the {@link Atom}.
   * @param atom_id_1 the {@link Atom}
   */
  public void setAtom(Atom atom_id_1);

  /**
   * Returns the related {@link Atom}.
   * @return the related {@link Atom}
   */
  public Atom getRelatedAtom();

  /**
   * Sets the related {@link Atom}.
   * @param atom_id_2 the related {@link Atom}
   */
  public void setRelatedAtom(Atom atom_id_2);

  /**
   * Returns the {@link Concept}.
   * @return the {@link Concept}
   */
  public Concept getConcept();

  /**
   * Sets the {@link Concept}.
   * @param concept_id_1 the {@link Concept}
   */
  public void setConcept(Concept concept_id_1);

  /**
   * Returns the related {@link Concept}.
   * @return the related {@link Concept}
   */
  public Concept getRelatedConcept();

  /**
   * Sets the related {@link Concept}.
   * @param concept_id_2 the related {@link Concept}
   */
  public void setRelatedConcept(Concept concept_id_2);

  /**
   * Returns the related {@link NativeIdentifier}.
   * @return the related {@link NativeIdentifier}
   */
  public NativeIdentifier getRelatedNativeIdentifier();

  /**
   * Sets the related {@link NativeIdentifier}.
   * @param sg_id_2 the related {@link NativeIdentifier}
   */
  public void setRelatedNativeIdentifier(NativeIdentifier sg_id_2);

  /**
   * Returns the relationship name.
   * @return the relationship name
   */
  public String getName();

  /**
   * Sets the relationship name.
   * @param relationship_name the relationship name
   */
  public void setName(String relationship_name);

  /**
   * Returns the relationship attribute.
   * @return the relationship attribute
   */
  public String getAttribute();

  /**
   * Sets the relationship attribute.
   * @param relationship_attribute the relationship attribute
   */
  public void setAttribute(String relationship_attribute);

  /**
   * Returns the {@link Source} of label.
   * @return the {@link Source} of label
   */
  public Source getSourceOfLabel();

  /**
   * Sets the {@link Source} of label.
   * @param source_of_label the {@link Source} of label
   */
  public void setSourceOfLabel(Source source_of_label);

  /**
   * Returns the group {@link Identifier}.
   * @return the group {@link Identifier}
   */
  public Identifier getGroupIdentifier();

  /**
   * Sets the group {@link Identifier}.
   * @param group_id the group {@link Identifier}
   */
  public void setGroupIdentifier(Identifier group_id);

  /**
   * Returns the {@link RUI}.
   * @return the {@link RUI}
   */
  public RUI getRUI();

  /**
   * Sets the {@link RUI}.
   * @param rui the {@link RUI}
   */
  public void setRUI(RUI rui);

  /**
   * Indicates whether or not the relationships is inversed with respect to the database.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isInverse();

  /**
   * Sets the flag indicating whether or not the relationships is inversed with
   * respect to the database.
   * @param inverse the "inverse" flag
   */
  public void setIsInverse(boolean inverse);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link Relationship} interface.
   */
  public class Default extends CoreData.Default implements Relationship {

    //
    // Fields
    //

    protected Atom atom_id_1 = null;
    protected Atom atom_id_2 = null;
    protected Concept concept_id_1 = null;
    protected Concept concept_id_2 = null;
    protected NativeIdentifier sg_id_2 = null;
    protected Identifier group_id = null;
    protected RUI rui = null;
    protected String relationship_name = null;
    protected String relationship_attribute = null;
    protected Source source_of_label = null;
    protected boolean inverse = false;

    //
    // Constructors
    //

    /**
     * This constructor is a dummy no-argument constructor.
     */
    public Default() {
      super();
    }

    /**
     * Instantiates a {@link Relationship.Default}.
     * @param relationship_id the relationship id.
     */
    public Default(int relationship_id) {
      this();
      setIdentifier(new Identifier.Default(relationship_id));
    }

    //
    // Implementation of Relationship interface
    //

    /**
     * Implements {@link Relationship#getAtom()}.
     * @return the {@link Atom}.
     */
    public Atom getAtom() {
      return atom_id_1;
    }

    /**
     * Implements {@link Relationship#setAtom(Atom)}.
     * @param atom_id_1 the {@link Atom}.
     */
    public void setAtom(Atom atom_id_1) {
      this.atom_id_1 = atom_id_1;
    }

    /**
     * Implements {@link Relationship#getRelatedAtom()}.
     * @return the {@link Atom}.
     */
    public Atom getRelatedAtom() {
      return atom_id_2;
    }

    /**
     * Implements {@link Relationship#setRelatedAtom(Atom)}.
     * @param atom_id_2 the {@link Atom}.
     */
    public void setRelatedAtom(Atom atom_id_2) {
      this.atom_id_2 = atom_id_2;
    }

    /**
     * Implements {@link Relationship#getConcept()}.
     * @return the {@link Concept}.
     */
    public Concept getConcept() {
      return concept_id_1;
    }

    /**
     * Implements {@link Relationship#setConcept(Concept)}.
     * @param concept_id_1 the {@link Concept}.
     */
    public void setConcept(Concept concept_id_1) {
      this.concept_id_1 = concept_id_1;
    }

    /**
     * Implements {@link Relationship#getRelatedConcept()}.
     * @return the {@link Concept}.
     */
    public Concept getRelatedConcept() {
      return concept_id_2;
    }

    /**
     * Implements {@link Relationship#setRelatedConcept(Concept)}.
     * @param concept_id_2 the {@link Concept}.
     */
    public void setRelatedConcept(Concept concept_id_2) {
      this.concept_id_2 = concept_id_2;
    }

    /**
     * Implements {@link Relationship#getRelatedNativeIdentifier()}.
     * @return the related native identifier.
     */
    public NativeIdentifier getRelatedNativeIdentifier() {
      return sg_id_2;
    }

    /**
         * Implements {@link Relationship#setRelatedNativeIdentifier(NativeIdentifier)}.
     * @param sg_id_2 the related native identifier.
     */
    public void setRelatedNativeIdentifier(NativeIdentifier sg_id_2) {
      this.sg_id_2 = sg_id_2;
    }

    /**
     * Implements {@link Relationship#getName()}.
     * @return the {@link String} representation of relationships name.
     */
    public String getName() {
      return relationship_name;
    }

    /**
     * Implements {@link Relationship#setName(String)}.
     * @param relationship_name the {@link String} representation of
     * relationship name.
     */
    public void setName(String relationship_name) {
      this.relationship_name = relationship_name;
    }

    /**
     * Implements {@link Relationship#getAttribute()}.
     * @return the {@link String} representation of attribute name.
     */
    public String getAttribute() {
      return relationship_attribute;
    }

    /**
     * Implements {@link Relationship#setAttribute(String)}.
     * @param relationship_attribute the {@link String} representation of
     * attribute name.
     */
    public void setAttribute(String relationship_attribute) {
      this.relationship_attribute = relationship_attribute;
    }

    /**
     * Implements {@link Relationship#getSourceOfLabel()}.
     * @return the {@link Source}.
     */
    public Source getSourceOfLabel() {
      return source_of_label;
    }

    /**
     * Implements {@link Relationship#setSourceOfLabel(Source)}.
     * @param source_of_label the {@link Source}.
     */
    public void setSourceOfLabel(Source source_of_label) {
      this.source_of_label = source_of_label;
    }

    /**
     * Implements {@link Relationship#getGroupIdentifier()}.
     * @return the {@link Identifier}.
     */
    public Identifier getGroupIdentifier() {
      return group_id;
    }

    /**
     * Implements {@link Relationship#setGroupIdentifier(Identifier)}.
     * @param group_id the {@link Identifier}.
     */
    public void setGroupIdentifier(Identifier group_id) {
      this.group_id = group_id;
    }

    /**
     * Implements {@link Relationship#getRUI()}.
     * @return the {@link RUI}.
     */
    public RUI getRUI() {
      return rui;
    }

    /**
     * Implements {@link Relationship#setRUI(RUI)}.
     * @param rui the {@link RUI}.
     */
    public void setRUI(RUI rui) {
      this.rui = rui;
    }

    /**
     * Indicates whether or not the relationships is inversed with respect to the database.
     * @return true if so, false otherwise
     */
    public boolean isInverse() {
      return inverse;
    }

    /**
         * Sets the flag indicating whether or not the relationships is inversed with
     * respect to the database.
     * @param inverse the flag
     */
    public void setIsInverse(boolean inverse) {
      this.inverse = inverse;
    }

  }
}
