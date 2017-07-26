/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  AdminClient
 *
 * 03/22/2007 BAC (1-D0BIJ): refresh caches call changed to set mid service.
 *****************************************************************************/

package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;
import gov.nih.nlm.meme.exception.BadValueException;
import java.util.Date;
import gov.nih.nlm.meme.common.PasswordAuthentication;

/**
 * This client API is used to remotely administer the
 * server.  See {@link ClientAPI} for information
 * on configuring properties required by this class.
 *
 * With the properties properly configured, accessing administrative
 * services is as simple as instantiating {@link AdminClient} and
 * calling the methods.  For example,
 *
 * <pre>
 *   // Authenticate a user (against the current data source)
 *   boolean authenticated = client.authenticate(user,password);
 *
 *   // Retrieve the server version
 *   String version_info = client.getServerVersion();
 *
 *   // Instantiate client
 *   AdminClient client = new AdminClient();
 *
 *   // Retrieve full log
 *   String full_log = client.getServerLog();
 *
 *   // Retrieve first 10 lines of log
 *   client.setHead(10);
 *   String head_log = client.getServerLog();
 *
 *   // Retrieve last 10 lines of log
 *   client.setTail(10);
 *   String tail_log = client.getServerLog();
 *
 *   // Retrieve a session log
 *   String session_log = client.getSessionLog(session_id);
 *   String session_log_tail = client.getSessionLogNotSeen(session_id);
 *
 *   // Retrieve the log for a transaction
 *   String transaction_log = client.getTransactionLog(transaction_id);
 *
 *   // Refresh the data source and MIDServices caches
 *   client.refreshCaches();
 *
 *   // Test the connection
 *   String ping = client.ping();
 *
 *   // Get statistics report
 *   String statistics = client.getStatisticsReport();
 *
 *   // Shutdown the server
 *   client.shutdownServer();
 *
 *   // Kill the server
 *   client.killServer();
 *
 *   // Enable the integrity system
 *   client.enableIntegritySystem();
 *
 *   // Disable the integrity system
 *   client.disableIntegritySystem();
 *
 *   // Enable the editing environment
 *   client.enableEditing();
 *
 *   // Disable the editing environment
 *   client.disableEditing();
 *
 *   // Enable atomic action validation
 *   client.enableAtomicActionValidation();
 *
 *   // Disable atomic action validation
 *   client.disableAtomicActionValidation();
 *
 *   // Enable molecular action validation
 *   client.enableMolecularActionValidation();
 *
 *   // Disable molecular action validation
 *   client.disableMolecularActionValidation();
 *
 * </pre>
 *
 * More administrative functions will be added in the future.
 *
 * @see ClientAPI
 * @author MEME Group
 */
public class AdminClient extends ClientAPI {

  //
  // Private Fields
  //

  private int head = 0;
  private int tail = 0;
  private String mid_service = "";

  //
  // Constructors
  //

  /**
   * Instantiates an {@link AdminClient}.
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated.
   */
  public AdminClient() throws MEMEException {
    super();
  }

  /**
       * Instantiates an {@link AdminClient} connected to the specified mid service.
   * @param mid_service a valid MID service
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated.
   */
  public AdminClient(String mid_service) throws MEMEException {
    super();
    this.mid_service = mid_service;
  }

  //
  // Methods
  //

  /**
   * Returns the mid service.
   * @return the mid service
   */
  public String getMidService() {
    return mid_service;
  }

  /**
   * Sets the mid service.
   * @param mid_service the mid service
   */
  public void setMidService(String mid_service) {
    this.mid_service = mid_service;
  }

