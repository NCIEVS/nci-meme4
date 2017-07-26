/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Source
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Date;

/**
 * Generically represents a UMLS source.
 *
 * @author MEME Group
 */

public interface Source extends Rankable {

  /**
   * Returns the source abbreviation.
   * @return the source abbreviation
   */
  public String toString();

  /**
   * Forces implementations to implement equality function.
   * @return <code>true</code> if equal, <code>false</code> otherwise
   * @param object the object to compare to
   */
  public boolean equals(Object object);

  /**
   * Returns the source abbreviation.
   * @return the source abbreviation
   */
  public String getSourceAbbreviation();

  /**
   * Sets the source abbreviation.
   * @param source the source abbreviation
   */
  public void setSourceAbbreviation(String source);

  /**
   * Returns the stripped source abbreviation.
   * @return the stripped source abbreviation
   */
  public String getStrippedSourceAbbreviation();

  /**
   * Sets the stripped source abbreviation.
   * @param stripped_source the stripped source abbreviation
   */
  public void setStrippedSourceAbbreviation(String stripped_source);

  /**
   * Returns the root source abbreviation.
   * @return the root source abbreviation
   */
  public String getRootSourceAbbreviation();

  /**
   * Sets the root source abbreviation.
   * @param root_source the root source abbreviation
   */
  public void setRootSourceAbbreviation(String root_source);

  /**
   * Returns the source version.
   * @return the source version
   */
  public String getSourceVersion();

  /**
   * Sets the source version.
   * @param version the source version
   */
  public void setSourceVersion(String version);

  /**
   * Returns the normalized source abbreviation.
   * @return the normalized source abbreviation
   */
  public String getNormalizedSourceAbbreviation();

  /**
   * Sets the normalized source abbreviation.
   * @param normalized_source the normalized source abbreviation
   */
  public void setNormalizedSourceAbbreviation(String normalized_source);

  /**
   * Returns the source family abbreviation.
   * @return the source family abbreviation
   */
  public String getSourceFamilyAbbreviation();

  /**
   * Sets the source family abbreviation.
   * @param source_family the source family abbreviation
   */
  public void setSourceFamilyAbbreviation(String source_family);

  /**
   * Returns the official name.
   * @return the official name
   */
  public String getOfficialName();

  /**
   * Sets the official name.
   * @param official_name the official name
   */
  public void setOfficialName(String official_name);

  /**
   * Returns the {@link CUI}.
   * @return the {@link CUI}
   */
  public CUI getCUI();

  /**
   * Sets the {@link CUI}.
   * @param cui the {@link CUI}
   */
  public void setCUI(CUI cui);

  /**
   * Returns the versioned {@link CUI}.
   * @return the versioned {@link CUI}
   */
  public CUI getVersionedCui();

  /**
   * Sets the versioned {@link CUI}.
   * @param vcui the versioned {@link CUI}
   */
  public void setVersionedCui(CUI vcui);

  /**
   * Returns the root {@link CUI}.
   * @return the root {@link CUI}
   */
  public CUI getRootCui();

  /**
   * Sets the root {@link CUI}.
   * @param rcui the root {@link CUI}
   */
  public void setRootCui(CUI rcui);

  /**
   * Returns the {@link Language}.
   * @return the {@link Language}
   */
  public Language getLanguage();

  /**
   * Sets the {@link Language}.
   * @param language the {@link Language}
   */
  public void setLanguage(Language language);

  /**
   * Returns the insert meta version.
   * @return the insert meta version
   */
  public String getInsertMetaVersion();

  /**
   * Sets the insert meta version.
   * @param insert_meta_version the insert meta version
   */
  public void setInsertMetaVersion(String insert_meta_version);

  /**
   * Returns the remove meta version.
   * @return the remove meta version
   */
  public String getRemoveMetaVersion();

  /**
   * Sets the remove meta version.
   * @param remove_meta_version the remove  meta version
   */
  public void setRemoveMetaVersion(String remove_meta_version);

  /**
   * Returns the NLM contact.
   * @return the NLM contact
   */
  public String getNLMContact();

  /**
   * Sets the NLM contact.
   * @param nlm_contact the NLM contact
   */
  public void setNLMContact(String nlm_contact);

  /**
   * Returns the inverter.
   * @return the inverter
   */
  public String getInverter();

  /**
   * Sets the inverter.
   * @param inverter the inverter
   */
  public void setInverter(String inverter);

  /**
   * Returns the acquisition contact.
   * @return the acquisition contact
   */
  public String getAcquisitionContact();

  /**
   * Sets the acquisition contact.
   * @param acquisition_contact the acquisition contact
   */
  public void setAcquisitionContact(String acquisition_contact);

  /**
   * Returns the content contact.
   * @return the content contact
   */
  public String getContentContact();

  /**
   * Sets the content contact.
   * @param content_contact the content contact
   */
  public void setContentContact(String content_contact);

  /**
   * Returns the license contact.
   * @return the license contact
   */
  public String getLicenseContact();

  /**
   * Sets the license contact.
   * @param license_contact the license contact
   */
  public void setLicenseContact(String license_contact);

  /**
   * Returns the license information.
   * @return the license information
   */
  public String getLicenseInformation();

  /**
   * Sets the license information.
   * @param license_info the license information
   */
  public void setLicenseInformation(String license_info);

  /**
   * Returns the restriction level.
   * @return the restriction level
   */
  public String getRestrictionLevel();

  /**
   * Sets the restriction level.
   * @param restriction_level the restriction level
   */
  public void setRestrictionLevel(String restriction_level);

  /**
   * Returns the term frequency.
   * @return the term frequency
   */
  public int getTermFrequency();

  /**
   * Sets the term frequency.
   * @param term_frequency the term frequency
   */
  public void setTermFrequency(int term_frequency);

  /**
   * Returns the cui frequency.
   * @return the cui frequency
   */
  public int getCuiFrequency();

  /**
   * Sets the cui frequency.
   * @param cui_frequency the cui frequency
   */
  public void setCuiFrequency(int cui_frequency);

  /**
   * Returns the context type.
   * @return the context type
   */
  public String getContextType();

  /**
   * Sets the context type.
   * @param context_type the context type
   */
  public void setContextType(String context_type);

  /**
   * Returns the attribute name list.
   * @return the attribute name list
   */
  public String getAttributeList();

  /**
   * Sets the attribute list. Should be a comma-separated list of ATN values.
   * @param attribute_list the attribute name list
   */
  public void setAttributeList(String attribute_list);

