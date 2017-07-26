/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  SemanticType
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a semantic type.
 * This class rouhgly corresponds to things like <code>SRDEF</code>
 * and the <i>MID</i> <code>semantic_types</code> table.
 *
 * @author MEME Group
 */

public interface SemanticType {

  //
  // Field Values
  //

  /**
   * Field value indicating a functional chemical semantic type.
   */
  public final static String FUNCTIONAL_CHEMICAL = "F";

  /**
   * Field value indicating a structural chemical semantic type.
   */
  public final static String STRUCTURAL_CHEMICAL = "S";

  //
  // Methods
  //

  /**
   * Indicates whether semantic type is functional chemical.
       * @return <code>true</code> if sty is a functional chemical; <code>false</code>
   * otherwise.
   */
  public boolean isFunctionalChemical();

  /**
   * Indicates whether semantic type is structural chemical.
       * @return <code>true</code> if sty is a structural chemical; <code>false</code>
   * otherwise.
   */
  public boolean isStructuralChemical();

  /**
   * Indicates whether or not this is is chemical semantic type.  It returns
   * <code>true</code> if it is chemical; <code>false</code> otherwise.
   * @return A <code>boolean</code> representation whether or not this
   * is a chemical semantic type
   */
  public boolean isChemical();

  /**
   * Sets the flag indicating whether or not this is chemical sty.
   * @param is_chemical the "is chem" flag value
   */
  public void setIsChemical(boolean is_chemical);

  /**
       * Indicates whether or not this is editing chemical semantic type.  It returns
   * <code>true</code> if it is editing chemical; <code>false</code> otherwise.
   * @return A <code>boolean</code> representation whether or not this
   * is an editing chemical semantic type
   */
  public boolean isEditingChemical();

  /**
   * Sets the flag indicating whether or not this is editing chemical sty.
   * @param is_editing_chemical the "is editing chem" flag value
   */
  public void setIsEditingChemical(boolean is_editing_chemical);

  /**
   * Returns the chemical type.
   * @return the chemical type
   */
  public String getChemicalType();

  /**
   * Sets the chemical type.
   * @param chemical_type the chemical type
   */
  public void setChemicalType(String chemical_type);

  /**
   * Returns the tree position.
   * @return the tree position
   */
  public String getTreePosition();

  /**
   * Sets the tree position.
   * @param tree_position the tree position
   */
  public void setTreePosition(String tree_position);

  /**
   * Returns the definition.
   * @return the definition
   */
  public String getDefinition();

  /**
   * Sets the definition.
   * @param definition the definition
   */
  public void setDefinition(String definition);

  /**
   * Returns the value.
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(String value);

  /**
   * Returns the type {@link Identifier}.
   * @return type {@link Identifier}
   */
  public Identifier getTypeIdentifier();

  /**
   * Sets the type {@link Identifier}.
   * @param identifier the type {@link Identifier}
   */
  public void setTypeIdentifier(Identifier identifier);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link SemanticType} interface.
   */
  public class Default implements SemanticType, Comparable {

    //
    // Fields
    //

    private boolean is_chemical = false;
    private boolean is_editing_chemical = false;
    private String chemical_type = null;
    private String tree_position = null;
    private String definition = null;
    private String value = null;
    private Identifier identifier;

    //
    // Constructors
    //

    /**
     * Instantiates a {@link SemanticType.Default}.
     */
    public Default() {
      super();
    }

    //
    // Implementation of SemanticType interface
    //

    /**
     * Implements {@link SemanticType#isFunctionalChemical()}.
         * @return <code>true</code> if sty is a functional chemical; <code>false</code>
     * otherwise
     */
    public boolean isFunctionalChemical() {
      return chemical_type.equals(FUNCTIONAL_CHEMICAL);
    }

    /**
     * Implements {@link SemanticType#isStructuralChemical()}.
         * @return <code>true</code> if sty is a structural chemical; <code>false</code>
     * otherwise
     */
    public boolean isStructuralChemical() {
      return chemical_type.equals(STRUCTURAL_CHEMICAL);
    }

    /**
     * Implements {@link SemanticType#isChemical()}.
     * @return A <code>boolean</code> representation whether or not this
     * is a chemical semantic type
     */
    public boolean isChemical() {
      return is_chemical;
    }

    /**
     * Implements {@link SemanticType#setIsChemical(boolean)}.
         * @param is_chemical A <code>boolean</code> representation whether or not this
     * is a chemical semantic type
     */
    public void setIsChemical(boolean is_chemical) {
      this.is_chemical = is_chemical;
    }

    /**
     * Implements {@link SemanticType#isEditingChemical()}.
     * @return A <code>boolean</code> representation whether or not this
     * is an editing chemical semantic type
     */
    public boolean isEditingChemical() {
      return is_editing_chemical;
    }

    /**
     * Implements {@link SemanticType#setIsEditingChemical(boolean)}.
     * @param is_editing_chemical A <code>boolean</code> representation whether or not this
     * is an editing chemical semantic type
     */
    public void setIsEditingChemical(boolean is_editing_chemical) {
      this.is_editing_chemical = is_editing_chemical;
    }

    /**
     * Implements {@link SemanticType#getChemicalType()}.
     * @return the chem type
     */
    public String getChemicalType() {
      return chemical_type;
    }

    /**
     * Implements {@link SemanticType#setChemicalType(String)}.
     * @param chemical_type the chem type
     */
    public void setChemicalType(String chemical_type) {
      this.chemical_type = chemical_type;
    }

    /**
     * Implements {@link SemanticType#getTreePosition()}.
     * @return the tree position
     */
    public String getTreePosition() {
      return tree_position;
    }

    /**
     * Implements {@link SemanticType#setTreePosition(String)}.
     * @param tree_position the tree position
     */
    public void setTreePosition(String tree_position) {
      this.tree_position = tree_position;
    }

    /**
     * Implements {@link SemanticType#getDefinition()}.
     * @return the definition
     */
    public String getDefinition() {
      return definition;
    }

    /**
     * Implements {@link SemanticType#setDefinition(String)}.
     * @param definition the definition
     */
    public void setDefinition(String definition) {
      this.definition = definition;
    }

    /**
     * Implements {@link SemanticType#getValue()}.
     * @return the value
     */
    public String getValue() {
      return value;
    }

    /**
     * Returns a {@link String} representation.
     * @return a {@link String} representation
     */
    public String toString() {
      return getValue();
    }

    /**
     * Implements {@link SemanticType#setValue(String)}.
     * @param value the value
     */
    public void setValue(String value) {
      this.value = value;
    }

    /**
     * Implements {@link SemanticType#getTypeIdentifier()}.
     * @return the type {@link Identifier}
     */
    public Identifier getTypeIdentifier() {
      return identifier;
    }

    /**
     * Implements {@link SemanticType#setTypeIdentifier(Identifier)}.
     * @param identifier the type {@link Identifier}
     */
    public void setTypeIdentifier(Identifier identifier) {
      this.identifier = identifier;
    }

    //
    // Comparable interface
    //

    /**
     * Comparison function.
     * @param o object to compare to
     * @return <codE>int</code> indicating relative sort order
     */
    public int compareTo(Object o) {
      if (! (o instanceof SemanticType) || o == null) {
        return 0;
      }
      return getValue().compareTo(o.toString());
    }

  }
}
