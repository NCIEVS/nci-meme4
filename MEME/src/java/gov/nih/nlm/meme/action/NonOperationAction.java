/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  NonOperationAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents a non operation action.
 *
 * @author MEME Group
 */

public class NonOperationAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  /**
   * Instantiates a {@link NonOperationAction} with no parameter.
   */
  public NonOperationAction() {
    setIsImplied(true);
  }

  //
  // Methods
  //

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {}

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    return this;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {}

}