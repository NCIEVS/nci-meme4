/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  DataSourceConstants
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEConstants;

/**
     * This interface holds constants commonly used by the MEME data source APIs.  It
 * supplements the {@link gov.nih.nlm.meme.MEMEConstants}.  Most of the
 * constants represent property names.
 *
 * @see gov.nih.nlm.meme.MEMEConstants
 * @author MEME Group
 */
public interface DataSourceConstants extends MEMEConstants {

  //
  // Connecting to the MID
  //

  /**
   * Name of the Java class used for a database connection, for example
   * "gov.nih.nlm.sql.MIDConnection".
   */
  public final static String MID_CONNECTION = "meme.mid.connection.class";

  /**
   * A MIDServices DbService name representing the MID.
   *
   */
  public final static String MID_SERVICE = "meme.mid.service.default";

  /**
   * JDBC Driver class for MID database.
   */
  public final static String MID_DRIVER_CLASS = "meme.mid.driver.class";

  /**
   * Password of the MID database user.
   */
  public final static String MID_PASSWORD = "meme.mid.password.default";

  /**
   * Name of MID database user.
   */
  public final static String MID_USER = "meme.mid.user.default";

  //
  // Using the schedule
  //

  /**
   * Property indicating what MID Services DB service should be used for the
   * schedule.
   */
  public final static String MEME_SCHEDULE = "meme.server.schedule.service";

  /**
   * Property indicating how long applications running in DELAY mode with
   * respect to the schedule should wait between actions.
   */
  public final static String MEME_SCHEDULE_DELAY = "meme.server.schedule.delay";

  //
  // DataSourcePool configuration
  //

  /**
   * DataSourcePool's optimal size.
   */
  public final static String DP_OPTIMAL_SIZE =
      "meme.datasourcepool.size.optimal";

  /**
   * DataSourcePool's maximum size.
   */
  public final static String DP_MAX_SIZE = "meme.datasourcepool.size.max";

  /**
   * DataSourcePool's auto extend.
   */
  public final static String DP_AUTOEXTEND = "meme.datasourcepool.autoextend";

  /**
   * List of properties which must to be set.
   */
  public final static String[] REQUIRED_PROPERTIES = {
      MEME_HOME, ORACLE_HOME};

  /**
   * Additional allowed properties.  These are read from the properties file
   * when the toolkit is initialized.
   */
  public final static String[] ALLOWABLE_PROPERTIES = {
      MID_CONNECTION, MID_SERVICE, MID_DRIVER_CLASS, MID_USER, MID_PASSWORD,
      MEME_SCHEDULE, MEME_SCHEDULE_DELAY, DP_OPTIMAL_SIZE, DP_MAX_SIZE,
      DP_AUTOEXTEND,
      ORACLE_HOME, MEME_HOME};

}
