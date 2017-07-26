/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.server
 * Object:  DataSourceConstants
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.sql;


/**
 * This interface holds constants commonly used when connecting
 * to an MRD data source.
 *
 * @see gov.nih.nlm.meme.sql.DataSourceConstants
 *
 * @author  MEME Group
 */
public interface DataSourceConstants
  extends gov.nih.nlm.meme.sql.DataSourceConstants {

  //
  // Connecting to the MRD
  //

  /**
   * Name of the Java class used for a database connection, for example
   * "gov.nih.nlm.sql.MRDConnection".
   */
  public final static String MRD_CONNECTION = "meme.mrd.connection.class";

  /**
   * A MRDServices DbService name representing the MRD.
   *
   */
  public final static String MRD_SERVICE = "meme.mrd.service.default";

  /**
   * JDBC Driver class for MRD database.
   */
  public final static String MRD_DRIVER_CLASS = "meme.mrd.driver.class";

  /**
   * Password of the MRD database user.
   */
  public final static String MRD_PASSWORD = "meme.mrd.password.default";

  /**
   * Name of MRD database user.
   */
  public final static String MRD_USER = "meme.mrd.user.default";

}
