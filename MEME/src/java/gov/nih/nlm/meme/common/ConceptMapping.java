/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ConceptMapping
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a concept mapping.
 *
 * @author MEME Group
 */

public interface ConceptMapping extends CoreData {

  /**
   * Returns the birth version.
   * @return the birth version
   */
  public String getBirthVersion();

  /**
   * Sets the birth version.
   * @param birth_version the birth version
   */
  public void setBirthVersion(String birth_version);

  /**
   * Returns the death version.
   * @return the death version
   */
  public String getDeathVersion();

  /**
   * Sets the death version.
   * @param death_version the death version
   */
  public void setDeathVersion(String death_version);

  /**
   * Returns the mapping reason.
   * @return the mapping reason
   */
  public String getMappingReason();

  /**
   * Sets the mapping reason.
   * @param mapping_reason the mapping reason
   */
  public void setMappingReason(String mapping_reason);

  /**
   * Indicates whether or not mapping is bequeathal.
   * @return <code>true</code> if the mapping is bequeathal;
   * <code>false</code> otherwise
   */
  public boolean isBequeathalMapping();

  /**
   * Indicates whether or not the relationship is synonymous mapping.
   * @return <code>true</code> if relationship is synonymous mapping;
   * <code>false</code> otherwise
   */
  public boolean isSynonymousMapping();

  /**
   * Indicates whether or not the relationship is deleted mapping
   * @return <code>true</code> if relationship is deleted mapping;
   * <code>false</code> otherwise
   */
  public boolean isDeletedMapping();

  /**
   * Sets the relationship name.
   * @param rel_name the relationship name
   */
  public void setRelationshipName(String rel_name);

  /**
   * Returns the relationship name.
   * @return the relationship name
   */
  public String getRelationshipName();

  /**
   * Sets the relationship attribute.
   * @param rel_attr the relationship attribute
   */
  public void setRelationshipAttribute(String rel_attr);

  /**
   * Returns the relationship attribute.
   * @return the relationship attribute
   */
  public String getRelationshipAttribute();

  /**
   * Sets the {@link CUI}.
   * @param cui the {@link CUI}
   */
  public void setCUI(CUI cui);

  /**
   * Returns the {@link CUI}.
   * @return the {@link CUI}
   */
  public CUI getCUI();

  /**
   * Sets the mapped to {@link CUI}.
   * @param cui the mapped to {@link CUI}
   */
  public void setMappedToCui(CUI cui);

  /**
   * Returns the mapped to {@link CUI}.
   * @return An object {@link CUI}
   */
  public CUI getMappedToCui();

  /**
   * Sets whether or not the almost SY.
   * @param almost_sy the value to set
   */
  public void setAlmostSY(boolean almost_sy);

  /**
   * Indicates whether or not the mapping represents "near synonymy".
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isAlmostSY();

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link ConceptMapping} interface.
   */
  public class Default extends CoreData.Default implements ConceptMapping {

    //
    // Fields
    //

    private String birth_version = null;
    private String death_version = null;
    private String mapping_reason = null;
    private String rel_name = null;
    private String rel_attr = null;
    private CUI cui = null;
    private CUI mapped_to_cui = null;
    private boolean almost_sy = false;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link ConceptMapping}.
     */
    public Default() {
      super();
    }

    //
    // Methods
    //

    /**
     * Implements {@link ConceptMapping#getBirthVersion()}.
     */
    public String getBirthVersion() {
      return this.birth_version;
    }

    /**
     * Implements {@link ConceptMapping#setBirthVersion(String)}.
     */
    public void setBirthVersion(String birth_version) {
      this.birth_version = birth_version;
    }

    /**
     * Implements {@link ConceptMapping#getDeathVersion()}.
     */
    public String getDeathVersion() {
      return this.death_version;
    }

    /**
     * Implements {@link ConceptMapping#setDeathVersion(String)}.
     */
    public void setDeathVersion(String death_version) {
      this.death_version = death_version;
    }

    /**
     * Implements {@link ConceptMapping#getMappingReason()}.
     */
    public String getMappingReason() {
      return this.mapping_reason;
    }

    /**
     * Implements {@link ConceptMapping#setMappingReason(String)}.
     */
    public void setMappingReason(String mapping_reason) {
      this.mapping_reason = mapping_reason;
    }

    /**
     * Implements {@link ConceptMapping#isBequeathalMapping()}.
     */
    public boolean isBequeathalMapping() {
      return getRelationshipName().equals("DEL");
    }

    /**
     * Implements {@link ConceptMapping#isSynonymousMapping()}.
     */
    public boolean isSynonymousMapping() {
      return rel_name.equals("SY") && mapped_to_cui != null;
    }

    /**
     * Implements {@link ConceptMapping#isDeletedMapping()}.
     */
    public boolean isDeletedMapping() {
      return mapped_to_cui == null && rel_name.equals("DEL");
    }

    /**
     * Implements {@link ConceptMapping#setRelationshipName(String)}.
     */
    public void setRelationshipName(String rel_name) {
      this.rel_name = rel_name;
    }

    /**
     * Implements {@link ConceptMapping#getRelationshipName()}.
     */
    public String getRelationshipName() {
      return this.rel_name;
    }

    /**
     * Implements {@link ConceptMapping#setRelationshipAttribute(String)}.
     */
    public void setRelationshipAttribute(String rel_attr) {
      this.rel_attr = rel_attr;
    }

    /**
     * Implements {@link ConceptMapping#getRelationshipAttribute()}.
     */
    public String getRelationshipAttribute() {
      return this.rel_attr;
    }

    /**
     * Implements {@link ConceptMapping#setCUI(CUI)}.
     */
    public void setCUI(CUI cui) {
      this.cui = cui;
    }

    /**
     * Implements {@link ConceptMapping#getCUI()}.
     */
    public CUI getCUI() {
      return this.cui;
    }

    /**
     * Implements {@link ConceptMapping#setMappedToCui(CUI)}.
     */
    public void setMappedToCui(CUI cui) {
      this.mapped_to_cui = cui;
    }

    /**
     * Implements {@link ConceptMapping#getMappedToCui()}.
     */
    public CUI getMappedToCui() {
      return this.mapped_to_cui;
    }

    /**
     * Implements {@link ConceptMapping#setAlmostSY(boolean)}.
     */
    public void setAlmostSY(boolean almost_sy) {
      this.almost_sy = almost_sy;
    }

    /**
     * Implements {@link ConceptMapping#isAlmostSY()}.
     */
    public boolean isAlmostSY() {
      return this.almost_sy;
    }

  }
}