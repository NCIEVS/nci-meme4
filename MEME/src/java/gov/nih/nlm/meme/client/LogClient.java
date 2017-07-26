/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  LogClient
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.exception.MEMEException;

/**
 * Officially, this class has been <b><i>deprecated</i></b> by
 * {@link AdminClient}.  Internally, this class actually uses
 * {@link AdminClient} instead of having its own implementations.
 *
 * This client API is used to request a copy of the
 * server log. See {@link ClientAPI} for information
 * on configuring properties required by this class.
 *
 * With the properties properly configured, acquiring the log
 * is as simple as instantiating <code>LogClient</code> and
 * calling <code>getLog</code>.  For example,
 *
 * <pre>
 *   // Instantiate client
 *   LogClient client = new LogClient();
 *
 *   // Retrieve full log
 *   String full_log = client.getLog();
 *
 *   // Retrieve first 10 lines of log
 *   client.setHead(10);
 *   String head_log = client.getLog();
 *
 *   // Retrieve last 10 lines of log
 *   client.setTail(10);
 *   String tail_log = client.getLog();
 * </pre>
 *
 * @see ClientAPI
 * @see AdminClient
 * @author MEME Group
 */

public class LogClient extends ClientAPI {

  //
  // Fields
  //
  private AdminClient client;

  //
  // Constructors
  //

  /**
   * This constructor calls the super class.
   * @throws MEMEException if the required properties are not set,
   * or if the protocol handler cannot be instantiated.
   */
  public LogClient() throws MEMEException {
    super();
    client = new AdminClient();
  }

  //
  // Methods
  //

  /**
   * Gets the log.
   * @return An object {@link String} containing the client log.
   * @throws MEMEException if failed to produce client log.
   */
  public String getLog() throws MEMEException {
    return client.getLog();
  }

  /**
   * Indicates to the client that future requests
   * should retrieve only the head of the log file.  Calling
   * this method overrides any previous {@link #setTail(int)} calls.
   * @param head An <code>int</code> representation of value of head.
   */
  public void setHead(int head) {
    client.setHead(head);
  }

  /**
   * Indicates to the client that future requests
   * should retrieve only the tail of the log file.  Calling
   * this method overrides any previous {@link #setHead(int)} calls.
   * @param tail An <code>int</code> representation of value of tail.
   */
  public void setTail(int tail) {
    client.setTail(tail);
  }

}
