/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MIDActionEngine
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.BatchMolecularTransaction;
import gov.nih.nlm.meme.action.LoadTableAction;
import gov.nih.nlm.meme.action.MEMEDataSourceAction;
import gov.nih.nlm.meme.action.MIDDataSourceAction;
import gov.nih.nlm.meme.action.MacroMolecularAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.QueryAction;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.SearchParameter;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.IntegrityViolationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.StaleDataException;
import gov.nih.nlm.meme.integrity.ViolationsVector;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;

/**
 * Generically represents MID data source.
 *
 * @author MEME Group
 */

public class MIDActionEngine extends ActionEngine.Default {

  /**
   * Instantiates a {@link MIDActionEngine} connected
   * to the specified {@link MIDDataSource}
   * @param mds the {@link MIDDataSource} in which to perform actions
   */
  public MIDActionEngine(MIDDataSource mds) {
    super(mds);
  }

  /**
   * Processes MID data source action.
   * @param action the {@link MIDDataSourceAction} to perform
   * @throws ActionException if failed to process MID data source action
   * @throws DataSourceException if failed
   */
  public void processAction(MIDDataSourceAction action) throws ActionException,
      DataSourceException {

    //
    // Get action timestamp and start time
    //
    Date timestamp = new Date();
    action.setTimestamp(timestamp);

    //
    // Set auto commit
    //
    try {
      data_source.setAutoCommit(false);
    } catch (SQLException se) {
      rollback(action);
      throw new ActionException("Failed to set auto commit.", action, se);
    }
    MIDDataSource mds = (MIDDataSource) data_source;
    action.getInitialState(mds);
    if (action.getAuthority() == null) {
      action.setAuthority(data_source.getAuthority("MTH"));
    }
    action.performAction(mds);

    //
    // Compute elapsed time
    //
    long elapsed_time = (new Date().getTime()) - timestamp.getTime();
    action.setElapsedTime(elapsed_time);
    logAction(action);

    data_source.restoreAutoCommit();
  }

  /**
   * Processes MEME data source action.
   * @param action the {@link MEMEDataSourceAction} to perform
   * @throws ActionException if failed to process MEME data source action
   * @throws DataSourceException if failed
   */
  public void processAction(MEMEDataSourceAction action) throws ActionException,
      DataSourceException {
    try {

      //
      // Get action timestamp and start time
      //
      Date timestamp = new Date();
      action.setTimestamp(timestamp);

      //
      // Set auto commit
      //
      try {
        data_source.setAutoCommit(false);
      } catch (SQLException se) {
        rollback(action);
        throw new ActionException("Failed to set auto commit.", action, se);
      }

      action.getInitialState(data_source);
      super.processAction(action);

      //
      // restore autocommit state
      //
      data_source.restoreAutoCommit();

    } catch (Exception e) {
      rollback(action);
      throw new ActionException("Failed to commit.", action, e);
    }
  }

