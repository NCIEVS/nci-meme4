/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  ReleaseInfo
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.common;

import gov.nih.nlm.meme.common.Authority;

import java.util.Date;

/**
 * Represents information about a release.
 *
 * @author  MRD Group
 */
public class ReleaseInfo {

  // Fields

  private String name, description, build_host, build_uri, release_host,
      release_uri;
  private String documentation_host, documentation_uri;
  private Date release_date, mbd, med, start_date, end_date;
  private String generator_class;
  private Authority authority, administrator;
  private boolean isBuilt, isPublished;
  private ReleaseInfo prev_release, prev_maj_release;

  public ReleaseInfo() {
  }

  /**
   * Sets the release name.
   * @param name the release name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the release name.
   * @return the release name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the release {@link Date}.
   * @param release_date the release {@link Date}
   */
  public void setReleaseDate(Date release_date) {
    this.release_date = release_date;
  }

  /**
   * Returns the release {@link Date}.
   * @return the release {@link Date}
   */
  public Date getReleaseDate() {
    return release_date;
  }

  /**
   * Sets the description.
   * @param description the description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the description.
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the generator class name.
   * @param generator_class the generator class name
   */
  public void setGeneratorClass(String generator_class) {
    this.generator_class = generator_class;
  }

  /**
   * Returns the generator class name.
   * @return the generator class name
   */
  public String getGeneratorClass() {
    return generator_class;
  }

  /**
   * Sets the build host.
   * @param build_host the build host
   */
  public void setBuildHost(String build_host) {
    this.build_host = build_host;
  }

  /**
   * Returns the build host.
   * @return the build host
   */
  public String getBuildHost() {
    return build_host;
  }

  /**
   * Sets the build uri.
   * @param build_uri the build uri
   */
  public void setBuildUri(String build_uri) {
    this.build_uri = build_uri;
  }

  /**
   * Returns the build uri.
   * @return the build uri
   */
  public String getBuildUri() {
    return build_uri;
  }

  /**
   * Sets the release host.
   * @param release_host the release host
   */
  public void setReleaseHost(String release_host) {
    this.release_host = release_host;
  }

  /**
   * Returns the release host.
   * @return the release host
   */
  public String getReleaseHost() {
    return release_host;
  }

  /**
   * Sets the release URI.
   * @param release_uri the release URI
   */
  public void setReleaseUri(String release_uri) {
    this.release_uri = release_uri;
  }

  /**
   * Returns the release URI.
   * @return the release URI
   */
  public String getReleaseUri() {
    return release_uri;
  }

  /**
   * Sets the documentation host.
   * @param documentation_host the documentation host
   */
  public void setDocumentationHost(String documentation_host) {
    this.documentation_host = documentation_host;
  }

  /**
   * Returns the documentation host.
   * @return the documentation host
   */
  public String getDocumentationHost() {
    return documentation_host;
  }

  /**
   * Sets the documentation URI.
   * @param documentation_uri the documentation URI
   */
  public void setDocumentationUri(String documentation_uri) {
    this.documentation_uri = documentation_uri;
  }

  /**
   * Returns the documentation URI.
   * @return the documentation URI
   */
  public String getDocumentationUri() {
    return documentation_uri;
  }

  /**
   * Sets the MED start {@link Date}.
   * @param med the MED start {@link Date}
   */
  public void setMEDStartDate(Date med) {
    this.med = med;
  }

  /**
   * Returns the MED start {@link Date}.
   * @return the MED start {@link Date}
   */
  public Date getMEDStartDate() {
    return med;
  }

  /**
   * Sets the MBD start {@link Date}.
   * @param mbd the MBD start {@link Date}
   */
  public void setMBDStartDate(Date mbd) {
    this.mbd = mbd;
  }

  /**
   * Returns the MBD start {@link Date}.
   * @return the MBD start {@link Date}
   */
  public Date getMBDStartDate() {
    return mbd;
  }

  /**
   * Sets the start {@link Date}.
   * @param start_date the start {@link Date}
   */
  public void setStartDate(Date start_date) {
    this.start_date = start_date;
  }

  /**
   * Returns the start {@link Date}.
   * @return the start {@link Date}
   */
  public Date getStartDate() {
    return start_date;
  }

  /**
   * Sets the end {@link Date}.
   * @param end_date the end {@link Date}
   */
  public void setEndDate(Date end_date) {
    this.end_date = end_date;
  }

  /**
   * Returns the end {@link Date}.
   * @return the end {@link Date}
   */
  public Date getEndDate() {
    return end_date;
  }

  /**
   * Sets the {@link Authority}.
   * @param authority the {@link Authority}
   */
  public void setAuthority(Authority authority) {
    this.authority = authority;
  }

  /**
   * Returns the {@link Authority}.
   * @return the {@link Authority}
   */
  public Authority getAuthority() {
    return authority;
  }

  /**
   * Sets the administrator.
   * @param administrator the administrator
   */
  public void setAdministrator(Authority administrator) {
    this.administrator = administrator;
  }

  /**
   * Returns the administrator.
   * @return the administrator
   */
  public Authority getAdministrator() {
    return administrator;
  }

  /**
   * Indicates whether or not the release is built.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isBuilt() {
    return isBuilt;
  }

  /**
   * Sets the flag indicating whether or not the release is built.
   * @param isBuilt <code>true</code> if so, <code>false</code> otherwise
   */
  public void setIsBuilt(boolean isBuilt) {
    this.isBuilt = isBuilt;
  }

  /**
   * Indicates whether or not the release is published.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isPublished() {
    return isPublished;
  }

  /**
   * Sets the flag indicating whether or not the release is published.
   * @param isBuilt <code>true</code> if so, <code>false</code> otherwise
   */
  public void setIsPublished(boolean isPublished) {
    this.isPublished = isPublished;
  }

  /**
   * Indicates whether or not the release is finished.
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isFinished() {
    return isBuilt && isPublished;
  }

  /**
   * Sets the previons {@link ReleaseInfo}.
   * @param prev_release the previons {@link ReleaseInfo}
   */
  public void setPreviousReleaseInfo(ReleaseInfo prev_release) {
    this.prev_release = prev_release;
  }

  /**
   * Returns the previons {@link ReleaseInfo}.
   * @return the previons {@link ReleaseInfo}
   */
  public ReleaseInfo getPreviousReleaseInfo() {
    return prev_release;
  }

  /**
   * Sets the previous major {@link ReleaseInfo}.
   * @param prev_maj_release the previous major {@link ReleaseInfo}
   */
  public void setPreviousMajorReleaseInfo(ReleaseInfo prev_maj_release) {
    this.prev_maj_release = prev_maj_release;
  }

  /**
   * Returns the previous major {@link ReleaseInfo}.
   * @return the previous major {@link ReleaseInfo}
   */
  public ReleaseInfo getPreviousMajorReleaseInfo() {
    return prev_maj_release;
  }
}