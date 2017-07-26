/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  ActionClient
 * Changes
 *   01/30/2006 RBE (1-763IU): change parameter name in getWorkLogsByType() 
 *   and getErrors()
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.Activity;
import gov.nih.nlm.meme.action.BatchMolecularTransaction;
import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.action.MEMEDataSourceAction;
import gov.nih.nlm.meme.action.MIDDataSourceAction;
import gov.nih.nlm.meme.action.MacroMolecularAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularApproveConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeAtomAction;
import gov.nih.nlm.meme.action.MolecularChangeAttributeAction;
import gov.nih.nlm.meme.action.MolecularChangeConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeRelationshipAction;
import gov.nih.nlm.meme.action.MolecularDeleteAtomAction;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.action.MolecularDeleteConceptAction;
import gov.nih.nlm.meme.action.MolecularDeleteRelationshipAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.action.MolecularMergeAction;
import gov.nih.nlm.meme.action.MolecularMoveAction;
import gov.nih.nlm.meme.action.MolecularSplitAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.common.AUI;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Authentication;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.common.LoggedError;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.StringIdentifier;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.IntegrityViolationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.StaleDataException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.Date;

/**
 * This client API is primarily used to perform molecular actions. It can also
 * be used to read a variety of actions from a MEME database as well as perform
 * batch or macro molecular actions and undo/redo a variety of logged actions.
 * 
 * See {@link ClientAPI} for information on configuring properties required by
 * this class.
 * 
 * To use this client, you will need to configure the client itself, and then
 * create a molecular action to perform and then send it to the server.
 * Typically, this client will be used in conjunction with other clients that
 * provide data access, such as the {@link AuxiliaryDataClient}.
 * 
 * Following is a sample usage:
 * 
 * <pre>
 *    //
 *    // Instantiate client
 *    // connected to default data source (&quot;editing-db&quot;)
 *    //
 *    ActionClient client = new ActionClient();
 * 
 *    //
 *    // Configure client
 *    //
 *    Identifier work_id = aux_data_client.getNextIdentifierForType(WorkLog.class);
 *    client.setWorkIdentifier(work_id);
 *    Identifier transaction_id = aux_data_client.getNextIdentifierForType(MolecularTransaction.class);
 *    client.setTransactionIdentifier(transaction_id);
 *    client.setChangeStatus(true);
 *    client.setIntegrityVector(...);
 *    client.setAuthority(...);
 * 
 *    ... elsewhere in the application ...
 * 
 *    //
 *    // Prepare an action
 *    //
 *    Concept source = ...;
 *    Concept target = ...;
 *    MolecularMergeAction action = new MolecularMergeAction(source,target);
 * 
 *    //
 *    // Performing action
 *    //
 *    try {
 *      client.processAction(action);
 *    } catch (MEMEException e) {...}
 * 
 * </pre>
 * 
 * This class requests an action to be performed. In addition to performing
 * actions, this client can undo actions previously performed, or redo actions
 * previously undone.
 * 
 * Furthermore, it maintains a log of all actions performed in a {@link WorkLog}
 * object. This object contains a {@link MolecularTransaction} object for each
 * time {@link #setTransactionIdentifier(Identifier)} is called. These logs can
 * be used to reconstruct the action sequence performed since the instantiation
 * of the client.
 * 
 * @author MEME Group
 */
public class ActionClient extends ClientAPI {

  //
  // Fields
  //
  private String mid_service = null;

  private Authentication auth = null;

  private Authority authority = null;

  private EnforcableIntegrityVector integrity_vector = new EnforcableIntegrityVector();

  private boolean status = true;

  private MolecularTransaction transaction = null;

  private WorkLog work_log = null;

