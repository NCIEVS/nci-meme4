/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  MaintenanceClient
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.util.Date;

/**
 * This client API is used to preform MID Maintenance operations.
 * See {@link ClientAPI} for information
 * on configuring properties required by this class.
 *
 * With the properties properly configured, accessing maintenance data
 * services is as simple as instantiating class and
 * calling its methods.  For example,
 *
 * <pre>
 *   // Instantiate client
 *   // connected to default data source ("editing-db")
 *   MaintenanceClient client = new MaintenanceClient();
 *
 *   // Assign cuis
 *   client.assignCuis();
 *
 * @author MEME Group
 */
public class MaintenanceClient extends ClientAPI {

  //
  // Fields
  //
  private String mid_service = null;
  private String session_id = null;
  private Authentication auth = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MaintenanceClient} connected to the
   * default MID service.
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated
   */
  public MaintenanceClient() throws MEMEException {
    this("editing-db");
  }

  /**
   * Instantiates an {@link MaintenanceClient} connected to the specified
   * MID service.
   * @param service A service name.
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated.
   */
  public MaintenanceClient(String service) throws MEMEException {
    super();
    this.mid_service = service;
  }

  //
  // Methods
  //

  /**
   * Returns the {@link MEMEServiceRequest}.
   * @return the {@link MEMEServiceRequest}
   */
  protected MEMEServiceRequest getServiceRequest() {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MaintenanceService");
    request.setMidService(mid_service);
    request.setAuthentication(auth);

    if (session_id == null) {
      request.setNoSession(true);
    } else {
      request.setSessionId(session_id);

    }
    return request;
  }

  /**
   * Sets the mid service.
   * @param mid_service a valid MID service name
   */
  public void setMidService(String mid_service) {
    if (!mid_service.equals(this.mid_service)) {
      this.mid_service = mid_service;
    }
  }

  /**
   * Returns the mid service.
   * @return a valid MID service name
   */
  public String getMidService() {
    return mid_service;
  }

  /**
   * Sets the {@link Authentication}.
   * @param auth the {@link Authentication}
   */
  public void setAuthentication(Authentication auth) {
    this.auth = auth;
  }

  /**
   * Sets the session id.
   * @param session_id the session id
   */
  public void setSessionId(String session_id) {
    this.session_id = session_id;
  }

  /**
   * Returns the session id.
   * @return the session id
   */
  public String getSessionId() {
    return session_id;
  }

  //
  // Accessing the data
  //

  /**
   * Assigns all CUIs (<B>SERVER CALL</b>).
   * @return the cui assignment log
   * @throws MEMEException if anything goes wrong
   */
  public String assignCuis() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "assign_cuis"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("log").getValue();

  }

  /**
   * Assign all CUIs using the specified {@link WorkLog} (<B>SERVER CALL</b>).
   * The opertation will be logged with the work id from the {@link WorkLog}.
   * @param work the specified {@link WorkLog}
   * @return the cui assignment log
   * @throws MEMEException if anything goes wrong
   */
  public String assignCuis(WorkLog work) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "assign_cuis"));
    request.addParameter(new Parameter.Default("work_id",
                                               work.getIdentifier().intValue()));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("log").getValue();

  }

  /**
   * Assign CUIs for the specified {@link Concept} (<B>SERVER CALL</B>).
   * Can be used after an action is konwn to have changed a {@link Concept}.
   * @param source the {@link Concept}
   * @throws MEMEException if anything goes wrong
   */
  public void assignCuis(Concept source) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "assign_cuis_with_source"));
    request.addParameter(new Parameter.Default("source", source));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Assign CUIs for the specified {@link Concept}s (<B>SERVER CALL</B>).
   * Can be used after an action is known to have changed a pair of concepts,
   * for example a "merge" or "split" action.
   * @param source the source concept {@link Concept}
   * @param target the target concept{@link Concept}
   * @throws MEMEException if anything goes wrong
   */
  public void assignCuis(Concept source, Concept target) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
        "assign_cuis_with_source_and_target"));
    request.addParameter(new Parameter.Default("source", source));
    request.addParameter(new Parameter.Default("target", target));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Executes the specified query and logs the operation (<B>SERVER CALL</B>).
   * This method can be used to arbitrarily change the database in a logged
   * way that can be synched with MRD.
   * @param query the query to execute
   * @throws MEMEException if anything goes wrong
   */
  public void executeQuery(String query) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "execute_query"));
    request.addParameter(new Parameter.Default("query", query));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Executes the specified query and logs the operation (<B>SERVER CALL</B>).
   * This method can be used to arbitrarily change the database in a logged
   * way that can be synched with MRD. This method supports the ability to
   * undo the query later with an inverse query.
   * @param query the query to execute
   * @param inverse_query the query to execute to undo the first query
   * @throws MEMEException if anything goes wrong
   */
  public void executeQuery(String query, String inverse_query) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "execute_query_with_inverse"));
    request.addParameter(new Parameter.Default("query", query));
    request.addParameter(new Parameter.Default("inverse_query", inverse_query));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
       * Requests that the server log the contents of a table as a "load table action"
   * (<B>SERVER CALL</B>).
   * This is a way of logging the loading of an ad-hoc table.  The table should
   * be created and populated, and then this method should be called. It will
   * guarantee that actions logging the creation/loading of the table will be
   * added to the system to allow them to by synched to MRD.
   * @param table the table whose loading should be logged
   * @throws MEMEException if anything goes wrong
   */
  public void loadTable(String table) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "load_table"));
    request.addParameter(new Parameter.Default("table", table));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Executes the specified command with the specified environment and
   * logs the call (<B>SERVER CALL</B>).  Allows a generic script call
   * to be made, logged, and synched with MRD.
   * @param command the command
   * @param env the environment (e.g. { "MEME_HOME=/d5/MEME4","ORACLE+HOME=x"}
   * @throws MEMEException if anything goes wrong
   */
  public void exec(String[] command, String[] env) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "exec_1"));
    request.addParameter(new Parameter.Default("command", command));
    request.addParameter(new Parameter.Default("env", env));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Executes the specified command with the specified environment and
   * logs the call and the ability to undo(<B>SERVER CALL</B>).
   * Allows a generic script call to be made, logged, and synched with MRD.
   * The inverse command allows this action to be undone by inovking that
   * inverse command.
   * @param command the command
   * @param inverse_command the command that undoes the first command
   * @param env the environment (e.g. { "MEME_HOME=/d5/MEME4","ORACLE+HOME=x"}
   * @throws MEMEException if anything goes wrong
   */
  public void exec(String[] command, String[] inverse_command, String[] env) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "exec_2"));
    request.addParameter(new Parameter.Default("command", command));
    request.addParameter(new Parameter.Default("inverse_command",
                                               inverse_command));
    request.addParameter(new Parameter.Default("env", env));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Logs the operation (<B>SERVER CALL).
   * @param authority the authority
   * @param activity the activity
   * @param detail the detail
   * @param transaction the {@link MolecularTransaction}
   * @param work the {@link WorkLog}
   * @param elapsed_time the elapsed time
   * @throws MEMEException if anything goes wrong
   */
  public void logOperation(Authority authority, String activity,
                           String detail, MolecularTransaction transaction,
                           WorkLog work, int elapsed_time) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "log_operation"));
    request.addParameter(new Parameter.Default("authority", authority.toString()));
    request.addParameter(new Parameter.Default("activity", activity));
    request.addParameter(new Parameter.Default("detail", detail));
    request.addParameter(new Parameter.Default("transaction_id",
                                               transaction.getIdentifier().
                                               intValue()));
    request.addParameter(new Parameter.Default("work_id",
                                               work.getIdentifier().intValue()));
    request.addParameter(new Parameter.Default("elapsed_time", elapsed_time));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Logs the progress (<B>SERVER CALL).  Used to log parts of a long-running
   * operation.
   * @param authority the authority
   * @param activity the activity
   * @param detail the detail
   * @param transaction the {@link MolecularTransaction}
   * @param work the {@link WorkLog}
   * @param elapsed_time the elapsed time
   * @throws MEMEException if anything goes wrong
   */
  public void logProgress(Authority authority, String activity,
                          String detail, MolecularTransaction transaction,
                          WorkLog work, int elapsed_time) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "log_progress"));
    request.addParameter(new Parameter.Default("authority", authority.toString()));
    request.addParameter(new Parameter.Default("activity", activity));
    request.addParameter(new Parameter.Default("detail", detail));
    request.addParameter(new Parameter.Default("transaction_id",
                                               transaction.getIdentifier().
                                               intValue()));
    request.addParameter(new Parameter.Default("work_id",
                                               work.getIdentifier().intValue()));
    request.addParameter(new Parameter.Default("elapsed_time", elapsed_time));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Resets the progress (<B>SERVER CALL).  Cleans up after logging progress of
   * long-running operation.
   * @param work the {@link WorkLog}
   * @throws MEMEException if anything goes wrong
   */
  public void resetProgress(WorkLog work) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "reset_progress"));
    request.addParameter(new Parameter.Default("work_id",
                                               work.getIdentifier().intValue()
                                               ));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

  }

  /**
   * Initializes the editing matrix (<B>SERVER CALL).
   * @param work the {@link WorkLog}
   * @return the most recent initialize matrix log
   * @throws MEMEException if anything goes wrong
   */
  public String initializeMatrix(WorkLog work) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "initialize_matrix"));
    request.addParameter(new Parameter.Default("work_id",
                                               work.getIdentifier().intValue()));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("log").getValue();

  }

  //
  // Main
  //

  /**
   *
   *  DO NOT REMOVE THIS METHOD. It is used by some .pl scripts.
   *
   * @param argv command line arguments
   */
  public static void main(String[] argv) {

    //
    // Main Header
    //

    MEMEToolkit.trace("-------------------------------------------------------");
    MEMEToolkit.trace("Starting test of MaintenanceClient ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

    if (argv.length < 2) {
      System.err.println(
          "Usage: java gov.nih.nlm.meme.client.MaintenanceClient"
          + " <db> <action> <params> \n");
      System.exit(1);
    }

    try {
      MaintenanceClient client = new MaintenanceClient(argv[0]);
      client.addClientProgressListener(new ClientProgressListener() {
        public void progressUpdated(ClientProgressEvent cpe) {
          System.out.println(cpe.getMessage() + " " + new Date());
        }
      }
      );

      String log = null;
      String action = argv[1];

      if (action.equals("assign_cuis")) {
        if (argv.length > 2) {
          WorkLog work = new WorkLog(Integer.parseInt(argv[2]));
          log = client.assignCuis(work);
        } else {
          log = client.assignCuis();
        }
      } else if (action.equals("exec")) {
        String cmd = argv[2];
        String[] env = null;
        if (argv.length > 3) {
          env = FieldedStringTokenizer.split(argv[3], ",");
        }
        client.exec(new String[] {cmd}
                    , env);
      } else if (action.equals("initialize_matrix")) {
        WorkLog work = new WorkLog(Integer.parseInt(argv[2]));
        log = client.initializeMatrix(work);
      } else if (action.equals("log_operation")) {
        Authority auth = new Authority.Default(argv[2]);
        MolecularTransaction transaction =
            (argv[5].equals("0") ? null :
             new MolecularTransaction(Integer.parseInt(argv[5])));
        WorkLog work = new WorkLog(Integer.parseInt(argv[6]));
        client.logOperation(auth, argv[3], argv[4],
                            transaction,
                            work,
                            Integer.parseInt(argv[7]));
      } else if (action.equals("log_progress")) {
        Authority auth = new Authority.Default(argv[2]);
        MolecularTransaction transaction =
            (argv[5].equals("0") ? null :
             new MolecularTransaction(Integer.parseInt(argv[5])));
        WorkLog work = new WorkLog(Integer.parseInt(argv[6]));
        client.logProgress(auth, argv[3], argv[4],
                           transaction,
                           work,
                           Integer.parseInt(argv[7]));
      } else if (action.equals("reset_progress")) {
        client.resetProgress(new WorkLog(Integer.parseInt(argv[2])));

      }
      if (log != null) {
        System.out.println(log);

      }
    } catch (MEMEException me) {
      me.setFatal(true);
      MEMEToolkit.handleError(me);
    }

    //
    // Main Footer
    //

    MEMEToolkit.trace("-------------------------------------------------------");
    MEMEToolkit.trace("Finished test of MaintenanceClient ..." + new Date());
    MEMEToolkit.trace("-------------------------------------------------------");

  }

}