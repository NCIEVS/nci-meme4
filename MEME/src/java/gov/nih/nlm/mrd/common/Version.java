/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  Version
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.common;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.BadValueException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The <code>Version</code> class provides package version information
 * for gov.nih.nlm.mrd.common package.
 *
 * <b>History</b>
 * <dl>
 * <dt>06/02/2005:</dt>
 *   <dd>Improved method javadocs.
 *   </dd>
 * <dt>03/20/2001:</dt><dd>1st Release.</dd>
 * </dl>
 *
 * @author  MRD Group
 * @version 4.1.0, 01/31/2002
 */

public class Version {

  //
  // Fields
  //

  private final static int release = 4;
  private final static double version = 1.0;
  private final static String version_authority = "MRD Group";
  private final static String version_date = "31-Jan-2002 00:00:00";
  private final static String package_name = "gov.nih.nlm.mrd.common";

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
      BadValueException bve = new BadValueException(
          "Badly formatted version date.");
      bve.setDetail("version_date", version_date);
      MEMEToolkit.handleError(bve);
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
   * @param argv An array object {@link String} representation of arguments.
   */
  public static void main(String argv[]) {
    System.out.println(gov.nih.nlm.mrd.common.Version.getVersionInformation());
  }
}
