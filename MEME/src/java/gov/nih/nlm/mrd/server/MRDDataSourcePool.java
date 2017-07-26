/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.server
 * Object:  MRDDataSourcePool
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.server;

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
import gov.nih.nlm.meme.server.PoolStatistics;
import gov.nih.nlm.mrd.sql.MRDDataSource;

import java.util.ArrayList;
import java.util.Date;

/**
 * Pool of database connections.  It minimize the
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
 *   InitializationContext server = new MRDApplicationServer();
 *   MRDDataSourcePool dsp = new MRDDataSourcePool();
 *   dsp.initialize(server);
 * </pre></p>
 *
 * @author  MEME Group
 */
public class MRDDataSourcePool implements Initializable {

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
   * Instantiates a {@link MRDDataSourcePool}.
   */
  public MRDDataSourcePool() {};

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

      MEMEToolkit.trace("MRDDataSourcePool:initialize() - "
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
            "MRDDataSourcePool:initialize() - Add new mid data source");
        inactive.add(ServerToolkit.newMRDDataSource());
      }

      //context.getServer().setMRDDataSourcePool(this);
      context.addHook(this);

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
   * Returns an {@link MRDDataSource} and moves it from the inactive list to
   * the active list.
   * @return an {@link MRDDataSource}
   * @throws PoolOverflowException if data source limit has been reached
   * @throws ReflectionException if connection could not be opened
   * @throws MidsvcsException if the midsvcs server is not available
   * @throws DataSourceConnectionException if cannot establish data source connection
   * @throws MissingPropertyException if data is missing in property file
   */
  public synchronized MRDDataSource getDataSource() throws
      PoolOverflowException, ReflectionException, MidsvcsException,
      DataSourceConnectionException, MissingPropertyException {

    MEMEToolkit.trace("MRDDataSourcePool::getDataSource()");

    // refresh usage statistics
    updateUsage(active.size(), inactive.size());

    MRDDataSource mds = null;
    if (inactive.size() > 0) {
      // remove data source from inactive list
      mds = (MRDDataSource) inactive.remove(inactive.size() - 1);

      if (!mds.isConnected()) {
        mds = ServerToolkit.getMRDDataSource();

      }
    } else {
      if ( (total_data_source_count >= max_size) || !auto_extend) {
        throw new PoolOverflowException("Data source pool limit reached.");
      }
      // inactive list is empty, create a new data source
      mds = ServerToolkit.newMRDDataSource();

      // increment total data source count
      total_data_source_count++;
    }

    // add data source to active list
    active.add(mds);

    return mds;
  }

  /**
   * Returns specified {@link MRDDataSource} to the pool and puts it
   * back on the inactive list
   * @param mds the {@link MRDDataSource} to return
   * @throws BadValueException if failed due to invalid data value
   */
  public synchronized void returnDataSource(MRDDataSource mds) throws
      BadValueException {

    MEMEToolkit.trace("MRDDataSourcePool::returnDataSource()");

    // refresh usage statistics
    updateUsage(active.size(), inactive.size());

    int active_size = active.size();

    if (active.size() > 0) {
      // Get index.
      int i = active.indexOf(mds);
      // remove data source from active list
      mds = (MRDDataSource) active.remove(i);
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
   * Optimizes the data source pool size based on real-time usage.
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
      MRDDataSource mds = null;
      mds = ServerToolkit.newMRDDataSource();

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
   * Refresh the cache for the data sources.
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
   * Indicates whether or not a particular data source can be returned
   * to the pool, or if it should be closed instead.
     @param ds the {@link MRDDataSource}
   * @return <code>true</code> if data source can be returned; <code>false</code> otherwise
   */
  public boolean isReturnable(MRDDataSource ds) {
    try {
      return ds.getServiceName().equals(MEMEToolkit.getProperty(ServerConstants.
          MRD_SERVICE))
          &&
          ds.getUserName().toLowerCase().equals(MEMEToolkit.getProperty(ServerConstants.
          MRD_USER).toLowerCase());
    } catch (DataSourceException dse) {
      return false;
    }
  }

  /**
   * Updates the average usage, sample size and last used.
   * @param active_count the count of active data sources
   * @param inactive_count the count of inactive data sources
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
         MEMEToolkit.trace("MRDDataSourcePool:updateUsage() - active_count (B)   : " + B);
         MEMEToolkit.trace("MRDDataSourcePool:updateUsage() - inactive_count (C) : " + C);
         MEMEToolkit.trace("MRDDataSourcePool:updateUsage() - last_sample (D)    : " + D);
         MEMEToolkit.trace("MRDDataSourcePool:updateUsage() - average_usage (E)  : " + E);
         MEMEToolkit.trace("MRDDataSourcePool:updateUsage() - sample_size (F)    : " + F);
         MEMEToolkit.trace("MRDDataSourcePool:updateUsage() - total_data_source_count : " + total_data_source_count);
     */
    //String s = "(((B/(B+C) * D)) + (E * F)) / (F+D)";
    //MEMEToolkit.trace("MRDDataSourcePool:updateUsage() - " + s);

    //s = "(((" + B + "/(" + B + "+" + C + ") * " + D + ")) + (" + E + " * " + F +
     //   ")) / (" + F + "+" + D + ")";
    //MEMEToolkit.trace("MRDDataSourcePool:updateUsage() - " + s);

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
    MRDDataSource mds = (MRDDataSource) inactive.remove(inactive.size() - 1);
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
   * Returns the current data source {@link PoolStatistics}.
   * @return the current data source {@link PoolStatistics}
   */
  public PoolStatistics getStatistics() {
    updateUsage(active.size(), inactive.size());

    return new PoolStatistics(active.size(), inactive.size(),
                              last_sample, average_usage, sample_size);
  }

} // end of class
