/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  MIDDataSourcePool
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.Initializable;
import gov.nih.nlm.meme.InitializationContext;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceConnectionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.MidsvcsException;
import gov.nih.nlm.meme.exception.MissingPropertyException;
import gov.nih.nlm.meme.exception.PoolOverflowException;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.sql.MIDDataSource;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represents a pool of database connections.  It minimize the
 * overhead associated with opening database connection.  To use this class
 * you must first set up all the necessary system properties
 *
 * The required properties are:
 * <ul><li>{@link gov.nih.nlm.meme.sql.DataSourceConstants#DP_OPTIMAL_SIZE}
 *     <li>{@link gov.nih.nlm.meme.sql.DataSourceConstants#DP_MAX_SIZE}
 *     <li>{@link gov.nih.nlm.meme.sql.DataSourceConstants#DP_AUTOEXTEND}
 * </ul>
 *
 * Then create a data source pool object and initialize it by passing
 * an instance of an {@link InitializationContext}.
 *
 * For example,<p>
 *
 * <pre>
 *   InitializationContext server = new MEMEApplicationServer();
 *   MIDDataSourcePool dsp = new MIDDataSourcePool();
 *   dsp.initialize(server);
 * </pre></p>
 *
 * @author MEME Group
 */

public class MIDDataSourcePool implements Initializable {

  //
  // Fields
  //

  private final double upper_threshold = .7;
  private final double lower_threshold = .3;
  private ArrayList active;
  private ArrayList inactive;
  private Date last_used;
  private int total_data_source_count;
  private int optimal_size;
  private int max_size;
  private long sample_size;
  private long last_sample;
  private double average_usage;
  private boolean auto_extend;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MIDDataSourcePool}.
   */
  public MIDDataSourcePool() {};

  //
  // Implementation of Initializable interface
  //

  /**
   * Initializes the pool.
   * @param context the {@link InitializationContext}
   * @throws InitializationException if initialization fails
   */
  public void initialize(InitializationContext context) throws
      InitializationException {

    try {

      String l_optimal_size = MEMEToolkit.getProperty(ServerConstants.
          DP_OPTIMAL_SIZE);
      String l_max_size = MEMEToolkit.getProperty(ServerConstants.DP_MAX_SIZE);
      String l_auto_extend = MEMEToolkit.getProperty(ServerConstants.
          DP_AUTOEXTEND);

      MEMEToolkit.logComment("  autoextend = " + l_auto_extend, true);
      MEMEToolkit.logComment("  max_size = " + l_max_size, true);
      MEMEToolkit.logComment("  optimal_size = " + l_optimal_size, true);

      // Validate properties
      if (l_optimal_size == null || l_optimal_size.equals("")) {
        throw new MissingPropertyException(
            "Failed to initialize data source pool.",
            ServerConstants.DP_OPTIMAL_SIZE);
      }

      if (l_max_size == null || l_max_size.equals("")) {
        throw new MissingPropertyException(
            "Failed to initialize data source pool.",
            ServerConstants.DP_MAX_SIZE);
      }

      if (l_auto_extend == null ||
          (!l_auto_extend.equals("true") && !l_auto_extend.equals("false"))) {
        throw new MissingPropertyException(
            "Failed to initialize data source pool.",
            ServerConstants.DP_AUTOEXTEND);
      }

      // Set the optimal size
      this.optimal_size = Integer.valueOf(l_optimal_size).intValue();

      // Set the maximum size
      this.max_size = Integer.valueOf(l_max_size).intValue();

      // Set the auto extend flag
      this.auto_extend = Boolean.valueOf(l_auto_extend).booleanValue();

      MEMEToolkit.trace("MIDDataSourcePool:initialize() - "
                        + optimal_size + "," + max_size + "," + auto_extend);

      if (max_size <= optimal_size) {
        BadValueException bve = new BadValueException(
            "Maximum size must be greater than the optimal size.");
        bve.setDetail(ServerConstants.DP_MAX_SIZE, Integer.toString(max_size));
        bve.setDetail(ServerConstants.DP_OPTIMAL_SIZE,
                      Integer.toString(optimal_size));
      }

      // Initialize active/inactive lists
      active = new ArrayList(max_size);
      inactive = new ArrayList(max_size);

      // Initialize statistics
      total_data_source_count = optimal_size;
      sample_size = 0;
      average_usage = 0.0;
      last_used = new Date();

      // Create initial DataSource objects
      // Let any exceptions percolate up
      for (int i = 0; i < optimal_size; i++) {
        MEMEToolkit.trace(
            "MIDDataSourcePool:initialize() - Add new mid data source");
        inactive.add(ServerToolkit.newMIDDataSource());
      }

      //context.getServer().setMIDDataSourcePool(this);
      if (context != null) {
        context.addHook(this);

      }
    } catch (Exception e) {
      InitializationException ie = new InitializationException(
          "Failed to initialize data source pool.", e);
      throw ie;
    }

  }

  //
  // Methods
  //

  /**
   * Moves a data source from inactive list to active list.
   * @return the {@link MIDDataSource}
   * @throws PoolOverflowException if data source limit has been reached
   * @throws ReflectionException if connection could not be opened
   * @throws MidsvcsException if the midsvcs server is not available
   * @throws DataSourceConnectionException if cannot establish data source connection
   * @throws MissingPropertyException if data is missing in property file
   */
  public synchronized MIDDataSource getDataSource() throws
      PoolOverflowException, ReflectionException, MidsvcsException,
      DataSourceConnectionException, MissingPropertyException {

    MEMEToolkit.trace("MIDDataSourcePool::getDataSource()");

    // refresh usage statistics
    updateUsage(active.size(), inactive.size());

    MIDDataSource mds = null;
    if (inactive.size() > 0) {
      // remove data source from inactive list
      mds = (MIDDataSource) inactive.remove(inactive.size() - 1);

      if (!mds.isConnected()) {
        mds = ServerToolkit.getMIDDataSource();

      }
    } else {
      if ( (total_data_source_count >= max_size) || !auto_extend) {
        throw new PoolOverflowException("Data source pool limit reached.");
      }
      // inactive list is empty, create a new data source
      mds = ServerToolkit.newMIDDataSource();

      // increment total data source count
      total_data_source_count++;
    }

    // add data source to active list
    active.add(mds);

    return mds;
  }

  /**
   * Moves a data source from active list to inactive list.
   * @param mds the {@link MIDDataSource}
   * @throws BadValueException if failed due to invalid data value
   */
  public synchronized void returnDataSource(MIDDataSource mds) throws
      BadValueException {

    MEMEToolkit.trace("MIDDataSourcePool::returnDataSource()");

    // refresh usage statistics
    updateUsage(active.size(), inactive.size());

    int active_size = active.size();

    if (active.size() > 0) {
      // Get index.
      int i = active.indexOf(mds);
      // remove data source from active list
      mds = (MIDDataSource) active.remove(i);
    }
    if (active.size() >= active_size) {
      throw new BadValueException(
          "Failed to return data source to active list.");
    }

    // add data source to inactive list
    inactive.add(mds);

    try {
      mds.commit();
    } catch (Exception e) {
      throw new BadValueException("Failed to commit returned data source.");
    }

  }

  /**
   * Optimizes the availability of data source.
   * @throws ReflectionException if connection could not be opened
   * @throws MidsvcsException if the midsvcs server is not available
   * @throws DataSourceConnectionException if cannot establish data source connection
   * @throws MissingPropertyException if data is missing in property file
   */
  public synchronized void optimize() throws ReflectionException,
      MidsvcsException,
      DataSourceConnectionException, MissingPropertyException {

    // refresh usage statistics
    updateUsage(active.size(), inactive.size());

    if (total_data_source_count < max_size && average_usage > upper_threshold) {
      MIDDataSource mds = null;
      mds = ServerToolkit.newMIDDataSource();

      // increment total data source count
      // add data source to inactive list
      inactive.add(mds);
      // increment total data source count
      total_data_source_count++;
    }

    if (total_data_source_count > optimal_size &&
        average_usage < lower_threshold) {
      // remove data source to inactive list
      removeDataSource();
    }

  }

  /**
   * Chooses a data source from the inactive list and refresh the caches.
   * @throws PoolOverflowException if data source limit has been reached
   * @throws ReflectionException if connection could not be opened
   * @throws MidsvcsException if the midsvcs server is not available
   * @throws DataSourceConnectionException if cannot establish data source connection
   * @throws MissingPropertyException if data is missing in property file
   * @throws DataSourceException if failed to load data source
   * @throws BadValueException if failed due to invalid data value
   * @throws ReflectionException if failed to load or instantiate class
   */
  public synchronized void refreshCaches() throws PoolOverflowException,
      ReflectionException, MidsvcsException,
      DataSourceConnectionException, MissingPropertyException,
      DataSourceException, BadValueException, ReflectionException {
    getDataSource().refreshCaches();
  }

  /**
   * Closes all open data source connections, re-opens based on
   * the default mid service.
   * @param mid_service the mid service
   * @throws InitializationException
   */
  public synchronized void reallocateDataSources(String mid_service) throws
      InitializationException {
    // Close all inactive data sources
    try {
      for (int i = 0; i < inactive.size(); i++) {
        ( (MIDDataSource) inactive.get(i)).close();
      }
      // re-initialize inactive list
      ServerToolkit.setProperty(ServerConstants.MID_SERVICE, mid_service);
      initialize(null);
      refreshCaches();
    } catch (InitializationException ie) {
      throw ie;
    } catch (Exception e) {
      MEMEToolkit.handleError(e);
    }
  }

  /**
   * Indicates whether or not a particular data source can be returned
   * to the pool, or if it should be closed instead.
   * @param ds the {@link MIDDataSource}
   * @return <code>true</code> if data source can be returned; <code>false</code> otherwise
   */
  public boolean isReturnable(MIDDataSource ds) {
    try {
      return ds.getServiceName().toLowerCase().equals(MEMEToolkit.getProperty(
          ServerConstants.MID_SERVICE).toLowerCase())
          &&
          ds.getUserName().toLowerCase().equals(MEMEToolkit.getProperty(
          ServerConstants.
          MID_USER).toLowerCase());
    } catch (DataSourceException dse) {
      return false;
    }
  }

  /**
   * Updates the average usage, sample size and last used.
   * @param active_count the active count
   * @param inactive_count the inactive count
   */
  private void updateUsage(int active_count, int inactive_count) {
    Date now = new Date();
    double usage = 0;

    // FORMULA:
    //
    //        B
    // A = ( --- * D ) + E * F
    //       B+C
    //     -------------------
    //             F+G
    //
    // WHERE:
    //
    double A = usage;
    double B = (double) active_count;
    double C = (double) inactive_count;
    double D = (double) now.getTime() - last_used.getTime(); // last_sample
    double E = average_usage;
    double F = (double) sample_size;

    //
    // IMPLEMENTATION:
    //
    //     A = (((B/(B+C) * D)) + (E * F)) / (F+D);

    //
    // Start Debug
    //

    /*
         MEMEToolkit.trace("");
         MEMEToolkit.trace("MIDDataSourcePool:updateUsage() - active_count (B)   : " + B);
         MEMEToolkit.trace("MIDDataSourcePool:updateUsage() - inactive_count (C) : " + C);
         MEMEToolkit.trace("MIDDataSourcePool:updateUsage() - last_sample (D)    : " + D);
         MEMEToolkit.trace("MIDDataSourcePool:updateUsage() - average_usage (E)  : " + E);
         MEMEToolkit.trace("MIDDataSourcePool:updateUsage() - sample_size (F)    : " + F);
         MEMEToolkit.trace("MIDDataSourcePool:updateUsage() - total_data_source_count : " + total_data_source_count);
     */
    //String s = "(((B/(B+C) * D)) + (E * F)) / (F+D)";
    //MEMEToolkit.trace("MIDDataSourcePool:updateUsage() - " + s);

    //s = "(((" + B + "/(" + B + "+" + C + ") * " + D + ")) + (" + E + " * " + F +
     //   ")) / (" + F + "+" + D + ")";
    //MEMEToolkit.trace("MIDDataSourcePool:updateUsage() - " + s);

    A = ( ( (B / (B + C) * D)) + (E * F)) / (F + D);

    //
    // End Debug
    //

    // Do the update
    average_usage = A; // results of average usage computation
    sample_size += (long) D; // value of last sample
    last_used = now; // this moment
    last_sample = (long) D;

  }

  /**
   * Removes the oldest data source from inactive list.
   * @return <code>true</code> if non-empty list; <code>false</code> otherwise
   */
  private synchronized boolean removeDataSource() {

    if (inactive.isEmpty()) {
      return false;
    }

    // remove data source from the list
    MIDDataSource mds = (MIDDataSource) inactive.remove(inactive.size() - 1);
    try {
      mds.close();
    } catch (Exception e) {
      // Do nothing
    }

    // decrement total data source count
    total_data_source_count--;

    return true;
  }

  /**
   * Returns the current data source pool statistics.
   * @return the current data source pool statistics
   */
  public PoolStatistics getStatistics() {
    updateUsage(active.size(), inactive.size());

    return new PoolStatistics(active.size(), inactive.size(),
                              last_sample, average_usage, sample_size);
  }

} // end of class
