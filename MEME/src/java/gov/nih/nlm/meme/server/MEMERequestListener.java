/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  MEMERequestListener
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.Initializable;

/**
 * Generically represents a socket server that listens for
 * application server requests.  Implementations of this interface will employ
 * a specific network protocol such as HTTP or HTTPS.
 *
 * @author MEME Group
 */

public interface MEMERequestListener extends Initializable, Runnable {

  /**
   * Starts the listener.
   */
  public void start();

  /**
   * Stops the listener. The application using it should wrap
   * it in a thread after constructing it and the call start() on that thread.
   */
  public void stop();

}