  /**
   * Returns the attribute name list.
   * @return the attribute name list
   */
  public String getAttributeNameList();

  /**
   * Sets the attribute name list.
   * @param attribute_name_list the attribute name list
   */
  public void setAttributeNameList(String attribute_name_list);

  /**
   * Returns the term type list.
   * @return the term type list
   */
  public String getTermTypeList();

  /**
   * Sets the term type list.
   * @param term_type_list the term type list
   */
  public void setTermTypeList(String term_type_list);

  /**
   * Returns the term group list.
   * @return the term group list
   */
  public String getTermGroupList();

  /**
   * Sets the term group list.
   * @param term_group_list the term group list
   */
  public void setTermGroupList(String term_group_list);

  /**
   * Returns the character encoding.
   * @return the character encoding
   */
  public String getCharacterEncoding();

  /**
   * Sets the character encoding.
   * @param character_encoding the character encoding
   */
  public void setCharacterEncoding(String character_encoding);

  /**
   * Returns the short name.
   * @return the short name
   */
  public String getShortName();

  /**
   * Sets the short name.
   * @param short_name the short name
   */
  public void setShortName(String short_name);

  /**
   * Returns the citation.
   * @return the citation
   */
  public String getCitation();

  /**
   * Sets the citation.
   * @param citation the citation
   */
  public void setCitation(String citation);

  /**
   * Returns the insertion date.
   * @return the insertion date
   */
  public Date getInsertionDate();

  /**
   * Sets the insertion date.
   * @param insertion_date the insertion date
   */
  public void setInsertionDate(Date insertion_date);

  /**
   * Returns the expiration date.
   * @return the expiration date
   */
  public Date getExpirationDate();

  /**
   * Sets the expiration date.
   * @param expiration_date the expiration date
   */
  public void setExpirationDate(Date expiration_date);

  /**
   * Indicates whether or not this is the current version of the source.
   * @return <code>true</code> if this is the current version;
   *         <code>false</code> otherwise
   */
  public boolean isCurrent();

  /**
   * Sets the flag indicating whether or not this is the current version.
   * @param is_current the "is current" flag value
   */
  public void setIsCurrent(boolean is_current);

  /**
   * Indicates whether or not this is the previous version of the source.
   * @return <code>true</code> if this is the previous version;
   *         <code>false</code> otherwise
   */
  public boolean isPrevious();

  /**
   * Sets the flag indicating whether or not this is the previous version.
   * @param is_previous the "is previous" flag value
   */
  public void setIsPrevious(boolean is_previous);

  /**
   * Returns the {@link Source} to outrank.
   * @return the {@link Source} to outrank
   */
  public Source getSourceToOutrank();

  /**
   * Sets the {@link Source} to outrank.
   * @param sot the {@link Source} to outrank
   */
  public void setSourceToOutrank(Source sot);

  /**
   * Returns the {@link Date} created.
   * @return the {@link Date} created
   */
  public Date getDateCreated();

  /**
   * Sets the {@link Date} created.
   * @param date_created the {@link Date} created
   */
  public void setDateCreated(Date date_created);

  /**
   * Returns the meta year.
   * @return the meta year
   */
  public int getMetaYear();

  /**
   * Sets the meta year.
   * @param meta_year the meta year
   */
  public void setMetaYear(int meta_year);

  /**
   * Returns the initial receipt date.
   * @return the initial receipt date
   */
  public Date getInitialReceiptDate();

  /**
   * Sets the initial receipt date.
   * @param init_rcpt_date the initial receipt date
   */
  public void setInitialReceiptDate(Date init_rcpt_date);

  /**
   * Returns the clean receipt date.
   * @return the clean receipt date
   */
  public Date getCleanReceiptDate();

  /**
   * Sets the clean receipt date.
   * @param clean_rcpt_date the clean receipt date
   */
  public void setCleanReceiptDate(Date clean_rcpt_date);

  /**
   * Returns the test insertion date.
   * @return the test insertion date
   */
  public Date getTestInsertionDate();

  /**
   * Sets the test insertion date.
   * @param test_insert_date the test insertion date
   */
  public void setTestInsertionDate(Date test_insert_date);

  /**
   * Returns the real insertion date.
   * @return the real insertion date
   */
  public Date getRealInsertionDate();

  /**
   * Sets the real insertion date.
   * @param real_insert_date the real insertion date
   */
  public void setRealInsertionDate(Date real_insert_date);

  /**
   * Returns the source contact.
   * @return the source contact
   */
  public String getSourceContact();

  /**
   * Sets the source contact.
   * @param source_contact the source contact
   */
  public void setSourceContact(String source_contact);

  /**
   * Returns the inverter contact.
   * @return the inverter contact
   */
  public String getInverterContact();

  /**
   * Sets the inverter contact.
   * @param inverter_contact the inverter contact
   */
  public void setInverterContact(String inverter_contact);

  /**
   * Returns the NLM path.
   * @return the NLM path
   */
  public String getNLMPath();

  /**
   * Sets the NLM path.
   * @param nlm_path the NLM path
   */
  public void setNLMPath(String nlm_path);

  /**
   * Returns the Apelon path.
   * @return the Apelon path
   */
  public String getApelonPath();

  /**
   * Sets the Apelon path.
   * @param apelon_path the Apelon path
   */
  public void setApelonPath(String apelon_path);

  /**
   * Returns the inversion script.
   * @return the inversion script
   */
  public String getInversionScript();

  /**
   * Sets the inversion script.
   * @param inversion_script the inversion script
   */
  public void setInversionScript(String inversion_script);

  /**
   * Returns the inverter notes file.
   * @return the inverter notes file
   */
  public String getInverterNotesFile();

  /**
   * Sets the inverter notes file.
   * @param inverter_notes_file the inverter notes file.
   */
  public void setInverterNotesFile(String inverter_notes_file);

  /**
   * Returns the conserve file.
   * @return the conserve file
   */
  public String getConserveFile();

  /**
   * Sets the conserve file.
   * @param conserve_file the conserve file
   */
  public void setConserveFile(String conserve_file);

  /**
   * Returns the SAB list.
   * @return the SAB list
   */
  public String getSABList();

  /**
   * Sets the SAB list.
   * @param sab_list the SAB list
   */
  public void setSABList(String sab_list);

  /**
   * Returns the meow display name.
   * @return the meow display name
   */
  public String getMeowDisplayName();