  /*
   * Implements {@link ActionEngine#processAction(MolecularAction)}.
   * @param action the {@link MolecularAction} to perform
   * @throws ActionException if failed to process molecular action
   * @throws IntegrityViolationException if failed due to integrity violation
   * @throws StaleDataException if failed due to process lock
   */
  public void processAction(MolecularAction action) throws ActionException,
      IntegrityViolationException, StaleDataException {

    try {

      //
      // Get action timestamp and start time
      //
      Date timestamp = new Date();
      action.setTimestamp(timestamp);

      //
      // Turn autocommit off (restore it before exiting)
      //
      try {
        data_source.setAutoCommit(false);
      } catch (SQLException se) {
        throw new ActionException("Failed to set auto commit.", action, se);
      }

      //
      // Lock source and target concepts, and if necessary related concepts as well
      //
      try {
        if (action.getTarget() != null) {
          data_source.lockConcepts(action.getSource(), action.getTarget(),
                                   action.lockRelatedConcepts());
        } else {
          data_source.lockConcept(action.getSource(),
                                  action.lockRelatedConcepts());
        }
      } catch (DataSourceException dse) {
        rollback(action);
        throw new ActionException("Failed to lock concept.", action, dse);
      }

      //
      // Ensure the source concept is fresh
      //
      if (action.getSource().getReadTimestamp() != null) {
        try {
          //
          // Find actions with the source id matching the concept_id
          // and a timestamp >= the read timestamp where the
          // action authority is not the same as this action
          //
          SearchParameter[] sps = new SearchParameter[3];
          sps[0] = new SearchParameter.Single("source_id",
                                              action.getSourceIdentifier());
          sps[1] = new SearchParameter.Range("timestamp",
                                             new Identifier.Default(
              MEMEToolkit.getDateFormat().format(
              action.getSource().getReadTimestamp())),
                                             null);
          sps[2] = new SearchParameter.Single("authority",
                                              new Identifier.Default(action.
              getAuthority().toString()));
          sps[2].setIsNegated(true);
          Iterator iter = data_source.findMolecularActions(sps);
          boolean flag = iter.hasNext();
          iter.remove();
          if (flag) {
            rollback(action);
            throw new StaleDataException(
                "Source concept data is stale." +
                "Please refresh concept before performing this action.");
          }
        } catch (DataSourceException dse) {
          rollback(action);
          throw new ActionException("Failed to check freshness.", action, dse);
        }
      }

      //
      // Additional freshness check, for when action operates on relationships
      //
      if (action.getSource().getReadTimestamp() != null &&
          action.lockRelatedConcepts()) {
        try {
          //
          // Find actions with the target id matching the concept_id
          // and a timestamp >= the read timestamp where the
          // action authority is not the same as this action
          //
          SearchParameter[] sps = new SearchParameter[3];
          sps[0] = new SearchParameter.Single("target_id",
                                              action.getSourceIdentifier());
          sps[1] = new SearchParameter.Range("timestamp",
                                             new Identifier.Default(
              MEMEToolkit.getDateFormat().format(
              action.getSource().getReadTimestamp())),
                                             null);
          sps[2] = new SearchParameter.Single("authority",
                                              new Identifier.Default(action.
              getAuthority().toString()));
          sps[2].setIsNegated(true);
          Iterator iter = data_source.findMolecularActions(sps);
          boolean flag = iter.hasNext();
          iter.remove();
          if (flag) {
            rollback(action);
            throw new StaleDataException(
                "Source concept data is stale." +
                "Please refresh concept before performing this action.");
          }
        } catch (DataSourceException dse) {
          rollback(action);
          throw new ActionException("Failed to check freshness.", action, dse);
        }
      }

      //
      // Reread concepts so actions operate on full data
      //
      Concept[] concepts_to_refresh = action.getConceptsToRefresh();
      if (concepts_to_refresh != null) {
        for (int i = 0; i < concepts_to_refresh.length; i++) {
          try {
            data_source.populateConcept(
                concepts_to_refresh[i], Ticket.getActionsTicket());
          } catch (MEMEException me) {
            rollback(action);
            throw new ActionException(
                "Failed to populate concept.", action, me);
          }
        }
      }

      //
      // Perform integrity checks, fail if violations exist
      //
      ViolationsVector vv = action.checkFatalIntegrities();
      action.setViolationsVector(vv);
      if (vv.getViolations().getChecks().length > 0) {
        rollback(action);
        throw new IntegrityViolationException(vv);
      }

      //
      // Obtain molecule id. MRD must also call getNextIdentifierForType()
      //
      Identifier molecule_id = null;
      try {
        molecule_id = data_source.getNextIdentifierForType(MolecularAction.class);
      } catch (DataSourceException dse) {
        rollback(action);
        throw new ActionException("Failed to get next id.", action, dse);
      }
      action.setIdentifier(molecule_id);

      //
      // Generate atomic actions
      //
      action.performAction();

      //
      // Perform the action
      //
      super.processAction(action);

      //
      // Insert molecular action row
      //
      try {
        addMolecularAction(action);
      } catch (MEMEException me) {
        rollback(action);
        throw new ActionException("Failed to add molecular action.", action,
                                  me);
      }

      //
      // restore autocommit state
      //
      data_source.restoreAutoCommit();

    } catch (IntegrityViolationException ioe) {
      throw ioe;
    } catch (StaleDataException sde) {
      throw sde;
    } catch (ActionException ac) {
      throw ac;
    } catch (Exception e) {
      rollback(action);
      throw new ActionException("Failed to perform molecular action.", action,
                                e);
    }
  }

