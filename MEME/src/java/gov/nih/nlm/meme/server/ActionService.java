/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  ActionService
 * Changes
 *   01/11/2006 BAC (1-739BX): work to get it running properly
 *   01/30/2006 RBE (1-763IU): change parameter name in processRequest - 
 *   get_work_logs_by_type
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.action.Activity;
import gov.nih.nlm.meme.action.AtomicAction;
import gov.nih.nlm.meme.action.AtomicInsertAction;
import gov.nih.nlm.meme.action.BatchMolecularTransaction;
import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.action.MEMEDataSourceAction;
import gov.nih.nlm.meme.action.MIDDataSourceAction;
import gov.nih.nlm.meme.action.MacroMolecularAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.TransactionAction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.LoggedError;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MIDActionEngine;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * Handles requests to perform molecular actions.
 *
 * @author MEME Group
 */
public class ActionService implements MEMEApplicationService {

  //
  // Private methods
  //

  /**
   * Process molecular action.
   * @param action the {@link MolecularAction}.
   * @param data_source the {@link MIDDataSouce}.
   * @throws MEMEException if failed to process the request.
   */
  private void processAction(MolecularAction action,
                             MIDDataSource data_source) throws MEMEException {

    //
    // Connect transaction and work log ancestors for action engine logging
    //
    if (action.getTransactionIdentifier() != null) {
      action.setParent(new MolecularTransaction(action.getTransactionIdentifier().
                                                intValue()));
      if (action.getWorkIdentifier() != null) {
        action.getParent().setParent(new WorkLog(action.getWorkIdentifier().
                                                 intValue()));
      }
    }
    data_source.getActionEngine().processAction(action);

  }

  /**
   * Processes the specified {@link MEMEDataSourceAction}.
   * @param action the {@link MEMEDataSourceAction} to process
   * @param data_source the {@link MIDDataSource}
   * @return transaction_id of the processed transaction
   * @throws MEMEException if failed to process the request
   */
  private int processAction(MEMEDataSourceAction action,
                            MIDDataSource data_source) throws MEMEException {

    //
    // perform action
    //
    data_source.getActionEngine().processAction(action);
    return (action.getIdentifier() == null ? 0 : action.getIdentifier().intValue());
  }

  /**
   * Processes the specified {@link MIDDataSourceAction}.
   * @param action the {@link MIDataSourceAction} to process
   * @param data_source the {@link MIDDataSource}
   * @return transaction_id of the processed transaction
   * @throws MEMEException if failed to process the request
   */
  private int processAction(MIDDataSourceAction action,
                            MIDDataSource data_source) throws MEMEException {

    //
    // perform action
    //
    ((MIDActionEngine)data_source.getActionEngine()).processAction(action);
    return action.getIdentifier().intValue();
  }

  /**
   * Processes the specified {@link BatchMolecularTransaction}.
   * @param transaction the {@link BatchMolecularTransaction} to process
   * @param data_source the {@link MIDDataSource}
   * @return transaction_id of the processed transaction
   * @throws MEMEException if failed to process the request
   */
  private int processAction(BatchMolecularTransaction transaction,
                            MIDDataSource data_source) throws MEMEException {

    //
    // Connect work log ancestor for action engine logging
    //
    if (transaction.getWorkIdentifier() != null) {
      transaction.setParent(new WorkLog(transaction.getWorkIdentifier().
                                        intValue()));
      //
      // perform action
      //
    }
    data_source.getActionEngine().processAction(transaction);

    return transaction.getIdentifier().intValue();
  }

  /**
   * Processes the specified {@link MacroMolecularAction}.
   * @param action the {@link MacroMolecularAction} to process
   * @param data_source the {@link MIDDataSource}
   * @return transaction_id of the processed transaction
   * @throws MEMEException if failed to process the request
   */
  private int processAction(MacroMolecularAction action,
                            MIDDataSource data_source) throws MEMEException {
    //
    // Connect transaction and work log ancestors for action engine logging
    //
    if (action.getTransactionIdentifier() != null) {
      action.setParent(new MolecularTransaction(action.getTransactionIdentifier().
                                                intValue()));
      if (action.getWorkIdentifier() != null) {
        action.getParent().setParent(new WorkLog(action.getWorkIdentifier().
                                                 intValue()));
      }
    }

    //
    // Perform action
    //
    data_source.getActionEngine().processAction(action);

    return action.getTransactionIdentifier().intValue();
  }

