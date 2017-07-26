/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ContentView
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generically represents a Content View.
 *
 * @author MEME Group
 */

public interface ContentView {

  /**
   * Adds the specified {@link ContentViewMember}.
   * @param cvm the {@link ContentViewMember}
   */
  public void addMember(ContentViewMember cvm);

  /**
   * Removes the specified {@link ContentViewMember}.
   * @param cvm the {@link ContentViewMember}
   */
  public void removeMember(ContentViewMember cvm);

  /**
   * Returns all {@link ContentViewMember}s.
   * @return all {@link ContentViewMember}s
   */
  public ContentViewMember[] getMembers();

  /**
   * Sets the {@link ContentViewMember}s.
   * @param cvm the {@link ContentViewMember}s
   */
  public void setMembers(ContentViewMember[] cvm);

  /**
   * Indicates whether or not the specified{@link Atom} is a member.
   * @param atom the {@link Atom} to test
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isMember(Atom atom);

  /**
   * Indicates whether or not the specified {@link Attribute} is a member.
   * @param attr the {@link Attribute} to test
   * @return <code>true</code> if so,  <code>false</code> otherwise
   */
  public boolean isMember(Attribute attr);

  /**
   * Indicates whether or not the specified {@link Relationship} is a member.
   * @param rel the {@link Relationship} to test
   * @return <code>true</code> if so,  <code>false</code> otherwise
   */
  public boolean isMember(Relationship rel);

  /**
   * Indicates whether or not the specified {@link Concept} is a member.
   * @param concept the {@link Concept} to test
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isMember(Concept concept);

  /**
   * Returns the {@link Identifier}.
   * @return the {@link Identifier}
   */
  public Identifier getIdentifier();

  /**
   * Sets the {@link Identifier}.
   * @param identifier the {@link Identifier}
   */
  public void setIdentifier(Identifier identifier);

  /**
   * Sets the {@link Identifier}.
   * @param id the {@link Identifier}
   */
  public void setIdentifier(String id);

  /**
   * Returns the contributor.
   * @return the contributor
   */
  public String getContributor();

  /**
   * Sets the contributor.
   * @param contributor the contributor
   */
  public void setContributor(String contributor);

  /**
   * Returns the contributor version.
   * @return the contributor version
   */
  public String getContributorVersion();

  /**
   * Sets the contributor version.
   * @param cont_version the contributor version
   */
  public void setContributorVersion(String cont_version);

  /**
   * Returns the contributor URL.
   * @return the contributor URL
   */
  public String getContributorURL();

  /**
   * Sets the contributor URL.
   * @param url the contributor URL
   */
  public void setContributorURL(String url);

  /**
   * Returns the contributor date.
   * @return the contributor date
   */
  public Date getContributorDate();

  /**
   * Sets the contributor date.
   * @param cont_date the contributor date
   */
  public void setContributorDate(Date cont_date);

  /**
   * Returns the maintainer.
   * @return the maintainer
   */
  public String getMaintainer();

  /**
   * Sets the maintainer.
   * @param maintainer the maintainer
   */
  public void setMaintainer(String maintainer);

  /**
   * Returns the maintainer version.
   * @return the maintainer version
   */
  public String getMaintainerVersion();

  /**
   * Sets the maintainer version.
   * @param maint_version the maintainer version
   */
  public void setMaintainerVersion(String maint_version);

  /**
   * Returns the maintainer URL.
   * @return the maintainer URL
   */
  public String getMaintainerURL();

  /**
   * Sets the maintainer URL.
   * @param url the maintainer URL
   */
  public void setMaintainerURL(String url);

  /**
   * Returns the maintainer date.
   * @return the maintainer date
   */
  public Date getMaintainerDate();

  /**
   * Sets the maintainer date.
   * @param maint_date the maintainer date
   */
  public void setMaintainerDate(Date maint_date);

  /**
       * Returns the previous Metathesaurus used to compute the content view members.
       * @return the previous Metathesaurus used to compute the content view members
   */
  public String getPreviousMeta();

  /**
   * Sets the previous Metathesaurus used to compute the content view members.
   * @param previous_meta the previous Metathesaurus used to compute the content view members
   */
  public void setPreviousMeta(String previous_meta);

  /**
   * Returns the contributor.
   * @return the contributor
   */
  public String getName();

  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the description.
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * @param desc the description
   */
  public void setDescription(String desc);

  /**
   * Returns the algorithm.
   * @return the algorithm
   */
  public String getAlgorithm();

  /**
   * Sets the algorithm.
   * @param algo the algorithm
   */
  public void setAlgorithm(String algo);