  /**
   * Returns server version information (<b>SERVER CALL</b>).
   * @return server version information
   * @throws MEMEException if anything goes wrong
   */
  public String getServerVersion() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "version"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("version").getValue();
  }

  //
  // LogClient API
  //

  /**
   * Returns the server log, same as <code>getServerLog</code> (<b>SERVER CALL</b>).
   * @return the server log
   * @throws MEMEException if anything goes wrong
   */
  public String getLog() throws MEMEException {
    return getServerLog();
  }

  /**
   * Returns the server log (<B>SERVER CALL</b>).
   * @return the server log
   * @throws MEMEException if anything goes wrong
   */
  public String getServerLog() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "server_log"));

    if (head > 0) {
      request.addParameter(new Parameter.Default("head", head));
    }
    if (tail > 0) {
      request.addParameter(new Parameter.Default("tail", tail));

      // Issue request
    }
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("log").getValue();
  }

  /**
   * Returns the session log for the specified session id (<B>SERVER CALL</b>).
   * @param session_id the session id
   * @return the session log for the specified session id
   * @throws MEMEException if anything goes wrong
   */
  public String getSessionLog(String session_id) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setSessionId(session_id);
    request.addParameter(new Parameter.Default("function", "session_log"));

    if (head > 0) {
      request.addParameter(new Parameter.Default("head", head));
    }
    if (tail > 0) {
      request.addParameter(new Parameter.Default("tail", tail));

      // Issue request
    }
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("log").getValue();
  }

  /**
       * Returns the session progress for the specified session id(<b>SERVER CALL</b>).
   * @param session_id the session id
   * @return the session progress for the specified session id
   * @throws MEMEException if anything goes wrong
   */
  public int getSessionProgress(String session_id) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setSessionId(session_id);
    request.addParameter(new Parameter.Default("function", "session_progress"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (int) request.getReturnValue("progress").getInt();
  }

  /**
   * Returns the portion of session log not yet seen for the specified
   * session id (<b>SERVER CALL</b>).
   * @param session_id the session id
   * @return the portion session log not yet seen for the specified
   * session id
   * @throws MEMEException if anything goes wrong
   */
  public String getSessionLogNotSeen(String session_id) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setSessionId(session_id);
    request.addParameter(new Parameter.Default("function",
                                               "session_log_not_seen"));

    if (head > 0) {
      request.addParameter(new Parameter.Default("head", head));
    }
    if (tail > 0) {
      request.addParameter(new Parameter.Default("tail", tail));

      // Issue request
    }
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("log").getValue();
  }

  /**
   * Returns the transaction log for the specified transaction id (<b>SERVER CALL</b>).
   * @param transaction_id the transaction id
   * @return the transaction log for the specified transaction id
   * @throws MEMEException if anything goes wrong
   */
  public String getTransactionLog(int transaction_id) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "transaction_log"));
    request.addParameter(new Parameter.Default("transaction_id", transaction_id));

    if (head > 0) {
      request.addParameter(new Parameter.Default("head", head));
    }
    if (tail > 0) {
      request.addParameter(new Parameter.Default("tail", tail));

      // Issue request
    }
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("log").getValue();
  }

  /**
   * Indicates to the client that future requests
   * should retrieve only the head of the log file.  Calling
   * this method overrides any previous {@link #setHead(int)} calls.
   * @param head the number of log file lines to return
   */
  public void setHead(int head) {
    this.head = head;
  }

  /**
   * Indicates to the client that future requests
   * should retrieve only the tail of the log file.  Calling
   * this method overrides any previous {@link #setTail(int)} calls.
   * @param tail the number of log file lines to return
   */
  public void setTail(int tail) {
    this.tail = tail;
  }

  //
  // Statistics API
  //

  /**
   * Returns a server statistics report (<b>SERVER CALL</b>).
   * @return a server statistics report
   * @throws MEMEException if anything goes wrong
   */
  public String getStatisticsReport() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "stats"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    // Process response

    return (String) request.getReturnValue("stats").getValue();
  }

  //
  // Dummy API
  //

  /**
   * An empty function to test that server connection is working (<b>SERVER CALL</b>).
   * @return a dummy response
   * @throws MEMEException if anything goes wrong
   */
  public String ping() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "dummy"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    // Process response
    return (String) request.getReturnValue("dummy").getValue();
  }

  /**
   * Performs the specified action sequence (<b>SERVER CALL</b>).
   * This method runs one of a series of pre-defined action sequences
   * for QA purposes.  This is potentially destructive and should not
   * be run unless you konw what you are doing.
   * @param function the action sequence to perform
   * @return a {@link String} indicating the elapsed time
   * @throws MEMEException if anything goes wrong
   */
  public String runActionSequence(String function) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("ActionSequences");
    request.setSessionId(getSessionId());
    request.addParameter(new Parameter.Default("function", function));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
    // Process response
    return (String) request.getReturnValue("elapsed_time").getValue();
  }

  //
  // Refresh API
  //

  /**
   * Refreshes MEME application server caches (<b>SERVER CALL</b>).
   * @throws MEMEException if anything goes wrong
   */
  public void refreshCaches() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setMidService(mid_service);
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "refresh_caches"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Refreshes MEME data source connections (<b>SERVER CALL</b>).
   * @param mid_service mid service to refresh
   * @throws MEMEException if anything goes wrong
   */
  public void refreshMidService(String mid_service) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setNoSession(true);
    request.setMidService(mid_service);
    request.addParameter(new Parameter.Default("function", "refresh_db"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  //
  // Shutdown API
  //

  /**
   * Shuts down the server (<b>SERVER CALL</b>).  The server will wait
   * to finish any currently running requests before shutting down.
   * @throws MEMEException if anything goes wrong.
   */
  public void shutdownServer() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "shutdown"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Kills the server (<b>SERVER CALL</b>). Does not wait for currently
   * running requests to complete, instead just exits JVM process.
   * @throws MEMEException if anything goes wrong
   */
  public void killServer() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "shutdown"));
    request.addParameter(new Parameter.Default("kill", "true"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Enables the integrity system (<B>SERVER CALL</B>).  This sets the switch
   * in the <code>ic_system_status</code> table.
   * @throws MEMEException if naything goes wrong
   */
  public void enableIntegritySystem() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "enable_integrity"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
       * Disables the integrity system (<b>SERVER CALL</b>). This sets the switch in
   * the <code>ic_system_status</code> table.
   * @throws MEMEException if anything goes wrong
   */
  public void disableIntegritySystem() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "disable_integrity"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
       * Determines whether or not integrity system is enabled (<B>SERVER CALL</b>).
   * @return a <code>true</code> if integrity system is enabled.
   * <code>false</code> otherwise.
   * @throws MEMEException if anything goes wrong
   */
  public boolean isIntegritySystemEnabled() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default(
        "function", "is_integrity_enabled"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return request.getReturnValue("is_integrity_enabled").getBoolean();
  }

  /**
   * Enables editing (<b>SERVER CALL</b>). This sets the switch in
   * the <code>dba_cutoff</code> table.
   * @throws MEMEException if anything goes wrong
   */
  public void enableEditing() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "enable_editing"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Disables editing (<B>SERVER CALL</b>). This sets the switch in
   * the <code>dba_cutoff</code> table.
   * @throws MEMEException if anything goes wrong
   */
  public void disableEditing() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "disable_editing"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Determines whether or not editing is enabled (<b>SERVER CALL</b>).
   * @return a <code>true</code> if editing is enabled.
   * <code>false</code> otherwise.
   * @throws MEMEException if anything goes wrong
   */
  public boolean isEditingEnabled() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default(
        "function", "is_editing_enabled"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return request.getReturnValue("is_editing_enabled").getBoolean();
  }

  /**
   * Enables atomic action validation (<B>SERVER CALL</b>).  This sets
   * a PL/SQL package variable.
   * @throws MEMEException if anything goes wrong
   */
  public void enableAtomicActionValidation() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default(
        "function", "enable_validate_atomic_action"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Disables atomic action validation action (<B>SERVER CALL</b>). This sets
   * a PL/SQL package variable
   * @throws MEMEException if anything goes wrong
   */
  public void disableAtomicActionValidation() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default(
        "function", "disable_validate_atomic_action"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Determines whether or not atomic action validation is enabled (<B>SERVER CALL</b>).
   * @return a <code>true</code> if atomic action validation is enabled.
   * <code>false</code> otherwise.
   * @throws MEMEException if anything goes wrong
   */
  public boolean isAtomicActionValidationEnabled() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default(
        "function", "is_validate_atomic_action_enabled"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return request.getReturnValue("is_validate_atomic_action_enabled").
        getBoolean();
  }

  /**
   * Enables molecular action validation (<B>SERVER CALL</b>). This sets
   * a PL/SQL package variable.
   * @throws MEMEException if anything goes wrong
   */
  public void enableMolecularActionValidation() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default(
        "function", "enable_validate_molecular_action"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Disabled molecular action validation (<B>SERVER CALL</b>). This sets a
   * PL/SQL package variable.
   * @throws MEMEException if anything goes wrong
   */
  public void disableMolecularActionValidation() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default(
        "function", "disable_validate_molecular_action"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Determines whether or not molecular action validation is enabled
   * (<B>SERVER CALL</b>).
   * @return a <code>true</code> if molecular action validation is enabled.
   * <code>false</code> otherwise.
   * @throws MEMEException if anything goes wrong
   */
  public boolean isMolecularActionValidationEnabled() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default(
        "function", "is_validate_molecular_action_enabled"));

    // Issue request
    request = getRequestHandler().processRequest(request);
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return request.getReturnValue("is_validate_molecular_action_enabled").
        getBoolean();
  }

  /**
   * Returns an {@link EditorPreferences} object if the specified
       * user and password can be authenticated by a data source (<b>SERVER CALL</b>).
   * If not, <code>null</code> is returned.
   * @param user the user name.
   * @param password the user password.
   * @return the {@link EditorPreferences} object corresponding to the user,
   *        or <code>null</code> if authentication fails.
   * @throws MEMEException if anything goes wrong
   */
  public EditorPreferences authenticate(String user, String password) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    // We are authenticating, so no connection needed.
    request.setMidService("");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "authenticate"));
    request.addParameter(new Parameter.Default("user", user));
    request.addParameter(new Parameter.Default("password", password));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (EditorPreferences) request.getReturnValue("editor_preferences").
        getValue();
  }

  /**
   * Sets system status for the specified key and value (<B>SERVER CALL</b>).
   * Causes the <code>system_status</code> table to be updated.
   * @param key the key
   * @param value the value
   * @throws MEMEException if anything goes wrong
   */
  public void setSystemStatus(String key, String value) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "set_system_status"));
    request.addParameter(new Parameter.Default("key", key));
    request.addParameter(new Parameter.Default("value", value));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Returns system status value for the specified key (<B>SERVER CALL</b>).
   * @param key the key
   * @return the system status value for the specified key
   * @throws MEMEException if anything goes wrong
   */
  public String getSystemStatus(String key) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "get_system_status"));
    request.addParameter(new Parameter.Default("key", key));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("system_status").getValue();
  }

  /**
   * Change the user's password
   * @param user String
   * @param old_pwd String
   * @param new_pwd String
   * @throws MEMEException
   */
  public void changePassword(String user, String old_pwd, String new_pwd) throws MEMEException {
    if(old_pwd.equals(new_pwd)) {
      throw new BadValueException("Old and new password must be different");
    }
    EditorPreferences ep = authenticate(user,old_pwd);
    if(ep == null) {
      throw new BadValueException("Invalid Password");
    }
    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.addParameter(new Parameter.Default("function", "change_password"));
    request.addParameter(new Parameter.Default("user", user));
    request.addParameter(new Parameter.Default("password", new_pwd));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Change the password for user by dba
   * @param user String
   * @param old_pwd String
   * @param new_pwd String
   * @throws MEMEException
   */
  public void changePasswordForUser(String dba_user, String dba_password, String user, String new_password) throws MEMEException {
    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setMidService(mid_service);
    request.setService("AdminService");
    request.setNoSession(true);
    request.setAuthentication(new PasswordAuthentication(dba_user,dba_password.toCharArray()));
    request.addParameter(new Parameter.Default("function", "change_password"));
    request.addParameter(new Parameter.Default("user", user));
    request.addParameter(new Parameter.Default("password", new_password));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }
  /**
   * Get the expiration date of user's password
   * @param user String
   * @param password String
   * @return Date
   * @throws MEMEException
   */

  public Date getPasswordExpirationDate(String user, String password) throws MEMEException {
      // Prepare request document
      MEMEServiceRequest request = super.getServiceRequest();
      request.setMidService(mid_service);
      if (user != null)
      request.setAuthentication(new PasswordAuthentication(user,password.toCharArray()));
      request.setService("AdminService");
      request.setNoSession(true);
      request.addParameter(new Parameter.Default("function", "get_password_expiration_date"));

      // Issue request
      request = getRequestHandler().processRequest(request);

      // Handle exceptions
      Exception[] exceptions = request.getExceptions();
      if (exceptions.length > 0) {
        throw (MEMEException) exceptions[0];
      }
      // Process response
      return (Date) request.getReturnValue("expiration_date").getValue();

  }
}
