/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  EditingClient
 *
 * 02/08/2006 RBE (1-763IU): method name changed from removeCheckToOverrideVector()
 * 													 to removeCheckFromOverrideVector() 
 * 
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.Activity;
import gov.nih.nlm.meme.action.AtomicAction;
import gov.nih.nlm.meme.action.BatchMolecularTransaction;
import gov.nih.nlm.meme.action.MacroMolecularAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.AtomChecklist;
import gov.nih.nlm.meme.common.AtomWorklist;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Checklist;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.common.ConceptWorklist;
import gov.nih.nlm.meme.common.ContextRelationship;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.MetaCode;
import gov.nih.nlm.meme.common.MetaProperty;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.PasswordAuthentication;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.common.Worklist;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.meme.integrity.IntegrityVector;
import gov.nih.nlm.meme.server.ServerConstants;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.Date;

/**
 * This client is a super-client that combines the functionality of a
 * number other clients including:
 * <ul>
 *   <li>{@link ActionClient}</li>
 *   <li>{@link AuxiliaryDataClient}</li>
 *   <li>{@link CoreDataClient}</li>
 *   <li>{@link FinderClient}</li>
 *   <li>{@link WorklistClient}</li>
 * </ul>
 *
 * This client API is intended to have enough functionality
 * to fully support an interactive editing environment.  Each
 * editing session will make use of a single {@link EditingClient}
 * which creates and manages all of the necessary {@link Authentication}
 * and session issues that the server requires.
 *
 * With the properties properly configured, accessing auxiliary data
 * services is as simple as instantiating class and
 * calling its methods.  There are only a small number of methods
 * that are managed directly by this class, all others are forwarded
 * to private instances of the sub-APIs.
 *
 * @author MEME Group
 */

public class EditingClient extends ClientAPI {

  //
  // Fields
  //

  private String mid_service = null;
  private String user = null;
  private Authentication auth = null;

  private ActionClient action_client;
  private FinderClient finder_client;
  private CoreDataClient core_data_client;
  private AuxiliaryDataClient auxiliary_data_client;
  private WorklistClient worklist_client;

  /**
   * Allows setHost and setPort methods to update all client
   * request handlers instead of just this one
   */
  private MEMERequestClient request_handler = new MEMERequestClient() {
    /**
     * Sets the host of all sub-clients.
     * @param host the specified host
     */
    public void setHost(String host) {
      EditingClient.super.getRequestHandler().setHost(host);
      action_client.getRequestHandler().setHost(host);
      finder_client.getRequestHandler().setHost(host);
      core_data_client.getRequestHandler().setHost(host);
      auxiliary_data_client.getRequestHandler().setHost(host);
      worklist_client.getRequestHandler().setHost(host);
    }

    /**
     * Sets the port of all sub-clients.
     * @param port the specified port
     */
    public void setPort(int port) {
      EditingClient.super.getRequestHandler().setPort(port);
      action_client.getRequestHandler().setPort(port);
      finder_client.getRequestHandler().setPort(port);
      core_data_client.getRequestHandler().setPort(port);
      auxiliary_data_client.getRequestHandler().setPort(port);
      worklist_client.getRequestHandler().setPort(port);
    }

    /**
     * Adds the specified {@link ClientProgressListener}.
     * @param l the {@link ClientProgressListener}
     */
    public void addClientProgressListener(ClientProgressListener l) {
      EditingClient.super.getRequestHandler().addClientProgressListener(l);
    }

    /**
     * Removes the specified {@link ClientProgressListener}.
     * @param l the {@link ClientProgressListener}
     */
    public void removeClientProgressListener(ClientProgressListener l) {
      EditingClient.super.getRequestHandler().removeClientProgressListener(l);
    }

    /**
     * Processes the request.
     * @param request the {@link MEMEServiceRequest}
     * @return the {@link MEMEServiceRequest} response
     */
    public MEMEServiceRequest processRequest(MEMEServiceRequest request) {
      try {
        return EditingClient.super.getRequestHandler().processRequest(request);
      } catch (MEMEException ex) {
        return null;
      }
    }
  };

  //
  // Constructors
  //

  /**
   * Instantiates the {@link EditingClient} connected to the specified MID
   * service using the specified username and password.
   * @param mid_service a valid MID service name
   * @param user the user name
   * @param password the password
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated
   */
  public EditingClient(String mid_service, String user, String password) throws
      MEMEException {
    this(mid_service, user, password, null, -1);
  }

