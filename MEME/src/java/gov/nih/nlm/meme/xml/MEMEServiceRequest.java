/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.xml
 * Object:  MEMEServiceRequest
 * 
 * 03/09/2006 RBE (1-AMPX1): Handling server error messages
 *
 *****************************************************************************/
package gov.nih.nlm.meme.xml;

import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.Authenticator;
import gov.nih.nlm.meme.common.ComponentMetaData;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Warning;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Object form of the XML "MEME service request". The DTD is available
 * <a href="/MEME/Data/MASRequest.dtd">
 * here</a>.
 *
 * @see ComponentMetaData
 * @see Authentication
 * @see Authenticator
 * @see Parameter
 * @see Warning
 *
 * @author MEME Group
 */

public class MEMEServiceRequest {

  //
  // Constants
  //

  /**
   * This class defines some constants.  The following constants used to
   * indicate the various priorities for idle requests.
   */
  public final static byte NO_IDLE = 0;
  public final static byte MIN_IDLE_PRIORITY = 1;
  public final static byte NORM_IDLE_PRIORITY = 100;
  public final static short MAX_IDLE_PRIORITY = 255;
  public final static long NO_TIMEOUT = 0;

  // Connection information

  /**
   * The following fields pertains to the ConnectionInformation section of the
   * XML document.
   */

  /**
   * session_id.  It is assigned by the server upon an initiation request and
   * the client re-uses it in subsequent requests to guarantee that the server
   * re-uses the resources it allocated.
   */
  private String session_id = null;

  /**
   * initiate_session.  Is a flag indicating whether or not a request is
   * attempting to initiate a session.  The default is false.
   */
  private boolean initiate_session = false;

  /**
   * nosession.  Is a flag indicating whether or not a request does not want
   * to initiate any session.  This is used for one-time requests that don't
   * need session overhead.
   */
  private boolean nosession = false;

  /**
   * terminate_session.  Is a flag indicating a session that is finished and
   * should be cleaned up by the server.  A value of true must always be
   * accompanied by an id parameter indicating which session to clean up.
   */
  private boolean terminate_session = false;

  /**
   * mid_service.  The name of the database to be connected.
   */
  private String mid_service = null;

  /**
   * timeout.  The server will have a process that monitors the time since
   * resources it allocated were last used.  If the time since the last use
   * exceeds the timeout for a session, then the resources are cleaned up and
   * the session is closed. Subsequent requests using the session id will not
   * be allowed, the client must initiate a new session.
   */
  private long timeout = 0; // milliseconds

  /**
   * idle.  A client can request that a job be performed during Server idle
   * time.  The default is NO_IDLE which means that the request should be
   * performed immediately.  Any other value indicates a priority and the
   * server processes the idle requests in priority order.
   */
  private byte idle = NO_IDLE;

  /**
   * authentication.  This is an instance of Authentication and represents the
   * client authentication mechanism.
   */
  private Authentication authentication = null;

  /**
   * Writer for the socket
   */
  private Writer writer = null;

  // Software versions

  /**
   * The following fields pertains to the SoftwareVersions section of the XML
   * document.
   */

  /**
   * current_software.  When initiating a session, a client can provide
   * information about current software components.  This hashmap maps
   * component names (most likely java packages in our case) to
   * ComponentMetaData objects.  The server takes this data and checks against
   * its own reckoning of what the current software versions are and returns a
   * message to the client (in the ClientResponse section of the XML document)
   * indicating whether or not upgraded software must be installed before the
   * request can be processed.
   */
  private HashMap current_software;

  // Service parameters

  /**
   * The following fields pertains to the ServiceParameters section of the XML
   * document.
   */

  /**
   * service.  This field contains the name of the service being requested.
   * For example "EditingInterface" or "ReportsGenerator".  These names are
   * typically mapped by the server to instances of MEMEApplicationService.
   */
  private String service;

  /**
   * parameters.  This HashMap maps parameter names to Parameter objects.
   * These parameters are used by the application service that ultimately
   * processes the request.
   */
  private HashMap parameters; // String -> value

  // Client response

  /**
   * The following fields pertains to the ClientResponse section of the XML
   * document.
   */

  /**
   * required_software.  This Hashmap works like current_software.  It maps
   * component names to ComponentMetaData objects which indicate what
   * components must be upgraded and provides information about where to
   * download the components from.
   */
  private HashMap required_software;

  /**
   * exceptions.  If the server returned any exceptions this list will contain
   * the Exception objects encountered.
   */
  private ArrayList exceptions;

