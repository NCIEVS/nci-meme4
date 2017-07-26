/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  WorklistClient
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.common.AtomChecklist;
import gov.nih.nlm.meme.common.AtomWorklist;
import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Checklist;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.common.ConceptWorklist;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Worklist;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.Arrays;

/**
 * This client API is used to perform worklist operations.
 * See {@link ClientAPI} for information
 * on configuring properties required by this class.
 *
 * To use this client, you will need to instantiate
 * it with the correct mid service and then simply
 * call its methods.
 *
 * Following is a sample usage.
 * <pre>
 *
 *   // Instantiate client
 *   // connected to default data source ("editing-db")
 *   WorklistClient client = new WorklistClient();
 *
 *   // Configure client (if necessary)
 *   client.setMidService(...);
 *   client.setAuthentication(...);
 *   client.setSessionId(...);
 *
 *   // Get worklist names
 *   client.getWorklistNames();
 *
 *   // Get checklist names
 *   client.getChecklistNames();
 *
 *   // Get all worklist and checklist names
 *   client.getWorklistAndChecklistNames();
 *
 *   // Get an atom checklist and worklist
 *   AtomChecklist ac_list = client.getAtomChecklist("chk_aaa")
 *   AtomWorklist aw_list = client.getAtomWorklist("wrk_aaa")
 *
 *   // Get a concept checklist and worklist
 *   ConceptChecklist cc_list = client.getConceptChecklist("chk_aaa")
 *   ConceptWorklist cw_list = client.getConceptWorklist("wrk_aaa")
 *
 *   // create or replace a checklist
 *   if (client.checklistExists(cc_list.getName()))
 *     client.removeChecklist(cc_list.getName());
 *   client.addConceptChecklist(cc_list);
 *
 *   // create or replace a worklist
 *   if (client.worklistExists(aw_list.getName()))
 *     client.removeWorklist(aw_list.getName());
 *   client.addAtomWorklist(aw_list);
 *
 * </pre>
 *
 * Currently the background implementation is tied to the
 * <code>meow.wms_worklist_info</code> and
 * <code>meow.ems_checklist_info</code> data structures.
 * As this changes with the future development of the EMS
 * and WMS, this class and its API may change.
 *
 * @author MEME Group
 */
public class WorklistClient extends ClientAPI {

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
   * Instantiates a {@link WorklistClient} connected to default mid service.
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated
   */
  public WorklistClient() throws MEMEException {
    this("editing-db");
  }

  /**
   * Instantiates a {@link WorklistClient} connected to specified mid service.
   * @param service a valid MID service
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated
   */
  public WorklistClient(String service) throws MEMEException {
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
    request.setService("WorklistService");
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
   * @param mid_service a MID service name
   */
  public void setMidService(String mid_service) {
    this.mid_service = mid_service;
  }

  /**
   * Returns the mid service.
   * @return the MID service name
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

  /**
   * Returns all current {@link Worklist}s (<B>SERVER CALL</B>).
   * @return all current {@link Worklist}s
   * @throws MEMEException if anything goes wrong
   */
  public Worklist[] getCurrentWorklists() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "current_worklists"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (Worklist[]) request.getReturnValue("current_worklists").getValue();
  }

