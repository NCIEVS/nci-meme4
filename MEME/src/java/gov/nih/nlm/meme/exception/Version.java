/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  Version
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

import gov.nih.nlm.meme.MEMEToolkit;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The <code>Version</code> class provides package version information
 * for gov.nih.nlm.meme.exception package.
 *
 * <b>History:</b>
 * <dl>
 * <dt>06/17/2005:</dt>
 * <dd>Javadoc comments cleaned up.
 * </dd>
 * <dt>01/25/2005:</dt>
 * <dd>{@link FailedToConnectException} and {@link UnknownStateException} were
 * created for reporting client side network errors.
 * </dd>
 * <dt>12/30/2004:</dt>
 * <dd>{@link ActionException} now has a no-argument constructor.  This should
 * hopefully solve the client-side issue where the serializer cannot find
 * a constructor for the interface "LoggedAction".
 * </dd>
 * <dt>11/03/2004:</dt>
 * <dd>Minor documentation changes.
 * </dd>
 * <dt>09/19/2004:</dt>
 * <dd>New exception: {@link FailedToConnectException}.
 * </dd>
 * <dt>04/19/2004:</dt>
 *           <dd>{@link StaleDataException} now prints the stack trace to the log.
 *           This is to help diagnose a RxNorm problem TPW was having.
 *           </dd>
 * <dt>02/06/2004:</dt>
 *           <dd>No substantive changes, only minor documentation changes.
 *           </dd>
 * <dt>01/16/2004:</dt>
 *           <dd>New Exception: {@link StaleDataException}. {@link IntegrityViolationException}
 *           no longer prints a stack trace. javadoc comments for all exceptions were updated.
 *           </dd>
 * <dt>12/04/2003:</dt>
 *           <dd>{@link MissingDataException} does not print a stack trace.
 *           Javadocs were updated for a couple of other classes.
 *           {@link ExpiredSessionException} was added.
 *           </dd>
 * <dt>11/07/2003:</dt>
 *           <dd>{@link IntegrityViolationException} no longer informs the administrator via email.
 *           </dd>
 * <dt>06/06/2003:</dt>
 *           <dd>{@link MEMEException} handles the case where enclosed
 *           exception is the exception itself better.
 *           </dd>
 * <dt>04/25/2003:</dt>
 *           <dd>Minor changes to {@link MissingDataException}.</dd>
 * <dt>04/09/2003:</dt>
 *           <dd>New exception: {@link MissingDataException}, it's fired
 *           when reading data from the database and not enough rows are found.
 *           The exception is configured to not inform the administrator in the
 *           way that a {@link DataSourceException} does, thus preventing
 *           the administrator from getting set large volumes of email.
 *           </dd>
 * <dt>04/01/2003:</dt>
 *           <dd>{@link MEMEException#toString()} method was udpated to
 *           allow the details section to be <code>null</code> without
 *           throwing a null pointer exception.  The {@link ExternalResourceException}
 *           now sets the flag to inform an administrator <i>and</i> sets
 *           the administrator email address.
 *           </dd>
 * <dt>03/05/2003:</dt>
 *           <dd>
 *           Addition of {@link ConcurrencyException} to deal with
 *           reentrant applications.  Also, {@link ExternalResourceException}
 *           was changed to inform the administrator.  This happens because
 *           of Socket exceptions noticed in the log.
 *           </dd>
 * <dt>08/28/2002:</dt>
 *           <dd>Addition of exceptions to support MID services
 *               Specifically {@link LvgServerException}
 *           </dd>
 * <dt>05/13/2002:</dt>
 *           <dd>Addition of exceptions to support action engine.
 *               Specifically {@link ActionException},
 *               {@link AtomicActionException}, and
 *               {@link IntegrityViolationException}.
 *           </dd>
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
  private final static String package_name = "gov.nih.nlm.meme.exception";

  //
  // Methods
  //

  /**
   * Returns the package current release.
   * @return the package current release
   */
  public static int getRelease() {
    return release;
  }

  /**
   * Returns the package current version.
   * @return the package current version
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
    System.out.println(gov.nih.nlm.meme.exception.Version.getVersionInformation());
  }
}
