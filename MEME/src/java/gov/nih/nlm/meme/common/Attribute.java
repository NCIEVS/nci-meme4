/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Attribute
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents an attribute belonging to
 * either an {@link Atom} or a {@link Concept}.
 *
 * @author MEME Group
 */

public interface Attribute extends AtomElement {

  //
  // Constants
  //

  /**
   * This is the attribute name for atom note.
   */
  public final static String ATOM_NOTE = "ATOM_NOTE";

  /**
   * This is the attribute name for any ATXs.
   */
  public final static String ATX_REL = "ATX_REL";

  /**
   * This is the attribute name for concept note.
   */
  public final static String CONCEPT_NOTE = "CONCEPT_NOTE";

  /**
   * This is the attribute name for context.
   */
  public final static String CONTEXT = "CONTEXT";

  /**
   * This is the attribute name for definition.
   */
  public final static String DEFINITION = "DEFINITION";

  /**
   * This is the attribute name for EC.
   */
  public final static String EC = "EC";

  /**
   * This is the attribute name for EZ.
   */
  public final static String EZ = "EZ";

  /**
   * This is the attribute name for Internal MeSH Note (IMN).
   */
  public final static String IMN = "IMN";

  /**
   * This is the attribute name for lexical tag.
   */
  public final static String LEXICAL_TAG = "LEXICAL_TAG";

  /**
   * This is the attribute name for MUI
   */
  public final static String MUI = "MUI";

  /**
   * This is the attribute name for non human.
   */
  public final static String NON_HUMAN = "NON_HUMAN";

  /**
   * This is the attribute name for RN.
   */
  public final static String RN = "RN";

  /**
   * This is the attribute name for record originator (RO).
   */
  public final static String RO = "RO";

  /**
   * This is the attribute name for semantic type.
   */
  public final static String SEMANTIC_TYPE = "SEMANTIC_TYPE";

  /**
   * This is the attribute name for SOS
   */
  public final static String SOS = "SOS";

  /**
   * This is the attribute name for syntactic category.
   */
  public final static String SYNTACTIC_CATEGORY = "SYNTACTIC_CATEGORY";

  /**
   * Returns the {@link Atom} to which this attribute is connected
   * @return the {@link Atom} to which this attribute is connected
   */
  public Atom getAtom();

  /**
   * Sets the {@link Atom} to which this attribute is connected.
   * @param atom the {@link Atom} to which this attribute is connected
   */
  public void setAtom(Atom atom);

  /**
   * Returns the {@link Concept} to which this attribute is connected.
   * @return the {@link Concept} to which this attribute is connected
   */
  public Concept getConcept();

  /**
   * Sets the {@link Concept} to which this attribute is connected.
   * @param concept the {@link Concept} to which this attribute is connected
   */
  public void setConcept(Concept concept);

  /**
   * Returns the attribute name.
   * @return the attribute name
   */
  public String getName();

  /**
   * Sets the attribute name.
   * @param attribute_name the attribute name
   */
  public void setName(String attribute_name);

  /**
   * Returns the attribute value.
   * @return the attribute value
   */
  public String getValue();

  /**
   * Sets the attribute value.
   * @param attribute_value the attribute value
   */
  public void setValue(String attribute_value);

  /**
   * Returns the {@link ATUI}.
   * @return the {@link ATUI}
   */
  public ATUI getATUI();

  /**
   * Sets the {@link ATUI}.
   * @param atui the {@link ATUI}
   */
  public void setATUI(ATUI atui);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link Attribute} interface.
   */
  public class Default extends CoreData.Default implements Attribute {

    //
    // Fields
    //

    private Atom atom = null;
    private Concept concept = null;
    private ATUI atui = null;
    private String attribute_name = null;
    private String attribute_value = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link Attribute}.
     */
    public Default() {
      super();
    }

    /**
     * Instantiates an default {@link Attribute} with the specified
     * identifier.
     * @param attribute_id the unique identifier for this attribute
     */
    public Default(int attribute_id) {
      super();
      setIdentifier(new Identifier.Default(attribute_id));
    }

    //
    // Implementation of Attribute interface
    //

    /**
     * Implements {@link Attribute#getAtom()}.
     */
    public Atom getAtom() {
      return atom;
    }

    /**
     * Implements {@link Attribute#setAtom(Atom)}.
     */
    public void setAtom(Atom atom) {
      this.atom = atom;
    }

    /**
     * Implements {@link Attribute#getConcept()}.
     */
    public Concept getConcept() {
      return concept;
    }

    /**
     * Implements {@link Attribute#setConcept(Concept)}.
     */
    public void setConcept(Concept concept) {
      this.concept = concept;
    }

    /**
     * Implements {@link Attribute#getName()}.
     */
    public String getName() {
      return attribute_name;
    }

    /**
     * Implements {@link Attribute#setName(String)}.
     */
    public void setName(String attribute_name) {
      this.attribute_name = attribute_name;
    }

    /**
     * Implements {@link Attribute#getValue()}.
     */
    public String getValue() {
      return attribute_value;
    }

    /**
     * Implements {@link Attribute#setValue(String)}.
     */
    public void setValue(String attribute_value) {
      this.attribute_value = attribute_value;
    }

    /**
     * Implements {@link Attribute#getATUI()}.
     */
    public ATUI getATUI() {
      return atui;
    }

    /**
     * Implements {@link Attribute#setATUI(ATUI)}.
     */
    public void setATUI(ATUI atui) {
      this.atui = atui;
    }

  }
}