  /**
   * Returns all {@link Worklist}s (<B>SERVER CALL</B>). These
   * are not fully populated worklists, but they contain the basic
   * metadata information.
   * @return all {@link Worklist}s
   * @throws MEMEException if anything goes wrong
   */
  public Worklist[] getWorklists() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "worklists"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (Worklist[]) request.getReturnValue("worklists").getValue();
  }

  /**
   * Returns sorted worklist names (<B>SERVER CALL</B>).
   * @return sorted worklist names
   * @throws MEMEException if anything goes wrong
   */
  public String[] getWorklistNames() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "worklist_names"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (String[]) request.getReturnValue("worklist_names").getValue();
  }

  /**
   * Returns all {@link Checklist}s (<B>SERVER CALL</B>). These
   * are not fully populated checklists, but they contain the basic
   * metadata information.
   * @return all {@link Checklist}s
   * @throws MEMEException if anything goes wrong
   */
  public Checklist[] getChecklists() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "checklists"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (Checklist[]) request.getReturnValue("checklists").getValue();
  }

  /**
   * Returns all checklist names in natural sort order (<B>SERVER CALL</B>).
   * @return all checklist names in natural sort order
   * @throws MEMEException if anything goes wrong
   */
  public String[] getChecklistNames() throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "checklist_names"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (String[]) request.getReturnValue("checklist_names").getValue();
  }

  /**
   * Returns all worklist and checklist names in natural sort order (<B>SERVER CALL</B>).
   * @return all worklist and checklist names in natural sort order
   * @throws MEMEException if anything goes wrong
   */
  public String[] getWorklistAndChecklistNames() throws MEMEException {
    String[] wn = getWorklistNames();
    String[] cn = getChecklistNames();
    String[] wcn = new String[wn.length + cn.length];

    System.arraycopy(wn, 0, wcn, 0, wn.length);
    System.arraycopy(cn, 0, wcn, wn.length, cn.length);
    Arrays.sort(wcn);

    return wcn;
  }

  /**
   * Returns the {@link AtomWorklist} matching the specified name (<B>SERVER CALL</B>).
   * @param name the worklist name
   * @return the {@link AtomWorklist} matching the specified name
   * @throws MEMEException if anything goes wrong
   */
  public AtomWorklist getAtomWorklist(String name) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "atom_worklist"));
    request.addParameter(new Parameter.Default("atom_worklist", name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (AtomWorklist) request.getReturnValue("atom_worklist").getValue();
  }

  /**
   * Returns the {@link AtomChecklist} matching the specified name (<B>SERVER CALL</B>).
   * @param name the checklist name
   * @return the {@link AtomChecklist} matching the specified name
   * @throws MEMEException if anything goes wrong
   */
  public AtomChecklist getAtomChecklist(String name) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "atom_checklist"));
    request.addParameter(new Parameter.Default("atom_checklist", name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (AtomChecklist) request.getReturnValue("atom_checklist").getValue();
  }

  /**
   * Returns the {@link ConceptWorklist} matching the specified name (<B>SERVER CALL</B>).
   * @param name the worklist name
   * @return the {@link ConceptWorklist} matching the specified name
   * @throws MEMEException if anything goes wrong
   */
  public ConceptWorklist getConceptWorklist(String name) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "concept_worklist"));
    request.addParameter(new Parameter.Default("concept_worklist", name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (ConceptWorklist)
        request.getReturnValue("concept_worklist").getValue();
  }

  /**
   * Returns the {@link ConceptChecklist} matching the specified name (<B>SERVER CALL</B>).
   * @param name the checklist name
   * @return the {@link ConceptChecklist} matching the specified name
   * @throws MEMEException if anything goes wrong
   */
  public ConceptChecklist getConceptChecklist(String name) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "concept_checklist"));
    request.addParameter(new Parameter.Default("concept_checklist", name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (ConceptChecklist)
        request.getReturnValue("concept_checklist").getValue();
  }

  /**
   * Adds the specified {@link AtomWorklist} (<B>SERVER CALL</B>).
   * This should be a fully populated worklist.
   * @param worklist the {@link AtomWorklist} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addAtomWorklist(AtomWorklist worklist) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "add_atom_worklist"));
    request.addParameter(new Parameter.Default("atom_worklist", worklist));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Adds the specified {@link ConceptWorklist} (<B>SERVER CALL</B>).
   * This should be a fully populated worklist.
   * @param worklist the {@link ConceptWorklist} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addConceptWorklist(ConceptWorklist worklist) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "add_concept_worklist"));
    request.addParameter(new Parameter.Default("concept_worklist",
                                               worklist));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Adds the specified {@link AtomChecklist} (<B>SERVER CALL</B>).
   * @param checklist the {@link AtomChecklist} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addAtomChecklist(AtomChecklist checklist) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "add_atom_checklist"));
    request.addParameter(new Parameter.Default("atom_checklist",
                                               checklist));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Adds the specified {@link ConceptChecklist} (<B>SERVER CALL</B>).
   * @param checklist the {@link ConceptChecklist} to add
   * @throws MEMEException if anything goes wrong
   */
  public void addConceptChecklist(ConceptChecklist checklist) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "add_concept_checklist"));
    request.addParameter(new Parameter.Default("concept_checklist",
                                               checklist));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Returns <code>true</code> if specified worklist exists,
   *         <code>false</code> otherwise (<B>SERVER CALL</B>).
   * @param worklist_name the worklist name
   * @return <code>true</code> if specified worklist exists,
   *         <code>false</code> otherwise
   * @throws MEMEException if anything goes wrong
   */
  public boolean worklistExists(String worklist_name) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "worklist_exist"));
    request.addParameter(new Parameter.Default("worklist_name",
                                               worklist_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return request.getReturnValue("worklist_exist").getBoolean();
  }

  /**
   * Returns <code>true</code> if specified checklist exists,
   *         <code>false</code> otherwise (<B>SERVER CALL</B>).
   * @param checklist_name the checklist name
   * @return <code>true</code> if specified checklist exists,
   *         <code>false</code> otherwise
   * @throws MEMEException if anything goes wrong
   */
  public boolean checklistExists(String checklist_name) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "checklist_exist"));
    request.addParameter(new Parameter.Default("checklist_name",
                                               checklist_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return request.getReturnValue("checklist_exist").getBoolean();
  }

  /**
   * Removes the specified worklist (<B>SERVER CALL</B>).
   * @param worklist_name the worklist name
   * @throws MEMEException if worklist could not be removed
   */
  public void removeWorklist(String worklist_name) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "remove_worklist"));
    request.addParameter(new Parameter.Default("worklist_name",
                                               worklist_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Removes the specified checklist (<B>SERVER CALL</B>).
   * @param checklist_name the checklist name
   * @throws MEMEException if checklist could not be removed
   */
  public void removeChecklist(String checklist_name) throws MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "remove_checklist"));
    request.addParameter(new Parameter.Default("checklist_name",
                                               checklist_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
       * Stamps the specified {@link Worklist} using the specified {@link Authority}
   *  (<B>SERVER CALL</B>).
   * @param worklist_name the worklist name
   * @param auth the {@link Authority}
   * @return the {@link MolecularTransaction} of the stamping operation
   * @throws MEMEException if anything goes wrong
   */
  public MolecularTransaction stampWorklist(String worklist_name,
                                            Authority auth) throws
      MEMEException {

    // Prepare request document
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
                                               "stamp_worklist"));
    request.addParameter(new Parameter.Default("auth", auth));
    request.addParameter(new Parameter.Default("worklist_name", worklist_name));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (MolecularTransaction) request.getReturnValue("transaction").
        getValue();
  }

}
