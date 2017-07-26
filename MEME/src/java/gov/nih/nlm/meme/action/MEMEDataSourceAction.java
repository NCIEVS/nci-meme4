/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MEMEDataSourceAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Generically represents an action that calls a {@link gov.nih.nlm.meme.sql.MEMEDataSource} method.
 *
 * @author MEME Group
 */
public interface MEMEDataSourceAction
    extends LoggedAction {

  /**
   * Reads initial state of any data used in action.  Useful for
   * supporting undo of set actions.
   * @param mds the data source
   * @throws DataSourceException if anything goes wrong
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException;

  /**
   * Performs the actions.
   * @param mds the data source
   * @throws DataSourceException if anything goes wrong
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException;
}