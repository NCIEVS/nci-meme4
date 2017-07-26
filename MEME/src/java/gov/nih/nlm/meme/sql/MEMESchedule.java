/*****************************************************************************
 * Package: gov.nih.nlm.meme.sql
 * Object:  MEMESchedule
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.Initializable;
import gov.nih.nlm.meme.InitializationContext;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.InitializationException;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Used to spuport scheduling of MEME tasks.  Based on <code>meme_schedule</code> table.
 *
 * @author MEME Group
 */
public class MEMESchedule implements Runnable, Initializable {

  //
  // Constants
  //

  /**
   * Used to indicate that a schedule listener should introduce a delay
   * into its cycle.
   */
  public final static String DELAY = "DELAY";

  /**
   * Used to indicate that a schedule listener should not introduce a
   * delay into its cycle.
   */
  public final static String NODELAY = "NODELAY";

  /**
   * Used to indicate that a schedule listener should stop running and
   * standby for the schedule to report a change in the CPU mode.
   */
  public final static String STANDBY = "STANDBY";

  /**
   * Used to indicate that a schedule listener should shut down.
   */
  public final static String SHUTDOWN = "SHUTDOWN";

  /**
   * Used to indicate that a schedule listener should follow
   * directions given by the schedule instead of overriding the
   * schedule.
   */
  public final static String CALENDAR = "CALENDAR";

  //
  // Fields
  //

  private HashMap listeners = new HashMap();
  private HashMap cpu_mode_map = new HashMap();
  private boolean keep_going = true;
  private int delay = 60000;
  private MEMEDataSource data_source;

  //
  // Constructors
  //

  /**
   * This constructor is an empty constructor.
   */
  public MEMESchedule() {};

  //
  // Implementation of Intializable interface
  //

  /**
   * This implements {@link Initializable#initialize(InitializationContext)}.
   * @param context the {@link InitializationContext} representation of
   * initialization context.
   * @throws InitializationException if initialization fails.
   */
  public void initialize(InitializationContext context) throws
      InitializationException {
    try {
      String schedule_property =
          MEMEToolkit.getProperty(DataSourceConstants.MEME_SCHEDULE);
      if (schedule_property != null && !schedule_property.equals("")) {

        // We need a data source object to connect to.
        // Doing it directly would require access to .server package
        // which is theoretically out of scope here, so we will
        // rely on the initialization context to figure it out
        context.addHook(this);

        // If the data source is null, throw init exception
        if (data_source == null) {
          throw new Exception("Data source is null.");
        }

        String s_delay =
            MEMEToolkit.getProperty(DataSourceConstants.MEME_SCHEDULE_DELAY);
        try {
          delay = Integer.parseInt(s_delay);
        } catch (Exception e) {
          // default is one minute
          delay = 60000;
        }

        // Wrap a Thread around the schedule and start it
        Thread schedule_thread = new Thread(this);
        schedule_thread.start();

      }
      // Else, no schedule to initialize.
    } catch (Exception e) {
      InitializationException ie = new InitializationException(
          "Failed to initialize schedule.", e);
      throw ie;
    }
  }

  /**
   * Sets the data source used by the schedule.
   * This method should be called by the InitializationContext
   * when the initialize method calls <code>context.addHook</code>
   * @param ds the {@link MEMEDataSource}.
   */
  public void setDataSource(MEMEDataSource ds) {
    data_source = ds;
  }

  /**
   * Gets the current cpu mode of an application registered as
   * MEMEScheduleListener in this MEMESchedule, if the run thread is running.
   * Otherwise it returns the cpu mode of the time the application got
   * registered. Evidently this method should only be used when the thread is
   * running.
   * @param listener the {@link MEMEScheduleListener} representation of
   * schedule listener.
   * @return the {@link String} representation of the CPU mode
   * (DELAY,NODELAY,STANDBY,SHUTDOWN).
   */
  public synchronized String getCpuMode(MEMEScheduleListener listener) {
    return ( (String) cpu_mode_map.get(listener));
  }

  /**
   * Registers a MEMEScheduleListener in the MEMESchedule
   * @param listener the {@link MEMEScheduleListener} representation of
   * schedule listener.
   * @throws DataSourceException if failed to add meme schedule listener.
   */
  public synchronized void addMEMEScheduleListener(MEMEScheduleListener
      listener) throws DataSourceException {

    String cpu_mode = data_source.getCpuMode(listener.getApplicationName());
    listeners.put(listener, listener.getApplicationName());
    cpu_mode_map.put(listener, cpu_mode);
  }

  /**
   * Removes a MEMEScheduleListener from the listener list maintained by the
   * MEMESchedule.
   * @param listener the {@link MEMEScheduleListener} representation of
   * schedule listener.
   */
  public synchronized void removeMEMEScheduleListener(
      MEMEScheduleListener listener) {
    listeners.remove(listener);
    cpu_mode_map.remove(listener);
  }

  /**
       * Periodically, it looks up the CPU mode for objects registered as listeners.
   * If the mode has changed since the last lookup, it informs the application
   * in question.
   */
  public synchronized void run() {
    while (keep_going) {
      //MEMEToolkit.trace("MEMESchedule");
      try {

        try {
          this.wait(delay);
        } catch (InterruptedException ie) {
          // Do nothing.
        }

        // Iterate through listeners.
        Iterator iterator = listeners.keySet().iterator();
        while (iterator.hasNext()) {
          final MEMEScheduleListener listener = (MEMEScheduleListener) iterator.
              next();
          String application = listener.getApplicationName();
          final String new_cpu_mode = data_source.getCpuMode(application);

          // If CPU mode changed, fire off a thread to inform
          // the application
          if (!new_cpu_mode.equals( (String) cpu_mode_map.get(listener))) {
            MEMEToolkit.trace("MEMESchedule new cpu_mode: " + new_cpu_mode);
            cpu_mode_map.put(listener, new_cpu_mode);
            Thread t = new Thread(new Runnable() {
              public void run() {
                MEMEScheduleEvent event = new
                    MEMEScheduleEvent(MEMESchedule.this, new_cpu_mode, null);
                listener.cpuModeChanged(event);
              }
            });
            t.start();
          }
        }
      } catch (DataSourceException dse) {
        // Handle exception now, because run cannot throw exception.
        dse.setFatal(false);
        MEMEToolkit.handleError(dse);
      }
    }
  }

  /**
   * Stop the schedule from running
   */
  public void stop() {
    MEMEToolkit.trace("MEMESchedule:: stopped");
    keep_going = false;
  }

}