  /**
   * warnings.  If the server returned any warnings, this list will contain
   * the Warnings encountered.
   */
  private ArrayList warnings;

  /**
   * return_values.  This field works like parameters.  It maps names to
   * Parameter objects which represent the return values from the server.
   * Just as a client can supply multiple parameters to a service, a service
   * can return multiple values to a client.
   */
  private HashMap return_values;

  /**
   * request_id. It is used to identify the request.
   */
  private int request_id = 0;

  //
  // Constructors
  //

  /**
   * Default constructor
   */
  public MEMEServiceRequest() {
    current_software = new HashMap();
    parameters = new HashMap();
    required_software = new HashMap();
    exceptions = new ArrayList();
    warnings = new ArrayList();
    return_values = new HashMap();
  }

  //
  // Methods
  //

  /**
   * Returns the session id.
   * @return An object {@link String} representation of session id.
   */
  public String getSessionId() {
    return session_id;
  }

  /**
   * Sets the session id.
   * @param session_id An object {@link String} representation of session id.
   */
  public void setSessionId(String session_id) {
    this.session_id = session_id;
  }

  /**
   * Indicates whether or not the request's session should be initiated.
   * @return A <code>boolean</code> representation of initiate session.
   */
  public boolean initiateSession() {
    return initiate_session;
  }

  /**
   * Sets the initiate session.
   * @param initiate_session A <code>boolean</code> representation of initiate session.
   */
  public void setInitiateSession(boolean initiate_session) {
    this.initiate_session = initiate_session;
  }

  /**
   * Indicates whether or not the request's have a session.
   * @return A <code>boolean</code> representation of nosession.
   */
  public boolean noSession() {
    return nosession;
  }

  /**
   * Sets the nosession.
   * @param nosession A <code>boolean</code> representation of nosession.
   */
  public void setNoSession(boolean nosession) {
    this.nosession = nosession;
  }

  /**
   * Indicates whether or not the request's session should be terminated.
   * @return A <code>boolean</code> representation of terminate session.
   */
  public boolean terminateSession() {
    return terminate_session;
  }

  /**
   * Sets the terminate session.
   * @param terminate_session A <code>boolean</code> representation of terminate session.
   */
  public void setTerminateSession(boolean terminate_session) {
    this.terminate_session = terminate_session;
  }

  /**
   * Sets the mid service.
   * @param mid_service An object {@link String} representation of mid service.
   */
  public void setMidService(String mid_service) {
    this.mid_service = mid_service;
  }

  /**
   * Returns the mid service.
   * @return An object {@link String} representation of mid service.
   */
  public String getMidService() {
    return mid_service;
  }

  /**
   * Returns the timeout.
   * @return A <code>long</code> representation of timeout.
   */
  public long getTimeout() {
    return timeout;
  }

  /**
   * Sets the timeout.
   * @param timeout A <code>long</code> representation of timeout.
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  /**
   * Determines whether or not the session is idle.
   * @return <code>true</code> if idle != NO_IDLE;
   * <code>false</code> otherwise.
   */
  public boolean isIdleSet() {
    if (idle != NO_IDLE) {
      return true;
    }
    return false;
  }

  /**
   * Returns the idle.
   * @return A <code>byte</code> representation of idle.
   */
  public byte getIdlePriority() {
    return idle;
  }

  /**
   * Sets the idle.  Applications should typically
   * use one of the constants defined in this class.
   * @param idle A <code>byte</code> representation of idle.
   */
  public void setIdlePriority(byte idle) {
    this.idle = idle;
  }

  /**
   * Allows the request to authenticate itself.
   * A typical transaction might work like this:
   * <pre>
   *    service_request.authenticate(authenticator);
   *    if (authenticator.failed())
   *       throw new Exception(authenticator.getReasonFailed())
   *</pre>
   *
   * @param a An Authenticator that tries to recieve authentication
   * information from this instance
   */
  public void authenticate(Authenticator a) {
    authentication.provideAuthentication(a);
  }

  /**
   * Returns the authentication.
   * @return An object {@link Authentication} representation of authentication.
   */
  public Authentication getAuthentication() {
    return authentication;
  }