  /**
   * Returns the category.
   * @return the category
   */
  public String getCategory();

  /**
   * Sets the category.
   * @param cat the category
   */
  public void setCategory(String cat);

  /**
   * Returns the sub category.
   * @return the sub category
   */
  public String getSubCategory();

  /**
   * Sets the sub category.
   * @param sub_cat the sub category
   */
  public void setSubCategory(String sub_cat);

  /**
   * Returns the content view class.
   * @return the content view class
   */
  public String getContentViewClass();

  /**
   * Sets the content view class.
   * @param cv_class the content view class
   */
  public void setContentViewClass(String cv_class);

  /**
   * Returns the code.
   * @return the code
   */
  public long getCode();

  /**
   * Sets the code.
   * @param code the code
   */
  public void setCode(long code);

  /**
   * Returns the cascade.
   * @return the cascade
   */
  public boolean getCascade();

  /**
   * Sets the cascade.
   * @param cascade the cascade
   */
  public void setCascade(boolean cascade);

  /**
   * Indicates whether or not the content view is generated by query.
   * @return <code>true</code> if the content view is generated by query;
   * <code>false</code> otherwise
   */
  public boolean isGeneratedByQuery();

  /**
   * Sets the flag indicating whether or not the content view is generated by query.
   * @param is_generated <code>true</code> if the content view is generated by
   * query; <code>false</code> otherwise
   */
  public void setIsGeneratedByQuery(boolean is_generated);

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link Atom} interface.
   */
  public class Default implements ContentView {

    //
    // Fields
    //

    List members = null;
    Identifier identifier = null;
    String contributor = null;
    String cont_version = null;
    String contributor_url = null;
    Date cont_date = new Date();
    String maintainer = null;
    String maint_version = null;
    String maintainer_url = null;
    Date maint_date = new Date();
    String name = null;
    String desc = null;
    String algo = null;
    String cat = null;
    String sub_cat = null;
    String cv_class = null;
    long code = 0;
    boolean cascade = false;
    boolean is_generated = false;
    String previous_meta = null;

    //
    // Constructors
    //

    /**
     * Instantiates an empty default {@link ContentViewMember}.
     */
    public Default() {
      super();
    }

    //
    // Implementation of ContentView interface
    //

    /**
     * Adds specified {@link ContentViewMember}.
     * @param cvm the {@link ContentViewMember}
     */
    public void addMember(ContentViewMember cvm) {
      if (members == null) {
        members = new ArrayList();
      }
      members.add(cvm);
    }

    /**
     * Removes the specified {@link ContentViewMember}.
     * @param cvm the {@link ContentViewMember}
     */
    public void removeMember(ContentViewMember cvm) {
      ContentViewMember[] cvms =
          (ContentViewMember[]) members.toArray(new ContentViewMember[] {});
      for (int i = 0; i < cvms.length; i++) {
        if (cvms[i].getIdentifier().equals(cvm.getIdentifier())) {
          members.remove(i);
        }
      }
    }

    /**
     * Returns the {@link ContentViewMember}s.
     * @return the {@link ContentViewMember}s
     */
    public ContentViewMember[] getMembers() {
      if (members == null) {
        return new ContentViewMember[0];
      } else {
        return (ContentViewMember[])
            members.toArray(new ContentViewMember[] {});
      }
    }

    /**
     * Sets the {@link ContentViewMember}s.
     * @param cvms the {@link ContentViewMember}s
     */
    public void setMembers(ContentViewMember[] cvms) {
      if (members == null) {
        members = new ArrayList();
      }
      for (int i = 0; i < cvms.length; i++) {
        members.add(cvms[i]);
      }
    }

    /**
         * Indicates whether or not the specified {@link Atom} is in the member list.
     * @param atom the specified {@link Atom}
     * @return <code>true</code> if so; <code>false</code> otherwise
     */
    public boolean isMember(Atom atom) {
      ContentViewMember cvm = new ContentViewMember.Default(atom);
      ContentViewMember cvm2 = new ContentViewMember.Default();
      cvm2.setIdentifier(atom.getSUI());
      return members.contains(cvm) || members.contains(cvm2);
    }

    /**
     * Indicates whether or not the specified {@link Attribute} is in the member list.
     * @param attr the specified {@link Attribute}
     * @return <code>true</code> if so; <code>false</code> otherwise
     */
    public boolean isMember(Attribute attr) {
      ContentViewMember cvm = new ContentViewMember.Default(attr);
      return members.contains(cvm);
    }

    /**
     * Indicates whether or not the specified {@link Relationship} is in the member list.
     * @param rel the specified {@link Relationship}
     * @return <code>true</code> if so; <code>false</code> otherwise
     */
    public boolean isMember(Relationship rel) {
      ContentViewMember cvm = new ContentViewMember.Default(rel);
      return members.contains(cvm);
    }

    /**
         * Indicates whether or not the specified {@link Concept} is in the member list.
     * @param concept the specified {@link Concept}
     * @return <code>true</code> if so; <code>false</code> otherwise
     */
    public boolean isMember(Concept concept) {
      ContentViewMember cvm = new ContentViewMember.Default(concept);
      return members.contains(cvm);
    }

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
     * Sets the {@link Identifier}.
     * @param id the {@link String} identifier
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
     * Returns the contributor version.
     * @return the contributor version
     */
    public String getContributorVersion() {
      return this.cont_version;
    }

    /**
     * Sets the contributor version.
     * @param cont_version the contributor version
     */
    public void setContributorVersion(String cont_version) {
      this.cont_version = cont_version;
    }

    /**
     * Returns the contributor URL.
     * @return the contributor URL
     */
    public String getContributorURL() {
      return contributor_url;
    }

    /**
     * Sets the contributor URL.
     * @param url the contributor URL
     */
    public void setContributorURL(String url) {
      contributor_url = url;
    }

    /**
     * Returns the contributor {@link Date}.
     * @return the contributor {@link Date}
     */
    public Date getContributorDate() {
      return this.cont_date;
    }

    /**
     * Sets the contributor {@link Date}.
     * @param cont_date the contributor {@link Date}
     */
    public void setContributorDate(Date cont_date) {
      this.cont_date = cont_date;
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
     * Returns the maintainer version.
     * @return the maintainer version
     */
    public String getMaintainerVersion() {
      return this.maint_version;
    }

    /**
     * Sets the maintainer version.
     * @param maint_version the maintainer version
     */
    public void setMaintainerVersion(String maint_version) {
      this.maint_version = maint_version;
    }

    /**
     * Returns the maintainer URL.
     * @return the maintainer URL
     */
    public String getMaintainerURL() {
      return maintainer_url;
    }

    /**
     * Sets the maintainer URL.
     * @param url the maintainer URL
     */
    public void setMaintainerURL(String url) {
      maintainer_url = url;
    }

    /**
     * Returns the maintainer {@link Date}.
     * @return the maintainer {@link Date}
     */
    public Date getMaintainerDate() {
      return this.maint_date;
    }

    /**
     * Sets the maintainer {@link Date}.
     * @param maint_date the maint_date
     */
    public void setMaintainerDate(Date maint_date) {
      this.maint_date = maint_date;
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
     * Returns the previous metathesaurus.
     * @return the prvious metathesaurus
     */
    public String getPreviousMeta() {
      return this.previous_meta;
    }

    /**
     * Sets the previous metathesaurus.
     * @param previous_meta the previous metathesaurus
     */
    public void setPreviousMeta(String previous_meta) {
      this.previous_meta = previous_meta;
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
     * @param desc the description
     */
    public void setDescription(String desc) {
      this.desc = desc;
    }

    /**
     * Returns the algorithm.
     * @return the algorithm
     */
    public String getAlgorithm() {
      return this.algo;
    }

    /**
     * Sets the algorithm.
     * @param algo the algorithm
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
     * Returns the sub-category.
     * @return the sub-category
     */
    public String getSubCategory() {
      return this.sub_cat;
    }

    /**
     * Sets the sub-category.
     * @param sub_cat the sub-category
     */
    public void setSubCategory(String sub_cat) {
      this.sub_cat = sub_cat;
    }

    /**
     * Returns the content-view-class.
     * @return the content-view-class
     */
    public String getContentViewClass() {
      return this.cv_class;
    }

    /**
     * Sets the content-view-class.
     * @param cv_class the content-view-class
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
     * Indicates whether or not this view cascades to objects connected
     * via the member identifiers.
     * @return <code>true</code> if so, <code>false</code> otherwise
     */
    public boolean getCascade() {
      return this.cascade;
    }

    /**
     * Sets the cascade flag.
     * @param cascade the cascade flag
     */
    public void setCascade(boolean cascade) {
      this.cascade = cascade;
    }

    /**
     * Indicates whether or not this view was generated by an SQL query.
     * @return <code>true</code> if so, <code>false</code> otherwise
     */
    public boolean isGeneratedByQuery() {
      return this.is_generated;
    }

    /**
     * Sets the "is generated by query" flag.
     * @param is_generated the "is generated by query" flag
     */
    public void setIsGeneratedByQuery(boolean is_generated) {
      this.is_generated = is_generated;
    }
  }
}