/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.server
 * Object:  ServerToolkit
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.server;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceConnectionException;
import gov.nih.nlm.meme.exception.MidsvcsException;
import gov.nih.nlm.meme.exception.MissingPropertyException;
import gov.nih.nlm.meme.exception.PoolOverflowException;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.mrd.sql.MRDDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for the MRD Application Server.
 * It contains methods for initializing applications, managing
 * the data source pool, and other useful things.
 *
 * @author  MEME Group
 */
public class ServerToolkit extends gov.nih.nlm.meme.server.ServerToolkit {

  //
  // Fields
  //

  private static MRDDataSourcePool mrd_dsp = null;

  //
  // Methods
  //

  /**
   * Sets data source pool.
   * @param dp the {@link MRDDataSourcePool}
   */
  public static void setDataSourcePool(MRDDataSourcePool dp) {
    mrd_dsp = dp;
  }

  /**
   * Returns a newly instantiated default {@link MRDDataSource}.
   * @return a newly instantiated default {@link MRDDataSource}
   * @throws ReflectionException if connection could not be opened
   * @throws MidsvcsException if the midsvcs server is not available
   * @throws DataSourceConnectionException if cannot establish data source connection
   * @throws MissingPropertyException if data is missing in property file
   */
  public static MRDDataSource newMRDDataSource() throws ReflectionException,
      MidsvcsException,
      DataSourceConnectionException, MissingPropertyException {
    return newMRDDataSource(null, null, null);
  };

  /**
   * Returns a newly instantiated {@link MRDDataSource}.
   * If service is "", use MRD_SERVICE property.
   *
   * Note: this method belongs in DataSourcePool.
   *
   * @param service the service name (e.g. editing-d)
   * @param user the username
   * @param password the password
   * @return a newly instantiated {@link MRDDataSource}
   * @throws ReflectionException if connection could not be opened
   * @throws MidsvcsException if the midsvcs server is not available
   * @throws DataSourceConnectionException if cannot establish data source connection
   * @throws MissingPropertyException if data is missing in property file
   * @see gov.nih.nlm.meme.MIDServices
   */
  public static MRDDataSource newMRDDataSource(String service, String user,
                                               String password) throws
      ReflectionException, MidsvcsException,
      DataSourceConnectionException, MissingPropertyException {

    MRDDataSource mrd_data_source = null;

    // If service is empty, look it up in property
    if (service == null || service.equals("")) {
      service = getProperty(ServerConstants.MRD_SERVICE);

      // If username is empty, look it up in property
    }
    if (user == null || user.equals("")) {
      user = getProperty(ServerConstants.MRD_USER);

      // If password is empty, look it up in property
    }
    if (password == null || password.equals("")) {
      password = getProperty(ServerConstants.MRD_PASSWORD);
      if (password == null)
        password = MIDServices.getDataSourcePassword(user);
    }

    String connection = MEMEToolkit.getProperty(ServerConstants.MRD_CONNECTION);
    String driver_class = MEMEToolkit.getProperty(ServerConstants.
                                                  MRD_DRIVER_CLASS);

    // If any parameters are null, then throw an exception
    if (service == null) {
      throw new MissingPropertyException(
          "Failed to create MRDDataSource with a null service name.",
          ServerConstants.MRD_SERVICE);
    }
    if (user == null) {
      throw new MissingPropertyException(
          "Failed to create MRDDataSource with a null user name.",
          ServerConstants.MRD_USER);
    }
    if (password == null) {
      throw new MissingPropertyException(
          "Failed to create MRDDataSource with a null password.",
          ServerConstants.MRD_PASSWORD);
    }
    if (connection == null) {
      throw new MissingPropertyException(
          "Failed to create MRDDataSource with a null connection class.",
          ServerConstants.MRD_CONNECTION);
    }
    if (driver_class == null) {
      throw new MissingPropertyException(
          "Failed to create MRDDataSource with a null JDBC driver class.",
          ServerConstants.MRD_DRIVER_CLASS);
    }

    // Initialize the MRDConnection
    Properties mrd_props = new Properties();
    mrd_props.setProperty("connection", connection);
    mrd_props.setProperty("driver_class", driver_class);
    mrd_props.setProperty("password", password);
    mrd_props.setProperty("user", user);
    mrd_props.setProperty("service", service);
    MEMEToolkit.trace("Connection props: " + mrd_props);
    mrd_data_source =
        (MRDDataSource) factory.newMEMEDataSource(mrd_props);
    // mrd_data_source.setOperationsQueueMode (true);
    return mrd_data_source;
  }

