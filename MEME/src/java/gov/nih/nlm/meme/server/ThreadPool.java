/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  ThreadPool
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.Initializable;
import gov.nih.nlm.meme.InitializationContext;
import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.MissingPropertyException;
import gov.nih.nlm.meme.exception.PoolOverflowException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Represents a pool of threads.
 *
 * To use this class you must create a thread pool object and
 * initialize passing an instance of a server context. For example,<p>
 *
 * <pre>
 *   ThreadPool tp = new ThreadPool();
 * </pre>
 *
 * @author MEME Group
 */

public class ThreadPool implements Initializable {

  //
  // Fields
  //

  private final double upper_threshold = .7;
  private final double lower_threshold = .3;
  private ArrayList active;
  private ArrayList inactive;
  private Date last_used;
  private int total_thread_count;
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
   * Instantiates {@link ThreadPool}.
   */
  public ThreadPool() {};

  //
  // Implementation of Initializable interface
  //

  /**
   * Initializes component.
   * @param context the {@link InitializationContext}
   * @throws InitializationException if initialization fails
   */
  public void initialize(InitializationContext context) throws
      InitializationException {

    try {

      String l_optimal_size = MEMEToolkit.getProperty(MEMEConstants.
          TP_OPTIMAL_SIZE);
      String l_max_size = MEMEToolkit.getProperty(MEMEConstants.TP_MAX_SIZE);
      String l_auto_extend = MEMEToolkit.getProperty(MEMEConstants.
          TP_AUTOEXTEND);

      MEMEToolkit.logComment("  autoextend = " + l_auto_extend, true);
      MEMEToolkit.logComment("  max_size = " + l_max_size, true);
      MEMEToolkit.logComment("  optimal_size = " + l_optimal_size, true);

      MEMEToolkit.trace("ThreadPool:initialize() - " +
                        l_optimal_size + " " +
                        l_max_size + " " +
                        l_auto_extend);
      // Validate properties
      if (l_optimal_size == null || l_optimal_size.equals("")) {
        throw new MissingPropertyException(
            "Failed to initialize thread pool.", MEMEConstants.TP_OPTIMAL_SIZE);
      }

      if (l_max_size == null || l_max_size.equals("")) {
        throw new MissingPropertyException(
            "Failed to initialize thread pool.", MEMEConstants.TP_MAX_SIZE);
      }

      if (l_auto_extend == null ||
          (!l_auto_extend.equals("true") && !l_auto_extend.equals("false"))) {
        throw new MissingPropertyException(
            "Failed to initialize thread pool.", MEMEConstants.TP_AUTOEXTEND);
      }

      // Get the optimal size property
      this.optimal_size = Integer.valueOf(l_optimal_size).intValue();

      // Get the maximum size property
      this.max_size = Integer.valueOf(l_max_size).intValue();

      // get the auto extend flag
      this.auto_extend = Boolean.valueOf(l_auto_extend).booleanValue();

      if (max_size <= optimal_size) {
        BadValueException bve = new BadValueException(
            "Maximum size must be greater than the optimal size.");
        bve.setDetail(MEMEConstants.TP_MAX_SIZE, Integer.toString(max_size));
        bve.setDetail(MEMEConstants.TP_OPTIMAL_SIZE,
                      Integer.toString(optimal_size));
        throw bve;
      }

      // Create active/inactive lists
      active = new ArrayList(max_size);
      inactive = new ArrayList(max_size);

      // Initialize statistics
      total_thread_count = optimal_size;
      sample_size = 0;
      average_usage = 0.0;
      last_used = new Date();

      // Create initial threads
      for (int i = 0; i < optimal_size; i++) {
        WorkThread wt = new WorkThread();
        inactive.add(wt);
      }

      // Send back to toolkit
      //context.getServer().setThreadPool(this);
      context.addHook(this);

    } catch (Exception e) {
      InitializationException ie = new InitializationException(
          "Failed to initialize thread pool.", e);
      throw ie;
    }
  }

  //
  // Methods
  //

  /**
   * Moves a thread from inactive list to active list.
   * @param runnable the {@link Runnable}
   * @return the {@link Thread}
   * @throws PoolOverflowException if data source limit has been reached
   */
  public synchronized Thread getThread(Runnable runnable) throws
      PoolOverflowException {

    MEMEToolkit.trace("ThreadPool:getThread() - start (" + inactive.size() +
                      " "
                      + active.size() + ")");

    // refresh usage statistics
    updateUsage(active.size(), inactive.size());

    WorkThread wt = null;

    if (inactive.size() > 0) {
      // remove thread from inactive list
      wt = (WorkThread) inactive.remove(inactive.size() - 1);
    } else {
      if ( (total_thread_count >= max_size) || !auto_extend) {
        throw new PoolOverflowException("Thread pool limit has been reached.");
      }
      // inactive list is empty, create a new work thread
      wt = new WorkThread();
      // increment total thread count
      total_thread_count++;
    }

    // add thread to active list
    active.add(wt);

    // pass the runnable to the work thread
    wt.setRunnable(runnable);

    return wt;
  }

  /**
   * Moves a thread from active list to inactive list.
   * @param thread the {@link Thread}
   * @throws BadValueException if failed due to invalid data value
   */
  public synchronized void returnThread(Thread thread) throws BadValueException {

    MEMEToolkit.trace("ThreadPool:returnThread() - start(" + inactive.size() +
                      "," + active.size() + ")");

    // refresh usage statistics
    updateUsage(active.size(), inactive.size());

    int active_size = active.size();

    WorkThread wt = (WorkThread) thread;
    if (wt.isRunning()) {
      throw new BadValueException(
          "Failed to return thread, thread is still running.", null);
    }

    if (active.size() > 0) {
      // get thread from active list
      int i = active.indexOf(thread);
      // remove thread from active list
      wt = (WorkThread) active.remove(i);
    }
    if (active.size() >= active_size) {
      throw new BadValueException("Failed to return thread to active list.",
                                  active);
    }

    // add thread to inactive list
    inactive.add(wt);

  }

