/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  MEMEApplicationServer
 *
 * 05/24/2006 RBE (1-BA55P) : Preventing certain errors from sending mail
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.Initializable;
import gov.nih.nlm.meme.InitializationContext;
import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.PasswordAuthenticator;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.ConcurrencyException;
import gov.nih.nlm.meme.exception.ExpiredSessionException;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.sql.MEMESchedule;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * The application entry point.  This is <i>the</i> application server.
 * 
 * CHANGES
 * 09/10/2007 JFW (1-DBSLD): Modify isReEntrant call to take a SessionContext argument 
 * 
 * @author MEME Group
 */

public class MEMEApplicationServer implements InitializationContext {

  //
  // Fields
  //

  protected MEMERequestListener listener = null;
  protected HashMap services_map = new HashMap();
  protected HashMap session_context_map = new HashMap();
  protected HashSet terminated_sessions = new HashSet();
  protected int max_session_id = 0;
  protected ArrayList server_threads = new ArrayList();

  //
  // Implementation of InitializationContext
  //

  /**
   * Adds an {@link Initializable} hook.
   * @param init the {@link Initializable} component
   */
  public void addHook(Initializable init) {
    if (init instanceof ThreadPool) {
      ServerToolkit.setThreadPool( (ThreadPool) init);
    } else if (init instanceof MIDDataSourcePool) {
      ServerToolkit.setDataSourcePool( (MIDDataSourcePool) init);
    } else if (init instanceof MEMESchedule) {
      MEMESchedule ms = (MEMESchedule) init;
      ServerToolkit.setMEMESchedule(ms);
      try {
        ms.setDataSource(ServerToolkit.newMIDDataSource());
      } catch (Exception e) {}
    } else if (init instanceof MEMERequestListener) {
      this.listener = (MEMERequestListener) init;
    } else if (init instanceof ServerThread) {
      server_threads.add( (ServerThread) init);
    }
  }

  //
  // Methods
  //

  /**
   * Receives a request from the listener and responsible to
   * package the request into a session context and call the right handler.
   * @param request the {@link MEMEServiceRequest}
   * @throws MEMEException If failed to process the request
   */
  public void processRequest(MEMEServiceRequest request) throws MEMEException {

    SessionContext context = null;
    try {

      //
      // If the request is trying to reconnect,
      // then find the session context, block until it is no longer running
      // and then attach the return values to reconnect request and send it back.
      //
      if (request.isReconnectRequest()) {
        manageReconnect(request);
        return;
      }

      // Perform Session Management
      // If any exceptions occur, just return (exceptions already added).
      // The context returned has been marked as running and last used has
      // been updated.
      context = manageSession(request);

      // if terminate session request -> done
      if (request.terminateSession()) {
        return;
      }

      // Perform DataSource Management
      synchronized (context) {
        manageDataSource(request, context);
      }

      // Get the application service from the map
      MEMEApplicationService service =
          (MEMEApplicationService) services_map.get(request.getService());
      MEMEToolkit.trace("MEMEApplicationServer.processRequest() -service " +
                        service);

      // If no service was found, report an error
      if (service == null) {
        BadValueException bve = new BadValueException(
            "Illegal service name.");
        bve.setDetail("service", request.getService());
        bve.setFatal(false);
        bve.setInformAdministrator(false);        
        throw bve;
      }

      // Check that if request has no session id and tries
      // to access a service that requires a session id
      if ( (request.getSessionId() == null ||
            request.getSessionId().equals("")) &&
          service.requiresSession()) {
        BadValueException bve = new BadValueException(
            "Request has no session id and tries to access " +
            "a service that requires a session id.");
        bve.setDetail("service", request.getService());
        bve.setFatal(false);
        throw bve;
      }

      if (service.isReEntrant(context) && service.isRunning()) {
        ConcurrencyException ce = new ConcurrencyException(
            "The service requested is already running.");
        ce.setDetail("service", request.getService());
        throw ce;
      }

      // Invoke the service
      service.processRequest(context);

    } catch (MEMEException me) {
      // If anything goes wrong, make sure data source is returned.
      if (context != null && context.getDataSource() != null) {
        try {
          context.getDataSource().rollback();
        } catch (Exception e) {}
        me.setDetail("data_source", context.getDataSource().getDataSourceName());
        if (context.getServiceRequest().getAuthentication() != null) {
          me.setDetail("authentication",
                       context.getServiceRequest().getAuthentication().toString());
        }
      }
      if (context != null) {
        synchronized (context) {
          manageEndOfRequest(context, request);
        }
      }
      throw me;
    } catch (RuntimeException e) {
      // If anything goes wrong, make sure data source is returned.
      if (context != null && context.getDataSource() != null) {
        try {
          context.getDataSource().rollback();
        } catch (Exception ex) {}
      }
      if (context != null) {
        synchronized (context) {
          manageEndOfRequest(context, request);
        }
      }
      throw e;
    }

    if (context != null) {
      synchronized (context) {
        manageEndOfRequest(context, request);
      }
    }

    // clear server parameters
    request.clearParameters();
  }

