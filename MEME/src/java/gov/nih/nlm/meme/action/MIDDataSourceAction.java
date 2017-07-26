/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MIDDataSourceAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MIDDataSource;

/**
 * Generically represents an action that calls a {@link gov.nih.nlm.meme.sql.MIDDataSource} method.
 *
 * @author MEME Group
 */

public interface MIDDataSourceAction
    extends LoggedAction {
  /**
   * Reads initial state of any data used in action.  Useful for
   * supporting undo of set actions.
   * @param mds the data source
   * @throws DataSourceException if anything goes wrong
   */
  public void getInitialState(MIDDataSource mds) throws DataSourceException;

  /**
   * Performs the actions.
   * @param mds the data source
   * @throws DataSourceException if anything goes wrong
   */
  public void performAction(MIDDataSource mds) throws DataSourceException;
}
