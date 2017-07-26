/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.server
 * Object:  ServerConstants
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.server;

import gov.nih.nlm.mrd.sql.DataSourceConstants;

/**
 * Holds constants commonly used by the MRD application server.
 *
 * @see gov.nih.nlm.meme.server.ServerConstants
 *
 * @author  MEME Group
 */
public interface ServerConstants extends gov.nih.nlm.meme.server.
    ServerConstants,
    DataSourceConstants {

  /**
   * Property representing the $MRD_HOME directory.
   */
  public final static String MRD_HOME = "env.MRD_HOME";
  public final static String ENV_HOME = "env.ENV_FILE";
  public final static String ENV_FILE = "env.ENV_HOME";

  /**
   * List of properties which must to be set.
   */
  public final static String[] REQUIRED_PROPERTIES =
      {ENV_HOME, ENV_FILE, MRD_HOME,
       MEME_HOME, ORACLE_HOME};

  /**
   * Additional allowed properties.  These are read from the properties file
   * when the toolkit is initialized.
   */
  public final static String[] ALLOWABLE_PROPERTIES = {
      ENV_HOME, ENV_FILE, MID_CONNECTION,
      MID_SERVICE, MID_DRIVER_CLASS, MID_USER, MID_PASSWORD,
      MRD_CONNECTION, MRD_SERVICE, MRD_DRIVER_CLASS, MRD_USER, MRD_PASSWORD,
      MEME_SCHEDULE, MEME_SCHEDULE_DELAY, MEME_BOOTSTRAP, MEME_SERVICES,
      DP_OPTIMAL_SIZE, DP_MAX_SIZE, DP_AUTOEXTEND,
      ORACLE_HOME, MEME_HOME, MRD_HOME, MEME_SERVER_HOST, MEME_SERVER_PORT
  };
}