  private boolean force = false;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link ActionClient} connected to the default mid service.
   * 
   * @throws MEMEException
   *           if the required properties are not set, or if the protocol
   *           handler cannot be instantiated.
   */
  public ActionClient() throws MEMEException {
    this("editing-db");
  }

  /**
   * Instantiates an {@link ActionClient} connected to the specified mid
   * service.
   * 
   * @param service
   *          A service name.
   * @throws MEMEException
   *           if the required properties are not set, or if the protocol
   *           handler cannot be instantiated.
   */
  public ActionClient(String service) throws MEMEException {
    super();
    this.mid_service = service;
    transaction = new MolecularTransaction();
    work_log = new WorkLog();
    work_log.addSubAction(transaction);
  }

  //
  // Methods
  //

  /**
   * Sets the mid service.
   * 
   * @param mid_service
   *          a valid MID service name
   */
  public void setMidService(String mid_service) {
    this.mid_service = mid_service;
  }

  /**
   * Returns the mid service.
   * 
   * @return a valid MID service name
   */
  public String getMidService() {
    return mid_service;
  }

  /**
   * Sets the {@link Authentication}.
   * 
   * @param auth
   *          the {@link Authentication}
   */
  public void setAuthentication(Authentication auth) {
    this.auth = auth;
  }

  /**
   * Sets the action {@link Authority}
   * 
   * @param authority
   *          the action {@link Authority}
   */
  public void setAuthority(Authority authority) {
    this.authority = authority;
    transaction.setAuthority(authority);
    work_log.setAuthority(authority);
  }

  /**
   * Returns the action {@link Authority}.
   * 
   * @return the action {@link Authority}
   */
  public Authority getAuthority() {
    return authority;
  }

  /**
   * Sets the flag indicating whether or not to force undo actions. Only applies
   * to molecular actions/transactions.
   * 
   * @param force
   *          boolean
   */
  public void setForce(boolean force) {
    this.force = force;
  }

  /**
   * Indicates whether or not the client is configured to force undo of
   * molecular actions/transactions.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isForce() {
    return force;
  }

  /**
   * Sets the integrity vector.
   * 
   * @param integrity_vector
   *          the {@link EnforcableIntegrityVector}
   */
  public void setIntegrityVector(EnforcableIntegrityVector integrity_vector) {
    this.integrity_vector = integrity_vector;
  }

  /**
   * Returns the integrity vector.
   * 
   * @return the {@link EnforcableIntegrityVector}
   */
  public EnforcableIntegrityVector getIntegrityVector() {
    return integrity_vector;
  }

  /**
   * Sets the "change status" flag.
   * 
   * @param status
   *          <code>true</code> if actions should change the concept status,
   *          <code>false</code> otherwise
   */
  public void setChangeStatus(boolean status) {
    this.status = status;
  }

  /**
   * Returns the "change status" flag.
   * 
   * @return the value of change status.
   */
  public boolean getChangeStatus() {
    return status;
  }

  /**
   * Sets the transaction identifier. Additionally it adds a new
   * {@link MolecularTransaction} object to the client's {@link WorkLog}.
   * 
   * @param transaction_id
   *          the new transaction {@link Identifier}
   */
  public void setTransactionIdentifier(Identifier transaction_id) {
    if (transaction.getIdentifier() == null) {
      transaction.setIdentifier(transaction_id);
      work_log.addSubAction(transaction);
    } else {
      transaction = new MolecularTransaction();
      transaction.setIdentifier(transaction_id);
      work_log.addSubAction(transaction);
    }
  }

  /**
   * Returns the {@link MolecularTransaction}
   * 
   * @return the {@link MolecularTransaction}
   */
  public MolecularTransaction getTransaction() {
    return transaction;
  }

  /**
   * Sets the work identifier. Additionally, it re-instantiates the client's
   * {@link WorkLog}.
   * 
   * @param work_id
   *          the work {@link Identifier}
   */
  public void setWorkIdentifier(Identifier work_id) {
    if (work_log.getIdentifier() == null) {
      work_log.setIdentifier(work_id);
    } else {
      work_log = new WorkLog();
      work_log.setIdentifier(work_id);
    }
  }

  /**
   * Returns the {@link WorkLog}.
   * 
   * @return the {@link WorkLog}
   */
  public WorkLog getWorkLog() {
    return work_log;
  }

  /**
   * Returns the {@link WorkLog} object for the specified <code>int</code>
   * work_id (<b>SERVER CALL</b>).
   * 
   * @param work_id
   *          a work id
   * @return the {@link WorkLog} corresponding to that work_id
   * @throws MEMEException
   *           if anything goes wrong
   */
  public WorkLog getWorkLog(int work_id) throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_work_log"));
    request.addParameter(new Parameter.Default("work_id", work_id));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (WorkLog) request.getReturnValue("get_work_log").getValue();

  }

  /**
   * Returns all of the {@link WorkLog} entries (<b>SERVER CALL</b>).
   * 
   * @return all of the {@link WorkLog} entries
   * @throws MEMEException
   *           if anything goes wrong
   */
  public WorkLog[] getWorkLogs() throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_work_logs"));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (WorkLog[]) request.getReturnValue("get_work_logs").getValue();

  }

  /**
   * Returns all of the {@link WorkLog} entries for the specified type (<b>SERVER
   * CALL</b>).
   * 
   * @param type
   *          the type
   * @return all of the {@link WorkLog} entries for the specified type
   * @throws MEMEException
   *           if anything goes wrong
   */
  public WorkLog[] getWorkLogsByType(String type) throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function",
        "get_work_logs_by_type"));
    request.addParameter(new Parameter.Default("type", type));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (WorkLog[]) request.getReturnValue("get_work_logs_by_type")
        .getValue();

  }

  /**
   * Returns the {@link Activity} for the specified {@link MolecularTransaction} (<b>SERVER
   * CALL</b>).
   * 
   * @param transaction
   *          the {@link MolecularTransaction}
   * @return the {@link Activity} for the specified {@link MolecularTransaction}
   * @throws MEMEException
   *           if anything goes wrong
   */
  public Activity getActivityLog(MolecularTransaction transaction)
      throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_activity_log"));
    request.addParameter(new Parameter.Default("transaction_id", transaction
        .getIdentifier().intValue()));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (Activity) request.getReturnValue("get_activity_log").getValue();

  }

  /**
   * Returns all {@link Activity} entries for the specified {@link WorkLog} (<b>SERVER
   * CALL</b>).
   * 
   * @param work
   *          the {@link WorkLog}
   * @return all {@link Activity} entries for the specified {@link WorkLog}
   * @throws MEMEException
   *           if anything goes wrong
   */
  public Activity[] getActivityLogs(WorkLog work) throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request
        .addParameter(new Parameter.Default("function", "get_activity_logs"));
    request.addParameter(new Parameter.Default("work_id", work.getIdentifier()
        .intValue()));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (Activity[]) request.getReturnValue("get_activity_logs").getValue();

  }

  /**
   * Returns the {@link LoggedError}s for the specified transaction (<b>SERVER
   * CALL</b>).
   * 
   * @param transaction
   *          the {@link MolecularTransaction}
   * @return the {@link LoggedError}s for the specified transaction
   * @throws MEMEException
   *           if anything goes wrong
   */
  public LoggedError[] getErrors(MolecularTransaction transaction)
      throws MEMEException {

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "get_errors"));
    request.addParameter(new Parameter.Default("transaction_id", transaction
        .getIdentifier().intValue()));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    return (LoggedError[]) request.getReturnValue("get_errors").getValue();

  }

  /**
   * Perform the specified {@link MolecularAction} (<b>SERVER CALL</b>).
   * 
   * @param action
   *          the {@link MolecularAction} to perform
   * @throws MEMEException
   *           if anything goes wrong
   * @throws IntegrityViolationException
   *           if failed due to integrity violations
   * @throws StaleDataException
   *           if failed due to stale data
   */
  public void processAction(MolecularAction action) throws MEMEException,
      IntegrityViolationException, StaleDataException {

    // Set action parameter
    action.setAuthority(authority);
    action.setIntegrityVector(integrity_vector);
    action.setChangeStatus(status);
    if (transaction != null) {
      action.setTransactionIdentifier(transaction.getIdentifier());
    }
    if (work_log != null) {
      action.setWorkIdentifier(work_log.getIdentifier());

    }
    MEMEServiceRequest request = getServiceRequest();
    request.setService("ActionService");
    request.addParameter(new Parameter.Default("function", "do"));
    request.addParameter(new Parameter.Default("action", action));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      if (exceptions[0] instanceof IntegrityViolationException) {
        throw (IntegrityViolationException) exceptions[0];
      }
      if (exceptions[0] instanceof StaleDataException) {
        throw (StaleDataException) exceptions[0];
      }
      throw (MEMEException) exceptions[0];
    }

    // Set the target id for split actions
    if (request.getReturnValue("target_id") != null) {
      Identifier target_id = (Identifier) request.getReturnValue("target_id")
          .getValue();
      if (target_id != null) {
        action.setTarget(new Concept.Default(target_id.intValue()));
      }
    }

    // Set the source id for insert concept actions
    if (request.getReturnValue("source_id") != null) {
      Identifier source_id = (Identifier) request.getReturnValue("source_id")
          .getValue();
      if (source_id != null) {
        action.getSource().setIdentifier(source_id);
      }
    }

    // Set the row id for new core data
    if (request.getReturnValue("new_id") != null) {
      Identifier new_id = (Identifier) request.getReturnValue("new_id")
          .getValue();
      if (action instanceof MolecularInsertAtomAction) {
        ((MolecularInsertAtomAction) action).getAtomToInsert().setIdentifier(
            new_id);
      } else if (action instanceof MolecularInsertAttributeAction) {
        ((MolecularInsertAttributeAction) action).getAttributeToInsert()
            .setIdentifier(new_id);
      } else if (action instanceof MolecularInsertConceptAction) {
        ((MolecularInsertConceptAction) action).getConceptToInsert()
            .setIdentifier(new_id);
      } else if (action instanceof MolecularInsertRelationshipAction) {
        ((MolecularInsertRelationshipAction) action).getRelationshipToInsert()
            .setIdentifier(new_id);
      }
    }

    // Set the molecule id
    if (request.getReturnValue("molecule_id") != null) {
      Identifier molecule_id = (Identifier) request.getReturnValue(
          "molecule_id").getValue();
      action.setIdentifier(molecule_id);
    }

    // Set the violations vector for actions
    if (request.getReturnValue("violations_vector") != null) {
      ViolationsVector vv = (ViolationsVector) request.getReturnValue(
          "violations_vector").getValue();
      if (vv != null) {
        action.setViolationsVector(vv);
      }
    }

  }

  /**
   * Performs the specified {@link BatchMolecularTransaction} (<b>SERVER CALL</b>).
   * 
   * @param transaction
   *          the {@link BatchMolecularTransaction} to perform
   * @return the <code>int</code> transaction_id of the action (assigned by
   *         server)
   * @throws MEMEException
   *           if anything goes wrong
   */
  public int processAction(BatchMolecularTransaction transaction)
      throws MEMEException {

    // Set action parameter
    transaction.setAuthority(authority);
    if (work_log != null) {
      transaction.setWorkIdentifier(work_log.getIdentifier());

    }
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "do_batch"));
    request.addParameter(new Parameter.Default("transaction", transaction));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // return response
    int transaction_id = (int) request.getReturnValue("transaction_id")
        .getInt();
    return transaction_id;
  }

  /**
   * Performs the specified {@link MIDDataSourceAction} (<b>SERVER CALL</b>).
   * 
   * @param action
   *          the {@link MIDDataSourceAction} to perform
   * @return the <code>int</code> action_id of the action (assigned by
   *         server)
   * @throws MEMEException
   *           if anything goes wrong
   */
  public int processAction(MIDDataSourceAction action) throws MEMEException {

    // Set action parameter
    action.setAuthority(authority);

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "do_mid"));
    request.addParameter(new Parameter.Default("action", action));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // return response
    int action_id = (int) request.getReturnValue("action_id").getInt();
    return action_id;
  }

  /**
   * Performs the specified {@link MEMEDataSourceAction} (<b>SERVER CALL</b>).
   * 
   * @param action
   *          the {@link MEMEDataSourceAction} to perform
   * @return the <code>int</code> action_id of the action (assigned by
   *         server)
   * @throws MEMEException
   *           if anything goes wrong
   */
  public int processAction(MEMEDataSourceAction action) throws MEMEException {

    // Set action parameter
    action.setAuthority(authority);

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "do_meme"));
    request.addParameter(new Parameter.Default("action", action));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // return response
    int action_id = (int) request.getReturnValue("action_id").getInt();
    return action_id;
  }

  /**
   * Performs the specified {@link MacroMolecularAction} (<b>SERVER CALL</b>).
   * 
   * @param action
   *          the {@link MacroMolecularAction} to perform
   * @return the <code>int</code> transaction_id of the action (assigned by
   *         server)
   * @throws MEMEException
   *           if anything goes wrong
   */
  public int processAction(MacroMolecularAction action) throws MEMEException {

    // Set action parameter
    action.setAuthority(authority);
    if (work_log != null) {
      action.setWorkIdentifier(work_log.getIdentifier());

    }
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "do_macro"));
    request.addParameter(new Parameter.Default("action", action));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // return response
    int transaction_id = (int) request.getReturnValue("transaction_id")
        .getInt();
    return transaction_id;
  }

  /**
   * Undoes the specified {@link MolecularAction} (<b>SERVER CALL</b>).
   * 
   * @param action
   *          the {@link MolecularAction} to undo
   * @throws MEMEException
   *           if anything goes wrong
   */
  public void processUndo(MolecularAction action) throws MEMEException {

    action.setAuthority(authority);
    if (transaction != null) {
      action.setTransactionIdentifier(transaction.getIdentifier());
    }
    if (work_log != null) {
      action.setWorkIdentifier(work_log.getIdentifier());

    }
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "undo"));
    request.addParameter(new Parameter.Default("action", action));
    request.addParameter(new Parameter.Default("force", force));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Undoes the specified {@link BatchMolecularTransaction} (<b>SERVER CALL</b>).
   * 
   * @param transaction
   *          the {@link BatchMolecularTransaction} to undo
   * @return the <code>int</code> transaction_id of the undo action (assigned
   *         by server)
   * @throws MEMEException
   *           if anything goes wrong
   */
  public int processUndo(BatchMolecularTransaction transaction)
      throws MEMEException {

    // Set action parameter
    transaction.setAuthority(authority);
    if (work_log != null) {
      transaction.setWorkIdentifier(work_log.getIdentifier());

    }
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "undo_batch"));
    request.addParameter(new Parameter.Default("transaction", transaction));
    request.addParameter(new Parameter.Default("force", force));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // return response
    int transaction_id = (int) request.getReturnValue("transaction_id")
        .getInt();
    return transaction_id;

  }

  /**
   * Undoes the specified {@link LoggedAction} (<b>SERVER CALL</b>).
   * 
   * @param action
   *          the {@link LoggedAction} to undo
   * @return the <code>int</code> action_id of the undo action (assigned by
   *         server)
   * @throws MEMEException
   *           if anything goes wrong
   */
  public int processUndo(LoggedAction action) throws MEMEException {

    // Set action parameter
    action.setAuthority(authority);
    if (transaction != null) {
      action.setParent(new MolecularTransaction(transaction.getIdentifier()
          .intValue()));
      if (work_log != null) {
        action.getParent().setParent(
            new WorkLog(work_log.getIdentifier().intValue()));
      }
    }

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "undo_action"));
    request.addParameter(new Parameter.Default("action", action));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // return response
    int transaction_id = (int) request.getReturnValue("action_id").getInt();
    return transaction_id;
  }

  /**
   * Redoes the specified {@link LoggedAction} (<b>SERVER CALL</b>).
   * 
   * @param action
   *          the {@link LoggedAction} to redo
   * @return the <code>int</code> action_id of the redo action (assigned by
   *         server)
   * @throws MEMEException
   *           if anything goes wrong
   */
  public int processRedo(LoggedAction action) throws MEMEException {

    // Set action parameter
    action.setAuthority(authority);
    if (transaction != null) {
      action.setParent(new MolecularTransaction(transaction.getIdentifier()
          .intValue()));
      if (work_log != null) {
        action.getParent().setParent(
            new WorkLog(work_log.getIdentifier().intValue()));
      }
    }

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "redo_action"));
    request.addParameter(new Parameter.Default("action", action));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // return response
    int action_id = (int) request.getReturnValue("action_id").getInt();
    return action_id;
  }

  /**
   * Undoes the specified {@link MacroMolecularAction} (<b>SERVER CALL</b>).
   * 
   * @param action
   *          the {@link MacroMolecularAction} to undo
   * @return the <code>int</code> transaction_id of the undo action (assigned
   *         by server)
   * @throws MEMEException
   *           if anything goes wrong
   */
  public int processUndo(MacroMolecularAction action) throws MEMEException {

    // Set action parameter
    action.setAuthority(authority);
    if (work_log != null) {
      action.setWorkIdentifier(work_log.getIdentifier());

    }
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "undo_macro"));
    request.addParameter(new Parameter.Default("action", action));
    request.addParameter(new Parameter.Default("force", force));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // return response
    int transaction_id = (int) request.getReturnValue("transaction_id")
        .getInt();
    return transaction_id;
  }

  /**
   * Redoes the specified {@link MolecularAction} (<b>SERVER CALL</b>).
   * 
   * @param action
   *          the {@link MolecularAction} to redo
   * @throws MEMEException
   *           if anything goes wrong
   */
  public void processRedo(MolecularAction action) throws MEMEException {

    action.setAuthority(authority);
    if (transaction != null) {
      action.setTransactionIdentifier(transaction.getIdentifier());
    }
    if (work_log != null) {
      action.setWorkIdentifier(work_log.getIdentifier());

    }
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "redo"));
    request.addParameter(new Parameter.Default("action", action));
    request.addParameter(new Parameter.Default("force", force));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }
  }

  /**
   * Undoes the specified {@link BatchMolecularTransaction} (<b>SERVER CALL</b>).
   * 
   * @param transaction
   *          the {@link BatchMolecularTransaction} to undo
   * @return the <code>int</code> transaction_id of the undo action (assigned
   *         by server)
   * @throws MEMEException
   *           if anything goes wrong
   */
  public int processRedo(BatchMolecularTransaction transaction)
      throws MEMEException {

    // Set action parameter
    transaction.setAuthority(authority);
    if (work_log != null) {
      transaction.setWorkIdentifier(work_log.getIdentifier());

    }
    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "redo_batch"));
    request.addParameter(new Parameter.Default("transaction", transaction));
    request.addParameter(new Parameter.Default("force", force));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // return response
    int transaction_id = (int) request.getReturnValue("transaction_id")
        .getInt();
    return transaction_id;
  }

  /**
   * Redoes the specified {@link MacroMolecularAction} (<b>SERVER CALL</b>).
   * 
   * @param action
   *          the {@link MacroMolecularAction} to redo
   * @return the <code>int</code> transaction_id of the redo action (assigned
   *         by server)
   * @throws MEMEException
   *           if anything goes wrong
   */
  public int processRedo(MacroMolecularAction action) throws MEMEException {

    // Set action parameter
    action.setAuthority(authority);
    if (work_log != null) {
      action.setWorkIdentifier(work_log.getIdentifier());

    }

    MEMEServiceRequest request = getServiceRequest();
    request.addParameter(new Parameter.Default("function", "redo_macro"));
    request.addParameter(new Parameter.Default("action", action));
    request.addParameter(new Parameter.Default("force", force));

    // Issue request
    request = getRequestHandler().processRequest(request);

    // Handle exceptions
    Exception[] exceptions = request.getExceptions();
    if (exceptions.length > 0) {
      throw (MEMEException) exceptions[0];
    }

    // return response
    int transaction_id = (int) request.getReturnValue("transaction_id")
        .getInt();
    return transaction_id;

  }

  //
  // Private Methods
  //

  /**
   * Returns the {@link MEMEServiceRequest}.
   * 
   * @return the {@link MEMEServiceRequest}
   */
  protected MEMEServiceRequest getServiceRequest() {

    // Prepare request document
    MEMEServiceRequest request = super.getServiceRequest();
    request.setService("ActionService");
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
  // Main Method
  //

  /**
   * DO NOT REMOVE THIS METHOD (used by $MEME_HOME/bin/batch.pl)
   * 
   * @param args
   *          An array of string argument.
   */
  public static void main(String[] args) {

    //
    // Usage
    //

    if (args.length != 10 && args.length != 6 && args.length != 5) {
      MEMEToolkit.setProperty(MEMEConstants.DEBUG, "true");
      System.out.println("BATCH & MACRO ACTION PARAMETERS:");
      System.out.println("[0] service name");
      System.out.println("[1] process_action (do_batch, do_macro)");
      System.out.println("[2] action         ('S'  - change_status,");
      System.out.println("                    'T'  - change_tobereleased,");
      System.out.println("                    'A'  - change_atom_id,");
      System.out.println("                    'C'  - change_concept_id,");
      System.out.println("                    'I'  - insert,");
      System.out.println("                    'D'  - delete,");
      System.out.println("                    'CF' - change_field,");
      System.out.println("                    'AC' - approve_concept)");
      System.out.println("[3] core_data_type (C, R, A, CS)");
      System.out.println("[4] table_name");
      System.out.println("[5] authority");
      System.out.println("[6] work_id        <-- 0");
      System.out.println("[7] rank_flag      <-- N");
      System.out.println("[8] new_value      (action == 'S', 'T')");
      System.out.println("[9] action_field   (action == 'CF')");
      System.out.println("");
      System.out.println("BATCH & MACRO UNDO/REDO PARAMETERS:");
      System.out.println("[0] service name");
      System.out
          .println("[1] process_action (undo_batch, undo_macro, redo_batch, redo_macro, undo, redo)");
      System.out.println("[2] transaction id");
      System.out.println("[3] work id");
      System.out.println("[4] force action (true/false)?");
      System.out.println("[5] authority");
      System.out.println("GENERIC ACTION UNDO/REDO PARAMETERS:");
      System.out.println("[0] service name");
      System.out.println("[1] process_action (undo_action, redo_action)");
      System.out.println("[2] action id");
      System.out.println("[3] work id");
      System.out.println("[4] authority");
      System.exit(1);
    }

    //
    // Extract common arguments
    //

    String service = args[0];
    String process_name = args[1];
    String action = args[2];
    String core_data_type = null;
    String table_name = null;
    String authority = null;
    String work_id = null;
    String rank_flag = null;
    String new_value = null;
    String action_field = null;

    if (process_name.equals("do_batch") || process_name.equals("do_macro")) {
      // Extract common to batch & macro arguments
      core_data_type = args[3];
      table_name = args[4];
      authority = args[5];
      work_id = args[6];
      rank_flag = args[7];
      new_value = args[8];
      action_field = args[9];
    } else if (args.length == 6) {
      authority = args[5];
      work_id = args[3];
    } else if (args.length == 5) {
      authority = args[4];
      work_id = args[3];
    }

    //
    // Create new ActionClient
    //

    ActionClient client = null;

    try {
      if (service != null) {
        client = new ActionClient(service);
      } else {
        client = new ActionClient();
      }
    } catch (MEMEException me) {
      MEMEToolkit.trace("Exception: " + me);
      System.exit(1);
    }

    //
    // Set client authority
    //
    client.setAuthority(new Authority.Default(authority));
    client.setWorkIdentifier(new Identifier.Default(work_id));

    // Define possible molecular actions
    //
    LoggedAction la = null;
    MolecularAction ma = null;
    BatchMolecularTransaction batch_action = null;
    MacroMolecularAction macro_action = null;

    //
    // Determine process to perform
    //

    if (process_name.equals("do_batch")) {

      //
      // Batch Action
      //

      batch_action = new BatchMolecularTransaction();
      batch_action.setStatus('R');
      batch_action.setActionName(action);
      batch_action.setCoreDataType(core_data_type);
      batch_action.setTableName(table_name);
      batch_action.setRankFlag(Boolean.valueOf(rank_flag).booleanValue());
      if (action.equals("S") || action.equals("T")) {
        batch_action.setNewValue(new_value);
      } else if (action.equals("CF")) {
        batch_action.setActionField(action_field);
      }

    } else if (process_name.equals("do_macro")) {

      //
      // Macro Action
      //

      macro_action = new MacroMolecularAction();
      macro_action.setStatus('R');
      macro_action.setActionName(action);
      macro_action.setCoreDataType(core_data_type);
      macro_action.setTableName(table_name);
      macro_action.setRankFlag(Boolean.valueOf(rank_flag).booleanValue());
      if (action.equals("S") || action.equals("T")) {
        macro_action.setNewValue(new_value);
      } else if (action.equals("CF")) {
        macro_action.setActionField(action_field);
      }

    } else if (process_name.equals("undo_batch")
        || process_name.equals("redo_batch")) {

      //
      // Undo Batch, Redo Batch
      //
      batch_action = new BatchMolecularTransaction(Integer.parseInt(args[2]));
      client.setForce(Boolean.valueOf(args[4]).booleanValue());

    } else if (process_name.equals("undo_macro")
        || process_name.equals("redo_macro")) {

      //
      // Undo Macro, Redo Macro
      //
      macro_action = new MacroMolecularAction();
      macro_action.setTransactionIdentifier(Integer.parseInt(args[2]));
      client.setForce(Boolean.valueOf(work_id).booleanValue());

    } else if (process_name.equals("undo") || process_name.equals("redo")) {

      //
      // Undo, Redo
      //
      ma = new MolecularAction(Integer.parseInt(args[2]));
      ma.setTransactionIdentifier(0);
      client.setForce(Boolean.valueOf(work_id).booleanValue());

    } else if (process_name.equals("undo_action")
        || process_name.equals("redo_action")) {

      //
      // Undo, Redo
      //
      la = new LoggedAction.Default() {
      };
      la.setIdentifier(new Identifier.Default(Integer.parseInt(args[2])));
      la.setParent(new WorkLog(Integer.parseInt(args[3])));

    } else {

      //
      // Do
      //

      int source_id = 0;
      int target_id = 0;
      int cracs_id = 0;
      // String table_name = null;

      if (action.equals("merge") || action.equals("split")
          || action.equals("move")) {
        // in move, target_id represents atom_to_move
        // in split, target_id represents atom_to_split
        // in merge, target_id represents target concept
        source_id = Integer.parseInt(args[3]);
        target_id = Integer.parseInt(args[4]);

      } else if (action.equals("approve")) {
        source_id = Integer.parseInt(args[3]);

      } else if (action.equals("delete") || action.equals("change")
          || action.equals("insert")) {
        table_name = args[3];
        cracs_id = Integer.parseInt(args[4]);

        // in change, target_id represents target concept
        target_id = Integer.parseInt(args[5]);

      }

      Concept source = new Concept.Default(source_id);
      Concept target = new Concept.Default(target_id);
      Concept concept = new Concept.Default(cracs_id);
      concept.setStatus('R');

      // Create relationship object
      Relationship rel = new Relationship.Default();
      rel.setConcept(source);
      rel.setRelatedConcept(new Concept.Default(1001));
      rel.setName("RT");
      rel.setAttribute("analyzes");
      rel.setSource(new Source.Default("MTH"));
      rel.setSourceOfLabel(new Source.Default("MTH"));
      rel.setLevel('C');
      rel.setStatus('R');
      rel.setReleased('N');
      rel.setTobereleased('Y');

      // Create atom object
      Atom atom = new Atom.Default();
      atom.setIdentifier(new Identifier.Default(13951));
      atom.setConcept(source);
      Source src = new Source.Default("MTH");
      src.setStrippedSourceAbbreviation("PDQ");
      atom.setSource(src);
      atom.setTermgroup(new Termgroup.Default("MTH/PT"));
      atom.setCode(new Code("NOCODE"));
      atom.setString("Test Atom");
      atom.setStatus('R');
      atom.setReleased('N');
      atom.setTobereleased('Y');
      atom.setLanguage(new Language.Default("English", "ENG"));
      atom.addRelationship(rel);
      atom.setSUI(new StringIdentifier("S0000474"));
      atom.setLUI(new StringIdentifier("L0000474"));
      atom.setISUI(new StringIdentifier("I1103609"));
      atom.setAUI(new AUI("A0118738"));

      if (action.equals("merge")) {
        MolecularMergeAction mma = new MolecularMergeAction(source, target);
        mma.setSource(source);
        mma.setTarget(target);
        mma.setTransactionIdentifier(source_id);
        ma = mma;

      } else if (action.equals("move")) {
        MolecularMoveAction mma = new MolecularMoveAction(source, target);
        mma.setSource(source);
        mma.setTarget(target);
        mma.setTransactionIdentifier(source_id);

        try {
          atom.setIdentifier(new Identifier.Default(target_id));
          mma.addAtomToMove(atom);
        } catch (MEMEException me) {
          MEMEToolkit.trace("Exception: " + me);
          System.exit(1);
        }
        ma = mma;

      } else if (action.equals("split")) {
        MolecularSplitAction msa = new MolecularSplitAction(source);
        msa.setSource(source);
        msa.setTransactionIdentifier(source_id);

        try {
          atom.setIdentifier(new Identifier.Default(target_id));
          msa.addAtomToSplit(atom);
        } catch (MEMEException me) {
          MEMEToolkit.trace("Exception: " + me);
          System.exit(1);
        }
        ma = msa;

      } else if (action.equals("approve")) {
        MolecularApproveConceptAction maca = new MolecularApproveConceptAction(
            source);
        maca.setSource(source);
        ma = maca;

      } else if (action.equals("change")) {

        if (table_name.equals("atom")) {
          atom.setIdentifier(new Identifier.Default(cracs_id));
          atom.setConcept(target);
          MolecularChangeAtomAction mcaa = new MolecularChangeAtomAction(atom);
          ma = mcaa;

        } else if (table_name.equals("relationship")) {
          rel.setIdentifier(new Identifier.Default(cracs_id));
          rel.setConcept(target);
          MolecularChangeRelationshipAction mcra = new MolecularChangeRelationshipAction(
              rel);
          ma = mcra;

        } else if (table_name.equals("attribute")) {
          Attribute attr = new Attribute.Default(cracs_id);
          attr.setConcept(target);
          MolecularChangeAttributeAction mcaa = new MolecularChangeAttributeAction(
              attr);
          ma = mcaa;

        } else if (table_name.equals("concept")) {
          concept.setIdentifier(new Identifier.Default(cracs_id));
          MolecularChangeConceptAction mcca = new MolecularChangeConceptAction(
              concept);
          ma = mcca;
        }

      } else if (action.equals("delete")) {

        if (table_name.equals("atom")) {
          atom = new Atom.Default(cracs_id);
          atom.setConcept(concept);
          MolecularDeleteAtomAction mdaa = new MolecularDeleteAtomAction(atom);
          ma = mdaa;

        } else if (table_name.equals("relationship")) {
          rel = new Relationship.Default(cracs_id);
          rel.setConcept(concept);
          MolecularDeleteRelationshipAction mdra = new MolecularDeleteRelationshipAction(
              rel);
          ma = mdra;

        } else if (table_name.equals("attribute")) {
          Attribute attr = new Attribute.Default(cracs_id);
          attr.setConcept(concept);
          MolecularDeleteAttributeAction mdaa = new MolecularDeleteAttributeAction(
              attr);
          ma = mdaa;

        } else if (table_name.equals("concept")) {
          MolecularDeleteConceptAction mdca = new MolecularDeleteConceptAction(
              concept);
          ma = mdca;
        }

      } else if (action.equals("insert")) {

        if (table_name.equals("atom")) {
          atom.setConcept(concept);
          MolecularInsertAtomAction miaa = new MolecularInsertAtomAction(atom);
          ma = miaa;

        } else if (table_name.equals("relationship")) {
          rel.setConcept(concept);
          MolecularInsertRelationshipAction mira = new MolecularInsertRelationshipAction(
              rel);
          ma = mira;

        } else if (table_name.equals("concept")) {
          concept = new Concept.Default();
          concept.addAtom(atom);
          atom.setConcept(concept);
          MolecularInsertConceptAction mica = new MolecularInsertConceptAction(
              concept);
          ma = mica;

        } else if (table_name.equals("attribute")) {
          Attribute attr = new Attribute.Default();
          attr.setConcept(concept);
          attr.setSource(new Source.Default("MTH"));
          attr.setName("Test Attribute");
          // attr.setValue("D25-26 qualif");
          attr
              .setValue("WHAT: JRA Rash. JRA Rash: a rash occurring characteristically in patients with systemic onset juvenile rheumatoid arthritis. It consists of discrete or confluent macular or maculopapular red or salmon-pink, usually non-pruritic lesions. The lesions are most prominent over the trunk, but may also be found on the face and extremities. WHY: The rheumatoid rash is an important diagnostic sign of juvenile arthritis of systemic or poly-articular onset. The rash is not commonly seen, however, in JRA of pauci-articular onset. HOW: Therheumatoid rash is characteristically fleeting, tending to appear in the evenings associated with an increase in temperature, and then disappearing, sometimes in less than an hour. The rash is salmon-pink, usually circumscribed and macular or occasionally maculopapular. The individual lesions vary in size from 2-6 mm. The larger lesions have a pale center with extreme pallor of the skin on the periphery of the rash. The rash is found predominantly on the chest, axillae, thighs, upper arms and face. It is not pruritic. The rash may be induced by rubbing or scratching the skin. The rash is of greatest extent when it first appears and does not spread in any regular manner. It may occur on and off for a few weeks or for many years. Steroids or salicylates have no specific effect on the rash. The rheumatoid rash is distinguished from a drug sensitivity rash by its characteristic daily recurrence, its fleeting quality, its non-pruritic nature, and its persistence in spite of discontinuance of drugs. It is differentiated from erythema marginatum by the smaller characteristic macules, the presence of the rash on the face (not seen in erythema marginatum) and by the fact that in erythema marginatum the rash extends in areas after its blah blah blah blah");
          attr.setStatus('R');
          attr.setLevel('C');
          attr.setTobereleased('Y');
          attr.setReleased('N');
          MolecularInsertAttributeAction miaa = new MolecularInsertAttributeAction(
              attr);
          ma = miaa;
        }
      } // end Do
    } // end determine action to perform

    //
    // Main Header
    //

    System.out.println("");
    System.out
        .println("-------------------------------------------------------");
    System.out.println("Starting ActionClient ..." + new Date());
    System.out
        .println("-------------------------------------------------------");

    //
    // Display all arguments
    //

    if (args.length == 5) {
      System.out.println("");
      System.out.println("\tDatabase: " + service);
      System.out.println("\tProcess: " + process_name);
      System.out.println("\tAction ID: " + action);
      System.out.println("\tWork ID: " + work_id);
      System.out.println("\tAuthority: " + authority);
      System.out.println("");
    } else if (args.length == 6) {
      System.out.println("\tDatabase: " + service);
      System.out.println("\tProcess: " + process_name);
      System.out.println("\tTransaction ID: " + action);
      System.out.println("\tWork ID: " + work_id);
      System.out.println("\tForce action: " + args[4]);
      System.out.println("\tAuthority: " + authority);
    } else {
      System.out.println("");
      System.out.println("\tDatabase: " + service);
      System.out.println("\tProcess: " + process_name);
      System.out.println("\tAction: " + action);
      System.out.println("\tCore Data Type: " + core_data_type);
      System.out.println("\tTable Name: " + table_name);
      System.out.println("\tAuthority: " + authority);
      System.out.println("\tWork ID: " + work_id);
      System.out.println("\tRank Flag: " + rank_flag);
      System.out.println("\tNew Value: " + new_value);
      System.out.println("\tAction Field: " + action_field);
      System.out.println("");
    }

    int transaction_id = 0;
    int status = 0;

    try {

      // Turn off the debug so that code debugging will not be displayed.
      MEMEToolkit.setProperty(MEMEConstants.DEBUG, "false");

      if (process_name.equals("do")) {
        client.processAction(ma);
      } else if (process_name.equals("undo")) {
        client.processUndo(ma);
      } else if (process_name.equals("redo")) {
        client.processRedo(ma);

      } else if (process_name.equals("do_batch")) {
        transaction_id = client.processAction(batch_action);
      } else if (process_name.equals("undo_batch")) {
        transaction_id = client.processUndo(batch_action);
      } else if (process_name.equals("redo_batch")) {
        transaction_id = client.processRedo(batch_action);

      } else if (process_name.equals("do_macro")) {
        transaction_id = client.processAction(macro_action);
      } else if (process_name.equals("undo_macro")) {
        transaction_id = client.processUndo(macro_action);
      } else if (process_name.equals("redo_macro")) {
        transaction_id = client.processRedo(macro_action);

      } else if (process_name.equals("undo_action")) {
        transaction_id = client.processUndo(la);
      } else if (process_name.equals("redo_action")) {
        transaction_id = client.processRedo(la);

      }
    } catch (MEMEException me) {
      me.printStackTrace();
      status = 1;
    }

    if (process_name.startsWith("undo") || process_name.startsWith("redo")) {
      System.out.println("\tAction ID: " + transaction_id);
    } else {
      System.out.println("\tTransaction ID: " + transaction_id);
    }
    System.out.println("");

    //
    // Main Footer
    //

    System.out
        .println("-------------------------------------------------------");
    System.out.println("Finished ActionClient ..." + new Date());
    System.out
        .println("-------------------------------------------------------");

    System.exit(status);

  }
}
