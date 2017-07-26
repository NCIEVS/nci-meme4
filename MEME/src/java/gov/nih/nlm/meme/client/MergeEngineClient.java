/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  MergeEngineClient
 *
 * Changes:
 *    03/01/2006 BAC (1-AIDWN): Main method catch clause does not wait for user to
 *      acknolwedge error to finish.  It now returns non zero return value
 *      upon merge set failure..
 *      
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.Date;

/**
 * This client API is used to process generate, load, and process sets of merge
 * facts. See {@link ClientAPI} for information on configuring properties
 * required by this class.
 * 
 * To use, just instantiate it, configure it, and then call
 * <code>processMergeSet()</code>.
 * 
 * <pre>
 * // Instantiate client
 * MergeEngineClient client = new MergeEngineClient();
 * 
 * // Process merge set
 * // This operation requires three parameters
 * // 1. A work id
 * // 2. An authority
 * // 3. A merge set name
 * int work_id = 12345;
 * 
 * Authority authority = new Authority.Default(&quot;MSH2002&quot;);
 * 
 * String merge_set = &quot;MSH2002-M1&quot;;
 * 
 * String log = client.processMergeSet(work_id, authority, merge_set);
 * 
 * </pre>
 * 
 * @author MEME Group
 */
public class MergeEngineClient extends ClientAPI {

  //
  // Fields
  //

  private String mid_service = "editing-db";

  private Authentication auth = null;

  //
  // Constructors
  //

  /**
   * Instantiate a {@link MergeEngineClient} connected to the default mid
   * service.
   * 
   * @throws MEMEException
   *           if the required properties are not set, or if the protocol
   *           handler cannot be instantiated
   */
  public MergeEngineClient() throws MEMEException {
    this("editing-db");
  }

  /**
   * Instantiate a {@link MergeEngineClient} connected to the specified mid
   * service.
   * 
   * @param mid_service
   *          a valid MID service name
   * @throws MEMEException
   *           if the required properties are not set, or if the protocol
   *           handler cannot be instantiated
   */
  public MergeEngineClient(String mid_service) throws MEMEException {
    super();
    this.mid_service = mid_service;
  }

  /**
   * Sets the mid service.
   * 
   * @param mid_service
   *          the MID service name
   */
  public void setMidService(String mid_service) {
    this.mid_service = mid_service;
  }

  /**
   * Returns the mid service.
   * 
   * @return the MID service name
   */
  public String getMidService() {
    return mid_service;
  }

  /**
   * Sets the {@link Authentication}
   * 
   * @param auth
   *          the {@link Authentication}
   */
  public void setAuthentication(Authentication auth) {
    this.auth = auth;
  }

  //
  // Methods
  //

  /**
   * Processes the specified merge set (<B>SERVER CALL</B>).
   * 
   * @param work_id
   *          an <code>int</code> work id, presumably one used elsewhere in
   *          processing a source insertion
   * @param authority
   *          the {@link Authority}
   * @param merge_set
   *          a merge set name
   * @return the merge engine log
   * @throws MEMEException
   *           if anything goes wrong
   */
  public String processMergeSet(int work_id, Authority authority,
      String merge_set) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.setService("MergeEngineService");
    request
        .addParameter(new Parameter.Default("function", "process_merge_set"));
    request.addParameter(new Parameter.Default("work_id", work_id));
    request.addParameter(new Parameter.Default("authority", authority
        .toString()));
    request.addParameter(new Parameter.Default("merge_set", merge_set));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    String log = (String) request.getReturnValue("log").getValue();

    return log;
  }

  /**
   * Returns the merge set log (<B>SERVER CALL</b>).
   * 
   * @return the merge set log
   * @throws MEMEException
   *           if anything goes wrong
   */
  public String getLog() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.setService("AdminService");
    request.addParameter(new Parameter.Default("function", "session_log"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    String log = (String) request.getReturnValue("log").getValue();
    return log;
  }

  /**
   * Returns the portion of the merge set log not yet seen (<B>SERVER CALL</b>).
   * 
   * @return the portion of the merge set log not yet seen
   * @throws MEMEException
   *           if anything goes wrong
   */
  public String getLogNotSeen() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.setService("AdminService");
    request.addParameter(new Parameter.Default("function",
        "session_log_not_seen"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // Process response
    return (String) request.getReturnValue("log").getValue();
  }

  /**
   * Returns the session progress (<B>SERVER CALL</B>).
   * 
   * @return the session progress
   * @throws MEMEException
   *           if anything goes wrong
   */
  public int getSessionProgress() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.setService("AdminService");
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
   * Returns the {@link MEMEServiceRequest}.
   * 
   * @return the {@link MEMEServiceRequest}
   */
  protected MEMEServiceRequest getServiceRequest() {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("MergeEngineService");
    request.setMidService(mid_service);
    request.setAuthentication(auth);

    if (getSessionId() == null) {
      request.setNoSession(true);
    } else {
      request.setSessionId(getSessionId());

    }
    return request;
  }

  //
  // Main
  //

  /**
   * 
   * DO NOT REMOVE THIS METHOD. It is used by merge.pl.
   * 
   * @param argv
   *          command line arguments
   */
  public static void main(String[] argv) {

    //
    // Main Header
    //

    MEMEToolkit
        .trace("-------------------------------------------------------");
    MEMEToolkit.trace("Starting test of MergeEngineClient ..." + new Date());
    MEMEToolkit
        .trace("-------------------------------------------------------");

    if (argv.length != 4) {
      System.err
          .println("Usage: java gov.nih.nlm.meme.client.MergeEngineClient"
              + " <merge set> <authority> <work_id> <db>\n");
      System.exit(1);
    }

    try {
      MergeEngineClient client = new MergeEngineClient(argv[3]);
      client.initiateSession();

      System.err.println("Session ID:    " + client.getSessionId());

      client.addClientProgressListener(new ClientProgressListener() {
        public void progressUpdated(ClientProgressEvent cpe) {
          System.out.println(cpe.getMessage() + " " + new Date());
        }
      });

      String log = client.processMergeSet(Integer.parseInt(argv[2]),
          new Authority.Default(argv[1]), argv[0]);
      System.out.println(log);

      client.terminateSession();

    } catch (MEMEException me) {
      me.setFatal(true);
      me.setInformUser(false);
      MEMEToolkit.handleError(me);
    }

    //
    // Main Footer
    //

    MEMEToolkit
        .trace("-------------------------------------------------------");
    MEMEToolkit.trace("Finished test of MergeEngineClient ..." + new Date());
    MEMEToolkit
        .trace("-------------------------------------------------------");

  }

}