  /**
   * Implements the Session architecture.  If a request is
   * initiating a session, it creates the new session.  If a request is using
   * an existing session_id, it looks up the session in the map.  If the id is
   * no good, it determines if the session is just old or if the ID is
   * actually illegal and reports the error.  If a request is terminating a
   * session, it removes the session from the map and returns.  Finally if a
   * request wants nosession, a single-use context object is created.
   * @param request the {@link MEMEServiceRequest}
   * @return the {@link SessionContext}
   * @throws BadValueException if failed to manage the session
   * @throws ExpiredSessionException if failed due to session expiration
   */
  protected synchronized SessionContext manageSession(MEMEServiceRequest
      request) throws BadValueException, ExpiredSessionException {

    SessionContext context = null;

    //
    // Verify that session parameters are valid, otherwise throw an exception
    //
    if ( (request.initiateSession() && request.terminateSession()) ||
        (request.initiateSession() && request.noSession()) ||
        (request.noSession() && request.terminateSession())) {
      BadValueException bve = new BadValueException(
          "Illegal session parameters.");
      bve.setDetail("initiate", String.valueOf(request.initiateSession()));
      bve.setDetail("terminate", String.valueOf(request.terminateSession()));
      bve.setDetail("no_session", String.valueOf(request.noSession()));
      throw bve;
    }

    //
    // Handle the case of a session-enabled request.
    // This is either a regular request, or a request to terminate the session.
    //
    if (request.getSessionId() != null && !request.getSessionId().equals("")) {
      MEMEToolkit.trace(
          "MEMEApplicationServer.manageSession(): -getSessionId(): "
          + request.getSessionId());

      //
      // Obtain the session id and its context
      //
      String session_id = request.getSessionId();
      context = getSessionContext(session_id);

      //
      // If the context could not be found, then the session is
      // terminated (or never existed).
      //
      if (context == null) {
        MEMEToolkit.trace(
            "MEMEApplicationServer.manageSession() -Context is null");

        if (terminated_sessions.contains(session_id)) {
          ExpiredSessionException ese = new ExpiredSessionException(
              "The request is attempting to use a session that " +
              "has been terminated or has timed out.");
          ese.setDetail("session_id", String.valueOf(session_id));
          throw ese;
        } else {
          BadValueException bve = new BadValueException(
              "Illegal session id.");
          bve.setDetail("session_id", String.valueOf(session_id));
          bve.setInformAdministrator(false);          
          throw bve;
        }
      }

      //
      // Configure the context for the current request
      //
      context.setServer(this);
      context.setServiceRequest(request);

      //
      // Terminate the session if requested
      //
      if (request.terminateSession()) {
        terminateSession( (SessionContext) session_context_map.get(session_id));
      }

      //
      // Handle the nosession case.  This is either a nosession request
      // or a request to initiate a new session
      //
    } else if (request.initiateSession() || request.noSession()) {

      //
      // Create a new context object
      //
      context = new SessionContext();
      context.setServer(this);
      context.setServiceRequest(request);

      //
      // If initiating a session,
      // Assign a new session id and save the contextx for future use.
      //
      if (request.initiateSession()) {
        request.setSessionId(String.valueOf(++max_session_id));
        putSessionContext(request.getSessionId(), context);

        //
        // Turn off the initiate flag
        //
        request.setInitiateSession(false);
      }

      //
      // Here, there is no session id and this is
      // not a nosession request nor a session
      // initiation request, raise an exception.
      //
    } else {
      BadValueException bve = new BadValueException(
          "Illegal session parameters.  The request must either use a session id, " +
          "attempt to initiate a session, or not use a session.");
      bve.setDetail("session_id", String.valueOf(request.getSessionId()));
      bve.setDetail("initiate", String.valueOf(request.initiateSession()));
      bve.setDetail("no_session", String.valueOf(request.noSession()));
      throw bve;
    }

    //
    // At this point, either have a request with a session_id, or a
    // nosession request and a SessionContext has been created.
    //
    return context;
  }

