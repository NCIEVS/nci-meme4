/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  ServerThread
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.Initializable;

/**
 * Generically represents an initializable server component
 * that will run in its own thread.
 *
 * @author MEME Group
 */
public interface ServerThread extends Initializable, Runnable {

  //
  // Initializable requires an initialize() method
  // Runnable requires a run() method
  // Here, we additionally require start() and stop()
  //

  /**
   * Starts the server thread.  This method will be
   * called by {@link MEMEApplicationServer#start()}.
   */
  public void start();

  /**
   * Stops the server thread.  This method will be
   * called by {@link MEMEApplicationServer#stop()}.
   */
  public void stop();

}
