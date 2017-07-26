/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  VersionBean
 *
 *****************************************************************************/

package gov.nih.nlm.meme.beans;



/**
 * This bean represents version information in action harvester form.
 * <dl>
 * <dt>08/06/2004: Version 4.1.5</dt>
 *  <dd>Major improvements include addition of meme.tld tag library descriptor
 *  and the two tags {@link gov.nih.nlm.meme.web.CalendarTag} and
 * {@link gov.nih.nlm.meme.web.FooterTag}.
 *  </dd>
 * </dl>
 *
 * @author MEME Group
 */

public class VersionBean {

  //
  // Fields
  //

  private String release = "4";
  private String version = "1.5";
  private String version_authority = "BAC";
  private String version_date = "08/06/2004";

  //
  // Methods
  //

  /**
   * Returns the release information.
   * @return the release information.
   */
  public String getRelease() {
    return release;
  }

  /**
   * Returns the version information.
   * @return the version information.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Returns the version authority information.
   * @return the version authority information.
   */
  public String getVersionAuthority() {
    return version_authority;
  }

  /**
   * Returns the version date information.
   * @return the version date information.
   */
  public String getVersionDate() {
    return version_date;
  }

}