  /**
   * Instantiates the {@link EditingClient} connected to the specified MID
   * service using the specified username and password.
   * @param mid_service a valid MID service name
   * @param user the user name
   * @param password the password
   * @param host the host
   * @param port the port
   * @throws MEMEException if the required properties are not set,
   *         or if the protocol handler cannot be instantiated
   */
  public EditingClient(String mid_service, String user, String password,
                       String host, int port) throws
      MEMEException {
    super();

    // Instantiate all client APIs
    action_client = new ActionClient();
    core_data_client = new CoreDataClient();
    finder_client = new FinderClient();
    auxiliary_data_client = new AuxiliaryDataClient();
    worklist_client = new WorklistClient(mid_service);

    if (host != null) {
      request_handler.setHost(host);
    }
    if (port > 0) {
      request_handler.setPort(port);

    }
    initiateSession();
    setMidService(mid_service);

    // Validate user authentication
    if (user != null && password != null) {
      this.user = user;
      setAuthentication(new PasswordAuthentication(user, password.toCharArray()));
    } else {
      throw new MEMEException("User or password must not be null.");
    }
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
   * Sets the mid service.  Additionally, this method sets the
   * mid service of all sub-APIs.
   * @param mid_service the mid service
   */
  public void setMidService(String mid_service) {
    this.mid_service = mid_service;
    action_client.setMidService(mid_service);
    core_data_client.setMidService(mid_service);
    finder_client.setMidService(mid_service);
    auxiliary_data_client.setMidService(mid_service);
    //auxiliary_data_client.refreshCache();
    worklist_client.setMidService(mid_service);
  }

  /**
   * Returns the {@link MEMEServiceRequest}. This needs to be here
   * because we make use of two admin services but do not implement
   * the full {@link gov.nih.nlm.meme.server.AdminService} API.
   * @return the {@link MEMEServiceRequest}
   */
  protected MEMEServiceRequest getServiceRequest() {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("AdminService");
    request.setMidService(mid_service);
    request.setAuthentication(auth);

    if (getSessionId() == null) {
      request.setNoSession(true);
    } else {
      request.setSessionId(getSessionId());

    }
    return request;
  }

  /**
   * Sets the {@link Authentication}.  Additionally, this method
   * sets the {@link Authentication} for the sub-APIs.
   * @param auth the {@link Authentication}
   */
  public void setAuthentication(Authentication auth) {
    this.auth = auth;
    action_client.setAuthentication(auth);
    core_data_client.setAuthentication(auth);
    finder_client.setAuthentication(auth);
    auxiliary_data_client.setAuthentication(auth);
    worklist_client.setAuthentication(auth);
  }

  /**
   * Sets the session id.  Additionally, this method
   * set the session id for the sub-APIs.
   * @param session_id the session id
   */
  public void setSessionId(String session_id) {
    super.setSessionId(session_id);
    action_client.setSessionId(session_id);
    core_data_client.setSessionId(session_id);
    finder_client.setSessionId(session_id);
    auxiliary_data_client.setSessionId(session_id);
    worklist_client.setSessionId(session_id);
  }

  /**
   * Returns the {@link EditorPreferences} corresponding to the user
   * that this session connecting to the server with.
   * @return the {@link EditorPreferences}
   * @throws MEMEException if failed to get client editor preferences
   */
  public EditorPreferences getClientEditorPreferences() throws MEMEException {
    return getEditorPreferencesByUsername(user != null ? user :
                                          MEMEToolkit.getProperty(
        ServerConstants.MID_USER));
  }

  /**
   * Determines whether or not editing is enabled. This code is borrowed
   * from {@link gov.nih.nlm.meme.server.AdminService}.
   * @return <code>true</code> if editing system is enabled;
   *         <code>false</code> otherwise.
   * @throws MEMEException if failed to determine editing system status
   */
  public boolean isEditingEnabled() throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default(
        "function", "is_editing_enabled"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    setSessionId(request.getSessionId());

    // Process response
    return request.getReturnValue("is_editing_enabled").getBoolean();
  }

  /**
   * Determines whether or not integrity system is enabled. This method
   * is borrowed from {@link gov.nih.nlm.meme.server.AdminService}.
   * @return <code>true</code> if integrity system is enabled;
   *         <code>false</code> otherwise.
   * @throws MEMEException if failed to determine integrity system status
   */
  public boolean isIntegritySystemEnabled() throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default(
        "function", "is_integrity_enabled"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    setSessionId(request.getSessionId());

    // Process response
    return request.getReturnValue("is_integrity_enabled").getBoolean();
  }

  //
  // ActionClient Methods
  //

  /*
   * Methods from ActionClient which are not implemented:
   *   ActionClient.setAuthority(Authority);
   *   ActionClient.getAuthority();
   */

  /**
   * Applies changes to the specified {@link EnforcableIntegrityVector}.
   * @param integrity_vector the {@link EnforcableIntegrityVector}
   */
  public void setIntegrityVector(EnforcableIntegrityVector integrity_vector) {
    action_client.setIntegrityVector(integrity_vector);
  }

  /**
   * Returns the {@link ActionClient#getIntegrityVector()} used for actions.
   * @return the {@link EnforcableIntegrityVector}
   */
  public EnforcableIntegrityVector getIntegrityVector() {
    return action_client.getIntegrityVector();
  }

  /**
   * Sets the "change status" flag for executing actions.
   * @param status <code>true</code> if actions should change
   *        the concept status, <code>false</code> otherwise
   */
  public void setChangeStatus(boolean status) {
    action_client.setChangeStatus(status);
  }

  /**
   * Returns the value of the "change status" flag.
   * @return a value of the "change status" flag
   */
  public boolean getChangeStatus() {
    return action_client.getChangeStatus();
  }

  /**
   * Sets the transaction {@link Identifier} used to perform actions.
   * @param transaction_id the transaction {@link Identifier} used to perform actions
   */
  public void setTransactionIdentifier(Identifier transaction_id) {
    action_client.setTransactionIdentifier(transaction_id);
  }

  /**
   * Returns the {@link MolecularTransaction}.
   * @return the {@link MolecularTransaction}
   */
  public MolecularTransaction getTransaction() {
    return action_client.getTransaction();
  }

  /**
   * Sets the work {@link Identifier} used to perform actions.
   * @param work_id the work {@link Identifier} used to perform actions
   */
  public void setWorkIdentifier(Identifier work_id) {
    action_client.setWorkIdentifier(work_id);
  }

  /**
   * Returns the {@link WorkLog}.
   * @return the {@link WorkLog}
   */
  public WorkLog getWorkLog() {
    return action_client.getWorkLog();
  }

  /**
   * Sets the {@link Authority}.
   * @param auth the {@link Authority}
   */
  public void setAuthority(Authority auth) {
    action_client.setAuthority(auth);
  }

  /**
   * Performs the specified action.
   * @param action a {@link MolecularAction}
   * @throws MEMEException if failed to process action
   */
  public void processAction(MolecularAction action) throws MEMEException {
    action_client.processAction(action);
  }

  /**
   * Performs the specified action.
   * @param transaction a {@link BatchMolecularTransaction}
   * @throws MEMEException if failed to process action
   */
  public void processAction(BatchMolecularTransaction transaction) throws
      MEMEException {
    action_client.processAction(transaction);
  }

  /**
   * Performs the specified action.
   * @param action a {@link MacroMolecularAction}
   * @throws MEMEException if failed to process action
   */
  public void processAction(MacroMolecularAction action) throws MEMEException {
    action_client.processAction(action);
  }

  /**
   * Performs undo of the specified action.
   * @param action a {@link MolecularAction}
   * @throws MEMEException if failed to process undo action
   */
  public void processUndo(MolecularAction action) throws MEMEException {
    action_client.processUndo(action);
  }

  /**
   * Performs undo of the specified action.
   * @param transaction a {@link BatchMolecularTransaction}
   * @throws MEMEException if failed to process undo action
   */
  public void processUndo(BatchMolecularTransaction transaction) throws
      MEMEException {
    action_client.processUndo(transaction);
  }

  /**
   * Performs undo of the specified action.
   * @param action a {@link MacroMolecularAction}
   * @throws MEMEException if failed to process undo action
   */
  public void processUndo(MacroMolecularAction action) throws MEMEException {
    action_client.processUndo(action);
  }

  /**
   * Performs redo of the specified action.
   * @param action a {@link MolecularAction}
   * @throws MEMEException if failed to process redo action
   */
  public void processRedo(MolecularAction action) throws MEMEException {
    action_client.processRedo(action);
  }

  /**
   * Performs redo of the specified action.
   * @param transaction a {@link BatchMolecularTransaction}
   * @throws MEMEException if failed to process redo action
   */
  public void processRedo(BatchMolecularTransaction transaction) throws
      MEMEException {
    action_client.processRedo(transaction);
  }

  /**
   * Performs redo of the specified action.
   * @param action a {@link MacroMolecularAction}
   * @throws MEMEException if failed to process redo action
   */
  public void processRedo(MacroMolecularAction action) throws MEMEException {
    action_client.processRedo(action);
  }

  //
  // CoreData Methods
  //

  /*
   * Methods from CoreDataClient which are not implemented:
   *   CoreDataClient.getServiceRequest();
   *   CoreDataClient.getSessionId();
   */

  /**
   * Returns a refreshed copy of the  specified {@link Concept}.
   * @param concept the {@link Concept}
   * @return a refreshed copy of the  specified {@link Concept}
   * @throws MEMEException if failed to process get concept
   */
  public Concept getConcept(Concept concept) throws MEMEException {
    return core_data_client.getConcept(concept);
  }

  /**
   * Executes {@link CoreDataClient#getConcept(Identifier)}.
   * @param identifier An object {@link Identifier}
   * @return object {@link Concept}
   * @throws MEMEException if failed to process get concept
   */
  public Concept getConcept(Identifier identifier) throws MEMEException {
    return core_data_client.getConcept(identifier);
  }

  /**
   * Executes {@link CoreDataClient#getConcept(int)}.
   * @param concept_id An <code>int</code> representation of concept id
   * @return object {@link Concept}
   * @throws MEMEException if failed to process get concept
   */
  public Concept getConcept(int concept_id) throws MEMEException {
    return core_data_client.getConcept(concept_id);
  }

  /**
   * Executes {@link CoreDataClient#getConcept(CUI)}.
   * @param cui An object {@link CUI}
   * @return object {@link Concept}
   * @throws MEMEException if failed to process get concept
   */
  public Concept getConcept(CUI cui) throws MEMEException {
    return core_data_client.getConcept(cui);
  }

  /**
   * Executes {@link CoreDataClient#getAtom(Atom)}.
   * @param atom An object {@link Atom}
   * @return object {@link Atom}
   * @throws MEMEException if failed to process get atom
   */
  public Atom getAtom(Atom atom) throws MEMEException {
    return core_data_client.getAtom(atom);
  }

  /**
   * Executes {@link CoreDataClient#getAtom(Identifier)}.
   * @param identifier An object {@link Identifier}
   * @return object {@link Atom}
   * @throws MEMEException if failed to process get atom
   */
  public Atom getAtom(Identifier identifier) throws MEMEException {
    return core_data_client.getAtom(identifier);
  }

  /**
   * Executes {@link CoreDataClient#getAtom(int)}.
   * @param atom_id An <code>int</code> representation of atom id
   * @return object {@link Atom}
   * @throws MEMEException if failed to process get atom
   */
  public Atom getAtom(int atom_id) throws MEMEException {
    return core_data_client.getAtom(atom_id);
  }

  /**
   * Returns the {@link Atom}s for the specified {@link Concept}.
   * @param c the {@link Concept}
   * @return the {@link Atom}s for the specified {@link Concept}
   * @throws MEMEException if failed to process get atoms
   * @throws MissingDataException if failed due to missing data
   */
  public Atom[] getAtoms(Concept c) throws MEMEException {
    return core_data_client.getAtoms(c);
  }

  /**
   * Returns the {@link Attribute}s for the specified id.
   * @param attr_id the attribute id
   * @return the {@link Attribute}s for the specified id
   * @throws MEMEException if failed to process get attribute
   */
  public Attribute getAttribute(int attr_id) throws MEMEException {
    return core_data_client.getAttribute(attr_id);
  }

  /**
   * Returns a refreshed copy of the specified {@link Attribute}.
   * @param attr the {@link Attribute}
   * @return a refreshed copy of the specified {@link Attribute}
   * @throws MEMEException if failed to process get attribute
   * @throws MissingDataException if failed due to missing data
   */
  public Attribute getAttribute(Attribute attr) throws MEMEException {
    return core_data_client.getAttribute(attr);
  }

  /**
   * Returns the {@link Attribute}s for the specified {@link Concept}.
   * @param c the {@link Concept}
   * @return the {@link Attribute}s for the specified {@link Concept}
   * @throws MEMEException if failed to process get attributes
   * @throws MissingDataException if failed due to missing data
   */
  public Attribute[] getAttributes(Concept c) throws MEMEException {
    return core_data_client.getAttributes(c);
  }

  /**
   * Executes {@link CoreDataClient#getDeadAtom(int)}.
   * @param atom_id An <code>int</code> representation of atom id
   * @return object {@link Atom}
   * @throws MEMEException if failed to process get dead atom
   */
  public Atom getDeadAtom(int atom_id) throws MEMEException {
    return core_data_client.getDeadAtom(atom_id);
  }

  /**
   * Executes {@link CoreDataClient#getDeadAttribute(int)}.
   * @param attr_id An <code>int</code> representation of attribute id
   * @return object {@link Attribute}
   * @throws MEMEException if failed to process get dead attribute
   */
  public Attribute getDeadAttribute(int attr_id) throws MEMEException {
    return core_data_client.getDeadAttribute(attr_id);
  }

  /**
   * Executes {@link CoreDataClient#getDeadConcept(int)}.
   * @param concept_id An <code>int</code> representation of concept id
   * @return object {@link Concept}
   * @throws MEMEException if failed to process get dead concept
   */
  public Concept getDeadConcept(int concept_id) throws MEMEException {
    return core_data_client.getDeadConcept(concept_id);
  }

  /**
   * Executes {@link CoreDataClient#getDeadRelationship(int)}.
   * @param rel_id An <code>int</code> representation of relationship id
   * @return object {@link Relationship}
   * @throws MEMEException if failed to process get dead relationship
   */
  public Relationship getDeadRelationship(int rel_id) throws MEMEException {
    return core_data_client.getDeadRelationship(rel_id);
  }

  /**
   * Executes {@link CoreDataClient#getDeadContextRelationship(int)}.
   * @param rel_id An <code>int</code> representation of relationship id
   * @return object {@link ContextRelationship}
   * @throws MEMEException if failed to process get dead context relationship
   */
  public ContextRelationship getDeadContextRelationship(int rel_id) throws
      MEMEException {
    return core_data_client.getDeadContextRelationship(rel_id);
  }

  /**
   * Executes {@link CoreDataClient#getRelationships(Concept)}.
   * @param c An object {@link Concept}
   * @return An array of object {@link Relationship}
   * @throws MEMEException if failed to process get relationships
   * @throws MissingDataException if failed due to missing data
   */
  public Relationship[] getRelationships(Concept c) throws MEMEException,
      MissingDataException {
    return core_data_client.getRelationships(c);
  }

  /**
   * Executes {@link CoreDataClient#getRelationships(Concept, int, int)}.
   * @param concept An object {@link Concept}
   * @param start the start index for reading a section of data
   * @param end the end index for reading a section of data
   * @return an array of {@link Relationship}
   * @throws MEMEException if failed to process get relationships
   * @throws MissingDataException if failed due to missing data
   */
  public Relationship[] getRelationships(Concept concept, int start, int end) throws
      MEMEException, MissingDataException {
    return core_data_client.getRelationships(concept, start, end);
  }

  /**
   * Executes {@link CoreDataClient#getRelationship(int)}.
   * @param rel_id An <code>int</code> representation of relationship id
   * @return object {@link Relationship}
   * @throws MEMEException if failed to process get relationship
   */
  public Relationship getRelationship(int rel_id) throws MEMEException {
    return core_data_client.getRelationship(rel_id);
  }

  /**
   * Executes {@link CoreDataClient#getRelationship(int)}.
   * @param rel the object {@link Relationship}
   * @return An object {@link Relationship}
   * @throws MEMEException if failed to process get relationship
   * @throws MissingDataException if failed due to missing data
   */
  public Relationship getRelationship(Relationship rel) throws MEMEException {
    return core_data_client.getRelationship(rel);
  }

  /**
   * Executes {@link CoreDataClient#getInverseRelationship(int)}.
   * @param rel_id An <code>int</code> representation of relationship id
   * @return object {@link Relationship}
   * @throws MEMEException if failed to process get inverse relationship
   */
  public Relationship getInverseRelationship(int rel_id) throws MEMEException {
    return core_data_client.getInverseRelationship(rel_id);
  }

  /**
   * Executes {@link CoreDataClient#getRelationshipCount(Concept)}.
   * @param concept An object {@link Concept}
   * @return relationship count
   * @throws MEMEException if failed to get relationship count
   */
  public int getRelationshipCount(Concept concept) throws MEMEException {
    return core_data_client.getRelationshipCount(concept);
  }

  /**
   * Executes {@link CoreDataClient#getContextRelationship(int)}.
   * @param cxt_rel_id the <code>int</code> rel id
   * @return the {@link ContextRelationship} for the specified context rel id
   * @throws MEMEException if failed to process get context relationship
   * @throws MissingDataException if failed due to missing data
   */
  public ContextRelationship getContextRelationship(int cxt_rel_id) throws
      MEMEException {
    return core_data_client.getContextRelationship(cxt_rel_id);
  }

  /**
   * Executes {@link CoreDataClient#getContextRelationships(Concept)}.
   * @param c An object {@link Concept}
   * @return An array of object {@link ContextRelationship}
   * @throws MEMEException if failed to process get context relationships
   * @throws MissingDataException if failed due to missing data
   */
  public ContextRelationship[] getContextRelationships(Concept c) throws
      MEMEException {
    return core_data_client.getContextRelationships(c);
  }

  /**
       * Executes {@link CoreDataClient#getContextRelationships(Concept, int, int)}.
   * @param concept An object {@link Concept}
   * @param start the start index for reading a section of data
   * @param end the end index for reading a section of data
   * @return an array of {@link ContextRelationship}
   * @throws MEMEException if failed to process get context relationships
   * @throws MissingDataException if failed due to missing data
   */
  public ContextRelationship[] getContextRelationships(Concept concept,
      int start, int end) throws MEMEException {
    return core_data_client.getContextRelationships(concept, start, end);
  }

  /**
   * Executes {@link CoreDataClient#getContextRelationshipCount(Concept)}.
   * @param concept An object {@link Concept}
   * @return context relationship count
   * @throws MEMEException if failed to get context relationship count
   */
  public int getContextRelationshipCount(Concept concept) throws MEMEException {
    return core_data_client.getContextRelationshipCount(concept);
  }

  /**
   * Executes {@link CoreDataClient#populateRelationships(Concept)}.
   * @param concept An object {@link Concept}
   * @throws MEMEException if failed to populate relationships
   */
  public void populateRelationships(Concept concept) throws MEMEException {
    core_data_client.populateRelationships(concept);
  }

  /**
   * Executes {@link CoreDataClient#populateRelationships(Concept)}.
   * @param concept An object {@link Concept}
   * @param start the start index for reading a section of data
   * @param end the end index for reading a section of data
   * @throws MEMEException if failed to process populate relationships
   * @throws MissingDataException if failed due to missing data
   */
  public void populateRelationships(Concept concept, int start, int end) throws
      MEMEException {
    core_data_client.populateRelationships(concept, start, end);
  }

  /**
   * Executes {@link CoreDataClient#populateContextRelationships(Concept)}.
   * @param concept An object {@link Concept}
   * @throws MEMEException if failed to populate context relationships
   */
  public void populateContextRelationships(Concept concept) throws
      MEMEException {
    core_data_client.populateContextRelationships(concept);
  }

  /**
   * Executes {@link CoreDataClient#populateContextRelationships(Concept)}.
   * @param concept An object {@link Concept}
   * @param start the start index for reading a section of data
   * @param end the end index for reading a section of data
   * @throws MEMEException if failed to process populate context relationships
   * @throws MissingDataException if failed due to missing data
   */
  public void populateContextRelationships(Concept concept, int start, int end) throws
      MEMEException {
    core_data_client.populateContextRelationships(concept, start, end);
  }

  /**
   * Executes {@link CoreDataClient#getReadLanguages()}.
   * @return selected languages
   */
  public String[] getReadLanguages() {
    return core_data_client.getReadLanguages();
  }

  /**
   * Executes {@link CoreDataClient#includeOrExcludeLanguages()}.
   * @return <code>true</code> if language list is explicitly included;
   * <code>false</code> if language list is explicitly excluded
   */
  public boolean includeOrExcludeLanguages() {
    return core_data_client.includeOrExcludeLanguages();
  }

  /**
   * Executes {@link CoreDataClient#setReadLanguagesToExclude(String[])}.
   * @param lats languages to exclude
   */
  public void setReadLanguagesToExclude(String[] lats) {
    core_data_client.setReadLanguagesToExclude(lats);
  }

  /**
   * Executes {@link CoreDataClient#setReadLanguagesToInclude(String[])}.
   * @param lats languages to include
   */
  public void setReadLanguagesToInclude(String[] lats) {
    core_data_client.setReadLanguagesToInclude(lats);
  }

  //
  // FinderClient Methods
  //

  /*
   * Methods from FinderClient which are not implemented:
   *   FinderClient.getServiceRequest();
   *   FinderClient.getSessionId();
   *   FinderClient.setAuthority(Authority);
   */

  /**
   * Executes {@link FinderClient#setMaxResultCount(int)}.
       * @param max_result_count An <code>int</code> representation of max result count
   */
  public void setMaxResultCount(int max_result_count) {
    finder_client.setMaxResultCount(max_result_count);
  }

  /**
   * Executes {@link FinderClient#getMaxResultCount()}.
   * @return max result count
   */
  public int getMaxResultCount() {
    return finder_client.getMaxResultCount();
  }

  /**
   * Executes {@link FinderClient#setRecursive(boolean)}.
   * @param recursive a flag indicating whether or not molecular action
   *        searches should be recursive
   */
  public void setRecursive(boolean recursive) {
    finder_client.setRecursive(recursive);
  }

  /**
   * Executes {@link FinderClient#getRecursive()}.
   * @return <code>true</code> if molecular action searches should be recursive
   *         <code>false</code> otherwise
   */
  public boolean getRecursive() {
    return finder_client.getRecursive();
  }

  /**
   * Executes {@link FinderClient#restrictBySemanticType(SemanticType)}.
   * @param sty An object {@link SemanticType}
   */
  public void restrictBySemanticType(SemanticType sty) {
    finder_client.restrictBySemanticType(sty);
  }

  /**
   * Executes {@link FinderClient#restrictBySemanticTypes(SemanticType[])}.
   * @param stys An array of object {@link SemanticType}
   */
  public void restrictBySemanticTypes(SemanticType[] stys) {
    finder_client.restrictBySemanticTypes(stys);
  }

  /**
   * Executes {@link FinderClient#restrictBySource(Source)}.
   * @param source An object {@link Source}
   */
  public void restrictBySource(Source source) {
    finder_client.restrictBySource(source);
  }

  /**
   * Executes {@link FinderClient#restrictBySources(Source[])}.
   * @param sources An array of object {@link Source}
   */
  public void restrictBySources(Source[] sources) {
    finder_client.restrictBySources(sources);
  }

  /**
   * Executes {@link FinderClient#restrictByReleasable()}.
   */
  public void restrictByReleasable() {
    finder_client.restrictByReleasable();
  }

  /**
   * Executes {@link FinderClient#restrictByChemicalSemanticType()}.
   */
  public void restrictByChemicalSemanticType() {
    finder_client.restrictByChemicalSemanticType();
  }

  /**
   * Executes {@link FinderClient#restrictByNonChemicalSemanticType()}.
   */
  public void restrictByNonChemicalSemanticType() {
    finder_client.restrictByNonChemicalSemanticType();
  }

  /**
   * Executes {@link FinderClient#restrictByConcept(Concept)}.
   * @param concept the {@link Concept} whose {@link Identifier}
   *        should be used to restrict the search
   */
  public void restrictByConcept(Concept concept) {
    finder_client.restrictByConcept(concept);
  }

  /**
   * Executes {@link FinderClient#restrictByWorklist(Worklist)}.
   * @param worklist An object {@link Worklist}
   */
  public void restrictByWorklist(Worklist worklist) {
    finder_client.restrictByWorklist(worklist);
  }

  /**
   * Executes {@link FinderClient#restrictByCoreDataType(Class)}.
   * @param c an object {@link Class}
   */
  public void restrictByCoreDataType(Class c) {
    finder_client.restrictByCoreDataType(c);
  }

  /**
   * Executes {@link FinderClient#restrictByTransaction(MolecularTransaction)}.
   * @param transaction the {@link MolecularTransaction}
   */
  public void restrictByTransaction(MolecularTransaction transaction) {
    finder_client.restrictByTransaction(transaction);
  }

  /**
   * Executes {@link FinderClient#restrictByActionType(MolecularAction)}.
   * @param molecular_action the {@link MolecularAction}
   */
  public void restrictByActionType(MolecularAction molecular_action) {
    finder_client.restrictByActionType(molecular_action);
  }

  /**
   * Executes {@link FinderClient#restrictByDateRange(Date, Date)}.
   * @param start_date the start {@link Date}
   * @param end_date the end {@link Date}
   */
  public void restrictByDateRange(Date start_date, Date end_date) {
    finder_client.restrictByDateRange(start_date, end_date);
  }

  /**
   * Executes {@link FinderClient#clearRestrictions()}.
   */
  public void clearRestrictions() {
    finder_client.clearRestrictions();
  }

  /**
   * Executes {@link FinderClient#findMolecularActions()}.
   * @return a {@link MolecularAction}<code>[]</code>
   * @throws MEMEException if failed to perform lookup
   */
  public MolecularAction[] findMolecularActions() throws MEMEException {
    return finder_client.findMolecularActions();
  }

  /**
   * Executes {@link FinderClient#findConceptsByCode(Code)}.
   * @param code An object {@link Code}
   * @return a {@link Concept}<code>[]</code> of matching concepts
   * @throws MEMEException if failed to perform lookup
   */
  public Concept[] findConceptsByCode(Code code) throws MEMEException {
    return finder_client.findConceptsByCode(code);
  }

  /**
   * Executes {@link FinderClient#findExactStringMatches(String)}.
   * @param string An object {@link String} representation of string
   * to be used to find for matching concepts
   * @return An array of object {@link Concept}
   * @throws MEMEException if failed to process find exact string
   */
  public Concept[] findExactStringMatches(String string) throws MEMEException {
    return finder_client.findExactStringMatches(string);
  }

  /**
   * Executes {@link FinderClient#findNormalizedStringMatches(String)}.
   * @param string An object {@link String} representation of normalized
   * string to be used to find for matching concepts
   * @return An array of object {@link Concept}
   * @throws MEMEException if failed to perform find normalized string
   */
  public Concept[] findNormalizedStringMatches(String string) throws
      MEMEException {
    return finder_client.findNormalizedStringMatches(string);
  }

  /**
   * Executes {@link FinderClient#findAllWordMatches(String[])}.
   * @param words An object {@link String} representation of words
   * to be used to find for matching concepts
   * @return An array of object {@link Concept}
   * @throws MEMEException if failed to perform find all word
   */
  public Concept[] findAllWordMatches(String[] words) throws MEMEException {
    return finder_client.findAllWordMatches(words);
  }

  /**
   * Executes {@link FinderClient#findAllNormalizedWordMatches(String[])}.
   * @param norm_words An object {@link String} representation of norm words
   * to be used to find for matching concepts
   * @return An array of object {@link Concept}
   * @throws MEMEException if failed to perform find all norm word
   */
  public Concept[] findAllNormalizedWordMatches(String[] norm_words) throws
      MEMEException {
    return finder_client.findAllNormalizedWordMatches(norm_words);
  }

  /**
   * Executes {@link FinderClient#findAnyWordMatches(String[])}.
   * @param words An object {@link String} representation of words
   * to be used to find for matching concepts
   * @return An array of object {@link Concept}
   * @throws MEMEException if failed to perform find any word
   */
  public Concept[] findAnyWordMatches(String[] words) throws MEMEException {
    return finder_client.findAnyWordMatches(words);
  }

  /**
   * Executes {@link FinderClient#findAnyNormalizedWordMatches(String[])}.
   * @param norm_words An object {@link String} representation of norm words
   * to be used to find for matching concepts
   * @return An array of object {@link Concept}
   * @throws MEMEException if failed to perform find any norm word
   */
  public Concept[] findAnyNormalizedWordMatches(String[] norm_words) throws
      MEMEException {
    return finder_client.findAnyNormalizedWordMatches(norm_words);
  }

  //
  // AuxiliaryDataClient Methods
  //

  /*
   * Methods from AuxiliaryDataClient which are not implemented:
   *   AuxiliaryDataClient.getServiceRequest();
   *   AuxiliaryDataClient.getSessionId();
   */

  /**
   * Executes {@link AuxiliaryDataClient#clearCache()}.
   */
  public void clearCache() {
    auxiliary_data_client.clearCache();
  }

  /**
   * Executes {@link AuxiliaryDataClient#refreshCache()}.
   */
  public void refreshCache() {
    auxiliary_data_client.refreshCache();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getSources()}.
   * @return An array of object {@link Source}
   * @throws MEMEException if failed to get sources
   */
  public Source[] getSources() throws MEMEException {
    return auxiliary_data_client.getSources();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getSourceAbbreviations()}.
       * @return An array of object {@link String} containing the source abbreviations
   * @throws MEMEException if failed to get source abbreviations
   */
  public String[] getSourceAbbreviations() throws MEMEException {
    return auxiliary_data_client.getSourceAbbreviations();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getSource(String)}.
   * @param sab An object {@link String} representation of source abbreviation
   * @return An object {@link Source}
   * @throws MEMEException if failed to get source
   */
  public Source getSource(String sab) throws MEMEException {
    return auxiliary_data_client.getSource(sab);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getCurrentSources()}.
   * @return An array of object {@link Source}
   * @throws MEMEException if failed to get current sources
   */
  public Source[] getCurrentSources() throws MEMEException {
    return auxiliary_data_client.getCurrentSources();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getCurrentSource(String)}.
   * @param stripped_sab An object {@link String} representation of stripped
   * source abbreviation
   * @return An object {@link Source}
   * @throws MEMEException if failed to get current source
   */
  public Source getCurrentSource(String stripped_sab) throws MEMEException {
    return auxiliary_data_client.getCurrentSource(stripped_sab);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getPreviousSource(String)}.
   * @param stripped_sab An object {@link String} representation of stripped
   * source abbreviation
   * @return An object {@link Source}
   * @throws MEMEException if failed to get previous source
   */
  public Source getPreviousSource(String stripped_sab) throws MEMEException {
    return auxiliary_data_client.getPreviousSource(stripped_sab);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getCurrentEnglishSources()}.
   * @return An array of object {@link Source}
   * @throws MEMEException if failed to get current english source
   */
  public Source[] getCurrentEnglishSources() throws MEMEException {
    return auxiliary_data_client.getCurrentEnglishSources();
  }

  /**
   * Adds the specified {@link Source}.
   * @param source the {@link Source}
   * @throws MEMEException if failed to add source
   */
  public void addSource(Source source) throws MEMEException {
    auxiliary_data_client.addSource(source);
  }

  /**
   * Adds the specified {@link Source}s.
   * @param sources the specified {@link Source}s
   * @throws MEMEException if failed to add sources
   */
  public void addSources(Source[] sources) throws MEMEException {
    auxiliary_data_client.addSources(sources);
  }

  /**
   * Executes {@link AuxiliaryDataClient#setSource(Source)}.
   * @param source An object {@link Source}
   * @throws MEMEException if failed to set the source object
   */
  public void setSource(Source source) throws MEMEException {
    auxiliary_data_client.setSource(source);
  }

  /**
   * Executes {@link AuxiliaryDataClient#removeSource(Source)}.
   * @param source an object {@link Source}
   * @throws MEMEException if failed to remove source
   */
  public void removeSource(Source source) throws MEMEException {
    auxiliary_data_client.removeSource(source);
  }

  /**
   * Executes {@link AuxiliaryDataClient#addTermgroup(Termgroup)}.
   * @param termgroup an object {@link Termgroup}
   * @throws MEMEException if failed to add termgroup
   */
  public void addTermgroup(Termgroup termgroup) throws MEMEException {
    auxiliary_data_client.addTermgroup(termgroup);
  }

  /**
   * Executes {@link AuxiliaryDataClient#removeTermgroup(Termgroup)}.
   * @param termgroup an object {@link Termgroup}
   * @throws MEMEException if failed to remove termgroup
   */
  public void removeTermgroup(Termgroup termgroup) throws MEMEException {
    auxiliary_data_client.removeTermgroup(termgroup);
  }

  /**
   * Executes {@link AuxiliaryDataClient#addTermgroups(Termgroup[])}.
   * @param termgroups an array of object {@link Termgroup}
   * @throws MEMEException if failed to add termgroups
   */
  public void addTermgroups(Termgroup[] termgroups) throws MEMEException {
    auxiliary_data_client.addTermgroups(termgroups);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getTermgroups()}.
   * @return An array of object {@link Termgroup}
   * @throws MEMEException if failed to get termgroups
   */
  public Termgroup[] getTermgroups() throws MEMEException {
    return auxiliary_data_client.getTermgroups();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getTermgroupsAsStrings()}.
   * @return An array of object {@link String} containing the termgroups
   * @throws MEMEException if failed to get termgroups as strings
   */
  public String[] getTermgroupsAsStrings() throws MEMEException {
    return auxiliary_data_client.getTermgroupsAsStrings();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getTermgroup(String)}.
   * @param sab_tty An object {@link String} representation of source/tty
   * @return An object {@link Termgroup}
   * @throws MEMEException if failed to get termgroup
   */
  public Termgroup getTermgroup(String sab_tty) throws MEMEException {
    return auxiliary_data_client.getTermgroup(sab_tty);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getTermgroup(String, String)}.
   * @param sab An object {@link String} representation of source abbreviation
   * @param tty An object {@link String} representation of termtype
   * @return An object {@link Termgroup}
   * @throws MEMEException if failed to get termgroup
   */
  public Termgroup getTermgroup(String sab, String tty) throws MEMEException {
    return auxiliary_data_client.getTermgroup(sab, tty);
  }

  /**
   * Executes {@link AuxiliaryDataClient#addLanguage(Language)}.
   * @param language an object {@link Language}
   * @throws MEMEException if failed to add language
   */
  public void addLanguage(Language language) throws MEMEException {
    auxiliary_data_client.addLanguage(language);
  }

  /**
   * Executes {@link AuxiliaryDataClient#removeLanguage(Language)}.
   * @param language an object {@link Language}
   * @throws MEMEException if failed to remove language
   */
  public void removeLanguage(Language language) throws MEMEException {
    auxiliary_data_client.removeLanguage(language);
  }

  /**
   * Executes {@link AuxiliaryDataClient#setLanguage(Language)}.
   * @param language an object {@link Language}
   * @throws MEMEException if failed to set language
   */
  public void setLanguage(Language language) throws MEMEException {
    auxiliary_data_client.setLanguage(language);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getLanguage(String)}.
   * @param lat the lat
   * @return an object {@link Language}
   * @throws MEMEException if failed to get language
   */
  public Language getLanguage(String lat) throws MEMEException {
    return auxiliary_data_client.getLanguage(lat);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getLanguages()}.
   * @return An array of object {@link Language}
   * @throws MEMEException if failed to get languages
   */
  public Language[] getLanguages() throws MEMEException {
    return auxiliary_data_client.getLanguages();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getMaxIdentifierForType(Class)}.
   * @param c object {@link Class}
   * @return An object {@link Identifier}
   * @throws MEMEException if failed to get max id
   */
  public Identifier getMaxIdentifierForType(Class c) throws MEMEException {
    return auxiliary_data_client.getMaxIdentifierForType(c);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getNextIdentifierForType(Class)}.
   * @param c object {@link Class}
   * @return An object {@link Identifier}
   * @throws MEMEException if failed to get next id
   */
  public Identifier getNextIdentifierForType(Class c) throws MEMEException {
    return auxiliary_data_client.getNextIdentifierForType(c);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getWorkLog(int)}.
   * @param work_id An <code>int</code> representation of work id
   * @return An object {@link WorkLog}
   * @throws MEMEException if failed to load meme work
   */
  public WorkLog getWorkLog(int work_id) throws MEMEException {
    return auxiliary_data_client.getWorkLog(work_id);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getWorkLogs()}.
   * @return An array of object {@link WorkLog}
   * @throws MEMEException if failed to load meme work
   */
  public WorkLog[] getWorkLogs() throws MEMEException {
    return auxiliary_data_client.getWorkLogs();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getActivityLog(MolecularTransaction)}.
   * @param transaction the {@link MolecularTransaction}
   * @return the activity for the specified transaction
   * @throws MEMEException if failed to load activity
   */
  public Activity getActivityLog(MolecularTransaction transaction) throws
      MEMEException {
    return auxiliary_data_client.getActivityLog(transaction);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getActivityLogs(WorkLog)}.
   * @param work the {@link WorkLog}
   * @return An array of object {@link Activity}
   * @throws MEMEException if failed to load activity
   */
  public Activity[] getActivityLogs(WorkLog work) throws MEMEException {
    return auxiliary_data_client.getActivityLogs(work);
  }

  /**
   * Executes {@link AuxiliaryDataClient#addMetaCode(MetaCode)}.
   * @param mcode an object {@link MetaCode}
   * @throws MEMEException if failed to add meta code
   */
  public void addMetaCode(MetaCode mcode) throws MEMEException {
    auxiliary_data_client.addMetaCode(mcode);
  }

  /**
   * Executes {@link AuxiliaryDataClient#removeMetaCode(MetaCode)}.
   * @param mcode an object {@link MetaCode}
   * @throws MEMEException if failed to remove meta code
   */
  public void removeMetaCode(MetaCode mcode) throws MEMEException {
    auxiliary_data_client.removeMetaCode(mcode);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getMetaCode(String, String)}.
   * @param code the meta code code
   * @param type the meta code type
   * @return an object {@link MetaCode}
   * @throws MEMEException if failed to get meta code
   */
  public MetaCode getMetaCode(String code, String type) throws MEMEException {
    return auxiliary_data_client.getMetaCode(code, type);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getMetaCodes()}.
   * @return an array {@link MetaCode}
   * @throws MEMEException if failed to get meta code
   */
  public MetaCode[] getMetaCodes() throws MEMEException {
    return auxiliary_data_client.getMetaCodes();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getMetaCodesByType(String)}.
   * @param type the meta code type
   * @return an array meta code
   * @throws MEMEException if failed to get meta codes
   */
  public MetaCode[] getMetaCodesByType(String type) throws MEMEException {
    return auxiliary_data_client.getMetaCodesByType(type);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getMetaCodeTypes()}.
   * @return an array meta code type
   * @throws MEMEException if failed to get meta code types
   */
  public String[] getMetaCodeTypes() throws MEMEException {
    return auxiliary_data_client.getMetaCodeTypes();
  }

  /**
   * Executes {@link AuxiliaryDataClient#addMetaProperty(MetaProperty)}.
   * @param meta_prop an object {@link MetaProperty}
   * @throws MEMEException if failed to add meta property
   */
  public void addMetaProperty(MetaProperty meta_prop) throws MEMEException {
    auxiliary_data_client.addMetaProperty(meta_prop);
  }

  /**
   * Executes {@link AuxiliaryDataClient#removeMetaProperty(MetaProperty)}.
   * @param meta_prop an object {@link MetaProperty}
   * @throws MEMEException if failed to remove meta property
   */
  public void removeMetaProperty(MetaProperty meta_prop) throws MEMEException {
    auxiliary_data_client.removeMetaProperty(meta_prop);
  }

  /**
       * Executes {@link AuxiliaryDataClient#getMetaProperty(String, String, String)}.
   * @param key the key
   * @param key_qualifier the key qualifier
   * @param value the value
   * @return a {@link MetaProperty}
   * @throws MEMEException if failed to get meta property
   */
  public MetaProperty getMetaProperty(String key, String key_qualifier,
                                      String value, String description) throws MEMEException {//naveen UMLS-60 added description parameter to getMetaProperty method
    return auxiliary_data_client.getMetaProperty(key, key_qualifier, value, description);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getMetaProperties()}.
   * @return an array {@link MetaProperty}
   * @throws MEMEException if failed to get meta properties
   */
  public MetaProperty[] getMetaProperties() throws MEMEException {
    return auxiliary_data_client.getMetaProperties();
  }

  /**
   * Returns the {@link MetaProperty} objects associated with the
   * specified key qualifier.
   * @param key_qualifier the key qualifier
   * @return the {@link MetaProperty} objects
   * @throws MEMEException if failed to get meta properties by key qualifier
   */
  public MetaProperty[] getMetaPropertiesByKeyQualifier(String key_qualifier) throws
      MEMEException {
    return auxiliary_data_client.getMetaPropertiesByKeyQualifier(key_qualifier);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getMetaPropertyKeyQualifiers()}.
   * @return a list of meta property key qualifiers
   * @throws MEMEException if failed to get meta property key qualifiers
   */
  public String[] getMetaPropertyKeyQualifiers() throws MEMEException {
    return auxiliary_data_client.getMetaPropertyKeyQualifiers();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getCodeByValue(String, String)}.
   * @param type An object {@link String} representation of type
   * @param value An object {@link String} representation of value
   * @return An object {@link String} representation of code from code map
   * @throws MEMEException if failed to load code map
   */
  public String getCodeByValue(String type, String value) throws MEMEException {
    return auxiliary_data_client.getCodeByValue(type, value);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getValueByCode(String, String)}.
   * @param type An object {@link String} representation of type
   * @param code An object {@link String} representation of code
   * @return An object {@link String} representation of value from code map
   * @throws MEMEException if failed to load code map
   */
  public String getValueByCode(String type, String code) throws MEMEException {
    return auxiliary_data_client.getValueByCode(type, code);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getValidStatusValuesForType(Class)}.
   * @param c An object {@link Class} representation of valid type
   * @return An array of <code>char</code> representation of valid status
   * @throws MEMEException if failed to get valid status
   */
  public char[] getValidStatusValuesForType(Class c) throws MEMEException {
    return auxiliary_data_client.getValidStatusValuesForType(c);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getValidStatusValuesForAtoms()}.
   * @return An array of <code>char</code> representation of valid status
   * @throws MEMEException if failed to get valid status
   */
  public char[] getValidStatusValuesForAtoms() throws MEMEException {
    return auxiliary_data_client.getValidStatusValuesForAtoms();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getValidStatusValuesForAttributes()}.
   * @return An array of <code>char</code> representation of valid status
   * @throws MEMEException if failed to get valid status
   */
  public char[] getValidStatusValuesForAttributes() throws MEMEException {
    return auxiliary_data_client.getValidStatusValuesForAttributes();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getValidStatusValuesForConcepts()}.
   * @return An array of <code>char</code> representation of valid status
   * @throws MEMEException if failed to get valid status
   */
  public char[] getValidStatusValuesForConcepts() throws MEMEException {
    return auxiliary_data_client.getValidStatusValuesForConcepts();
  }

  /**
       * Executes {@link AuxiliaryDataClient#getValidStatusValuesForRelationships()}.
   * @return An array of <code>char</code> representation of valid status
   * @throws MEMEException if failed to get valid status
   */
  public char[] getValidStatusValuesForRelationships() throws MEMEException {
    return auxiliary_data_client.getValidStatusValuesForRelationships();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getValidLevelValuesForType(Class)}.
   * @param c An object {@link Class} representation of valid type
   * @return An array of <code>char</code> representation of valid level
   * @throws MEMEException if failed to get valid level
   */
  public char[] getValidLevelValuesForType(Class c) throws MEMEException {
    return auxiliary_data_client.getValidLevelValuesForType(c);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getValidLevelValuesForAttributes()}.
   * @return An array of <code>char</code> representation of valid level
   * @throws MEMEException if failed to get valid level
   */
  public char[] getValidLevelValuesForAttributes() throws MEMEException {
    return auxiliary_data_client.getValidLevelValuesForAttributes();
  }

  /**
       * Executes {@link AuxiliaryDataClient#getValidLevelValuesForRelationships()}.
   * @return An array of <code>char</code> representation of valid level
   * @throws MEMEException if failed to get valid level
   */
  public char[] getValidLevelValuesForRelationships() throws MEMEException {
    return auxiliary_data_client.getValidLevelValuesForRelationships();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getValidReleasedValues()}.
   * @return An array of <code>char</code> representation of valid released
   * @throws MEMEException if failed to get valid released values
   */
  public char[] getValidReleasedValues() throws MEMEException {
    return auxiliary_data_client.getValidReleasedValues();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getValidTobereleasedValues()}.
   * @return An array of <code>char</code> representation of valid tobereleased
   * @throws MEMEException if failed to get valid tobereleased values
   */
  public char[] getValidTobereleasedValues() throws MEMEException {
    return auxiliary_data_client.getValidTobereleasedValues();
  }

  /**
       * Executes {@link AuxiliaryDataClient#addEditorPreferences(EditorPreferences)}.
   * @param ep An object {@link EditorPreferences}
   * @throws MEMEException if failed to add editor preferences
   */
  public void addEditorPreferences(EditorPreferences ep) throws MEMEException {
    auxiliary_data_client.addEditorPreferences(ep);
  }

  /**
   * Executes {@link AuxiliaryDataClient#removeEditorPreferences(EditorPreferences)}.
   * @param ep An object {@link EditorPreferences}
   * @throws MEMEException if failed to remove editor preferences
   */
  public void removeEditorPreferences(EditorPreferences ep) throws
      MEMEException {
    auxiliary_data_client.removeEditorPreferences(ep);
  }

  /**
       * Executes {@link AuxiliaryDataClient#setEditorPreferences(EditorPreferences)}.
   * @param ep An object {@link EditorPreferences}
   * @throws MEMEException if failed to set editor preferences
   */
  public void setEditorPreferences(EditorPreferences ep) throws MEMEException {
    auxiliary_data_client.setEditorPreferences(ep);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getEditorPreferences()}.
   * @return An array of object {@link EditorPreferences}
   * @throws MEMEException if failed to get editor preferences
   */
  public EditorPreferences[] getEditorPreferences() throws MEMEException {
    return auxiliary_data_client.getEditorPreferences();
  }

  /**
       * Executes {@link AuxiliaryDataClient#getEditorPreferencesByUsername(String)}.
   * @param username A {@link String} representation of editor's name
   * @return The {@link EditorPreferences} representing the editor
   * @throws MEMEException if failed to get editor preferences
   */
  public EditorPreferences getEditorPreferencesByUsername(String username) throws
      MEMEException {
    return auxiliary_data_client.getEditorPreferencesByUsername(username);
  }

  /**
       * Executes {@link AuxiliaryDataClient#getEditorPreferencesByInitials(String)}.
   * @param initials A {@link String} representation of editor's initials
   * @return The {@link EditorPreferences} representing the editor
   * @throws MEMEException if failed to get editor preferences
   */
  public EditorPreferences getEditorPreferencesByInitials(String initials) throws
      MEMEException {
    return auxiliary_data_client.getEditorPreferencesByInitials(initials);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getAuthority(String)}.
   * @param auth An object {@link String} representation of authority
   * @return An object {@link Authority}
   * @throws MEMEException if failed to get authority
   */
  public Authority getAuthority(String auth) throws MEMEException {
    return auxiliary_data_client.getAuthority(auth);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getMolecularAction(int)}.
   * @param molecule_id An <code>int</code> representation of molecule id
   * @return An object {@link MolecularAction}
   * @throws MEMEException if failed to get molecular action
   */
  public MolecularAction getMolecularAction(int molecule_id) throws
      MEMEException {
    return auxiliary_data_client.getMolecularAction(molecule_id);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getFullMolecularAction(int)}.
   * @param molecule_id An <code>int</code> representation of molecule id
   * @return An object {@link MolecularAction}
   * @throws MEMEException if failed to get full molecular action
   */
  public MolecularAction getFullMolecularAction(int molecule_id) throws
      MEMEException {
    return auxiliary_data_client.getFullMolecularAction(molecule_id);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getAtomicAction(int)}.
       * @param atomic_action_id An <code>int</code> representation of atomic action id
   * @return An object {@link AtomicAction}
   * @throws MEMEException if failed to get atomic action
   */
  public AtomicAction getAtomicAction(int atomic_action_id) throws
      MEMEException {
    return auxiliary_data_client.getAtomicAction(atomic_action_id);
  }

  /**
   * Executes {@link AuxiliaryDataClient#addApplicationVector(String, IntegrityVector)}.
       * @param application An object {@link String} representation of ic application
   * @param iv An object {@link IntegrityVector}
   * @throws MEMEException if failed to add application vector
   */
  public void addApplicationVector(String application, IntegrityVector iv) throws
      MEMEException {
    auxiliary_data_client.addApplicationVector(application, iv);
  }

  /**
   * Executes {@link AuxiliaryDataClient#setApplicationVector(String, IntegrityVector)}.
       * @param application An object {@link String} representation of ic application
   * @param iv An object {@link IntegrityVector}
   * @throws MEMEException if failed to set application vector
   */
  public void setApplicationVector(String application, IntegrityVector iv) throws
      MEMEException {
    auxiliary_data_client.setApplicationVector(application, iv);
  }

  /**
   * Executes {@link AuxiliaryDataClient#removeApplicationVector(String)}.
       * @param application An object {@link String} representation of ic application
   * @throws MEMEException if failed to remove application vector
   */
  public void removeApplicationVector(String application) throws MEMEException {
    auxiliary_data_client.removeApplicationVector(application);
  }

  /**
       * Removes the specified {@link IntegrityCheck} from the specified application's
   * vector.
   * @param application the applicaiton
   * @param ic the {@link IntegrityCheck}
   * @throws MEMEException if failed to get application vector
   */
  public void removeCheckFromApplicationVector(String application,
                                               IntegrityCheck ic) throws
      MEMEException {
    auxiliary_data_client.removeCheckFromApplicationVector(application, ic);
  }

  /**
   * Executes {@link AuxiliaryDataClient#addCheckToApplicationVector(String, IntegrityCheck, String)}.
       * @param application An object {@link String} representation of ic application.
   * @param ic An object {@link IntegrityCheck}.
   * @param code An object {@link String} representation of code.
   * @throws MEMEException if failed to add application vector
   */
  public void addCheckToApplicationVector(String application, IntegrityCheck ic,
                                          String code) throws MEMEException {
    auxiliary_data_client.addCheckToApplicationVector(application, ic, code);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getApplicationVector(String)}.
   * @param app An object {@link String} representation of application
   * @return An object {@link EnforcableIntegrityVector}
   * @throws MEMEException if failed to get application vector
   */
  public EnforcableIntegrityVector getApplicationVector(String app) throws
      MEMEException {
    return auxiliary_data_client.getApplicationVector(app);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getApplicationsWithVectors()}.
   * @return A list of applications with vectors
   * @throws MEMEException if failed to get applications with vector
   */
  public String[] getApplicationsWithVectors() throws MEMEException {
    return auxiliary_data_client.getApplicationsWithVectors();
  }

  /**
       * Executes {@link AuxiliaryDataClient#addOverrideVector(int, IntegrityVector)}.
   * @param ic_level An <code>int</code> representation of ic level
   * @param iv An object {@link IntegrityVector}
   * @throws MEMEException if failed to add application vector
   */
  public void addOverrideVector(int ic_level, IntegrityVector iv) throws
      MEMEException {
    auxiliary_data_client.addOverrideVector(ic_level, iv);
  }

  /**
       * Executes {@link AuxiliaryDataClient#setOverrideVector(int, IntegrityVector)}.
   * @param ic_level An <code>int</code> representation of ic level
   * @param iv An object {@link IntegrityVector}
   * @throws MEMEException if failed to set application vector
   */
  public void setOverrideVector(int ic_level, IntegrityVector iv) throws
      MEMEException {
    auxiliary_data_client.setOverrideVector(ic_level, iv);
  }

  /**
   * Executes {@link AuxiliaryDataClient#removeOverrideVector(int)}.
   * @param ic_level An <code>int</code> representation of ic level
   * @throws MEMEException if failed to remove application vector
   */
  public void removeOverrideVector(int ic_level) throws MEMEException {
    auxiliary_data_client.removeOverrideVector(ic_level);
  }

  /**
   * Removes the specified {@link IntegrityCheck} from the override
   * vector indicated by the specified editor level.
   * @param editor_level the editor level
   * @param ic the {@link IntegrityCheck}
   * @throws MEMEException if failed to get application vector
   */
  public void removeCheckFromOverrideVector(int editor_level, IntegrityCheck ic) throws
      MEMEException {
    auxiliary_data_client.removeCheckFromOverrideVector(editor_level, ic);
  }

  /**
   * Executes {@link AuxiliaryDataClient#removeOverrideVector(int)}.
   * @param ic_level An <code>int</code> representation of ic level
   * @param ic An object {@link IntegrityCheck}
   * @param code the code
   * @throws MEMEException if failed to add application vector
   */
  public void addCheckToOverrideVector(int ic_level, IntegrityCheck ic,
                                       String code) throws MEMEException {
    auxiliary_data_client.addCheckToOverrideVector(ic_level, ic, code);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getOverrideVector(int)}.
   * @param editor_level An <code>int</code> representation of editor level
   * @return An object {@link IntegrityVector}
   * @throws MEMEException if failed to get override vector
   */
  public IntegrityVector getOverrideVector(int editor_level) throws
      MEMEException {
    return auxiliary_data_client.getOverrideVector(editor_level);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getLevelsWithOverrideVectors()}.
   * @return A list of levels with override vectors
   * @throws MEMEException if failed to get levels with override vector
   */
  public int[] getLevelsWithOverrideVectors() throws MEMEException {
    return auxiliary_data_client.getLevelsWithOverrideVectors();
  }

  /**
   * Executes {@link AuxiliaryDataClient#addIntegrityCheck(IntegrityCheck)}.
   * @param ic an object {@link IntegrityCheck}
   * @throws MEMEException if failed to add integrity check
   */
  public void addIntegrityCheck(IntegrityCheck ic) throws MEMEException {
    auxiliary_data_client.addIntegrityCheck(ic);
  }

  /**
   * Executes {@link AuxiliaryDataClient#setIntegrityCheck(IntegrityCheck)}.
   * @param ic an object {@link IntegrityCheck}
   * @throws MEMEException if failed to set integrity check
   */
  public void setIntegrityCheck(IntegrityCheck ic) throws MEMEException {
    auxiliary_data_client.setIntegrityCheck(ic);
  }

  /**
   * Executes {@link AuxiliaryDataClient#removeIntegrityCheck(IntegrityCheck)}.
   * @param ic an object {@link IntegrityCheck}
   * @throws MEMEException if failed to remove integrity check
   */
  public void removeIntegrityCheck(IntegrityCheck ic) throws MEMEException {
    auxiliary_data_client.removeIntegrityCheck(ic);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getIntegrityCheck(String)}.
   * @param ic_name An object {@link String} representation of ic name
   * @return An object {@link IntegrityCheck}
   * @throws MEMEException if failed to get integrity check
   */
  public IntegrityCheck getIntegrityCheck(String ic_name) throws MEMEException {
    return auxiliary_data_client.getIntegrityCheck(ic_name);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getIntegrityChecks()}.
   * @return the {@link IntegrityCheck} for the specified name
   * @throws MEMEException if failed to get integrity checks
   */
  public IntegrityCheck[] getIntegrityChecks() throws MEMEException {
    return auxiliary_data_client.getIntegrityChecks();
  }

  /**
       * Executes {@link AuxiliaryDataClient#activateIntegrityCheck(IntegrityCheck)}.
   * @param ic an object {@link IntegrityCheck}
   * @throws MEMEException if failed to activate integrity check
   */
  public void activateIntegrityCheck(IntegrityCheck ic) throws MEMEException {
    auxiliary_data_client.activateIntegrityCheck(ic);
  }

  /**
       * Executes {@link AuxiliaryDataClient#activateIntegrityCheck(IntegrityCheck)}.
   * @param ic an object {@link IntegrityCheck}
   * @throws MEMEException if failed to deactivate integrity check
   */
  public void deactivateIntegrityCheck(IntegrityCheck ic) throws MEMEException {
    auxiliary_data_client.deactivateIntegrityCheck(ic);
  }

  /**
   * Executes {@link AuxiliaryDataClient#getValidRelationshipNames()}.
   * @return An array of object {@link String} representation of list of valid
   * relationship names
   * @throws MEMEException if failed to get valid relationship names
   */
  public String[] getValidRelationshipNames() throws MEMEException {
    return auxiliary_data_client.getValidRelationshipNames();
  }

  /**
   * Executes {@link AuxiliaryDataClient#getValidRelationshipAttributes()}.
   * @return An array of object {@link String} representation of list of valid
   * relationship attributes
   * @throws MEMEException if failed to get valid relationship attributes
   */
  public String[] getValidRelationshipAttributes() throws MEMEException {
    return auxiliary_data_client.getValidRelationshipAttributes();
  }

  /**
   * Executes {@link AuxiliaryDataClient#addValidSemanticType(SemanticType)}.
   * @param sty an object {@link SemanticType}
   * @throws MEMEException if failed to add semantic type
   */
  public void addValidSemanticType(SemanticType sty) throws MEMEException {
    auxiliary_data_client.addValidSemanticType(sty);
  }

  /**
       * Executes {@link AuxiliaryDataClient#removeValidSemanticType(SemanticType)}.
   * @param sty an object {@link SemanticType}
   * @throws MEMEException if failed to remove semantic type
   */
  public void removeValidSemanticType(SemanticType sty) throws MEMEException {
    auxiliary_data_client.removeValidSemanticType(sty);
  }

  /**
   * Returns the valid {@link SemanticType}s.
   * @return the valid {@link SemanticType}s
   * @throws MEMEException if failed to get semantic types
   */
  public SemanticType[] getValidSemanticTypes() throws MEMEException {
    return auxiliary_data_client.getValidSemanticTypes();
  }

  /**
   * Returns the valid semantic type values.
   * @return the valid semantic type values
   * @throws MEMEException if failed to get semantic type values
   */
  public String[] getValidSemanticTypeValues() throws MEMEException {
    return auxiliary_data_client.getValidSemanticTypeValues();
  }

  //
  // WorklistClient Methods
  //

  /*
   * Methods from WorklistClient which are not implemented:
   *   WorklistClient.getServiceRequest();
   *   WorklistClient.getSessionId();
   */

  /**
   * Executes {@link WorklistClient#getCurrentWorklists()}.
   * @return An array of object {@link Worklist}
   * @throws MEMEException if failed to get current worklists
   */
  public Worklist[] getCurrentWorklists() throws MEMEException {
    return worklist_client.getCurrentWorklists();
  }

  /**
   * Executes {@link WorklistClient#getWorklists()}.
   * @return An array of object {@link Worklist}
   * @throws MEMEException if failed to get worklists
   */
  public Worklist[] getWorklists() throws MEMEException {
    return worklist_client.getWorklists();
  }

  /**
   * Executes {@link WorklistClient#getWorklistNames()}.
   * @return An array of object {@link String} representation of worklist name
   * @throws MEMEException if failed to get worklist names
   */
  public String[] getWorklistNames() throws MEMEException {
    return worklist_client.getWorklistNames();
  }

  /**
   * Executes {@link WorklistClient#getChecklists()}.
   * @return An array of object {@link Checklist} representation of checklists
   * @throws MEMEException if failed to get checklists
   */
  public Checklist[] getChecklists() throws MEMEException {
    return worklist_client.getChecklists();
  }

  /**
   * Executes {@link WorklistClient#getChecklistNames()}.
   * @return An array of object {@link String} representation of checklist name
   * @throws MEMEException if failed to get checklist names
   */
  public String[] getChecklistNames() throws MEMEException {
    return worklist_client.getChecklistNames();
  }

  /**
   * Executes {@link WorklistClient#getWorklistAndChecklistNames()}.
   * @return An a sorted list of object {@link String} representation of worklist and checklist name
   * @throws MEMEException if failed to get worklist and checklist names
   */
  public String[] getWorklistAndChecklistNames() throws MEMEException {
    return worklist_client.getWorklistAndChecklistNames();
  }

  /**
   * Executes {@link WorklistClient#getAtomWorklist(String)}.
   * @param name worklist name
   * @return An object {@link AtomWorklist}
   * @throws MEMEException if failed to get atom worklist
   */
  public AtomWorklist getAtomWorklist(String name) throws MEMEException {
    return worklist_client.getAtomWorklist(name);
  }

  /**
   * Executes {@link WorklistClient#getAtomChecklist(String)}.
   * @param name checklist name
   * @return An object {@link AtomChecklist}
   * @throws MEMEException if failed to get atom checklist
   */
  public AtomChecklist getAtomChecklist(String name) throws MEMEException {
    return worklist_client.getAtomChecklist(name);
  }

  /**
   * Executes {@link WorklistClient#getConceptWorklist(String)}.
   * @param name worklist name
   * @return An object {@link ConceptWorklist}
   * @throws MEMEException if failed to get concept worklist
   */
  public ConceptWorklist getConceptWorklist(String name) throws MEMEException {
    return worklist_client.getConceptWorklist(name);
  }

  /**
   * Executes {@link WorklistClient#getConceptChecklist(String)}.
   * @param name checklist name
   * @return An object {@link ConceptChecklist}
   * @throws MEMEException if failed to get concept checklist
   */
  public ConceptChecklist getConceptChecklist(String name) throws MEMEException {
    return worklist_client.getConceptChecklist(name);
  }

  /**
   * Executes {@link WorklistClient#addAtomWorklist(AtomWorklist)}.
   * @param worklist An object {@link AtomWorklist}
   * @throws MEMEException if failed to add atom worklist
   */
  public void addAtomWorklist(AtomWorklist worklist) throws MEMEException {
    worklist_client.addAtomWorklist(worklist);
  }

  /**
   * Executes {@link WorklistClient#addConceptWorklist(ConceptWorklist)}.
   * @param worklist An object {@link ConceptWorklist}
   * @throws MEMEException if failed to add concept worklist
   */
  public void addConceptWorklist(ConceptWorklist worklist) throws MEMEException {
    worklist_client.addConceptWorklist(worklist);
  }

  /**
   * Executes {@link WorklistClient#addAtomChecklist(AtomChecklist)}.
   * @param checklist An object {@link AtomChecklist}
   * @throws MEMEException if failed to add atom checklist
   */
  public void addAtomChecklist(AtomChecklist checklist) throws MEMEException {
    worklist_client.addAtomChecklist(checklist);
  }

  /**
   * Executes {@link WorklistClient#addConceptChecklist(ConceptChecklist)}.
   * @param checklist An object {@link ConceptChecklist}
   * @throws MEMEException if failed to add concept checklist
   */
  public void addConceptChecklist(ConceptChecklist checklist) throws
      MEMEException {
    worklist_client.addConceptChecklist(checklist);
  }

  /**
   * Executes {@link WorklistClient#worklistExists(String)}.
       * @param worklist_name An object {@link String} representation of worklist name
   * @return <code>true</code> if worklist exist; otherwise <code>false</code>
   * @throws MEMEException if failed to determine worklist existence
   */
  public boolean worklistExists(String worklist_name) throws MEMEException {
    return worklist_client.worklistExists(worklist_name);
  }

  /**
   * Executes {@link WorklistClient#checklistExists(String)}.
   * @param checklist_name An object {@link String} representation of checklist name
   * @return <code>true</code> if checklist exist; otherwise <code>false</code>
   * @throws MEMEException if failed to determine checklist existence
   */
  public boolean checklistExists(String checklist_name) throws MEMEException {
    return worklist_client.checklistExists(checklist_name);
  }

  /**
   * Executes {@link WorklistClient#removeWorklist(String)}
       * @param worklist_name An object {@link String} representation of worklist name
   * @throws MEMEException if failed to remove worklist
   */
  public void removeWorklist(String worklist_name) throws MEMEException {
    worklist_client.removeWorklist(worklist_name);
  }

  /**
   * Executes {@link WorklistClient#removeChecklist(String)}
   * @param checklist_name An object {@link String} representation of checklist name
   * @throws MEMEException if failed to remove checklist
   */
  public void removeChecklist(String checklist_name) throws MEMEException {
    worklist_client.removeChecklist(checklist_name);
  }

  /**
   * Executes {@link WorklistClient#stampWorklist(String, Authority)}
       * @param worklist_name An object {@link String} representation of worklist name
   * @param auth An object {@link Authority}
   * @return An object {@link MolecularTransaction}
   * @throws MEMEException if failed to stamp worklist
   */
  public MolecularTransaction stampWorklist(String worklist_name,
                                            Authority auth) throws
      MEMEException {
    return worklist_client.stampWorklist(worklist_name, auth);
  }

  /**
   * Returns the {@link MEMERequestClient} that will be handling
   * all server requests
   * @return the {@link MEMERequestClient} that will be handling
   * all server requests
   */
  public MEMERequestClient getRequestHandler() {
    return request_handler;
  }

}