  /**
   * Sets the meow display name.
   * @param meow_display_name the meow display name
   */
  public void setMeowDisplayName(String meow_display_name);

  /**
   * Returns the source description.
   * @return the source description
   */
  public String getSourceDescription();

  /**
   * Sets the source description.
   * @param source_desc the source description
   */
  public void setSourceDescription(String source_desc);

  /**
   * Returns the status.
   * @return the status
   */
  public String getStatus();

  /**
   * Sets the status.
   * @param status the status
   */
  public void setStatus(String status);

  /**
   * Returns the worklist sortkey location.
   * @return the worklist sortkey location
   */
  public String getWorklistSortkeyLocation();

  /**
   * Sets the worklist sortkey location.
   * @param worklist_sortkey_loc the worklist sortkey location
   */
  public void setWorklistSortkeyLocation(String worklist_sortkey_loc);

  /**
   * Returns the inversion notes.
   * @return the inversion notes
   */
  public String getInversionNotes();

  /**
   * Sets the inversion notes.
   * @param inversion_notes the inversion notes
   */
  public void setInversionNotes(String inversion_notes);

  /**
   * Returns the notes.
   * @return the notes
   */
  public String getNotes();

  /**
   * Sets the notes.
   * @param notes the notes
   */
  public void setNotes(String notes);

  /**
   * Returns the inverse recipe location.
   * @return the inverse recipe location
   */
  public String getInverseRecipeLocation();

  /**
   * Sets the inverse recipe location.
   * @param inv_recipe_loc the inverse recipe location
   */
  public void setInverseRecipeLocation(String inv_recipe_loc);

  /**
   * Indicates whether or not this source has a suppressible editable record
   * @return <code>true</code> if this is suppressible editable record;
   *         <code>false</code> otherwise
   */
  public boolean getSuppressibleEditableRecord();

  /**
   * Sets the flag indicating whether or not this source has a suppressible
   * editable record.
   * @param suppress_edit_rec <code>true</codE> if so, <code>false</code>
   * otherwise
   */
  public void setSuppressibleEditableRecord(boolean suppress_edit_rec);

  /**
   * Returns the last contacted date.
   * @return the last contacted date
   */
  public Date getLastContactedDate();

  /**
   * Sets the last contacted date.
   * @param last_contacted the last contacted date
   */
  public void setLastContactedDate(Date last_contacted);

  /**
   * Returns the test insertion start date.
   * @return the test insertion start date
   */
  public Date getTestInsertionStartDate();

  /**
   * Sets the test insertion start date.
   * @param test_insertion_start the test insertion start date
   */
  public void setTestInsertionStartDate(Date test_insertion_start);

  /**
   * Returns the test insertion end date.
   * @return the test insertion end date
   */
  public Date getTestInsertionEndDate();

  /**
   * Sets the test insertion end date.
   * @param test_insertion_end the test insertion end date
   */
  public void setTestInsertionEndDate(Date test_insertion_end);

  /**
   * Returns the real insertion start date.
   * @return the real insertion start date
   */
  public Date getRealInsertionStartDate();

  /**
   * Sets the real insertion start date.
   * @param real_insertion_start the real insertion start date
   */
  public void setRealInsertionStartDate(Date real_insertion_start);

  /**
   * Returns the real insertion end date.
   * @return the real insertion end date
   */
  public Date getRealInsertionEndDate();

  /**
   * Sets the real insertion end date.
   * @param real_insertion_end the real insertion end date
   */
  public void setRealInsertionEndDate(Date real_insertion_end);

  /**
   * Returns the editing start date.
   * @return the editing start date
   */
  public Date getEditingStartDate();

  /**
   * Sets the editing start date.
   * @param editing_start the editing start date
   */
  public void setEditingStartDate(Date editing_start);

  /**
   * Returns the editing end date.
   * @return the editing end date
   */
  public Date getEditingEndDate();

  /**
   * Sets the editing end date.
   * @param editing_end the editing end date
   */
  public void setEditingEndDate(Date editing_end);

  /**
   * Returns the latest available.
   * @return the latest available
   */
  public String getLatestAvailable();

  /**
   * Sets the latest available.
   * @param latest_available the latest available
   */
  public void setLatestAvailable(String latest_available);

  /**
   * Returns the release url list.
   * @return the release url list
   */
  public String getReleaseUrlList();

  /**
   * Sets the release url list.
   * @param release_url_list the release url list
   */
  public void setReleaseUrlList(String release_url_list);

  /**
   * Returns the internal url list.
   * @return the internal url list
   */
  public String getInternalUrlList();

  /**
   * Sets the internal url list.
   * @param internal_url_list the internal url list
   */
  public void setInternalUrlList(String internal_url_list);

  /**
   * Indicates whether or not this {@link Source} asserts the direction
   * of its relationships.
   * @return <code>true</code> if it does, <codE>false</code> otherwise
   */
  public boolean getRelationshipDirectionalityFlag();

  /**
   * Sets the flag indicating whether or not this {@link Source} asserts
   * relationship directionality.
   * @param rel_directionality_flag the "rel directionality" flag
   */
  public void setRelationshipDirectionalityFlag(boolean rel_directionality_flag);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of
   * {@link Source} interface.
   */
  public class Default implements Source, Comparable {

    //
    // Fields
    //

    private Rank rank = null;
    private Source sot = null;
    private CUI vcui = null;
    private CUI rcui = null;
    private Language language = null;
    private String source = null;
    private String root_source = null;
    private String version = null;
    private String normalized_source = null;
    private String source_family = null;
    private String official_name = null;
    private String insert_meta_version = null;
    private String remove_meta_version = null;
    private String nlm_contact = null;
    private String inverter = null;
    private String acquisition_contact = null;
    private String content_contact = null;
    private String license_contact = null;
    private String license_info = null;
    private String restriction_level = null;
    private int term_frequency = 0;
    private int cui_frequency = 0;
    private String context_type = null;
    private String attribute_name_list = null;
    private String attribute_list = null;
    private String term_type_list = null;
    private String term_group_list = null;
    private String character_encoding = null;
    private String short_name = null;
    private String citation = null;
    private Date insertion_date = null;
    private Date expiration_date = null;
    private boolean is_current = false;
    private boolean is_previous = false;
    private Date date_created = null;
    private int meta_year = 0;
    private Date init_rcpt_date = null;
    private Date clean_rcpt_date = null;
    private Date test_insert_date = null;
    private Date real_insert_date = null;
    private String source_contact = null;
    private String inverter_contact = null;
    private String nlm_path = null;
    private String apelon_path = null;
    private String inversion_script = null;
    private String inverter_notes_file = null;
    private String conserve_file = null;
    private String sab_list = null;
    private String meow_display_name = null;
    private String source_desc = null;
    private String status = null;
    private String worklist_sortkey_loc = null;
    private String inversion_notes = null;
    private String notes = null;
    private String inv_recipe_loc = null;
    private boolean suppress_edit_rec = false;
    private Date last_contacted = null;
    private Date test_insertion_start = null;
    private Date test_insertion_end = null;
    private Date real_insertion_start = null;
    private Date real_insertion_end = null;
    private Date editing_start = null;
    private Date editing_end = null;
    private String latest_available = null;
    private String release_url_list = null;
    private String internal_url_list = null;
    private boolean rel_directionality_flag = false;

