/**
 * Version.java
 */

package gov.nih.nlm.umls.jekyll.relae;

import java.text.SimpleDateFormat;
import java.util.Date;
import gov.nih.nlm.meme.MEMEToolkit;

/**
 * The <code>Version</code> class provides package version and
 * history information for gov.nih.nlm.umls.jekyll.relae
 * package.
 *
 * History:
 *
 *  03/15/2004   4.0.0: Made suitable to be run via Web Start.
 *  07/22/2002   2.4.0: "mapped_to" and "mapped_from" are added to the list
 *                      of valid relationship attributes.
 *  07/08/2002   2.3.0: User is now able to pull up worklists as well.
 *  06/04/2002   2.2.0: All NLM% relationships are now displayed in the
 *                      relationships box.
 *  05/17/2002   2.1.0: NLM03 rels deletion funtion is added,
 *                      relationships are now being created between
 *                      concept_ids.
 *  03/26/2002   2.0.0: Version for SNF editing.
 */

public class Version {

  //
  // Fields
  //

  private final static int release = 5;
  private final static double version = 0.0;
  private final static String version_authority = "VOA";
  private final static String version_date = "14-Jun-2004 00:00:00";
  private final static String package_name =
      "gov.nih.nlm.umls.jekyll.relae";

  //
  // Methods
  //

  /**
   * This method returns the package current release.
   * @return An <code>int</code> representation of package current release.
   */
  public static int getRelease() {
    return release;
  }

  /**
   * This method returns the package current version.
   * @return A <code>double</code> representation of package current version.
   */
  public static double getVersion() {
    return version;
  }

  /**
   * This method returns the package current version authority.
   * @return A <code>String</code> representation of package current version
   * authority.
   */
  public static String getAuthority() {
    return version_authority;
  }

  /**
   * This method returns the package current version date.
   * @return A <code>Date</code> representation of package current version
   * date.
   */
  public static Date getDate() {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    Date date = null;
    try {
      date = formatter.parse(version_date);
    }
    catch (Exception e) {
      MEMEToolkit.handleError(e);
    }
    return date;
  }

  /**
   * This method returns the package current package name.
   * @return A <code>String</code> representation of package current name.
   */
  public static String getPackage() {
    return package_name;
  }

  /**
   * This method returns the package current version information.
   * @return A <code>String</code> representation of package current
   * version information.
   */
  public static String getVersionInformation() {
    return getPackage() + ": " + getRelease() + "." + getVersion() + ", "
        + getDate() + " (" + getAuthority() + ")";
  }

  //
  // Main
  //

  /**
   * This can be used to print package version information
   * on the command line.
   */
  public static void main(String argv[]) {
    MEMEToolkit.trace(gov.nih.nlm.meme.Version.getVersionInformation());
  }
}