  /**
   * Adds appropriate data source (or none) to the {@link SessionContext}.
   * @param request the {@link MEMEServiceRequest}
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to manage the data source
   */
  protected void manageDataSource(MEMEServiceRequest request,
                                  SessionContext context) throws MEMEException {

    boolean needs_data_source = false;
    PasswordAuthenticator pa = null;
    String user = null;
    String password = null;
    MEMEDataSource data_source = null;

    MEMEToolkit.trace(
        "MEMEApplicationServer.manageDataSource() -request.getMidService(): "
        + request.getMidService());

    //
    // Verify that the request needs a data source
    //
    if (request.getMidService() != null) {

      //
      // Get the authentication information if it exists.
      //
      Authentication authentication = request.getAuthentication();
      if (authentication != null) {
        pa = new PasswordAuthenticator();
        authentication.provideAuthentication(pa);
        user = pa.getUsername();
        password = new String(pa.getPassword());
        //pa.clearPassword();
      } else {
        user = null;
        password = null;
      }

      //
      // If the context has a session data source, attempt to use that one.
      // This only works if the service name matches the one used by the
      // data source itself (and the authentication information matches).
      //
      data_source = context.getSessionDataSource();
      if (data_source != null) {
        if (data_source.getServiceName().toLowerCase().equals(request.
            getMidService().toLowerCase()) &&
            ( (user == null &&
               data_source.getUserName().toLowerCase().equals(
            MEMEToolkit.getProperty(ServerConstants.MID_USER).toLowerCase())) ||
             user.toLowerCase().equals(data_source.getUserName().toLowerCase()))) {
          needs_data_source = false;
        } else {
          needs_data_source = true;
        }
      } else {
        //
        // If the session data source of the context is null
        // we will need to obtain a new data source for this request
        //
        needs_data_source = true;
      }

    } else {
      //
      // User is not requesting a data source, return.
      //
      return;
    }

    //
    // If the request needs a new data source, get it.
    //
    if (needs_data_source) {

      MEMEDataSource mds = getDataSource(request.getMidService(), user,
                                         password);

      //
      // Connect the data source to the context.
      //
      context.setDataSource(mds);
      MEMEToolkit.trace("MEMEApplicationServer.manageDataSource() -mds: " + mds);
      MEMEToolkit.trace(
          "MEMEApplicationServer.manageDataSource() -DataSource created.");

      //
      // If the session data source does not match the
      // one used by the current request, return it
      //
      if (data_source != null) {
        returnDataSource(data_source);

      }
    } else {
      context.setDataSource(data_source);
    }

    MEMEToolkit.trace(
        "MEMEApplicationServer.manageDataSource() -request.getService(): "
        + request.getService());

    //
    // Set the session data source to null until the request
    // is done being processed. {@see #manageEndOfSession(SessionContext,MEMEServiceRequest)}.
    //
    context.setSessionDataSource(null);

    //
    // Finished
    //
    return;

  }