  /**
   * Undoes the specified {@link MolecularAction}.
   * @param action the {@link MolecularAction} to undo
   * @param data_source the {@link MIDDataSource}
   * @throws MEMEException if failed process the request
   */
  private void processUndo(MolecularAction action,
                           MIDDataSource data_source) throws MEMEException {

    //
    // Action id must be set
    //
    if (action == null || action.getIdentifier() == null) {
      throw new BadValueException("Invalid molecule id");
    }

    //
    // Get molecular action
    //
    int molecule_id = action.getIdentifier().intValue();
    MolecularAction full_action = data_source.getFullMolecularAction(
        molecule_id);

    //
    // If not found, bail
    //
    if (full_action == null) {
      throw new BadValueException("Invalid molecule id");
    }

    //
    // Get inverse action
    //
    full_action.setTransactionIdentifier(action.getTransactionIdentifier());
    full_action.setWorkIdentifier(action.getWorkIdentifier());
    full_action.setAuthority(action.getAuthority());
    MolecularAction inverse_action = (MolecularAction) full_action.
        getInverseAction();

    //
    // Perform inverse action
    //
    data_source.getActionEngine().processAction(inverse_action);

  }

  /**
   * Undoes the specified {@link LoggedAction}.
   * @param action the {@link LoggedAction} to undo
   * @param data_source the {@link MIDDataSource}
   * @returns action id of {@link LoggedAction}
   * @throws MEMEException if failed process the request
   */
  private int processUndo(LoggedAction action,
                          MIDDataSource data_source) throws MEMEException {

    //
    // Action id must be set
    //
    if (action == null || action.getIdentifier() == null) {
      throw new BadValueException("Invalid action id");
    }

    //
    // Get molecular action
    //
    int action_id = action.getIdentifier().intValue();
    LoggedAction full_action = data_source.getAction(action_id);

    //
    // If not found, bail
    //
    if (full_action == null) {
      throw new BadValueException("Invalid action id");
    }

    //
    // Get inverse action
    //
    full_action.setParent(action.getParent());
    full_action.setAuthority(action.getAuthority());
    LoggedAction inverse_action = full_action.getInverseAction();

    //
    // Perform inverse action
    // We must figure out what action to perform
    //
    if (inverse_action instanceof MIDDataSourceAction) {
      ( (MIDActionEngine) data_source.getActionEngine()).processAction( (
          MIDDataSourceAction) inverse_action);
    } else if (inverse_action instanceof MEMEDataSourceAction) {
      data_source.getActionEngine().processAction( (MEMEDataSourceAction)
                                                  inverse_action);
    } else if (inverse_action instanceof MolecularAction) {
      data_source.getActionEngine().processAction( (MolecularAction)
                                                  inverse_action);
    } else if (inverse_action instanceof MacroMolecularAction) {
      data_source.getActionEngine().processAction( (MacroMolecularAction)
                                                  inverse_action);
    } else if (inverse_action instanceof BatchMolecularTransaction) {
      data_source.getActionEngine().processAction( (BatchMolecularTransaction)
                                                  inverse_action);

    }
    return inverse_action.getIdentifier().intValue();
  }

  /**
   * Re-processes the specified {@link LoggedAction}.
   * @param action the {@link LoggedAction} to re-perform
   * @param data_source the {@link MIDDataSource}
   * @return action_id for {@link LoggedAction} redo
   * @throws MEMEException if failed process the request
   */
  private int processRedo(LoggedAction action,
                          MIDDataSource data_source) throws MEMEException {

    //
    // Action id must be set
    //
    if (action == null || action.getIdentifier() == null) {
      throw new BadValueException("Invalid action id");
    }

    //
    // Get molecular action
    //
    int action_id = action.getIdentifier().intValue();
    LoggedAction full_action = data_source.getAction(action_id);

    //
    // If not found, bail
    //
    if (full_action == null) {
      throw new BadValueException("Invalid action id");
    }

    //
    // Get inverse action
    //
    full_action.setParent(action.getParent());
    full_action.setAuthority(action.getAuthority());

    //
    // Perform inverse action
    // We must figure out what action to perform
    //
    if (action instanceof MIDDataSourceAction) {
      ( (MIDActionEngine) data_source.getActionEngine()).processAction( (
          MIDDataSourceAction) full_action);
    } else if (action instanceof MEMEDataSourceAction) {
      data_source.getActionEngine().processAction( (MEMEDataSourceAction)
                                                  full_action);
    } else if (action instanceof MolecularAction) {
      data_source.getActionEngine().processAction( (MolecularAction)
                                                  full_action);
    } else if (action instanceof MacroMolecularAction) {
      data_source.getActionEngine().processAction( (MacroMolecularAction)
                                                  full_action);
    } else if (action instanceof BatchMolecularTransaction) {
      data_source.getActionEngine().processAction( (BatchMolecularTransaction)
                                                  full_action);

    }
    return full_action.getIdentifier().intValue();
  }