  /**
   * Returns a {@link MRDDataSource} from the data source pool.
   * @return a {@link MRDDataSource} from the data source pool
   * @throws ReflectionException if connection could not be opened
   * @throws MidsvcsException if the midsvcs server is not available
   * @throws DataSourceConnectionException if cannot establish data source connection
   * @throws MissingPropertyException if data is missing in property file
   * @throws PoolOverflowException if data source limit has been reached
   */
  public static MRDDataSource getMRDDataSource() throws ReflectionException,
      MidsvcsException, DataSourceConnectionException,
      MissingPropertyException, PoolOverflowException {
    return mrd_dsp.getDataSource();
  }

  /**
   * Returns a {@link MRDDataSource} from the specified parameters.
   * @param service the service name
   * @param user the username
   * @param pwd the password
   * @return a {@link MRDDataSource} from the specified parameters
   * @throws ReflectionException if connection could not be opened
   * @throws MidsvcsException if the midsvcs server is not available
   * @throws DataSourceConnectionException if cannot establish data source connection
   * @throws MissingPropertyException if data is missing in property file
   * @throws PoolOverflowException if data source limit has been reached
   */
  public static MRDDataSource getMRDDataSource(String service, String user,
                                               String pwd) throws
      ReflectionException, MidsvcsException, DataSourceConnectionException,
      MissingPropertyException, PoolOverflowException {

    // If the service, user and password are the defaults, then
    // just call getMRDDataSource()

    String default_service = MEMEToolkit.getProperty(
        ServerConstants.MRD_SERVICE);
    String default_user = MEMEToolkit.getProperty(ServerConstants.MRD_USER);
    String default_pwd = MEMEToolkit.getProperty(ServerConstants.MRD_PASSWORD);
    if (default_pwd == null) {
      default_pwd = MIDServices.getDataSourcePassword(default_user);
    }
    
    if ( (service == null || service.equals("") ||
          default_service.equals(service)) &&
        (user == null || default_user.equals(user)) &&
        (pwd == null || default_pwd.equals(pwd))) {
      return getMRDDataSource();
    } else {
      return newMRDDataSource(service, user, pwd);
    }
  }

  /**
   * Returns specified {@link MEMEDataSource} to the data source pool, or closes
   * it if it does not belong in the pool.
   * @param ds the {@link MEMEDataSource} to return
   * @throws BadValueException if failed due to invalid data value
   */
  public static void returnDataSource(MEMEDataSource ds) throws
      BadValueException {
    if (ds != null) {

      if (ds instanceof MIDDataSource) {
        if (mid_dsp.isReturnable( (MIDDataSource) ds)) {
          mid_dsp.returnDataSource( (MIDDataSource) ds);
        } else {
          try {
            ds.close();
          } catch (Exception e) {
          // Do nothing
          }
        }
      } else if (ds instanceof MRDDataSource) {
        if (mrd_dsp.isReturnable( (MRDDataSource) ds)) {
          mrd_dsp.returnDataSource( (MRDDataSource) ds);
        } else {
          try {
            ds.close();
          } catch (Exception e) {
          // Do nothing
          }
        }
      }
    }
  }

  /**
   * Returns the version of LVG used by the server.
   * @return the version of LVG used by the server
   */
  public static String getLVGVersion() throws IOException, FileNotFoundException {
    File file = new File(getProperty(LVG_CONFIG_FILE));
    Properties p = new Properties();
    p.load(new FileInputStream(file));
    file = new File(new File(p.getProperty("LVG_DIR")),"lib");
    String[] names = file.list();
    for (int i = 0; i < names.length; i++){
      if (names[i].startsWith("lvg") && names[i].endsWith("dist.jar"))
        return names[i].substring(3,7);
    }
    throw new IOException("Could not find lvg<year>dist.jar file in "+
        p.getProperty("LVG_DIR") + " lib directory.");
  }
  
} // end class ServerToolkit
