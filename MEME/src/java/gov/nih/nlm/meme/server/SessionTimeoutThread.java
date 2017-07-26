/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  SessionTimeoutThread
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.InitializationContext;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.InitializationException;

/**
 * Manages timing out of sessions.
 *
 * @author MEME Group
 */
public class SessionTimeoutThread implements ServerThread {

  //
  // Fields
  //

  private MEMEApplicationServer server = null;
  private boolean keep_going = true;
  private Thread thread = null;
  private int count_down = 5;

  //
  // Implementation of Initializable interface
  //

  /**
   * Initialize component.
   * @param context the {@link InitializationContext}
   * @throws InitializationException if initialization failed
   */
  public void initialize(InitializationContext context) throws
      InitializationException {

    // get reference to server
    server = (MEMEApplicationServer) context;

    // add server hook
    context.addHook(this);

  }

  /**
   * Runs in the background to check every minute if there
   * are any non-running expired sessions, if so those sessions will be
   * terminated.
   */
  public void run() {

    while (keep_going) {

      // we don't want to kill this loop,
      // so catch all exceptions
      try {

        // sleep for 1 min
        Thread.sleep(60000);

        // Garbage collect everytime it wakes up
        System.gc();
        if (--count_down <= 0) {
          count_down = 5;
          MEMEToolkit.logComment("FREE MEMORY - " + Statistics.getFreeMemory() +
                                 " of " + Statistics.getTotalMemory(),
                                 true);
        }

        // Check sessions;
        SessionContext[] sessions = server.getSessions();
        for (int i = 0; i < sessions.length; i++) {
          if (!sessions[i].isRunning() && sessions[i].isExpired()) {
            server.terminateSession(sessions[i]);
          }
        }

      } catch (InterruptedException ie) {
        // Do nothing
      } catch (Exception e) {
        DeveloperException de = new DeveloperException(
            "Error in session timeout loop", this);
        de.setEnclosedException(e);
        de.setFatal(false);
        de.setInformUser(false);
        MEMEToolkit.handleError(de);
      }
    }
  }

  /**
   * Stops the thread.
   */
  public void stop() {
    keep_going = false;
    thread.interrupt();
  }

  /**
   * Starts the thread.
   */
  public void start() {
    // start
    thread = new Thread(this);
    thread.start();
  }

}