  /**
   * Undoes the specified {@link BatchMolecularTransaction}.
   * @param transaction the {@link BatchMolecularTransaction} to undo
   * @param data_source the {@link MIDDataSource}
   * @param force indicates whether or not to force the action at the atomic action level
   * @return transaction_id of the processed transaction
   * @throws MEMEException if failed to process the request
   */
  private int processUndo(BatchMolecularTransaction transaction,
                          MIDDataSource data_source,
                          boolean force) throws MEMEException {

    //
    // Use a transaction action to undo
    //
    TransactionAction undo = TransactionAction.newUndoTransactionAction(
        transaction.getIdentifier(), force);

    //
    // Configureset authority (if not already set)
    //
    undo.setAuthority(transaction.getAuthority());
    undo.setParent(new WorkLog(transaction.getWorkIdentifier().intValue()));

    //
    // Perform action
    //
    data_source.getActionEngine().processAction(undo);

    return undo.getIdentifier().intValue();
  }

  /**
   * Undoes the specified {@link MacroMolecularAction}.
   * @param action the {@link MacroMolecularAction}to undo
   * @param data_source the {@link MIDDataSource}
   * @param force indicates whether or not to force the action at the atomic action level
   * @return transaction_id of the processed transaction
   * @throws MEMEException if failed to process the request
   */
  private int processUndo(MacroMolecularAction action,
                          MIDDataSource data_source,
                          boolean force) throws MEMEException {

    //
    // Use a transaction action to undo
    //
    TransactionAction undo = TransactionAction.newUndoTransactionAction(action.
        getTransactionIdentifier(), force);

    //
    // Configure action
    //
    undo.setAuthority(action.getAuthority());
    undo.setParent(new WorkLog(action.getWorkIdentifier().intValue()));

    //
    // Perform action
    //
    data_source.getActionEngine().processAction(undo);

    return undo.getIdentifier().intValue();
  }

  /**
   * Re-processes the specified {@link MolecularAction}.
   * @param action the {@link MolecularAction} to re-process
   * @param data_source the {@link MIDDataSource}
   * @throws MEMEException if failed to process the request
   */
  private void processRedo(MolecularAction action,
                           MIDDataSource data_source) throws MEMEException {
    //
    // Action id must be set
    //
    if (action == null || action.getIdentifier() == null) {
      throw new BadValueException("Invalid molecule id");
    }

    //
    // Get molecular action
    //
    int molecule_id = action.getIdentifier().intValue();
    MolecularAction full_action = data_source.getFullMolecularAction(
        molecule_id);

    //
    // If not found, bail
    //
    if (full_action == null) {
      throw new BadValueException("Invalid molecule id");
    }

    //
    // Configure inverse action
    //
    full_action.setAuthority(action.getAuthority());
    full_action.setTransactionIdentifier(action.getTransactionIdentifier());
    full_action.setWorkIdentifier(action.getWorkIdentifier());
    full_action.setSource(new Concept.Default(full_action.getSourceIdentifier().
                                              intValue()));
    full_action.setTarget(new Concept.Default(full_action.getTargetIdentifier().
                                              intValue()));

    //
    // Connect transaction and work log ancestors for action engine logging
    //
    if (full_action.getTransactionIdentifier() != null) {
      full_action.setParent(new MolecularTransaction(full_action.
          getTransactionIdentifier().intValue()));
      if (action.getWorkIdentifier() != null) {
        full_action.getParent().setParent(new WorkLog(full_action.
            getWorkIdentifier().intValue()));
      }
    }

    //
    // Perform inverse action
    //
    data_source.getActionEngine().processAction(full_action);
  }

  /**
   * Re-processes the specified {@link BatchMolecularTransaction}.
   * @param transaction the {@link BatchMolecularTransaction} to re-perform
   * @param data_source the {@link MIDDataSource}
   * @return transaction_id of the processed transaction
   * @param force indicates whether or not to force the action at the atomic action level
   * @throws MEMEException if failed to process the request
   */
  private int processRedo(BatchMolecularTransaction transaction,
                          MIDDataSource data_source,
                          boolean force) throws MEMEException {

    //
    // Use a transaction action to undo
    //
    TransactionAction redo = TransactionAction.newRedoTransactionAction(
        transaction.getIdentifier(), force);

    //
    // configure action
    //
    redo.setAuthority(transaction.getAuthority());
    redo.setParent(new WorkLog(transaction.getWorkIdentifier().intValue()));

    //
    // Perform action
    //
    data_source.getActionEngine().processAction(redo);

    return redo.getIdentifier().intValue();

  }

