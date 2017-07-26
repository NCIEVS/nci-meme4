/**
 * SessionToken.java
 */

package gov.nih.nlm.umls.jekyll.relae;

/**
 * Placeholder for information necessary
 * to perform molecular actions.
 */
public class SessionToken {

  //
  // Private Fields
  //
  private int relationship_id = 0;
  private int concept_id_1 = 0;
  private int concept_id_2 = 0;
  private String CUI_1 = null;
  private String CUI_2 = null;
  private String current_rel = null;
  private String current_attr = null;
  // user selected value for relationship name
  private String new_rel = null;
  // user selected value for relationship attribute
  private String new_attr = null;
  // source of a relationship
  private String source = null;
  // source of label of a relationship
  private String source_of_label = null;
  // authority of a relationship
  private String authority = null;

  //
  // Accessor Methods
  //
  public int getConceptId_1() {
    return concept_id_1;
  }

  public int getConceptId_2() {
    return concept_id_2;
  }

  public String getAuthority() {
    return authority;
  }

  public String getCUI_1() {
    return CUI_1;
  }

  public String getCUI_2() {
    return CUI_2;
  }

  public String getCurrent_Attr() {
    return current_attr;
  }

  public String getCurrent_Rel() {
    return current_rel;
  }

  public String getNew_Attr() {
    return new_attr;
  }

  public String getNew_Rel() {
    return new_rel;
  }

  public int getRelationship_Id() {
    return relationship_id;
  }

  public String getSource() {
    return source;
  }

  public String getSource_Of_Label() {
    return source_of_label;
  }

  public void setConceptId_1(int concept_id) {
    concept_id_1 = concept_id;
  }

  public void setConceptId_2(int concept_id) {
    concept_id_2 = concept_id;
  }

  public void setAuthority(String initials) {
    authority = initials;
  }

  public void setCUI_1(String cui) {
    CUI_1 = cui;
  }

  public void setCUI_2(String cui) {
    CUI_2 = cui;
  }

  public void setCurrent_Attr(String current_attr) {
    this.current_attr = current_attr;
  }

  public void setCurrent_Rel(String current_rel) {
    this.current_rel = current_rel;
  }

  public void setNew_Attr(String new_attr) {
    this.new_attr = new_attr;
  }

  public void setNew_Rel(String new_rel) {
    this.new_rel = new_rel;
  }

  public void setRelationship_Id(int relationship_id) {
    this.relationship_id = relationship_id;
  }

  public void setSABandSL(String src) {
    source = src;
    source_of_label = src;
  }
}
