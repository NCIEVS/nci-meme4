/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  ServerToolkit
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.Initializable;
import gov.nih.nlm.meme.InitializationContext;
import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceConnectionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.MidsvcsException;
import gov.nih.nlm.meme.exception.MissingPropertyException;
import gov.nih.nlm.meme.exception.PoolOverflowException;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.sql.MEMEDataSourceFactory;
import gov.nih.nlm.meme.sql.MEMESchedule;
import gov.nih.nlm.meme.sql.MIDDataSource;

import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Utility class for the MEME Application Server.
 * It contains methods for initializing applications.
 *
 * @author MEME Group
 */
public class ServerToolkit extends MEMEToolkit {

  //
  // Fields
  //

  protected static MIDDataSourcePool mid_dsp = null;
  protected static ThreadPool thread_pool = null;
  protected static MEMESchedule schedule;
  protected static MEMEDataSourceFactory factory = new MEMEDataSourceFactory();
  protected static boolean kill_with_wait = true;

  /**
   * Initialize the toolkit.
   * Initialization is primarily done by calling
   * {@link gov.nih.nlm.meme.MEMEToolkit#initialize(String[], String[])}
   * with the MAS application specific properties.  Additionally, it should
   * create a MEMESchedule object if needed.
   * @param context the {@link InitializationContext}
   * @throws InitializationException if initialization failed.
   */
  public static void initialize(InitializationContext context) throws
      InitializationException {

    // Initialize the MEMEToolkit first
    MEMEToolkit.initialize(ServerConstants.ALLOWABLE_PROPERTIES,
                           ServerConstants.REQUIRED_PROPERTIES);

    String nclass = null;
    try {
      // Tokenize the bootstrap property to separate on ,
      StringTokenizer st =
          new StringTokenizer(getProperty(ServerConstants.MEME_BOOTSTRAP), ",");
      while (st.hasMoreTokens()) {
        nclass = st.nextToken();
        // Assume initializable classes have an no-argument constructor
        // and instantiate it, then cast it to Initializable

        MEMEToolkit.logComment("INITIALIZE COMPONENT - " + nclass, true);
        Initializable init = (Initializable) Class.forName(nclass).newInstance();

        // Call the initialize method passing it the context.
        // initialize methods should throw ANY exceptions they encounter
        // back to this method where they will be handled.
        init.initialize(context);
        trace("ServerToolkit:initialize() - " + nclass +
              " successfully initialized.");

      }
    } catch (InitializationException ie) {
      handleError(ie);
    } catch (Exception e) {
      ReflectionException re = new ReflectionException(
          "Failed to load or instantiate class.", null, e);
      re.setDetail("class", nclass);
      handleError(re);
    }
  }

  //
  // Accessor methods
  //

  /**
   * Sets the server toolkit's thread pool reference.
   * This is only called by
       * {@link gov.nih.nlm.meme.server.MEMEApplicationServer#addHook(Initializable)}
   * @param kww indicates whether to kill with wait
   */
  public static void setKillWithWait(boolean kww) {
    kill_with_wait = kww;
  }

  /**
   * Sets the server toolkit's thread pool reference.
   * This is only called by
       * {@link gov.nih.nlm.meme.server.MEMEApplicationServer#addHook(Initializable)}
   * @param tp the {@link ThreadPool}
   */
  public static void setThreadPool(ThreadPool tp) {
    thread_pool = tp;
  }

  /**
   * Sets the server toolkit's data source pool reference.
   * This is only called by
       * {@link gov.nih.nlm.meme.server.MEMEApplicationServer#addHook(Initializable)}
   * @param dp the {@link MIDDataSourcePool}
   */
  public static void setDataSourcePool(MIDDataSourcePool dp) {
    mid_dsp = dp;
  }

  /**
   * Sets the server toolkit's thread pool reference.
   * This is only called by
       * {@link gov.nih.nlm.meme.server.MEMEApplicationServer#addHook(Initializable)}
   * @param sch the {@link MEMESchedule}
   */
  public static void setMEMESchedule(MEMESchedule sch) {
    schedule = sch;
  }

  /**
   * Get the MEMESchedule used by the toolkit.
   * @return the {@link MEMESchedule}
   */
  public static MEMESchedule getMEMESchedule() {
    return schedule;
  }