    //
    // Constructors
    //

    /**
     * Instantiates an empty {@link Source.Default}.
     */
    public Default() {
      super();
    }

    /**
     * Instantiates a {@link Source.Default}.
     * @param source the source abbreviation
     */
    public Default(String source) {
      this.source = source;
      this.rank = Rank.EMPTY_RANK;
    }

    //
    // Comparable implementation
    //

    public int compareTo(Object o) {
      if (o == null || ! (o instanceof Source)) {
        return 0;
      }
      return toString().compareTo(o.toString());
    }

    //
    // Overriden Object Methods
    //

    /**
     * REturns an <code>int</code> hashcode.
     * @return an <code>int</code> hashcode
     */
    public int hashCode() {
      return toString().hashCode();
    }

    //
    // Implementation of Source interface
    //

    /**
     * Returns a {@link String} representation.
     * @return a {@link String} representation
     */
    public String toString() {
      return source;
    }

    /**
     * Equality function based on {@link String} representation.
     * @param object object to compare
     * @return <code>true</code> if equal, <codE>false</code> otherwise
     */
    public boolean equals(Object object) {
      if ( (object == null) || (! (object instanceof Source))) {
        return false;
      }
      return source.equals(object.toString());
    }

    /**
     * Implements {@link Source#getSourceAbbreviation()}.
     * @return the {@link String} representation of source abbreviation
     */
    public String getSourceAbbreviation() {
      return source;
    }

    /**
     * Implements {@link Source#setSourceAbbreviation(String)}.
     * @param source the {@link String} representation of source
     * abbreviation
     */
    public void setSourceAbbreviation(String source) {
      this.source = source;
    }

    /**
     * Implements {@link Source#getStrippedSourceAbbreviation()}.
     * @return the {@link String} representation of stripped source
     * abbreviation
     */
    public String getStrippedSourceAbbreviation() {
      return getRootSourceAbbreviation();
    }

    /**
     * Implements {@link Source#setStrippedSourceAbbreviation(String)}.
     * @param stripped_source the {@link String} representation of
     * stripped source abbreviation
     */
    public void setStrippedSourceAbbreviation(String stripped_source) {
      setRootSourceAbbreviation(stripped_source);
    }

    /**
     * Implements {@link Source#getRootSourceAbbreviation()}.
     * @return the {@link String} representation of root source
     * abbreviation
     */
    public String getRootSourceAbbreviation() {
      return root_source;
    }

    /**
     * Implements {@link Source#setRootSourceAbbreviation(String)}.
     * @param root_source the {@link String} representation of
     * root source abbreviation
     */
    public void setRootSourceAbbreviation(String root_source) {
      this.root_source = root_source;
    }

    /**
     * Implements {@link Source#getSourceVersion()}.
     * @return the {@link String} representation of source version
     */
    public String getSourceVersion() {
      return version;
    }

    /**
     * Implements {@link Source#setSourceVersion(String)}.
     * @param version the {@link String} representation of source version
     */
    public void setSourceVersion(String version) {
      this.version = version;
    }

    /**
     * Implements {@link Source#getNormalizedSourceAbbreviation()}.
     * @return the {@link String} representation of normalized source
     * abbreviation
     */
    public String getNormalizedSourceAbbreviation() {
      return normalized_source;
    }

    /**
     * Implements {@link Source#setNormalizedSourceAbbreviation(String)}.
     * @param normalized_source the {@link String} representation of
     * normalized source abbreviation
     */
    public void setNormalizedSourceAbbreviation(String normalized_source) {
      this.normalized_source = normalized_source;
    }

    /**
     * Implements {@link Source#getSourceFamilyAbbreviation()}.
     * @return the {@link String} representation of source  family
     * abbreviation
     */
    public String getSourceFamilyAbbreviation() {
      return source_family;
    }

    /**
     * Implements {@link Source#setSourceFamilyAbbreviation(String)}.
     * @param source_family the {@link String} representation of source
     * family abbreviation
     */
    public void setSourceFamilyAbbreviation(String source_family) {
      this.source_family = source_family;
    }

    /**
     * Implements {@link Source#getOfficialName()}.
     * @return the {@link String} representation of official name
     */
    public String getOfficialName() {
      return official_name;
    }

    /**
     * Implements {@link Source#setOfficialName(String)}.
     * @param official_name the {@link String} representation of official
     * name
     */
    public void setOfficialName(String official_name) {
      this.official_name = official_name;
    }

    /**
     * Implements {@link Source#getCUI()}.
     * @return the {@link CUI}
     */
    public CUI getCUI() {
      return getVersionedCui();
    }

    /**
     * Implements {@link Source#setCUI(CUI)}.
     * @param cui the {@link CUI}
     */
    public void setCUI(CUI cui) {
      setVersionedCui(cui);
    }

    /**
     * Returns the versioned {@link CUI}.
     * @return the versioned {@link CUI}
     */
    public CUI getVersionedCui() {
      return vcui;
    }

    /**
     * Sets the versioned {@link CUI}.
     * @param vcui the versioned {@link CUI}
     */
    public void setVersionedCui(CUI vcui) {
      this.vcui = vcui;
    }

    /**
     * Returns the root {@link CUI}.
     * @return the root {@link CUI}
     */
    public CUI getRootCui() {
      return rcui;
    }

    /**
     * Sets the root {@link CUI}.
     * @param rcui the root {@link CUI}
     */
    public void setRootCui(CUI rcui) {
      this.rcui = rcui;
    }

