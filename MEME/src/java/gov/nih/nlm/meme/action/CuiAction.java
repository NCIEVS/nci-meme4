/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  CuiAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents a cui action.
 *
 * @author MEME Group
 */

public class CuiAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private Concept source = null;
  private Concept target = null;
  private String mode = null;
  private String log = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link CuiAction} with no parameter.
   */
  private CuiAction() {}

  /**
   * Instantiates a {@link CuiAction} with
   * the specified source concept.
   * @param source an object {@link Concept}
   */
  private CuiAction(Concept source) {
    this.source = source;
  }

  /**
   * Instantiates a {@link CuiAction} with
   * the specified source and target concept.
   * @param source an object {@link Concept}
   * @param target an object {@link Concept}
   */
  private CuiAction(Concept source, Concept target) {
    this.source = source;
    this.target = target;
  }

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
   * Returns an action that will properly assign the CUI to the specified
   * {@link Concept} (and any connected ones).
   * @param source the specified {@link Concept}
   * @return an action that will properly assign the CUI to the specified
   */
  public static CuiAction newAssignCuiAction(Concept source) {
    CuiAction ca = new CuiAction(source);
    ca.setMode("ASSIGN_SOURCE");
    return ca;
  }

  /**
   * Returns an action that will properly assign the CUI to the specified
   * {@link Concept}s (and any connected ones).
   * @param source the specified {@link Concept}
   * @param target the target {@link Concept}
   * @return an action that will properly assign the CUI to the specified
   * concepts
   */
  public static CuiAction newAssignCuiAction(Concept source, Concept target) {
    CuiAction ca = new CuiAction(source, target);
    ca.setMode("ASSIGN_SOURCE_AND_TARGET");
    return ca;
  }

  /**
   * Returns an action that will assign CUIs for the whole database.
   * @return an action that will assign CUIs for the whole database
   */
  public static CuiAction newFullAssignCuiAction() {
    CuiAction ca = new CuiAction();
    ca.setMode("FULL_ASSIGN");
    return ca;
  }

  /**
   * Returns an action that will assign CUIs for the whole database.
   * The specified work identifier will be used.
   * @param work the {@link WorkLog}
   * @return an action that will assign CUIs for the whole database
   */
  public static CuiAction newFullAssignCuiAction(WorkLog work) {
    CuiAction ca = new CuiAction();
    ca.setMode("FULL_ASSIGN");
    ca.setParent(work);
    return ca;

  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("ASSIGN_SOURCE"))
      mds.assignCuis(source);
    else if (mode.equals("ASSIGN_SOURCE_AND_TARGET"))
      mds.assignCuis(source, target);
    else if (mode.equals("FULL_ASSIGN")) {
      if (getParent() == null)
        setParent(mds.newWorkLog(mds.getAuthority("MTH"),
                                 "MAINTENANCE", "ASSIGN_CUIS_FULL"));
      log = mds.assignCuis( (WorkLog)getParent());
    }
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