  /**
   * Optimizes the availability of threads.
   */
  public synchronized void optimize() {

    // refresh usage statistics
    updateUsage(active.size(), inactive.size());

    if (total_thread_count < max_size && average_usage > upper_threshold) {
      WorkThread wt = new WorkThread();
      // add thread to inactive list
      inactive.add(wt);
      // increment total thread count
      total_thread_count++;
    }

    if (total_thread_count > optimal_size && average_usage < lower_threshold) {
      // remove the oldest thread to inactive list
      removeOldestThread();
    }

  }

  /**
   * Updates the average usage, sample size and last used.
   * @param active_count count of active threads
   * @param inactive_count count of inactive threads
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
         MEMEToolkit.trace("ThreadPool:updateUsage() - active_count (B)   : " + B);
         MEMEToolkit.trace("ThreadPool:updateUsage() - inactive_count (C) : " + C);
         MEMEToolkit.trace("ThreadPool:updateUsage() - last_sample (D)    : " + D);
         MEMEToolkit.trace("ThreadPool:updateUsage() - average_usage (E)  : " + E);
         MEMEToolkit.trace("ThreadPool:updateUsage() - sample_size (F)    : " + F);
         MEMEToolkit.trace("ThreadPool:updateUsage() - total_thread_count : " + total_thread_count);
     */
    //String s = "(((B/(B+C) * D)) + (E * F)) / (F+D)";
    //MEMEToolkit.trace("ThreadPool:updateUsage() - " + s);

    //s = "(((" + B + "/(" + B + "+" + C + ") * " + D + ")) + (" + E + " * " + F +
    //    ")) / (" + F + "+" + D + ")";
    //MEMEToolkit.trace("ThreadPool:updateUsage() - " + s);

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
   * Removes the oldest thread from inactive list.
   * @return <code>true</code> if non-empty list; <code>false</code> otherwise.
   */
  private boolean removeOldestThread() {

    if (inactive.isEmpty()) {
      return false;
    }

    // sort list
    Collections.sort(inactive);
    // remove the oldest thread from the list
    inactive.remove(inactive.size() - 1);
    // decrement total thread count
    total_thread_count--;

    return true;
  }

  /**
   * Returns the current thread {@link PoolStatistics}.
   * @return the current thread {@link PoolStatistics}
   */
  public PoolStatistics getStatistics() {
    updateUsage(active.size(), inactive.size());
    return new PoolStatistics(active.size(), inactive.size(),
                              last_sample, average_usage, sample_size);
  }

  //
  // Inner Classes
  //

  public class WorkThread extends Thread implements Comparable {

    //
    // Fields
    //

    private Runnable runnable;
    private Date last_used;
    private boolean currently_running;
    private boolean start_called = false;

    //
    // Constructors
    //

    /**
     * Instantiates a {@link ThreadPool.WorkThread}.
     */

    public WorkThread() {
      this.runnable = null;
      this.last_used = new Date();
      this.currently_running = false;
    }

    //
    // Methods
    //

    /**
     * Starts the thread.
     */
    public void start() {
      if (!start_called) {
        MEMEToolkit.trace("ThreadPool.WorkThread:start() - start thread");
        start_called = true;
        super.start();
      } else {
        synchronized (WorkThread.this) {
          MEMEToolkit.trace("ThreadPool.WorkThread:start() - wake thread");
          WorkThread.this.notifyAll();
        }
      }
    }

    /**
     * Sets the {@link Runnable}.
     * @param runnable the {@link Runnable}
     */
    public void setRunnable(Runnable runnable) {
      this.runnable = runnable;
      this.last_used = new Date();
    }

    /**
     * Indicates whether or not this thread is currently running.
     * @return <code>true</code> if so, <codE>false</code> otherwise
     */
    public boolean isRunning() {
      return this.currently_running;
    }

    /**
     * Returns the {@link Date} of last use.
     * @return the {@link Date} of last use
     */
    public Date getLastUsed() {
      return this.last_used;
    }

    //
    // Implementation of Comparable interface
    //

    /**
     * Comparison function.
     * @param object object to compare to
     * @return indicates relative sort order
     */
    public int compareTo(Object object) {
      return ( (WorkThread) object).getLastUsed().compareTo(last_used);
    }

    //
    // Implementation of Thread class
    //

    /**
     * Runs in an infinite loop, blocking each time the work has been
     * completed. The next call
     * to start will start it again by calling notify
     * on the object.
     */
    public void run() {
      while (true) {
        while (runnable == null) {
          synchronized (WorkThread.this) {
            try {
              MEMEToolkit.trace("ThreadPool.WorkThread:run() - thread wait");
              WorkThread.this.wait();
            } catch (InterruptedException e) {
              // Do nothing
            }
          }
        }
        ;

        this.currently_running = true;
        this.last_used = new Date();

        // Do not allow run ot throw an exception and not return the thread to the pool
        if (runnable != null) {
          try {
            runnable.run();
          } catch (Exception e) {
            // Run garbage collection in case we are running out of memory.
            System.gc();
          }
        }

        currently_running = false;
        // reset runnable
        runnable = null;

        try {
          returnThread(WorkThread.this);
        } catch (BadValueException bve) {
          // Run cannot throw exception, so handle error now.
          bve.setFatal(false);
          MEMEToolkit.handleError(bve);
        }
      }
    }

  } // end of inner class

} // end of class