    /**
     * Implements {@link Source#getLanguage()}.
     * @return the {@link Language}
     */
    public Language getLanguage() {
      return language;
    }

    /**
     * Implements {@link Source#setLanguage(Language)}.
     * @param language the {@link Language}
     */
    public void setLanguage(Language language) {
      this.language = language;
    }

    /**
     * Implements {@link Source#getInsertMetaVersion()}.
     * @return the {@link String} representation of insert meta version
     */
    public String getInsertMetaVersion() {
      return insert_meta_version;
    }

    /**
     * Implements {@link Source#setInsertMetaVersion(String)}.
     * @param insert_meta_version the {@link String} representation of
     * insert meta version
     */
    public void setInsertMetaVersion(String insert_meta_version) {
      this.insert_meta_version = insert_meta_version;
    }

    /**
     * Implements {@link Source#getRemoveMetaVersion()}.
     * @return the {@link String} representation of remove meta version
     */
    public String getRemoveMetaVersion() {
      return remove_meta_version;
    }

    /**
     * Implements {@link Source#setRemoveMetaVersion(String)}.
     * @param remove_meta_version the {@link String} representation of
     * remove meta version
     */
    public void setRemoveMetaVersion(String remove_meta_version) {
      this.remove_meta_version = remove_meta_version;
    }

    /**
     * Implements {@link Source#getNLMContact()}.
     * @return the {@link String} representation of NLM contact
     */
    public String getNLMContact() {
      return nlm_contact;
    }

    /**
     * Implements {@link Source#setNLMContact(String)}.
     * @param nlm_contact the {@link String} representation of NLM
     * contact
     */
    public void setNLMContact(String nlm_contact) {
      this.nlm_contact = nlm_contact;
    }

    /**
     * Implements {@link Source#getInverter()}.
     * @return the {@link String} representation of inverter
     */
    public String getInverter() {
      return inverter;
    }

    /**
     * Implements {@link Source#setInverter(String)}.
     * @param inverter the {@link String} representation of inverter
     */
    public void setInverter(String inverter) {
      this.inverter = inverter;
    }

    /**
     * Implements {@link Source#getAcquisitionContact()}.
     * @return the {@link String} representation of acquisition contact
     */
    public String getAcquisitionContact() {
      return acquisition_contact;
    }

    /**
     * Implements {@link Source#setAcquisitionContact(String)}.
     * @param acquisition_contact the {@link String} representation of
     * acquisition contact
     */
    public void setAcquisitionContact(String acquisition_contact) {
      this.acquisition_contact = acquisition_contact;
    }

    /**
     * Implements {@link Source#getContentContact()}.
     * @return the {@link String} representation of content contact
     */
    public String getContentContact() {
      return content_contact;
    }

    /**
     * Implements {@link Source#setContentContact(String)}.
     * @param content_contact the {@link String} representation of
     * content contact
     */
    public void setContentContact(String content_contact) {
      this.content_contact = content_contact;
    }

    /**
     * Implements {@link Source#getLicenseContact()}.
     * @return the {@link String} representation of license contact
     */
    public String getLicenseContact() {
      return license_contact;
    }

    /**
     * Implements {@link Source#setLicenseContact(String)}.
     * @param license_contact the {@link String} representation of
     * license contact
     */
    public void setLicenseContact(String license_contact) {
      this.license_contact = license_contact;
    }

    /**
     * Implements {@link Source#getLicenseInformation()}.
     * @return the {@link String} representation of license information
     */
    public String getLicenseInformation() {
      return license_info;
    }

    /**
     * Implements {@link Source#setLicenseInformation(String)}.
     * @param license_info the {@link String} representation of
     * license information
     */
    public void setLicenseInformation(String license_info) {
      this.license_info = license_info;
    }

    /**
     * Implements {@link Source#getRestrictionLevel()}.
     * @return the {@link String} representation of restriction level
     */
    public String getRestrictionLevel() {
      return restriction_level;
    }

    /**
     * Implements {@link Source#setRestrictionLevel(String)}.
     * @param restriction_level the {@link String} representation of
     * restriction level
     */
    public void setRestrictionLevel(String restriction_level) {
      this.restriction_level = restriction_level;
    }

    /**
     * Implements {@link Source#getTermFrequency()}.
     * @return An <code>int</code> representation of term frequency
     */
    public int getTermFrequency() {
      return term_frequency;
    }

    /**
     * Implements {@link Source#setTermFrequency(int)}.
     * @param term_frequency An <code>int</code> representation of
     * term frequency
     */
    public void setTermFrequency(int term_frequency) {
      this.term_frequency = term_frequency;
    }

    /**
     * Implements {@link Source#getCuiFrequency()}.
     * @return An <code>int</code> representation of cui frequency
     */
    public int getCuiFrequency() {
      return cui_frequency;
    }

    /**
     * Implements {@link Source#setCuiFrequency(int)}.
     * @param cui_frequency An <code>int</code> representation of
     * term frequency
     */
    public void setCuiFrequency(int cui_frequency) {
      this.cui_frequency = cui_frequency;
    }

    /**
     * Implements {@link Source#getContextType()}.
     * @return the {@link String} representation of context type
     */
    public String getContextType() {
      return context_type;
    }

    /**
     * Implements {@link Source#setContextType(String)}.
     * @param context_type the {@link String} representation of
     * context type
     */
    public void setContextType(String context_type) {
      this.context_type = context_type;
    }

    /**
     * Implements {@link Source#getAttributeList()}.
     * @return the {@link String} representation of attribute_list
     */
    public String getAttributeList() {
      return attribute_list;
    }

    /**
     * Implements {@link Source#setAttributeList(String)}.
     * @param attribute_list the {@link String} representation of
     * attribute list
     */
    public void setAttributeList(String attribute_list) {
      this.attribute_list = attribute_list;
    }

    /**
     * Implements {@link Source#getAttributeNameList()}.
     * @return the {@link String} representation of attribute_name_list
     */
    public String getAttributeNameList() {
      return attribute_name_list;
    }

    /**
     * Implements {@link Source#setAttributeNameList(String)}.
     * @param attribute_name_list the {@link String} representation of
     * attribute name list
     */
    public void setAttributeNameList(String attribute_name_list) {
      this.attribute_name_list = attribute_name_list;
    }

    /**
     * Implements {@link Source#getTermTypeList()}.
     * @return the {@link String} representation of term_type_list
     */
    public String getTermTypeList() {
      return term_type_list;
    }

