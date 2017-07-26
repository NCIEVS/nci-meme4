/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ConceptSemanticType
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a semantic type {@link Attribute} connected to a {@link Concept}.
 *
 * @author MEME Group
 */

public class ConceptSemanticType extends Attribute.Default implements
    SemanticType {

  //
  // Fields
  //

  private SemanticType sty = new SemanticType.Default();

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link ConceptSemanticType}.
   */
  public ConceptSemanticType() {
    super();
  }

  /**
   * Instantiates a {@link ConceptSemanticType}
   * with the specified attribute id.
   * @param attribute_id the <code>int</code> attribute id
   */
  public ConceptSemanticType(int attribute_id) {
    this();
    setIdentifier(new Identifier.Default(attribute_id));
  }

  /**
   * Instantiates a {@link ConceptSemanticType}
   * with the specified {@link SemanticType}.
   * @param st the {@link SemanticType}
   */
  public ConceptSemanticType(SemanticType st) {
    this();
    sty.setIsChemical(st.isChemical());
    sty.setIsEditingChemical(st.isEditingChemical());
    sty.setChemicalType(st.getChemicalType());
    sty.setTreePosition(st.getTreePosition());
    sty.setDefinition(st.getDefinition());
    sty.setValue(st.getValue());
    sty.setTypeIdentifier(st.getTypeIdentifier());
  }

  //
  // Impelementation of SemanticType
  //

  /**
   * Implements {@link SemanticType#isFunctionalChemical()}.
   */
  public boolean isFunctionalChemical() {
    return sty.isFunctionalChemical();
  }

  /**
   * Implements {@link SemanticType#isStructuralChemical()}.
   */
  public boolean isStructuralChemical() {
    return sty.isStructuralChemical();
  }

  /**
   * Implements {@link SemanticType#isChemical()}.
   */
  public boolean isChemical() {
    return sty.isChemical();
  }

  /**
   * Implements {@link SemanticType#setIsChemical(boolean)}.
   */
  public void setIsChemical(boolean is_chemical) {
    sty.setIsChemical(is_chemical);
  }

  /**
   * Implements {@link SemanticType#isEditingChemical()}.
   */
  public boolean isEditingChemical() {
    return sty.isEditingChemical();
  }

  /**
   * Implements {@link SemanticType#setIsEditingChemical(boolean)}.
   */
  public void setIsEditingChemical(boolean is_editing_chemical) {
    sty.setIsEditingChemical(is_editing_chemical);
  }

  /**
   * Implements {@link SemanticType#getChemicalType()}.
   */
  public String getChemicalType() {
    return sty.getChemicalType();
  }

  /**
   * Implements {@link SemanticType#setChemicalType(String)}.
   */
  public void setChemicalType(String chemical_type) {
    sty.setChemicalType(chemical_type);
  }

  /**
   * Implements {@link SemanticType#getTreePosition()}.
   */
  public String getTreePosition() {
    return sty.getTreePosition();
  }

  /**
   * Implements {@link SemanticType#setTreePosition(String)}.
   */
  public void setTreePosition(String tree_position) {
    sty.setTreePosition(tree_position);
  }

  /**
   * Implements {@link SemanticType#getDefinition()}.
   */
  public String getDefinition() {
    return sty.getDefinition();
  }

  /**
   * Implements {@link SemanticType#setDefinition(String)}.
   */
  public void setDefinition(String definition) {
    sty.setDefinition(definition);
  }

  /**
   * Implements {@link SemanticType#getValue()}.
   */
  public String getValue() {
    return sty.getValue();
  }

  /**
   * Implements {@link SemanticType#setValue(String)}.
   */
  public void setValue(String value) {
    sty.setValue(value);
  }

  /**
   * Implements {@link SemanticType#getTypeIdentifier()}.
   */
  public Identifier getTypeIdentifier() {
    return sty.getTypeIdentifier();
  }

  /**
   * Implements {@link SemanticType#setTypeIdentifier(Identifier)}.
   */
  public void setTypeIdentifier(Identifier identifier) {
    sty.setTypeIdentifier(identifier);
  }
}