  /**
   * Returns a Thread from the thread pool.  If the ThreadPool
   * is full, this method blocks until a thread is available.
   * @param runnable the {@link Runnable}
   * @return the {@link Thread}
   * @throws PoolOverflowException if data source limit has been reached
   */
  public static Thread getThread(Runnable runnable) throws
      PoolOverflowException {
    MEMEToolkit.trace("ServerToolkit:getThread()");
    return thread_pool.getThread(runnable);
  }

  /**
   * Returns thread pool.
   * @return the {@link ThreadPool}
   */
  public static ThreadPool getThreadPool() {
    MEMEToolkit.trace("ServerToolkit:getThreadPool()");
    return thread_pool;
  }

  /**
   * Returns data source pool.
   * @return the {@link MIDDataSourcePool}
   */
  public static MIDDataSourcePool getDataSourcePool() {
    MEMEToolkit.trace("ServerToolkit:getDataSourcePool()");
    return mid_dsp;
  }

  /**
   * Currently unimplimented.
       * This method will block until the active count in the threadpool returns zero.
   */
  public static void waitUntilThreadPoolInactive() {
    while (kill_with_wait) {
      PoolStatistics ps = thread_pool.getStatistics();
      logComment("Waiting for active threads to finish (" + ps.getActiveCount() +
                 ")", true);
      if (ps.getActiveCount() == 1) {
        return;
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ie) {
        // Do nothing
      }
    }
  }

  /**
   * Creates and returns a new MIDDataSource using the
   * service name from the ServerConstants.MID_SERVICE property.
   *
   * @return the {@link MIDDataSource}
   * @throws ReflectionException if connection could not be opened
   * @throws MidsvcsException if the midsvcs server is not available
   * @throws DataSourceConnectionException if cannot establish data source connection
   * @throws MissingPropertyException if data is missing in property file
   */
  public static MIDDataSource newMIDDataSource() throws ReflectionException,
      MidsvcsException,
      DataSourceConnectionException, MissingPropertyException {
    return newMIDDataSource(null, null, null);
  };

  /**
   * Creates and returns a new MIDDataSource .
   * If service is "", use MID_SERVICE property.
   *
   * This method belongs in MIDDataSourcePool.
   *
   * @param service the service name (e.g. editing-db).
   * @param user the user
   * @param password the password
   * @return the {@link MIDDataSource}
   * @throws ReflectionException if connection could not be opened
   * @throws MidsvcsException if the midsvcs server is not available
   * @throws DataSourceConnectionException if cannot establish data source connection
   * @throws MissingPropertyException if data is missing in property file
   * @see gov.nih.nlm.meme.MIDServices
   */
  public static MIDDataSource newMIDDataSource(String service, String user,
                                               String password) throws
      ReflectionException, MidsvcsException,
      DataSourceConnectionException, MissingPropertyException {

    MIDDataSource mid_data_source = null;

    // If service is empty, look it up in property
    if (service == null || service.equals("")) {
      service = getProperty(ServerConstants.MID_SERVICE);

      // If username is empty, look it up in property
    }
    if (user == null || user.equals("")) {
      user = getProperty(ServerConstants.MID_USER);

      // If password is empty, look it up in property
    }
    if (password == null || password.equals("")) {
      password = getProperty(ServerConstants.MID_PASSWORD);
      if (password == null || password.equals(""))
        password = MIDServices.getDataSourcePassword(user,service);
    }

    String connection = MEMEToolkit.getProperty(ServerConstants.MID_CONNECTION);
    String driver_class = MEMEToolkit.getProperty(ServerConstants.
                                                  MID_DRIVER_CLASS);

    // If any parameters are null, then throw an exception
    if (service == null) {
      throw new MissingPropertyException(
          "Failed to create MIDDataSource with a null service name.",
          ServerConstants.MID_SERVICE);
    }
    if (user == null) {
      throw new MissingPropertyException(
          "Failed to create MIDDataSource with a null user name.",
          ServerConstants.MID_USER);
    }
    if (password == null) {
      throw new MissingPropertyException(
          "Failed to create MIDDataSource with a null password.",
          ServerConstants.MID_PASSWORD);
    }
    if (connection == null) {
      throw new MissingPropertyException(
          "Failed to create MIDDataSource with a null connection class.",
          ServerConstants.MID_CONNECTION);
    }
    if (driver_class == null) {
      throw new MissingPropertyException(
          "Failed to create MIDDataSource with a null JDBC driver class.",
          ServerConstants.MID_DRIVER_CLASS);
    }

    // Initialize the MIDConnection
    Properties mid_props = new Properties();
    mid_props.setProperty("connection", connection);
    mid_props.setProperty("driver_class", driver_class);
    mid_props.setProperty("password", password);
    mid_props.setProperty("user", user);
    mid_props.setProperty("service", service);

    mid_data_source =
        (MIDDataSource) factory.newMEMEDataSource(mid_props);

    // mid_data_source.setOperationsQueueMode (true);

    // logComment("MIDDataSource initialized");

    return mid_data_source;

  }