    /**
     * Implements {@link Source#setTermTypeList(String)}.
     * @param term_type_list the {@link String} representation of
     * term type list
     */
    public void setTermTypeList(String term_type_list) {
      this.term_type_list = term_type_list;
    }

    /**
     * Implements {@link Source#getTermGroupList()}.
     * @return the {@link String} representation of term_group_list
     */
    public String getTermGroupList() {
      return term_group_list;
    }

    /**
     * Implements {@link Source#setTermGroupList(String)}.
     * @param term_group_list the {@link String} representation of
     * term group list
     */
    public void setTermGroupList(String term_group_list) {
      this.term_group_list = term_group_list;
    }

    /**
     * Implements {@link Source#getCharacterEncoding()}.
     * @return the {@link String} representation of charater encoding
     */
    public String getCharacterEncoding() {
      return character_encoding;
    }

    /**
     * Implements {@link Source#setCharacterEncoding(String)}.
     * @param character_encoding the {@link String} representation of
     * character encoding
     */
    public void setCharacterEncoding(String character_encoding) {
      this.character_encoding = character_encoding;
    }

    /**
     * Implements {@link Source#getShortName()}.
     * @return the {@link String} representation of short name
     */
    public String getShortName() {
      return short_name;
    }

    /**
     * Implements {@link Source#setShortName(String)}.
     * @param short_name the {@link String} representation of
     * short name
     */
    public void setShortName(String short_name) {
      this.short_name = short_name;
    }

    /**
     * Implements {@link Source#getCitation()}.
     * @return the {@link String} representation of citation
     */
    public String getCitation() {
      return citation;
    }

    /**
     * Implements {@link Source#setCitation(String)}.
     * @param citation the {@link String} representation of
     * citation
     */
    public void setCitation(String citation) {
      this.citation = citation;
    }

    /**
     * Implements {@link Source#getInsertionDate()}.
     * @return the {@link Date} representation of insertion date
     */
    public Date getInsertionDate() {
      return insertion_date;
    }

    /**
     * Implements {@link Source#setInsertionDate(Date)}.
     * @param insertion_date the {@link Date} representation of
     * insertion date
     */
    public void setInsertionDate(Date insertion_date) {
      this.insertion_date = insertion_date;
    }

    /**
     * Implements {@link Source#getExpirationDate()}.
     * @return the {@link Date} representation of expiration date
     */
    public Date getExpirationDate() {
      return expiration_date;
    }

    /**
     * Implements {@link Source#setExpirationDate(Date)}.
     * @param expiration_date the {@link Date} representation of
     * expiration date.
     */
    public void setExpirationDate(Date expiration_date) {
      this.expiration_date = expiration_date;
    }

    /**
     * Implements {@link Source#isCurrent()}
     * @return A <code>boolean</code> representation of "is current" flag
     */
    public boolean isCurrent() {
      return this.is_current;
    }

    /**
     * Implements {@link Source#setIsCurrent(boolean)}
     * @param is_current A <code>boolean</code> representation of
     * "is current" flag
     */
    public void setIsCurrent(boolean is_current) {
      this.is_current = is_current;
    }

    /**
     * Implements {@link Source#isPrevious()}
     * @return A <code>boolean</code> representation of "is previous" flag
     */
    public boolean isPrevious() {
      return is_previous;
    }

    /**
     * Implements {@link Source#setIsPrevious(boolean)}
     * @param is_previous A <code>boolean</code> representation of
     * "is previous" flag
     */
    public void setIsPrevious(boolean is_previous) {
      this.is_previous = is_previous;
    }

    /**
     * Implements {@link Source#getSourceToOutrank()}.
     * @return the {@link Source}
     */
    public Source getSourceToOutrank() {
      return sot;
    }

    /**
     * Implements {@link Source#setSourceToOutrank(Source)}.
     * @param sot the {@link Source}
     */
    public void setSourceToOutrank(Source sot) {
      this.sot = sot;
    }

    /**
     * Implements {@link Source#getDateCreated()}.
     * @return the {@link Date} representation of date created
     */
    public Date getDateCreated() {
      return date_created;
    }

    /**
     * Implements {@link Source#setDateCreated(Date)}.
     * @param date_created the {@link Date} representation of
     * date created
     */
    public void setDateCreated(Date date_created) {
      this.date_created = date_created;
    }

    /**
     * Implements {@link Source#getMetaYear()}.
     * @return An <code>int</code> representation of meta year
     */
    public int getMetaYear() {
      return meta_year;
    }

    /**
     * Implements {@link Source#setMetaYear(int)}.
     * @param meta_year An <code>int</code> representation of
     * meta year
     */
    public void setMetaYear(int meta_year) {
      this.meta_year = meta_year;
    }

    /**
     * Implements {@link Source#getInitialReceiptDate()}.
     * @return the {@link Date} representation of initial receipt date
     */
    public Date getInitialReceiptDate() {
      return init_rcpt_date;
    }

    /**
     * Implements {@link Source#setInitialReceiptDate(Date)}.
     * @param init_rcpt_date the {@link Date} representation of
     * initial receipt date
     */
    public void setInitialReceiptDate(Date init_rcpt_date) {
      this.init_rcpt_date = init_rcpt_date;
    }

    /**
     * Implements {@link Source#getCleanReceiptDate()}.
     * @return the {@link Date} representation of clean receipt date
     */
    public Date getCleanReceiptDate() {
      return clean_rcpt_date;
    }

    /**
     * Implements {@link Source#setCleanReceiptDate(Date)}.
     * @param clean_rcpt_date the {@link Date} representation of
     * clean receipt date
     */
    public void setCleanReceiptDate(Date clean_rcpt_date) {
      this.clean_rcpt_date = clean_rcpt_date;
    }

    /**
     * Implements {@link Source#getTestInsertionDate()}.
     * @return the {@link Date} representation of test insertion date
     */
    public Date getTestInsertionDate() {
      return test_insert_date;
    }

    /**
     * Implements {@link Source#setTestInsertionDate(Date)}.
     * @param test_insert_date the {@link Date} representation of
     * test insert date
     */
    public void setTestInsertionDate(Date test_insert_date) {
      this.test_insert_date = test_insert_date;
    }

    /**
     * Implements {@link Source#getRealInsertionDate()}.
     * @return the {@link Date} representation of real insertion date
     */
    public Date getRealInsertionDate() {
      return real_insert_date;
    }