  /**
   * Handles the end of a request.  It primarily returns resources
   * used by the context which will not be needed i n future requests.
   * @param context the {@link SessionContext}
   * @param request the {@link MEMEServiceRequest}
   * @throws MEMEException if failed to manage end of request
   */
  protected void manageEndOfRequest(SessionContext context,
                                    MEMEServiceRequest request) throws
      MEMEException {

    //
    // If we are not using sessions
    // we should return open data source objects
    //
    if (request.noSession()) {
      if (context.getDataSource() != null) {
        returnDataSource(context.getDataSource());

        //
        // Otherwise, we should refresh the context map
        //
      }
    } else {

      //
      // Update context use information
      //
      context.setLastUse(new Date());
      context.setIsRunning(false);

      //
      // Set session data source if it is not already set
      // and if the session has not been terminated already
      //
      if (context.getSessionDataSource() == null &&
          !terminated_sessions.contains(request.getSessionId())) {
        context.setSessionDataSource(context.getDataSource());

        //
        // Otherwise, return the data source
        //
      } else if (context.getDataSource() != null &&
                 !terminated_sessions.contains(request.getSessionId())) {
        returnDataSource(context.getDataSource());

        //
        // Remove request and data source from context
        //
      }
      context.removeDataSource();
      context.removeServiceRequest();

      context.notifyAll();
    }

  }

  /**
   * Handles a "reconnect" request.
   * @param request the {@link MEMEServiceRequest}
       * @throws BadValueException if the session id does not have a waiting request
   */
  protected void manageReconnect(MEMEServiceRequest request) throws
      BadValueException {

    MEMEToolkit.logComment("RECONNECT REQUEST - session_id = " +
                           request.getSessionId(), true);

    SessionContext context =
        (SessionContext) session_context_map.get(request.getSessionId());

    if (context == null) {
      BadValueException bve = new BadValueException(
          "Bad session_id for reconnect request.");
      bve.setDetail("session_id", String.valueOf(request.getSessionId()));
      throw bve;
    }

    synchronized (context) {

      //
      // Block while session runs
      //
      if (context.isRunning()) {
        MEMEToolkit.logComment(
            "RECONNECT REQUEST - original request still running", true);
        while (context.isRunning()) {
          try {
            context.wait();
          } catch (InterruptedException ioe) {}
        }
      }
    }

    //
    // Recover missed request to send back.
    //
    int old_id = request.getRequestId();
    request.copy(context.getLatestRequest());

    MEMEToolkit.logComment("RECONNECT REQUEST - id=" + old_id +
                           ", reconnected_id=" + request.getRequestId(), true);

  }

  /**
   * Returns the correct kind of MEMEDataSource for this server.
   * The purpose of this method is to allow the MRDApplicationServer
   * to return MRD database connections instead of MID database connections.
   *
   * @param mid_service the mid services name
   * @param user the username
   * @param password the password
   * @return the {@link MIDDataSource}
   * @throws MEMEException if failed to get the data source
   */
  public MEMEDataSource getDataSource(String mid_service, String user,
                                      String password) throws MEMEException {
    return ServerToolkit.getMIDDataSource(mid_service, user, password);
  }

  /**
   * Returns the data source to the server toolkit.
   * The purpose of this method is to allow the MRDApplicationServer
   * to return MRD database connections instead of MID database connections.
   * @param ds the {@link MEMEDataSource}
   * @throws MEMEException if failed to return data source
   */
  public void returnDataSource(MEMEDataSource ds) throws MEMEException {
    ServerToolkit.returnDataSource(ds);
  }

