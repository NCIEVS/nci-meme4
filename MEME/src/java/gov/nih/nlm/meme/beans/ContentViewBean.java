/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  ContentViewBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;

import gov.nih.nlm.meme.client.ContentViewClient;
import gov.nih.nlm.meme.common.ContentView;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.exception.MEMEException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link ContentViewClient} wrapper used for editing content views.
 *
 * @author MEME Group
 */
 public class ContentViewBean extends ClientBean {

  //
  // Fields
  //

  private ContentViewClient cvc = null;
  Identifier identifier = null;
  String contributor = null;
  String cont_version = null;
  String cont_url = null;
  Date cont_date = new Date();
  String maintainer = null;
  String maint_version = null;
  String maint_url = null;
  Date maint_date = new Date();
  String name = null;
  String prev_meta = null;
  String desc = null;
  String algo = null;
  String cat = null;
  String sub_cat = null;
  String cv_class = null;
  long code = 0;
  boolean cascade = false;
  boolean is_generated = false;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ContentViewBean}.
   * @throws MEMEException if failed to construct this class.
   */
  public ContentViewBean() throws MEMEException {
    super();
    cvc = new ContentViewClient("");
  }

  //
  // Methods
  //
  /**
   * Factory method to create a {@link ContentView} from the set of parameters
   * tracked by this bean.
   * @return a configured {@link ContentView}
   * @throws ParseException if anything goes wrong
   */
  public ContentView newContentView() throws ParseException {
    ContentView cv = new ContentView.Default();
    cv.setIdentifier(getIdentifier());
    cv.setContributor(getContributor());
    cv.setContributorVersion(getContributorVersion());
    cv.setContributorURL(getContributorURL());
    cv.setContributorDate(getDateFormat().parse(getContributorDate()));
    cv.setMaintainer(getMaintainer());
    cv.setMaintainerVersion(getMaintainerVersion());
    cv.setMaintainerURL(getMaintainerURL());
    cv.setMaintainerDate(getDateFormat().parse(getMaintainerDate()));
    cv.setName(getName());
    cv.setPreviousMeta(getPreviousMeta());
    cv.setDescription(getDescription());
    cv.setAlgorithm(getAlgorithm());
    cv.setCategory(getCategory());
    cv.setSubCategory(getSubCategory());
    cv.setContentViewClass(getContentViewClass());
    cv.setCode(getCode());
    cv.setCascade(getCascade());
    cv.setIsGeneratedByQuery(isGeneratedByQuery());
    return cv;
  }

  /**
   * Returns a fully configured {@link ContentViewClient}.
   * @return a fully configured {@link ContentViewClient}
   */
  public ContentViewClient getContentViewClient() {
    configureClient(cvc);
    cvc.setMidService(getMidService());
    return cvc;
  }

  /**
   * Returns the HTML code <code>SELECTED</code>
   * if the specified host matches the client bean host.
   * @param host the host to check
   * @return the SELECTED code or ""
   */
  public String getHostSelectedFlag(String host) {
    if (host.equals(getHost()))
      return "SELECTED";
    else return "";
  }

  //
  // Implementation of ContentView interface
  //

  /**
   * Returns the {@link Identifier}.
   * @return the {@link Identifier}
   */
  public Identifier getIdentifier() {
    return this.identifier;
  }

  /**
   * Sets the {@link Identifier}.
   * @param identifier the {@link Identifier}
   */
  public void setIdentifier(Identifier identifier) {
    this.identifier = identifier;
  }

  /**
   * Sets the {@link Identifier}
   * @param id the {@link Identifier} as a {@link String}
   */
  public void setIdentifier(String id) {
    setIdentifier(new Identifier.Default(id));
  }

  /**
   * Returns the contributor.
   * @return the contributor
   */
  public String getContributor() {
    return this.contributor;
  }

  /**
   * Sets the contributor.
   * @param contributor the contributor
   */
  public void setContributor(String contributor) {
    this.contributor = contributor;
  }

  /**
   * Returns the contributor's version number.
   * @return the contributor's version number
   */
  public String getContributorVersion() {
    return this.cont_version;
  }

  /**
   * Sets the contributor's version number.
   * @param cont_version the contributor's version number
   */
  public void setContributorVersion(String cont_version) {
    this.cont_version = cont_version;
  }

  /**
   * Returns the contributor's URL.
   * @return the contributor's URL
   */
  public String getContributorURL() {
    return this.cont_url;
  }

  /**
   * Sets the contributor's URL.
   * @param cont_url the contributor's URL
   */
  public void setContributorURL(String cont_url) {
    this.cont_url = cont_url;
  }

  /**
   * Returns the contributor's date.
   * @return the contributor's date
   */
  public String getContributorDate() {
    return getDateFormat().format(cont_date);
  }

  /**
   * Sets the contributor's date.  Appends a time of "00:00:00" to the
   * end if not specified.
   * @param date the contributor's date
   */
  public void setContributorDate(String date) {
    try {
      if (date.length() > 10)
        this.cont_date = getDateFormat().parse(date);
      else
        this.cont_date = getDateFormat().parse(date + " 00:00:00");
    } catch (ParseException pe) {
      RuntimeException re = new RuntimeException("Invalid contributor date format");
      re.initCause(pe);
      throw re;
    }
  }

  /**
   * Returns the maintainer.
   * @return the maintainer
   */
  public String getMaintainer() {
    return this.maintainer;
  }

  /**
   * Sets the maintainer.
   * @param maintainer the maintainer
   */
  public void setMaintainer(String maintainer) {
    this.maintainer = maintainer;
  }

  /**
   * Returns the maintainer's version.
   * @return the maintainer's version
   */
  public String getMaintainerVersion() {
    return this.maint_version;
  }

  /**
   * Sets the maintainer's version.
   * @param maint_version the maintainer's version
   */
  public void setMaintainerVersion(String maint_version) {
    this.maint_version = maint_version;
  }

  /**
   * Returns the maintainer's URL.
   * @return the maintainer's URL
   */
  public String getMaintainerURL() {
    return this.maint_url;
  }

  /**
   * Sets the maintainer's URL.
   * @param maint_url the maintainer's URL.
   */
  public void setMaintainerURL(String maint_url) {
    this.maint_url = maint_url;
  }

  /**
   * Returns the maintainer's date.
   * @return the maintainer's date
   */
  public String getMaintainerDate() {
    return getDateFormat().format(maint_date);
  }

  /**
   * Sets the maintainer's date. Appends a time of "00:00:00" to the
   * end if not specified.
   * @param date the maintainer's date.
   * @throws java.text.ParseException if date cannot be parsed
   */
  public void setMaintainerDate(String date) throws java.text.ParseException {
    try {
      if (date.length() > 10)
        this.maint_date = getDateFormat().parse(date);
      else
        this.maint_date = getDateFormat().parse(date + " 00:00:00");
    }
    catch (ParseException pe) {
      RuntimeException re = new RuntimeException(
          "Invalid maintainer date format");
      re.initCause(pe);
      throw re;
    }
  }

  /**
   * Returns the name.
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the previous version of the Metathesaurus that the data
   * is based upon.
   * @return the previous version of the Metathesaurus that the data is based on
   */
  public String getPreviousMeta() {
    return this.prev_meta;
  }

  /**
   * Sets the previous version of the Metathesaurus that the data is based on.
   * @param prev_meta the previous version of the Metathesaurus that the data based on
   */
  public void setPreviousMeta(String prev_meta) {
    this.prev_meta = prev_meta;
  }

  /**
   * Returns the description.
   * @return the description
   */
  public String getDescription() {
    return this.desc;
  }

  /**
   * Sets the description.
   * @param desc the description.
   */
  public void setDescription(String desc) {
    this.desc = desc;
  }

  /**
   * Returns a description of the algorithm used to compute the data.
   * @return a description of the algorithm used to compute the data
   */
  public String getAlgorithm() {
    return this.algo;
  }

  /**
   * Sets a description of the algorithm used to compute the data.
   * @param algo a description of the algorithm used to compute the data
   */
  public void setAlgorithm(String algo) {
    this.algo = algo;
  }

  /**
   * Returns the category.
   * @return the category
   */
  public String getCategory() {
    return this.cat;
  }

  /**
   * Sets the category.
   * @param cat the category
   */
  public void setCategory(String cat) {
    this.cat = cat;
  }

  /**
   * Returns the sub category.
   * @return the sub category
   */
  public String getSubCategory() {
    return this.sub_cat;
  }

  /**
   * Sets the sub category.
   * @param sub_cat the sub category
   */
  public void setSubCategory(String sub_cat) {
    this.sub_cat = sub_cat;
  }

  /**
   * Returns the content view class.
   * @return the content view class
   */
  public String getContentViewClass() {
    return this.cv_class;
  }

  /**
   * Sets the content view class.
   * @param cv_class the content view class
   */
  public void setContentViewClass(String cv_class) {
    this.cv_class = cv_class;
  }

  /**
   * Returns the code.
   * @return the code
   */
  public long getCode() {
    return this.code;
  }

  /**
   * Sets the code.
   * @param code the code
   */
  public void setCode(long code) {
    this.code = code;
  }

  /**
   * Returns the flag indicating whether to cascade this view to elements
   * connected to the specified list of identifiers.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean getCascade() {
    return this.cascade;
  }

  /**
   * sets the flag indicating whether to cascade this view to elements
   * connected to the specified list of identifiers.
   * @param cascade <code>true</code> if so, <code>false</code> otherwise
   */
  public void setCascade(boolean cascade) {
    this.cascade = cascade;
  }

  /**
   * Returns the flag indicating whether or not the set of identifiers
   * for this view is generated by a query.
   * @return <codE>true</codE> if so, <codE>false</code> otherwise
   */
  public boolean isGeneratedByQuery() {
    return this.is_generated;
  }

  /**
   * Sets the flag indicating whether or not the set of identifiers
   * for this view is generated by a query.
   * @param is_generated <codE>true</codE> if so, <codE>false</code> otherwise
   */
  public void setIsGeneratedByQuery(boolean is_generated) {
    this.is_generated = is_generated;
  }

  /**
   * Returns the standard {@link SimpleDateFormat}.
   * @return the standard {@link SimpleDateFormat}
   */
  public SimpleDateFormat getDateFormat() {
    return new SimpleDateFormat("MM/dd/yyyy");
  }

  /**
   * Resets all field values.
   */
  public void reset() {
    identifier = null;
    contributor = null;
    cont_version = null;
    cont_url = null;
    cont_date = new Date();
    maintainer = null;
    maint_version = null;
    maint_url = null;
    maint_date = new Date();
    name = null;
    prev_meta = null;
    desc = null;
    algo = null;
    cat = null;
    sub_cat = null;
    cv_class = null;
    code = 0;
    cascade = false;
    is_generated = false;
  }

}