    /**
     * Implements {@link Source#setRealInsertionDate(Date)}.
     * @param real_insert_date the {@link Date} representation of
     * real insert date
     */
    public void setRealInsertionDate(Date real_insert_date) {
      this.real_insert_date = real_insert_date;
    }

    /**
     * Implements {@link Source#getSourceContact()}.
     * @return the {@link String} representation of source contact
     */
    public String getSourceContact() {
      return source_contact;
    }

    /**
     * Implements {@link Source#setSourceContact(String)}.
     * @param source_contact the {@link String} representation of
     * source contact
     */
    public void setSourceContact(String source_contact) {
      this.source_contact = source_contact;
    }

    /**
     * Implements {@link Source#getInverterContact()}.
     * @return the {@link String} representation of inverter contact
     */
    public String getInverterContact() {
      return inverter_contact;
    }

    /**
     * Implements {@link Source#setInverterContact(String)}.
     * @param inverter_contact the {@link String} representation of
     * inverter contact
     */
    public void setInverterContact(String inverter_contact) {
      this.inverter_contact = inverter_contact;
    }

    /**
     * Implements {@link Source#getNLMPath()}.
     * @return the {@link String} representation of NLM path
     */
    public String getNLMPath() {
      return nlm_path;
    }

    /**
     * Implements {@link Source#setNLMPath(String)}.
     * @param nlm_path the {@link String} representation of
     * NLM path
     */
    public void setNLMPath(String nlm_path) {
      this.nlm_path = nlm_path;
    }

    /**
     * Implements {@link Source#getApelonPath()}.
     * @return the {@link String} representation of Apelon path
     */
    public String getApelonPath() {
      return apelon_path;
    }

    /**
     * Implements {@link Source#setApelonPath(String)}.
     * @param apelon_path the {@link String} representation of
     * Apelon path
     */
    public void setApelonPath(String apelon_path) {
      this.apelon_path = apelon_path;
    }

    /**
     * Implements {@link Source#getInversionScript()}.
     * @return the {@link String} representation of inversion script
     */
    public String getInversionScript() {
      return inversion_script;
    }

    /**
     * Implements {@link Source#setInversionScript(String)}.
     * @param inversion_script the {@link String} representation of
     * inversion script
     */
    public void setInversionScript(String inversion_script) {
      this.inversion_script = inversion_script;
    }

    /**
     * Implements {@link Source#getInverterNotesFile()}.
     * @return the {@link String} representation of inverter notes file
     */
    public String getInverterNotesFile() {
      return inverter_notes_file;
    }

    /**
     * Implements {@link Source#setInverterNotesFile(String)}.
     * @param inverter_notes_file the {@link String} representation of
     * inverter notes file
     */
    public void setInverterNotesFile(String inverter_notes_file) {
      this.inverter_notes_file = inverter_notes_file;
    }

    /**
     * Implements {@link Source#getConserveFile()}.
     * @return the {@link String} representation of conserve file
     */
    public String getConserveFile() {
      return conserve_file;
    }

    /**
     * Implements {@link Source#setConserveFile(String)}.
     * @param conserve_file the {@link String} representation of
     * conserve file
     */
    public void setConserveFile(String conserve_file) {
      this.conserve_file = conserve_file;
    }

    /**
     * Implements {@link Source#getSABList()}.
     * @return the {@link String} representation of SAB List
     */
    public String getSABList() {
      return sab_list;
    }

    /**
     * Implements {@link Source#setSABList(String)}.
     * @param sab_list the {@link String} representation of
     * SAB list
     */
    public void setSABList(String sab_list) {
      this.sab_list = sab_list;
    }

    /**
     * Implements {@link Source#getMeowDisplayName()}.
     * @return the {@link String} representation of meow display name
     */
    public String getMeowDisplayName() {
      return meow_display_name;
    }

    /**
     * Implements {@link Source#setMeowDisplayName(String)}.
     * @param meow_display_name the {@link String} representation of
     * meow display name
     */
    public void setMeowDisplayName(String meow_display_name) {
      this.meow_display_name = meow_display_name;
    }

    /**
     * Implements {@link Source#getSourceDescription()}.
     * @return the {@link String} representation of source description
     */
    public String getSourceDescription() {
      return source_desc;
    }

    /**
     * Implements {@link Source#setSourceDescription(String)}.
     * @param source_desc the {@link String} representation of
     * source description
     */
    public void setSourceDescription(String source_desc) {
      this.source_desc = source_desc;
    }

    /**
     * Implements {@link Source#getStatus()}.
     * @return the {@link String} representation of status
     */
    public String getStatus() {
      return status;
    }

    /**
     * Implements {@link Source#setStatus(String)}.
     * @param status the {@link String} representation of status
     */
    public void setStatus(String status) {
      this.status = status;
    }

    /**
     * Implements {@link Source#getWorklistSortkeyLocation()}.
     * @return the {@link String} representation of worklist
     * sortkey location
     */
    public String getWorklistSortkeyLocation() {
      return worklist_sortkey_loc;
    }

    /**
     * Implements {@link Source#setWorklistSortkeyLocation(String)}.
         * @param worklist_sortkey_loc the {@link String} representation of worklist
     * sortkey location
     */
    public void setWorklistSortkeyLocation(String worklist_sortkey_loc) {
      this.worklist_sortkey_loc = worklist_sortkey_loc;
    }

    /**
     * Implements {@link Source#getInversionNotes()}.
     * @return the {@link String} representation of inversion notes
     */
    public String getInversionNotes() {
      return inversion_notes;
    }

    /**
     * Implements {@link Source#setInversionNotes(String)}.
     * @param inversion_notes the {@link String} representation of
     * inversion notes
     */
    public void setInversionNotes(String inversion_notes) {
      this.inversion_notes = inversion_notes;
    }

    /**
     * Implements {@link Source#getNotes()}.
     * @return the {@link String} representation of notes
     */
    public String getNotes() {
      return notes;
    }

    /**
     * Implements {@link Source#setNotes(String)}.
     * @param notes the {@link String} representation of notes
     */
    public void setNotes(String notes) {
      this.notes = notes;
    }

    /**
     * Implements {@link Source#getInverseRecipeLocation()}.
     * @return the {@link String} representation of inverse recipe
     * location
     */
    public String getInverseRecipeLocation() {
      return inv_recipe_loc;
    }