  /**
   * This method returns a new MIDDataSource.
   * @return the {@link MIDDataSource}
   * @throws ReflectionException if connection could not be opened.
   * @throws MidsvcsException if the midsvcs server is not available.
   * @throws DataSourceConnectionException if cannot establish data source connection.
   * @throws MissingPropertyException if data is missing in property file.
   * @throws PoolOverflowException if data source limit has been reached.
   */
  public static MIDDataSource getMIDDataSource() throws ReflectionException,
      MidsvcsException, DataSourceConnectionException,
      MissingPropertyException, PoolOverflowException {
    return mid_dsp.getDataSource();
  }

  /**
   * This method returns a new MIDDataSource.
   * @param service the service name
   * @param user the user
   * @param pwd the password
   * @return the {@link MIDDataSource}
   * @throws ReflectionException if connection could not be opened
   * @throws MidsvcsException if the midsvcs server is not available
   * @throws DataSourceConnectionException if cannot establish data source connection
   * @throws MissingPropertyException if data is missing in property file
   * @throws PoolOverflowException if data source limit has been reached
   */
  public static MIDDataSource getMIDDataSource(String service, String user,
                                               String pwd) throws
      ReflectionException, MidsvcsException, DataSourceConnectionException,
      MissingPropertyException, PoolOverflowException {

    // If the service, user and password are the defaults, then
    // just call getMIDDataSource()

    String default_service = MEMEToolkit.getProperty(ServerConstants.
        MID_SERVICE);
    String default_tns = MIDServices.getService(default_service);
    String default_user = MEMEToolkit.getProperty(ServerConstants.MID_USER);

    MIDDataSource mds = null;

    if ( (service == null ||
          service.equals("") ||
          default_service.equals(service) ||
          service.equals(default_tns)) &&
        (user == null ||
         default_user.toLowerCase().equals(user.toLowerCase()))) {
      mds = getMIDDataSource();
    } else {
      mds = newMIDDataSource(service, user, pwd);
    }

    boolean validate_atomic_actions =
        Boolean.valueOf(MEMEToolkit.getProperty(MEMEConstants.
                                                VALIDATE_ATOMIC_ACTIONS,
                                                "false")).booleanValue();

    if (validate_atomic_actions) {
      try {
        mds.setAtomicActionValidationEnabled();
      } catch (DataSourceException dse) {
        throw new DataSourceConnectionException(
            "Failed to set atomic action validation.", dse);
      }
    } else {
      try {
        mds.setAtomicActionValidationDisabled();
      } catch (DataSourceException dse) {
        throw new DataSourceConnectionException(
            "Failed to set atomic action validation.", dse);
      }
    }

    boolean validate_molecular_actions =
        Boolean.valueOf(MEMEToolkit.getProperty(MEMEConstants.
                                                VALIDATE_MOLECULAR_ACTIONS,
                                                "false")).booleanValue();

    if (validate_molecular_actions) {
      try {
        mds.setMolecularActionValidationEnabled();
      } catch (DataSourceException dse) {
        throw new DataSourceConnectionException(
            "Failed to set molecular action validation.", dse);
      }
    } else {
      try {
        mds.setMolecularActionValidationDisabled();
      } catch (DataSourceException dse) {
        throw new DataSourceConnectionException(
            "Failed to set molecular action validation.", dse);
      }
    }

    return mds;
  }

  /**
   * Takes a data source object and either returns it to the
   * object {@link MIDDataSourcePool} or closes it if it cannot be returned.
   * This method is used by applications that want to discard
   * data source objects that are no longer being used
   * @param ds the {@link MEMEDataSource}
   * @throws BadValueException if failed due to invalid data value
   */
  public static void returnDataSource(MEMEDataSource ds) throws
      BadValueException {
    if (ds != null) {
      if (mid_dsp.isReturnable( (MIDDataSource) ds)) {
        mid_dsp.returnDataSource( (MIDDataSource) ds);
      } else {
        try {
          ds.close();
        } catch (Exception e) {
          // Do nothing
        }
      }
    }
  }

} // end class ServerToolkit
