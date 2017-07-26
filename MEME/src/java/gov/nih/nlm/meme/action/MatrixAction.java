/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MatrixAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents a cui action.
 *
 * @author MEME Group
 */

public class MatrixAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private String mode = null;
  private String log = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MatrixAction} with no parameter.
   */
  private MatrixAction() {}

  //
  // Methods
  //

  /**
   * Set the action mode.
   * @param mode the action mode
   */
  private void setMode(String mode) {
    this.mode = mode;
  }

  /**
   * Performs a new initialize matrix action
   * @param work the {@link WorkLog}
   * @return an object {@link MatrixAction}
   */
  public static MatrixAction newInitializeMatrixAction(WorkLog work) {
    MatrixAction ma = new MatrixAction();
    ma.setMode("INITIALIZE_MATRIX");
    ma.setParent(work);
    return ma;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("INITIALIZE_MATRIX"))
      log = mds.initializeMatrix( (WorkLog)getParent());
  }

  /**
   * @return the most recent initialize matrix log
   */
  public String getLog() {
    return log;
  }

  /**
   * Implements {@link MEMEDataSourceAction#getInitialState(MEMEDataSource)}.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {}

}