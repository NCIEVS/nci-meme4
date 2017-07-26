/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  Version
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.BadValueException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The <code>Version</code> class provides package version information
 * for gov.nih.nlm.meme.integrity package.
 *
 * <b>History:</b>
 * <dl>
 * <dt>06/17/2005</dt>
 * <dd>Javadoc comments improved.
 * </dd>
 * <dt>12/13/2004:</dt>
 * <dd>{@link MGV_M} logic excludes non-ENG atoms.
 * </dd>
 * <dt>11/03/2004:</dt>
 * <dd>Minor documentation changes.
 * </dd>
 * <dt>07/09/2004:</dt>
 * <dd>Update to {@link MGV_M} to allow NEC merges involving SNOMEDCT, SNMI,
 * and RCD99.
 * </dd>
 * <dt>06/14/2004:</dt>
 * <dd>New check {@link MGV_STY}.  Bug fix for {@link MGV_MUI}.
 * </dd>
 * <dt>02/19/2004:</dt>
 *              <dd>{@link MGV_M} now uses root source abbreviations for comparison.
 *              </dd>
 * <dt>02/06/2004:</dt>
 *              <dd>Only minor changes to resolve javadoc issues.
 *              </dd>
 * <dt>01/16/2004:</dt>
 *              <dd>Javadocs for all classes were updated.
 *              </dd>
 * <dt>12/04/2003:</dt>
 *              <dd>Added {@link MoveInhibitor}.  {@link MGV_H1} and {@link MGV_H2}
 *              were updated to work as move inhibitors.  {@link EnforcableIntegrityVector}
 *              was updated support <code>applyMoveInhibitors</code> methods.
 *              </dd>
 * <dt>08/27/2003:</dt>
 *              <dd>{@link DT_I3B} was fixed again .
 *              </dd>
 * <dt>08/25/2003:</dt>
 *              <dd>{@link DT_I3B} was buggy.  This was fixed.
 *              </dd>
 * <dt>06/27/2003:</dt>
 *              <dd>{@link DT_M1} allows unapproved STYs (because concept approval
 *                  will approve the STY
 *              </dd>
 * <dt>06/19/2003:</dt>
 *              <dd>{@link MGV_MM1} and {@link MGV_MM2} were changed to
 *              extend {@link MGV_MM}.  They are just dummy classes to allow
 *              old data in the integrity system to continue to work.  A
 *              new similar one {@link MGV_MM1B} was also added.
 *              </dd>
 * <dt>04/01/2003:</dt>
 *              <dd>DT_M1 now reports a violation if the concept
 *                  has no semantic types.</dd>
 * <dt>03/07/2001:</dt>
 *              <dd>MGV_MUI procedure added.</dd>
 * <dt>11/01/2001</dt><dd>1st Release.</dd>
 * </dl>
 *
 * @author MEME Group
 */

public class Version {

  //
  // Fields
  //

  private final static int release = 4;
  private final static double version = 0.0;
  private final static String version_authority = "MEME Group";
  private final static String version_date = "28-Aug-2002 00:00:00";
  private final static String package_name = "gov.nih.nlm.meme.integrity";

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
   * @return the package current version authority
   */
  public static String getAuthority() {
    return version_authority;
  }

  /**
   * Returns the package current version date.
   * @return the package current version date
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
   * @return the package current package name
   */
  public static String getPackage() {
    return package_name;
  }

  /**
   * Returns the package current version information.
   * @return the package current version information
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
   * @param argv An array of argument.
   */
  public static void main(String argv[]) {
    System.out.println(gov.nih.nlm.meme.integrity.Version.getVersionInformation());
  }
}
