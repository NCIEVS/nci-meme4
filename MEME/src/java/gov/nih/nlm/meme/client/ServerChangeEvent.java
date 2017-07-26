/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  ServerChangeEvent
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

/**
 * Used to indicate that the MEME server used by an application has changed.
 *
 * @author BAC, RBE
 *
 */

public class ServerChangeEvent {

  //
  // Fields
  //

  private String host = null;
  private int port = 0;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ServerChangeEvent} with
   * the specified host and port settings.
   * @param host the new host
   * @param port the new port
   */
  public ServerChangeEvent(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /**
   * Returns the host
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * Returns the port.
   * @return the port
   */
  public int getPort() {
    return port;
  }

}