  /**
   * Sets the authentication.
   * @param authentication An object {@link Authentication} representation of
   * a value to be set to authentication.
   */
  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }

  /**
   * This method takes the {@link ComponentMetaData} object, asks for
   * its name {@link ComponentMetaData#getName()} and adds it to
   * the current software HashMap.
       * @param componentMetaData An object {@link ComponentMetaData} representation of
   * an element to be added to a HashMap.
   */
  public void addCurrentSoftware(ComponentMetaData componentMetaData) {
    current_software.put(componentMetaData.getName(), componentMetaData);
  }

  /**
   * Removes entries from the underlying current_software HashMap.
   * @param name An object {@link String} representation of key to remove an
   * element in a HashMap.
   */
  public void removeCurrentSoftware(String name) {
    current_software.remove(name);
  }

  /**
   * <b>NOT USED</b>
   * @return all {@link ComponentMetaData}
   */
  public ComponentMetaData[] getCurrentSoftware() {
    return (ComponentMetaData[]) current_software.values().
        toArray(new ComponentMetaData[] {});
  }

  /**
   * Sets the service.
   * @param service An object {@link String} representation of service.
   */
  public void setService(String service) {
    this.service = service;
  }

  /**
   * Returns the service.
   * @return An object {@link String} representation of service.
   */
  public String getService() {
    return service;
  }

  /**
   * Constructs a {@link gov.nih.nlm.meme.common.Parameter.Default}
   * object from the fields provided and adds it to the parameters HashMap.
   * @param name An object {@link String} representation of key in an element
   * in a HashMap.
   * @param value An object {@link Object} representation of parameter value.
   * @param primitive A <code>boolean</code> representation of parameter
   * primitive.
   */
  public void addParameter(String name, Object value, boolean primitive) {
    parameters.put(name, new Parameter.Default(name, value, primitive));
  }

  /**
   * This method takes the {@link Parameter} object, asks for its name
   * {@link Parameter#getName()} and adds it to the parameters HashMap.
   * @param parameter An object {@link Parameter} representation of
   * an element to be added to a HashMap.
   */
  public void addParameter(Parameter parameter) {
    parameters.put(parameter.getName(), parameter);
  }

  /**
   * Removes all entries from the underlying parameters HashMap.
   */
  public void clearParameters() {
    parameters.clear();
  }

  /**
   * Removes entries from the underlying parameters HashMap.
   * @param name An object {@link String} representation of key to remove an
   * element in a HashMap.
   */
  public void removeParameter(String name) {
    parameters.remove(name);
  }

  /**
   * Returns the parameter hashed with a particular name.
   * @param name An object {@link String} representation of name.
   * @return An object {@link Parameter} representation of parameter.
   */
  public Parameter getParameter(String name) {
    return (Parameter) parameters.get(name);
  }

  /**
   * Returns all {@link Parameter}s.
   * @return all {@link Parameter}s
   */
  public Parameter[] getParameters() {
    return (Parameter[]) parameters.values().toArray(new Parameter[] {});
  }

  /**
   * This method takes the {@link ComponentMetaData} object, asks for
   * its name {@link ComponentMetaData#getName()} and adds it to the
   * required software HashMap.
       * @param componentMetaData An object {@link ComponentMetaData} representation of
   * an element to be added to a HashMap.
   */
  public void addRequiredSoftware(ComponentMetaData componentMetaData) {
    required_software.put(componentMetaData.getName(), componentMetaData);
  }

  /**
   * Removes entries from the underlying required_software HashMap.
   * @param name An object {@link String} representation of key to remove an
   * element in a HashMap.
   */
  public void removeRequiredSoftware(String name) {
    required_software.remove(name);
  }

  /**
   * <b>NOT USED</b>.
   * @return all {@link ComponentMetaData}
   */
  public ComponentMetaData[] getRequiredSoftware() {
    return (ComponentMetaData[]) required_software.values().
        toArray(new ComponentMetaData[] {});
  }

  /**
   * Adds exception to an exceptions ArrayList.
   * @param exception An object {@link Exception} representation of
   * an element to be added to ArrayList.
   */
  public void addException(Exception exception) {
    exceptions.add(exception);
  }

  /**
   * Removes exception to an exceptions ArrayList.
   * @param exception An object {@link Exception} representation of
   * an element to be removed to ArrayList.
   */
  public void removeException(Exception exception) {
    exceptions.clear();
  }

  /**
   * Returns list of exception.
   * @return An array of object {@link Exception}.
   */
  public Exception[] getExceptions() {
    return (Exception[]) exceptions.toArray(new Exception[] {});
  }

  /**
   * Adds warning to a warning ArrayList.
   * @param warning An object {@link Warning} representation of
   * an element to be added to ArrayList.
   */
  public void addWarning(Warning warning) {
    warnings.add(warning);
  }

  /**
   * Removes warning to a warning ArrayList.
   * @param warning An object {@link Warning} representation of
   * an element to be removed to ArrayList.
   */
  public void removeWarning(Warning warning) {
    warnings.clear();
  }

  /**
   * Returns list of warning.
   * @return An array of object {@link Warning}.
   */
  public Warning[] getWarnings() {
    return (Warning[]) warnings.toArray(new Warning[] {});
  }

  /**
   * Constructs a {@link gov.nih.nlm.meme.common.Parameter.Default}
   * object from the fields provided and adds it to the return_values HashMap.
   * @param name An object {@link String} representation of key in an element
   * in a HashMap.
   * @param value An object {@link Object} representation of parameter value.
   * @param primitive A <code>boolean</code> representation of parameter
   * primitive.
   */
  public void addReturnValue(String name, Object value, boolean primitive) {
    return_values.put
        (name, new Parameter.Default(name, value, primitive));
  }

  /**
   * This method takes the {@link Parameter} object, asks for its name
   * {@link Parameter#getName()} and adds it to the return_values HashMap.
   * @param parameter An object {@link Parameter} representation of parameter.
   */
  public void addReturnValue(Parameter parameter) {
    return_values.put(parameter.getName(), parameter);
  }

  /**
   * Removes all entries from the underlying return_values HashMap.
   */
  public void clearReturnValues() {
    return_values.clear();
  }

  /**
   * Removes entries from the underlying return_values HashMap.
   * @param name An object {@link String} representation of key to remove an
   * element in a HashMap.
   */
  public void removeReturnValue(String name) {
    return_values.remove(name);
  }

  /**
   * Gets the return value matching the name.
   * @param name the parameter name.
   * @return An object {@link Parameter}.
   */
  public Parameter getReturnValue(String name) {
    return (Parameter) return_values.get(name);
  }

  /**
   * Returns all return values.
   * @return all return values
   */
  public Parameter[] getReturnValues() {
    return (Parameter[]) return_values.values().toArray(new Parameter[] {});
  }

  /**
   * Returns list of parameter.
   * @return An object {@link HashMap} representation of parameter.
   */
  public HashMap getParametersAsHashMap() {
    return parameters;
  }

  /**
   * Returns a {@link HashMap} of all return values.
   * @return a {@link HashMap} of all return values
   */
  public HashMap getReturnValuesAsHashMap() {
    return return_values;
  }

  /**
   * Returns the request id.
   * @return An <code>int</code> representation of request id.
   */
  public int getRequestId() {
    return request_id;
  }

  /**
   * Sets the request id.
   * @param request_id An <code>int</code> representation of request id.
   */
  public void setRequestId(int request_id) {
    this.request_id = request_id;
  }

  /**
   * Returns a reference to the writer.
   * @return a reference to the {@link Writer}.
   */
  public Writer getWriter() {
    return writer;
  }

  /**
   * Sets the reference to the writer
   * @param writer the {@link Writer}
   */
  public void setWriter(Writer writer) {
    this.writer = writer;
  }

  /**
   * Indicates whether or not this is a reconnect request.
   * @return <code>true</codE> if so, <code>false</code> otherwise.
   */
  public boolean isReconnectRequest() {
  	return getService() != null && getService().equals("reconnect");
  }

  /**
   * Sets the flag indicating that this is a reconnect request.
   */
  public void setReconnectRequest() {
    setService("reconnect");
  }

  /**
   * Copies values from specified request into this one.
   * @param request specified {@link MEMEServiceRequest}.
   */
  public void copy(MEMEServiceRequest request) {
    this.setAuthentication(request.getAuthentication());
    this.setIdlePriority(request.getIdlePriority());
    this.setMidService(request.getMidService());
    this.setRequestId(request.getRequestId());
    this.setService(request.getService());
    this.setSessionId(request.getSessionId());
    this.setTimeout(request.getTimeout());
    Warning[] warnings = request.getWarnings();
    for (int i = 0; i < warnings.length; i++) {
      this.addWarning(warnings[i]);
    }
    Exception[] exceptions = request.getExceptions();
    for (int i = 0; i < exceptions.length; i++) {
      this.addException(exceptions[i]);
    }
    Parameter[] params = request.getParameters();
    for (int i = 0; i < params.length; i++) {
      this.addParameter(params[i]);
    }
    Parameter[] ret_vals = request.getReturnValues();
    for (int i = 0; i < ret_vals.length; i++) {
      this.addReturnValue(ret_vals[i]);
    }
  }
}
