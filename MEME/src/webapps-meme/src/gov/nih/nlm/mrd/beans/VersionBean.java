/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.beans
 * Object:  VersionBean
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.beans;

/**
 * This bean represents version information in action harvester form.
 *
 * @author Bobby Edrosa
 */
public class VersionBean {

  //
  // Fields
  //
  private String release = "4";
  private String version = "1";
  private String version_authority = "BAC";
  private String version_date = "01/07/2003";

  //
  // Methods
  //

  /**
   * Returns the release information.
   * @return the release information
   */
  public String getRelease() {
    return release;
  }

  /**
   * Returns the version information.
   * @return the version information
   */
  public String getVersion() {
    return version;
  }

  /**
   * Returns the version authority information.
   * @return the version authority information
   */
  public String getVersionAuthority() {
    return version_authority;
  }

  /**
   * Returns the version date information.
   * @return the version date information
   */
  public String getVersionDate() {
    return version_date;
  }

}