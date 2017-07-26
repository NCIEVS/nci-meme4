/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  ClientAPI
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * This class is the generic superclass for all client API's..
 * Any <i>client side API</i> written should extend this class as it
 * manages the connection to the server. To correctly interact with
 * this class, subclasses should <i>always</i> call <code>super()</code>
 * in their constructors, and should initiate processing of their
 * requests by using {@link #getRequestHandler()}.  For example,
 * <pre>
 *  public class SomeClientAPI extends ClientAPI {
 *
 *    public SomeClientAPI() { <b>super();</b> }
 *
 *    public Object clientAPIMethod() {
 *      MEMEServiceRequest request = new MEMEServiceRequest();
 *      // set parameters of service request
 *      request.addParameter(new Parameter("name","value"));
 *
 *      // handle request (this connects to the server)
 *      <b>request = getRequestHandler().processRequest(request);</b>
 *
 *      // deal with results
 *      ...
 *    >
 *  }
 * </pre>
 * In order for this class to be used, three properties
 * must be set in the {@link MEMEToolkit}.  They are: <ul>
 * <li><code>meme.client.server.host</code> -
 *     the machine currently running the <i>MEMEApplicationServer</i></li>
 * <li><code>meme.client.server.port</code> -
 *     the port that the <i>MEME Application Server</i> is listening on</li>
 * <li><code>meme.client.protocol.class</code> -
 *     the class implementing {@link MEMERequestClient} which is used to
 *     communicate with the server</li>
 * </ul>
 * The best approach to ensure these properties are set is to use
 * the properties file <code>$MEME_HOME/bin/meme.prop</code>. This
 * file is a releasable component in MEME4 and should be properly
 * configured at all times.  To make use of this file, either
 * invoke java with the parameter
 * <code>-Dmeme.properties.file=$MEME_HOME/bin/meme.prop</code> or
 * use the <code>$MEME_HOME/bin/memerun.pl</code> wrapper script.
 *
 * @see ClientConstants
 * @author MEME Group
 */
public class ClientAPI {

  //
  // Private Fields
  //

  private String session_id = null;
  private long timeout = 0;

  /**
   * Handles connection to the server.
   */
  private MEMERequestClient request_handler = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ClientAPI} that will generate reports
   * from the default mid service (<code>editing-db</code>).
   * This constructor requires three properties to be set:
   * <code>meme.client.protocol.class</code>,
   * <code>meme.client.server.host</code>,
   * <code>meme.client.server.port</code>
   * @throws MEMEException if connection could not be opened.
   * or if the protocol handler cannot be instantiated.
   */
  public ClientAPI() throws MEMEException {

    try {
      ClientToolkit.initialize();
    } catch (InitializationException ie) {
      MEMEException me = new MEMEException(
          "Failed to initialize client API.", ie);
      throw me;
    }

    Object object = null;
    try {
      // Create protocol handler
      object = Class.forName(MEMEToolkit.getProperty(
          ClientConstants.PROTOCOL_HANDLER)).newInstance();
    } catch (Exception e) {
      ReflectionException re = new ReflectionException(
          "Failed to load or instantiate class.", null, e);
      throw re;
    }

    // Assign protocol handler
    request_handler = (MEMERequestClient) object;

    // Initialize the client
    MEMEToolkit.trace("Server host is :" +
                      MEMEToolkit.getProperty(ClientConstants.SERVER_HOST));
    MEMEToolkit.trace("Server port is :" +
                      MEMEToolkit.getProperty(ClientConstants.SERVER_PORT));
    request_handler.setHost(MEMEToolkit.getProperty(ClientConstants.SERVER_HOST));
    request_handler.setPort(Integer.valueOf(
        MEMEToolkit.getProperty(ClientConstants.SERVER_PORT)).intValue());
  }

  /**
   * Returns the {@link MEMERequestClient} that will be handling
   * all server requests.
   * @return the {@link MEMERequestClient} that will be handling
   * all server requests
   */
  public MEMERequestClient getRequestHandler() {
    return request_handler;
  }

  /**
   * Returns timeout.
   * @return timeout.
   */
  public long getTimeout() {
    return timeout;
  }

  /**
   * Sets timeout.
   * @param timeout the timeout
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  /**
   * Returns session id.
   * @return session id.
   */
  public String getSessionId() {
    return session_id;
  }

  /**
   * Sets session id.
   * @param session_id session id.
   */
  public void setSessionId(String session_id) {
    this.session_id = session_id;
  }

  /**
   * Initiates a new server session.
   * @throws MEMEException if failed to initiate session
   */
  public void initiateSession() throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default(
        "function", "initiate_session"));
    request.setMidService(null);
    request.setInitiateSession(true);
    request.setNoSession(false);
    request.setTerminateSession(false);

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Set the session id
    setSessionId(request.getSessionId());
  }

  /**
   * Terminates the server session.  This method should be called before
   * the class is disposed of.
   * @throws MEMEException if failed to terminate session
   */
  public void terminateSession() throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default(
        "function", "initiate_session"));
    request.setMidService(null);
    request.setTerminateSession(true);
    request.setSessionId(session_id);

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    session_id = null;
  }

  /**
   * Add a listener.
   * @param l the {@link ClientProgressListener} to add
   */
  public void addClientProgressListener(ClientProgressListener l) {
    getRequestHandler().addClientProgressListener(l);
  }

  /**
   * Remove a listener.
   * @param l the {@link ClientProgressListener} to remove
   */
  public void removeClientProgressListener(ClientProgressListener l) {
    getRequestHandler().removeClientProgressListener(l);
  }

  //
  // Private Methods
  //

  /**
   * Returns the {@link MEMEServiceRequest}.
   * @return the {@link MEMEServiceRequest}
   */
  protected MEMEServiceRequest getServiceRequest() {

    // Prepare request document
    MEMEServiceRequest request = new MEMEServiceRequest();
    request.setService("AdminService");
    request.setTimeout(timeout);

    return request;
  }

}