  /**
   * Returns all {@link SessionContext}s.
   * @return all {@link SessionContext}s
   */
  public SessionContext[] getSessions() {
    return (SessionContext[]) session_context_map.values().toArray(new
        SessionContext[0]);
  }

  /**
   * Returns the {@link SessionContext} for the specified session id.
   * @param session_id the session id
   * @return the {@link SessionContext} for the specified session id
   */
  public SessionContext getSessionContext(String session_id) {
    SessionContext context = (SessionContext) session_context_map.get(
        session_id);
    if (context != null) {
      context.setLastUse(new Date());
      context.setIsRunning(true);
    }
    return context;
  }

  /**
   * Adds a new {@link SessionContext} for the specified session id.
   * @param session_id the session id.
   * @param context the {@link SessionContext} to add
   */
  protected void putSessionContext(String session_id, SessionContext context) {
    context.setLastUse(new Date());
    context.setIsRunning(false);
    session_context_map.put(session_id.toString(), context);
    Statistics.sessionInitiated(context);
  }

  /**
   * Kills the specified {@link SessionContext}'s session.
   * This must clean up resources associated with the
   * session as well.
   * @param context the {@link SessionContext}
   */
  public void terminateSession(SessionContext context) {

    //
    // Clean up session resources
    //
    try {
      context.terminateSession();
      MEMEToolkit.logComment("Session was terminated. session_id = "
                             + context.getSessionId());
    } catch (MEMEException e) {}

    //
    // Clean up server references to session
    //
    String session_id = context.getSessionId();
    session_context_map.remove(session_id);

    //
    // Update statistics
    //
    Statistics.sessionTerminated(context);

    //
    // Add session id to terminated list
    //
    terminated_sessions.add(session_id);
  }

  /**
   * Starts the server listener.
   */
  public void start() {

    // Initialize the toolkit which will instantiate all of the Initializable components
    // in meme.bootstrap.classes.

    try {
      ServerToolkit.initialize(this);
    } catch (InitializationException ie) {
      MEMEToolkit.handleError(ie);
    }

    // When the MEMERequestListener (rather its implementation)
    // is initialized, it will call back and set the listener field.
    // So if it is null, we cannot proceed.
    if (listener == null) {
      // Serious error
      InitializationException ie = new InitializationException(
          "No listener started. The " + ServerConstants.MEME_BOOTSTRAP +
          " property must contain a MEMERequest listener.");
      ie.setDetail(ServerConstants.MEME_BOOTSTRAP,
                   MEMEToolkit.getProperty(ServerConstants.MEME_BOOTSTRAP));
      MEMEToolkit.handleError(ie);
    }

    // Instantiates all the classes listed in MEME_SERVICES property
    String nservices = null;
    try {
      // Tokenize the bootstrap property to separate on ,
      StringTokenizer st =
          new StringTokenizer(MEMEToolkit.getProperty(ServerConstants.
          MEME_SERVICES), ",");

      while (st.hasMoreTokens()) {
        nservices = st.nextToken();

        // Create an instance of a class and add to the hashmap
        MEMEApplicationService current_class =
            (MEMEApplicationService) Class.forName(nservices).newInstance();

        MEMEToolkit.logComment("SERVICE ADDED - "
                               +
                               MEMEToolkit.getUnqualifiedClassName(
            current_class), true);
        services_map.put(MEMEToolkit.getUnqualifiedClassName(current_class),
                         current_class);

        MEMEToolkit.trace("MEMEApplicationServer:start() - " + nservices +
                          " Successfully instantiated and added to HashMap.");
      }

    } catch (Exception e) {
      InitializationException ie = new InitializationException(
          "Failed to instantiate service.");
      ie.setDetail("service", nservices);
      MEMEToolkit.handleError(ie);
    }

    MEMEToolkit.logComment("START SERVER", true);

    // Log environment info
    logEnvironment();

    // Log version info
    logVersions();

    // Stop the server threads
    Iterator iter = server_threads.iterator();
    while (iter.hasNext()) {
      ServerThread t = (ServerThread) iter.next();
      MEMEToolkit.logComment("STARTING SERVER THREAD - " +
                             t.getClass().getName(), true);
      t.start();
    }

    // Start the listener
    listener.start();

  }

