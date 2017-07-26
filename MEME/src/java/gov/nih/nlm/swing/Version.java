/*****************************************************************************
 *
 * Package: gov.nih.nlm.swing
 * Object:  Version
 *
 *****************************************************************************/

package gov.nih.nlm.swing;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The <code>Version</code> class aggregates change information
 * for the <code>gov.nih.nlm.swing</code> package.  Eventually,
 * it may be used to maintain its own version information.
 *
 * <b>History:</b>
 * <dl>
 * <dt>06/17/2005</dt>
 * <dd>Some work on javadoc comments and standardizing formatting.
 * </dd>
 * <dt>12/27/2004</dt>
 * <dd>Version tracking class was created.
 * </dd>
 * </dl>
 *
 * @author MEME Group
 *
 */
public class Version {

  //
  // Fields
  //

  private final static int release = 1;
  private final static double version = 0.0;
  private final static String version_authority = "MEME Group";
  private final static String version_date = "27-Dec-2004 15:09:00";
  private final static String package_name = "gov.nih.nlm.swing";

  //
  // Methods
  //

  /**
   * Returns the package current release.
   * @return An <code>int</code> representation of package current release.
   */
  public static int getRelease() {
    return release;
  }

  /**
   * Returns the package current version.
   * @return A <code>double</code> representation of package current version.
   */
  public static double getVersion() {
    return version;
  }

  /**
   * Returns the package current version authority.
   * @return An object {@link String} representation of package current version
   * authority.
   */
  public static String getAuthority() {
    return version_authority;
  }

  /**
   * Returns the package current version date.
   * @return An object {@link Date} representation of package current version
   * date.
   */
  public static Date getDate() {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    Date date = null;
    try {
      date = formatter.parse(version_date);
    } catch (Exception e) {
      e.printStackTrace();
      // do nothing
    }
    return date;
  }

  /**
   * Returns the package current package name.
   * @return An object {@link String} representation of package current name.
   */
  public static String getPackage() {
    return package_name;
  }

  /**
   * Returns the package current version information.
   * @return An object {@link String} representation of package current
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
   * @param argv An array of string argument.
   */
  public static void main(String argv[]) {
    System.out.println(gov.nih.nlm.swing.Version.getVersionInformation());
  }
}
