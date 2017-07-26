/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  MaintenanceService
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.action.CuiAction;
import gov.nih.nlm.meme.action.ExecAction;
import gov.nih.nlm.meme.action.LoadTableAction;
import gov.nih.nlm.meme.action.MatrixAction;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.action.QueryAction;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

/**
 * Handles maintenance requests.
 *
 * @author MEME Group
 */

public class MaintenanceService implements MEMEApplicationService {

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Receives requests from the {@link MEMEApplicationServer}
   * Handles the request based on the "function" parameter.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {

    // Get Service Request and function parameter
    MEMEServiceRequest request = context.getServiceRequest();
    String function = (String) request.getParameter("function").getValue();
    MIDDataSource data_source = (MIDDataSource) context.getDataSource();

    //
    // Assign CUIs
    //
    if (function.equals("assign_cuis")) {
      CuiAction ca = null;
      if (request.getParameter("work_id") != null) {
        ca = CuiAction.newFullAssignCuiAction(
            new WorkLog(request.getParameter("work_id").getInt()));
      } else {
        ca = CuiAction.newFullAssignCuiAction();
      }
      data_source.getActionEngine().processAction(ca);
      request.addReturnValue(new Parameter.Default("log", ca.getLog()));
    }
    if (function.equals("assign_cuis_with_source")) {
      if (request.getParameter("source") != null) {
        Concept source = (Concept) request.getParameter("source").getValue();
        CuiAction ca = CuiAction.newAssignCuiAction(source);
        data_source.getActionEngine().processAction(ca);
      }
    }
    if (function.equals("assign_cuis_with_source_and_target")) {
      if (request.getParameter("source") != null ||
          request.getParameter("target") != null) {
        Concept source = (Concept) request.getParameter("source").getValue();
        Concept target = (Concept) request.getParameter("target").getValue();
        CuiAction ca = CuiAction.newAssignCuiAction(source, target);
        data_source.getActionEngine().processAction(ca);
      }
    }

    //
    // Execute a query (and log it)
    //
    if (function.equals("execute_query")) {
      if (request.getParameter("query") != null) {
        String query = (String) request.getParameter("query").getValue();
        QueryAction qa = QueryAction.executeQueryAction(query);
        data_source.getActionEngine().processAction(qa);
      }
    }
    if (function.equals("execute_query_with_inverse")) {
      if (request.getParameter("inverse_query") != null) {
        String query = (String) request.getParameter("query").getValue();
        String inverse_query = (String) request.getParameter("inverse_query").
            getValue();
        QueryAction qa = QueryAction.executeQueryAction(query, inverse_query);
        data_source.getActionEngine().processAction(qa);
      }
    }

    //
    // Sync loading of a table with MRD
    //
    if (function.equals("load_table")) {
      if (request.getParameter("table") != null) {
        String table = (String) request.getParameter("table").getValue();
        LoadTableAction lta = LoadTableAction.newInsertDataAction(table);
        data_source.getActionEngine().processAction(lta);
      }
    }

    //
    // Exec a script (and log it)
    //
    if (function.equals("exec_1")) {
      if (request.getParameter("command") != null && request.getParameter("env") != null) {
        String[] command = (String[]) request.getParameter("command").getValue();
        String[] env = (String[]) request.getParameter("env").getValue();
        ExecAction ea = ExecAction.newExecAction(command, env);
        data_source.getActionEngine().processAction(ea);
      }
    }

    if (function.equals("exec_2")) {
      if (request.getParameter("command") != null &&
          request.getParameter("inverse_command") != null &&
          request.getParameter("env") != null) {
        String[] command = (String[]) request.getParameter("command").getValue();
        String[] inverse_command = (String[]) request.getParameter(
            "inverse_command").getValue();
        String[] env = (String[]) request.getParameter("env").getValue();
        ExecAction ea = ExecAction.newExecAction(command, inverse_command, env);
        data_source.getActionEngine().processAction(ea);
      }
    }

    //
    // Log an operation
    //
    if (function.equals("log_operation")) {
      if (request.getParameter("authority") != null &&
          request.getParameter("activity") != null &&
          request.getParameter("detail") != null &&
          request.getParameter("transaction_id") != null &&
          request.getParameter("work_id") != null &&
          request.getParameter("elapsed_time") != null) {
        String authority = (String) request.getParameter("authority").getValue();
        String activity = (String) request.getParameter("activity").getValue();
        String detail = (String) request.getParameter("detail").getValue();
        int transaction_id = request.getParameter("transaction_id").getInt();
        int work_id = request.getParameter("work_id").getInt();
        int elapsed_time = request.getParameter("elapsed_time").getInt();
        data_source.logOperation(
            new Authority.Default(authority),
            activity, detail,
            new MolecularTransaction(transaction_id),
            new WorkLog(work_id), elapsed_time);
      }
    }

    //
    // Log or reset progress info
    //
    if (function.equals("log_progress")) {
      if (request.getParameter("authority") != null &&
          request.getParameter("activity") != null &&
          request.getParameter("detail") != null &&
          request.getParameter("transaction_id") != null &&
          request.getParameter("work_id") != null &&
          request.getParameter("elapsed_time") != null) {
        String authority = (String) request.getParameter("authority").getValue();
        String activity = (String) request.getParameter("activity").getValue();
        String detail = (String) request.getParameter("detail").getValue();
        int transaction_id = request.getParameter("transaction_id").getInt();
        int work_id = request.getParameter("work_id").getInt();
        int elapsed_time = request.getParameter("elapsed_time").getInt();
        data_source.logProgress(
            new Authority.Default(authority),
            activity, detail,
            new MolecularTransaction(transaction_id),
            new WorkLog(work_id), elapsed_time);
      }
    }
    if (function.equals("reset_progress")) {
      if (request.getParameter("work_id") != null) {
        int work_id = request.getParameter("work_id").getInt();
        data_source.resetProgress(new WorkLog(work_id));
      }
    }

    //
    // Initialize editing matrix
    //
    if (function.equals("initialize_matrix")) {
      if (request.getParameter("work_id") != null) {
        int work_id = request.getParameter("work_id").getInt();
        MatrixAction ma = MatrixAction.newInitializeMatrixAction(new WorkLog(
            work_id));
        data_source.getActionEngine().processAction(ma);
        request.addReturnValue(new Parameter.Default("log", ma.getLog()));
      }
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
