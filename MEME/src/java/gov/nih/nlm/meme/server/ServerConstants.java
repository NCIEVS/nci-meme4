/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  ServerConstants
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.sql.DataSourceConstants;

/**
 * This interface holds constants commonly used by the MEME server.  It
 * supplements the {@link gov.nih.nlm.meme.MEMEConstants}.  Most of the
 * constants represent property names.
 *
 * @see gov.nih.nlm.meme.MEMEConstants
 *
 * @author MEME Group
 */

public interface ServerConstants extends DataSourceConstants {

  //
  // Server configuration
  //

  /**
   * Property whose value will be a comma-separated list of  fully-qualified
   * names of classes implementing the {@link gov.nih.nlm.meme.Initializable} interface.
   * The classes in this list are initialized by the {@link ServerToolkit}.
   */
  public final static String MEME_BOOTSTRAP = "meme.server.bootstrap.classes";

  /**
   * Property indicating services.
   */
  public final static String MEME_SERVICES = "meme.server.services.classes";

  /**
   * Property indicating server host.
   */
  public final static String MEME_SERVER_HOST = "meme.server.host";

  /**
   * Property indicating server port.
   */
  public final static String MEME_SERVER_PORT = "meme.server.port";

  /**
   * Property indicating default timeout for a session.
   */
  public final static String MEME_SESSION_TIMEOUT =
      "meme.server.session.timeout";

  /**
   * List of properties which must to be set.
   */
  public final static String[] REQUIRED_PROPERTIES = {
      ENV_FILE, ENV_HOME, MEME_HOME, ORACLE_HOME};

  /**
   * Additional allowed properties.  These are read from the properties file
   * when the toolkit is initialized.
   */
  public final static String[] ALLOWABLE_PROPERTIES = {
      ENV_FILE, ENV_HOME, MID_CONNECTION, MID_SERVICE, MID_DRIVER_CLASS,
      MID_USER, MID_PASSWORD, DEBUG,
      MEME_SCHEDULE, MEME_SCHEDULE_DELAY, MEME_BOOTSTRAP, MEME_SERVICES,
      DP_OPTIMAL_SIZE, DP_MAX_SIZE, DP_AUTOEXTEND, MEME_SESSION_TIMEOUT,
      ORACLE_HOME, MEME_HOME, MEME_SERVER_HOST, MEME_SERVER_PORT};

}