  /**
   * Implements {@link ActionEngine#processAction(BatchMolecularTransaction)}.
   * @param transaction the {@link BatchMolecularTransaction} to perform
   * @throws ActionException if failed to process batch molecular transaction
   */
  public void processAction(BatchMolecularTransaction transaction) throws
      ActionException {
    //
    // For batch actions, we should first use LoadDataActions to report
    // contents of the driving table to MRD, then perform the action.
    //
    try {

      //
      // Get action timestamp and start time
      //
      Date timestamp = new Date();
      transaction.setTimestamp(timestamp);

      String table_name = null;

      //
      // Turn autocommit off (restore it before exiting)
      //
      try {
        data_source.setAutoCommit(false);
      } catch (SQLException se) {
        throw new ActionException("Failed to set auto commit.", transaction, se);
      }

      //
      // If we are logging actions, log data used to drive batch action
      //
      if (!data_source.getSystemStatus("log_actions").equals("OFF")) {

        //
        // Handle insert case
        //
        if (transaction.getActionName().equals("I")) {
          //
          // Driving table is the "source" table for the corresponding data type
          //
          table_name = data_source.getValueByCode("table_name",
                                                  "S" +
                                                  transaction.getCoreDataType());
          //
          // Here we want to send a truncate action to MRD
          //
          QueryAction qa = QueryAction.executeQueryAction("truncate table " +
              table_name);
          qa.setAuthority(transaction.getAuthority());
          qa.setTimestamp(timestamp);
          qa.setParent(transaction.getParent());
          logAction(qa);

          //
          // Now send table contents to MRD
          //
          LoadTableAction lta = LoadTableAction.newInsertDataAction(table_name);
          lta.setAuthority(transaction.getAuthority());
          lta.setTimestamp(timestamp);
          lta.setParent(transaction.getParent());
          processAction(lta);

        } else if (!transaction.getActionName().equals("AC")) {

          //
          // For non-insert, non-approval actions, driving table
          // is specified in the action.  Drop and re-create it
          //
          table_name = transaction.getTableName();
          QueryAction qa = QueryAction.executeQueryAction(
              "BEGIN MEME_UTILITY.drop_it( 'table', '" + table_name +
              "'); END;");
          qa.setAuthority(transaction.getAuthority());
          qa.setTimestamp(timestamp);
          qa.setParent(transaction.getParent());
          logAction(qa);

          //
          // Create the table
          //
          LoadTableAction lta = LoadTableAction.newCreateTableAction(table_name);
          lta.setAuthority(transaction.getAuthority());
          lta.setTimestamp(timestamp);
          lta.setParent(transaction.getParent());
          processAction(lta);

          //
          // Now send table contents to MRD
          //
          lta = LoadTableAction.newInsertDataAction(table_name);
          lta.setAuthority(transaction.getAuthority());
          lta.setTimestamp(timestamp);
          lta.setParent(transaction.getParent());
          processAction(lta);
        } else {
          // In the AC case, do nothing
        }
      }
      super.processAction(transaction);

      //
      // restore autocommit state
      //
      data_source.restoreAutoCommit();

    } catch (ActionException ae) {
      throw ae;
    } catch (Exception e) {
      ActionException ae = new ActionException(
          "Failed to perform batch action.", transaction, e);
      throw ae;
    }
  }

  /**
   * Implements {@link ActionEngine#processAction(BatchMolecularTransaction)}.
   * @param action the {@link MacroMolecularAction} to perform
   * @throws ActionException if failed to process batch molecular transaction
   */
  public void processAction(MacroMolecularAction action) throws
      ActionException {

    //
    // For macro actions, we should first use LoadDataActions to report
    // contents of the driving table to MRD, then perform the action.
    //
    try {
      //
      // Get action timestamp and start time
      //
      Date timestamp = new Date();
      action.setTimestamp(timestamp);

      String table_name = null;

      //
      // Turn autocommit off (restore it before exiting)
      //
      try {
        data_source.setAutoCommit(false);
      } catch (SQLException se) {
        throw new ActionException("Failed to set auto commit.", action, se);
      }

      //
      // If we are logging actions, log data used to drive batch action
      //
      if (!data_source.getSystemStatus("log_actions").equals("OFF")) {

        //
        // Handle insert case
        //
        if (action.getActionName().equals("I")) {
          //
          // Driving table is the "source" table for the corresponding data type
          //
          table_name = data_source.getValueByCode("table_name",
                                                  "S" + action.getCoreDataType());
          //
          // Here we want to send a truncate action to MRD
          //
          QueryAction qa = QueryAction.executeQueryAction("truncate table " +
              table_name);
          qa.setAuthority(action.getAuthority());
          qa.setTimestamp(timestamp);
          qa.setParent(action.getParent());
          logAction(qa);

          //
          // Now send table contents to MRD
          //
          LoadTableAction lta = LoadTableAction.newInsertDataAction(table_name);
          lta.setAuthority(action.getAuthority());
          lta.setTimestamp(timestamp);
          lta.setParent(action.getParent());
          processAction(lta);

        } else if (!action.getActionName().equals("AC")) {

          //
          // For non-insert, non-approval actions, driving table
          // is specified in the action.  Drop and re-create it
          //
          table_name = action.getTableName();
          QueryAction qa = QueryAction.executeQueryAction(
              "BEGIN MEME_UTILITY.drop_it( 'table', '" + table_name +
              "'); END;");
          qa.setAuthority(action.getAuthority());
          qa.setTimestamp(timestamp);
          qa.setParent(action.getParent());
          logAction(qa);

          //
          // Create the table
          //
          LoadTableAction lta = LoadTableAction.newCreateTableAction(table_name);
          lta.setAuthority(action.getAuthority());
          lta.setTimestamp(timestamp);
          lta.setParent(action.getParent());
          processAction(lta);

          //
          // Now send table contents to MRD
          //
          lta = LoadTableAction.newInsertDataAction(table_name);
          lta.setAuthority(action.getAuthority());
          lta.setTimestamp(timestamp);
          lta.setParent(action.getParent());
          processAction(lta);
        } else {
          // In the AC case, do nothing
        }
      }

      super.processAction(action);

      //
      // restore autocommit state
      //
      data_source.restoreAutoCommit();

    } catch (ActionException ae) {
      throw ae;
    } catch (Exception e) {
      ActionException ae = new ActionException(
          "Failed to perform macro action.", action, e);
      throw ae;
    }
  }

}