  /**
   * Writes version info for known packages.
   */
  protected void logVersions() {
    MEMEToolkit.logComment("VERSION = " +
                           gov.nih.nlm.meme.Version.getVersionInformation(), true);
  }

  /**
   * Writes environment info for known variables.
   */
  protected void logEnvironment() {
    MEMEToolkit.logComment(
        MEMEConstants.PROPERTIES_FILE + " = " +
        MEMEToolkit.getProperty(MEMEConstants.PROPERTIES_FILE), true);
    MEMEToolkit.logComment(
        ServerConstants.MID_SERVICE + " = " +
        MEMEToolkit.getProperty(ServerConstants.MID_SERVICE), true);
    MEMEToolkit.logComment(
        ServerConstants.MID_CONNECTION + " = " +
        MEMEToolkit.getProperty(ServerConstants.MID_CONNECTION), true);
    MEMEToolkit.logComment(
        ServerConstants.MID_USER + " = " +
        MEMEToolkit.getProperty(ServerConstants.MID_USER), true);
    MEMEToolkit.logComment(
        ServerConstants.MEME_SERVER_PORT + " = " +
        MEMEToolkit.getProperty(ServerConstants.MEME_SERVER_PORT), true);
    MEMEToolkit.logComment(
        MEMEConstants.DEBUG + " = " +
        MEMEToolkit.getProperty(MEMEConstants.DEBUG), true);
    MEMEToolkit.logComment(
        MEMEConstants.VIEW + " = " +
        MEMEToolkit.getProperty(MEMEConstants.VIEW), true);
    MEMEToolkit.logComment(
        MEMEConstants.DTD_DIRECTORY + " = " +
        MEMEToolkit.getProperty(MEMEConstants.DTD_DIRECTORY), true);
    MEMEToolkit.logComment(
        MEMEConstants.TMP_DIRECTORY + " = " +
        MEMEToolkit.getProperty(MEMEConstants.TMP_DIRECTORY), true);
    MEMEToolkit.logComment(
        MEMEConstants.MEME_HOME + " = " +
        MEMEToolkit.getProperty(MEMEConstants.MEME_HOME), true);
    MEMEToolkit.logComment(
        MEMEConstants.ORACLE_HOME + " = " +
        MEMEToolkit.getProperty(MEMEConstants.ORACLE_HOME), true);
  }

  /**
   * Shuts down the listener to ensure that no new connection can enter
   * the system.  It really should wait until any processes still running are
       * finished.  Waiting until things are all finished will require waiting until
   * the thread pool has only inactive threads.  We currently do not have a
   * mechanism for this, so instead, we will have a place holder for it which
   * will be a call to {@link ServerToolkit#waitUntilThreadPoolInactive()}.
   */
  public void stop() {

    // Stop the listener
    listener.stop();

    // Stop the server threads
    Iterator iter = server_threads.iterator();
    while (iter.hasNext()) {
      ServerThread t = (ServerThread) iter.next();
      MEMEToolkit.logComment("STOPPING SERVER THREAD - " +
                             t.getClass().getName(), true);
      t.stop();
    }

    // Block until running processes are finished.
    // This only works if all threads that we use come from the thread pool
    ServerToolkit.waitUntilThreadPoolInactive();

    MEMEToolkit.trace("MEMEApplicationServer.stop() - Server stop.");

    MEMEToolkit.logComment("SERVER STOP", true);

  }

  /**
   * Entry point, starts server.
   * @param s command line args
   */
  public static void main(String[] s) {
    // Create and start server
    MEMEApplicationServer server = new MEMEApplicationServer();
    server.start();
  }

}