  /**
   * Re-processes the specified {@link MacroMolecularAction}.
   * @param action the {@link MacroMolecularAction} to re-process
   * @param data_source the {@link MIDDataSource}
   * @return transaction_id of the processed transaction
   * @param force indicates whether or not to force the action at the atomic action level
   * @throws MEMEException if failed to process the request
   */
  private int processRedo(MacroMolecularAction action,
                          MIDDataSource data_source,
                          boolean force) throws MEMEException {

    //
    // Use a transaction action to undo
    //
    TransactionAction redo = TransactionAction.newRedoTransactionAction(action.
        getTransactionIdentifier(), force);

    //
    // Configure action
    //
    redo.setAuthority(action.getAuthority());
    redo.setParent(new WorkLog(action.getWorkIdentifier().intValue()));

    //
    // Perform action
    //
    data_source.getActionEngine().processAction(redo);

    return redo.getIdentifier().intValue();
  }

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Receives a request from the {@link MEMEApplicationServer}.  This method
   * calls one of the private methods based on the value of the "function"
   * parameter passed from the client.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {

    //
    // Get Service Request and function parameter
    //
    MEMEServiceRequest request = context.getServiceRequest();
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();
    String function = (String) request.getParameter("function").getValue();
    boolean force = false;
    if (request.getParameter("force") != null) {
      force = request.getParameter("force").getBoolean();
    }
    //
    // LOGS
    //
    if (function.equals("get_work_log")) {
      if (request.getParameter("work_id") != null) {
        int work_id = (int) request.getParameter("work_id").getInt();
        WorkLog work_log = data_source.getWorkLog(work_id);
        request.addReturnValue(new Parameter.Default("get_work_log", work_log));
      }
    } else if (function.equals("get_work_logs")) {
      WorkLog[] work_logs = data_source.getWorkLogs();
      request.addReturnValue(new Parameter.Default("get_work_logs", work_logs));
    } else if (function.equals("get_work_logs_by_type")) {
      if (request.getParameter("type") != null) {
        String type = (String) request.getParameter("type").getValue();
        WorkLog[] work_logs = data_source.getWorkLogsByType(type);
        request.addReturnValue(new Parameter.Default("get_work_logs_by_type",
            work_logs));
      }
    } else if (function.equals("get_activity_log")) {
      if (request.getParameter("transaction_id") != null) {
        int transaction_id = (int) request.getParameter("transaction_id").
            getInt();
        Activity activity = data_source.getActivityLog(new MolecularTransaction(
            transaction_id));
        request.addReturnValue(new Parameter.Default("get_activity_log",
            activity));
      }
    } else if (function.equals("get_activity_logs")) {
      if (request.getParameter("work_id") != null) {
        int work_id = (int) request.getParameter("work_id").getInt();
        Activity[] activities = data_source.getActivityLogs(new WorkLog(work_id));
        request.addReturnValue(new Parameter.Default("get_activity_logs",
            activities));
      }
    } else if (function.equals("get_errors")) {
      if (request.getParameter("transaction_id") != null) {
        int transaction_id = (int) request.getParameter("transaction_id").
            getInt();
        LoggedError[] errors = data_source.getErrors(new MolecularTransaction(
            transaction_id));
        request.addReturnValue(new Parameter.Default("get_errors", errors));
      }
    }

    //
    // ACTIONS
    //
    else if (function.equals("do")) {
      MolecularAction action = (MolecularAction) request.getParameter("action").
          getValue();
      processAction(action, data_source);

      // This return value id for a split so the client has
      // a reference for the new concept id.
      if (action.getTarget() != null) {
        request.addReturnValue(
            new Parameter.Default("target_id",
                                  action.getTarget().getIdentifier()));

        // Return the concept id when a new concept is inserted.
      }
      if (action instanceof MolecularInsertConceptAction) {
        request.addReturnValue(
            new Parameter.Default("source_id",
                                  action.getSource().getIdentifier()));

        // Return the id for inserted core data
      }
      AtomicAction[] actions = action.getAtomicActions();
      for (int i = 0; i < actions.length; i++) {
        if (actions[i] instanceof AtomicInsertAction) {
          request.addReturnValue(
              new Parameter.Default("new_id",
                                    actions[i].getRowIdentifier()));
          break;
        }
      }

      if (action.getViolationsVector() != null) {
        request.addReturnValue(
            new Parameter.Default("violations_vector",
                                  action.getViolationsVector()));
      }

      request.addReturnValue(
          new Parameter.Default("molecule_id",
                                action.getIdentifier()));

    }

    else if (function.equals("undo")) {
      processUndo(
          (MolecularAction) request.getParameter("action").getValue(),
          data_source);
    }

    else if (function.equals("redo")) {
      processRedo(
          (MolecularAction) request.getParameter("action").getValue(),
          data_source);
    }

    //
    // DATA SOURCE ACTIONS
    //
    else if (function.equals("do_meme")) {
      MEMEDataSourceAction mda =
          (MEMEDataSourceAction) request.getParameter("action").
          getValue();
      int action_id = processAction(mda, data_source);
      request.addReturnValue(new Parameter.Default("action_id",
          action_id));
    }

    else if (function.equals("do_mid")) {
      MIDDataSourceAction mda =
          (MIDDataSourceAction) request.getParameter("action").
          getValue();
      int action_id = processAction(mda, data_source);
      request.addReturnValue(new Parameter.Default("action_id",
          action_id));
    }

    //
    // BATCH ACTIONS
    //
    else if (function.equals("do_batch")) {
      BatchMolecularTransaction bmt =
          (BatchMolecularTransaction) request.getParameter("transaction").
          getValue();
      int transaction_id = processAction(bmt, data_source);

      //
      // Generate log for mproc_change_status.pl
      //
      if (bmt.getActionName().equals("AC") || bmt.getActionName().equals("CA")) {
        MolecularAction[] actions = bmt.getMolecularActions();
        StringBuffer log = new StringBuffer(1000);
        for (int i = 0; i < actions.length; i++) {
          log.append(actions[i].getSourceIdentifier()).append("|R|");
          if (actions[i].getStatus() == 'E') {
            log.append("0|ERROR: Check server log for more details\n");
          } else if (actions[i].getStatus() == 'V') {
            log.append("0|").append(actions[i].getViolationsVector()).append(
                "\n");
          } else {
            log.append("1|\n");
          }
        }
        request.addReturnValue(new Parameter.Default("log", log.toString()));
      }
      request.addReturnValue(new Parameter.Default("transaction_id",
          transaction_id));

    }

    else if (function.equals("undo_batch")) {
      int transaction_id = processUndo(
          (BatchMolecularTransaction) request.getParameter("transaction").
          getValue(),
          data_source, force);
      // Sets the return value
      request.addReturnValue(new Parameter.Default("transaction_id",
          transaction_id));

    }

    else if (function.equals("redo_batch")) {
      int transaction_id = processRedo(
          (BatchMolecularTransaction) request.getParameter("transaction").
          getValue(),
          data_source, force);
      // Sets the return value
      request.addReturnValue(new Parameter.Default("transaction_id",
          transaction_id));
    }

    //
    // MACRO ACTIONS
    //
    else if (function.equals("do_macro")) {
      int transaction_id = processAction(
          (MacroMolecularAction) request.getParameter("action").getValue(),
          data_source);
      // Sets the return value
      request.addReturnValue(new Parameter.Default("transaction_id",
          transaction_id));

    }

    else if (function.equals("undo_macro")) {
      int transaction_id = processUndo(
          (MacroMolecularAction) request.getParameter("action").getValue(),
          data_source, force);
      // Sets the return value
      request.addReturnValue(new Parameter.Default("transaction_id",
          transaction_id));

    }

    else if (function.equals("redo_macro")) {
      int transaction_id = processRedo(
          (MacroMolecularAction) request.getParameter("action").getValue(),
          data_source, force);
      // Sets the return value
      request.addReturnValue(new Parameter.Default("transaction_id",
          transaction_id));
    }

    //
    // GENERIC ACTIONS
    //
    else if (function.equals("undo_action")) {
      int action_id = processUndo(
          (LoggedAction) request.getParameter("action").getValue(), data_source);

      //
      // Sets the return value
      //
      request.addReturnValue(new Parameter.Default("action_id", action_id));

    }

    else if (function.equals("redo_action")) {
      int action_id = processRedo(
          (LoggedAction) request.getParameter("action").getValue(), data_source);

      //
      // Sets the return value
      //
      request.addReturnValue(new Parameter.Default("action_id", action_id));

    }

  } // end processRequest

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean requiresSession() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isRunning() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isReEntrant() {
    return false;
  }

}
