/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MIDActionEngine
 *
 *****************************************************************************/
package gov.nih.nlm.mrd.sql;

import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.action.MEMEDataSourceAction;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.ActionEngine;
import gov.nih.nlm.meme.sql.MIDActionEngine;
import gov.nih.nlm.meme.sql.MIDDataSource;

import java.sql.SQLException;
import java.util.Date;

/**
 * Generically represents MID data source.
 *
 * @author MEME Group
 */

public class MRDActionEngine extends ActionEngine.Default {

  /**
   * Instantiates a {@link MIDActionEngine} connected
   * to the specified {@link MIDDataSource}
   * @param mds the {@link MIDDataSource} in which to perform actions
   */
  public MRDActionEngine(MRDDataSource mds) {
    super(mds);
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
      if (action.getTimestamp() == null)
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

  /**
   * Implements {@link ActionEngine#logAction(LoggedAction)}.
   * @param la the {@link LoggedAction}
   * @throws ActionException if failed to add the event
   */
  public void logAction(LoggedAction la) throws ActionException {
    
  }
 
}