    /**
     * Implements {@link Source#setInverseRecipeLocation(String)}.
     * @param inv_recipe_loc the {@link String} representation of inverse
     * recipe location
     */
    public void setInverseRecipeLocation(String inv_recipe_loc) {
      this.inv_recipe_loc = inv_recipe_loc;
    }

    /**
     * Implements {@link Source#getSuppressibleEditableRecord()}
         * @return A <code>boolean</code> representation of "suppress edit rec" flag
     */
    public boolean getSuppressibleEditableRecord() {
      return this.suppress_edit_rec;
    }

    /**
     * Implements {@link Source#setSuppressibleEditableRecord(boolean)}
     * @param suppress_edit_rec A <code>boolean</code> representation of
     * "suppress edit rec" flag
     */
    public void setSuppressibleEditableRecord(boolean suppress_edit_rec) {
      this.suppress_edit_rec = suppress_edit_rec;
    }

    /**
     * Implements {@link Source#getLastContactedDate()}.
     * @return the {@link Date} representation of last contacted date
     */
    public Date getLastContactedDate() {
      return last_contacted;
    }

    /**
     * Implements {@link Source#setLastContactedDate(Date)}.
     * @param last_contacted the {@link Date} representation of
     * last contacted date
     */
    public void setLastContactedDate(Date last_contacted) {
      this.last_contacted = last_contacted;
    }

    /**
     * Implements {@link Source#getTestInsertionStartDate()}.
     * @return the {@link Date} representation of test insertion start date
     */
    public Date getTestInsertionStartDate() {
      return test_insertion_start;
    }

    /**
     * Implements {@link Source#setTestInsertionStartDate(Date)}.
     * @param test_insertion_start the {@link Date} representation of
     * test insertion start date
     */
    public void setTestInsertionStartDate(Date test_insertion_start) {
      this.test_insertion_start = test_insertion_start;
    }

    /**
     * Implements {@link Source#getTestInsertionEndDate()}.
     * @return the {@link Date} representation of test insertion end date
     */
    public Date getTestInsertionEndDate() {
      return test_insertion_end;
    }

    /**
     * Implements {@link Source#setTestInsertionEndDate(Date)}.
     * @param test_insertion_end the {@link Date} representation of
     * test insertion end date
     */
    public void setTestInsertionEndDate(Date test_insertion_end) {
      this.test_insertion_end = test_insertion_end;
    }

    /**
     * Implements {@link Source#getRealInsertionStartDate()}.
     * @return the {@link Date} representation of real insertion start date
     */
    public Date getRealInsertionStartDate() {
      return real_insertion_start;
    }

    /**
     * Implements {@link Source#setRealInsertionStartDate(Date)}.
     * @param real_insertion_start the {@link Date} representation of
     * real insertion start date
     */
    public void setRealInsertionStartDate(Date real_insertion_start) {
      this.real_insertion_start = real_insertion_start;
    }

    /**
     * Implements {@link Source#getRealInsertionEndDate()}.
     * @return the {@link Date} representation of real insertion end date
     */
    public Date getRealInsertionEndDate() {
      return real_insertion_end;
    }

    /**
     * Implements {@link Source#setRealInsertionEndDate(Date)}.
     * @param real_insertion_end the {@link Date} representation of
     * real insertion end date
     */
    public void setRealInsertionEndDate(Date real_insertion_end) {
      this.real_insertion_end = real_insertion_end;
    }

    /**
     * Implements {@link Source#getEditingStartDate()}.
     * @return the {@link Date} representation of editing start date
     */
    public Date getEditingStartDate() {
      return editing_start;
    }

    /**
     * Implements {@link Source#setEditingStartDate(Date)}.
     * @param editing_start the {@link Date} representation of
     * editing start date
     */
    public void setEditingStartDate(Date editing_start) {
      this.editing_start = editing_start;
    }

    /**
     * Implements {@link Source#getEditingEndDate()}.
     * @return the {@link Date} representation of editing end date
     */
    public Date getEditingEndDate() {
      return editing_end;
    }

    /**
     * Implements {@link Source#setEditingEndDate(Date)}.
     * @param editing_end the {@link Date} representation of
     * editing end date
     */
    public void setEditingEndDate(Date editing_end) {
      this.editing_end = editing_end;
    }

    /**
     * Implements {@link Source#getLatestAvailable()}.
     * @return the {@link String} representation of latest available
     */
    public String getLatestAvailable() {
      return latest_available;
    }

    /**
     * Implements {@link Source#setLatestAvailable(String)}.
     * @param latest_available the {@link String} representation of
     * latest available
     */
    public void setLatestAvailable(String latest_available) {
      this.latest_available = latest_available;
    }

    /**
     * Implements {@link Source#getReleaseUrlList()}.
     * @return the {@link String} representation of release url list
     */
    public String getReleaseUrlList() {
      return release_url_list;
    }

    /**
     * Implements {@link Source#setReleaseUrlList(String)}.
     * @param release_url_list the {@link String} representation of
     * release url list
     */
    public void setReleaseUrlList(String release_url_list) {
      this.release_url_list = release_url_list;
    }

    /**
     * Implements {@link Source#getInternalUrlList()}.
     * @return the {@link String} representation of internal url list
     */
    public String getInternalUrlList() {
      return internal_url_list;
    }

    /**
     * Implements {@link Source#setInternalUrlList(String)}.
     * @param internal_url_list the {@link String} representation of
     * internal url list
     */
    public void setInternalUrlList(String internal_url_list) {
      this.internal_url_list = internal_url_list;
    }

    /**
     * Implements {@link Source#getRelationshipDirectionalityFlag()}.
     * @return a <code>boolean</code> values that represents release
     * directionality flag
     */
    public boolean getRelationshipDirectionalityFlag() {
      return this.rel_directionality_flag;
    }

    /**
     * Implements {@link Source#setRelationshipDirectionalityFlag(boolean)}.
     * @param rel_directionality_flag A <code>boolean</code> representation of
     * flag to determine the release directionality
     */
    public void setRelationshipDirectionalityFlag(boolean
                                                  rel_directionality_flag) {
      this.rel_directionality_flag = rel_directionality_flag;
    }

    //
    // Implementation of Rankable interface
    //

    /**
     * Implements {@link Rankable#getRank()}.
     * @return the {@link Rank}
     */
    public Rank getRank() {
      return rank;
    }

    /**
     * Implements {@link Rankable#setRank(Rank)}.
     * @param rank the {@link Rank}
     */
    public void setRank(Rank rank) {
      this.rank = rank;
    }

  }
}
