/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  Version
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.BadValueException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The <code>Version</code> class provides package version information
 * for gov.nih.nlm.meme.action package.
 *
 * <b>History</b>
 * <dl>
 * <dt>01/25/2005:</dt>
 * <dd>Minor changes to molecular actions.  Certain ones should not even
 * bother to compute CUIs (like relationship/attribute actions).  Also, we
 * now have slightly improved toString() methods for "move" and "split" actions
 * that list the atom ids involved.
 *
 * {@link LoggedAction}<code>.removeSubAction(LoggedAction)</code>.
 *
 * New {@link Recipe} and {@link RecipeStep} classes.
 * </dd>
 * <dt>01/14/2005:</dt>
 * <dd> {@link MEMEDataSourceAction} and {@link MIDDataSourceAction} interfaces
 * have better javadoc comments.
 *
 * {@link LoggedAction} now defines <code>getInverseAction</code>
 *
 * The various {@link MEMEDataSourceAction} and {@link MIDDataSourceAction}
 * implementations now use <code>getInitialState</code> and <code>getInverseAction</code>.
 *
 * We have a new {@link NonOperationAction} for handling the inverse action
 * of things that do not have an invers.
 *
 * {@link NextIdentifierAction} was changed slightly so serializing it did not
 * require writing a field of type {@link Class} to the file.  This caused
 * that weird array store exception.  It now stores the class name as a string.
 *
 * {@link MolecularApproveConceptAction} had a bug fix relating to the function
 * that converts C level RT? or LK rels to RT when the concept is approved. IT now
 * does this properly.
 *
 * {@link MolecularMergeAction} was allowing a situation where a C level rel
 * would remain status N while a demotion overlapped it.  This hole in the
 * logic was repaired.  If the problem persists, it is likely from move/split actions.
 *
 * {@link AtomicAction} implementations no longer have constructors for
 * {@link gov.nih.nlm.meme.common.ConceptMapping} objects.
 * </dt>
 * <dt>12/13/2004:</dt>
 * <dd>{@link MolecularAction} has a switch to control whether it causes a CUI assignemtn
 *
 * {@link MEMEDataSourceAction}s now have a <code>getInitialState()</code> method.
     * {@link MIDDataSourceAction}s now have a <code>getInitialState()</code> method.
 *
 * New actions: {@link SystemStatusAction}, {@link CuiAction}, {@link MatrixAction},
 * {@link ExecAction}, {@link QueryAction}, {@link LoadTableAction}
 * </dd>
 * <dt>11/05/2004:</dt>
 * <dd>This is the first version of the action package.  All actions from
 *     the client package were moved here and the client ones were made
 *     to extend these (and were then deprecated).
 *
 *     This package was created as part of an ambitious project to overhaul
 *     action handling and logging in MEME4 with the eventual goal of having
 *     every change to the database be represented as a MEME4 action which
 *     can be logged and synchronized with MRD.
 *
 * </dd>
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
  private final static String version_date = "03-Nov-2004 00:00:00";
  private final static String package_name = "gov.nih.nlm.meme.action";

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
   * @param argv An array of arguments.
   */
  public static void main(String argv[]) {
    System.out.println(gov.nih.nlm.meme.client.Version.getVersionInformation());
  }
}
