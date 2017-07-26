/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MetaCodeAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.MetaCode;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents a meta code action.
 *
 * @author MEME Group
 */

public class MetaCodeAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private MetaCode mcode = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MetaCodeAction} with
   * the specified meta code.
   * @param mcode an object {@link MetaCode}
   */
  private MetaCodeAction(MetaCode mcode) {
    this.mcode = mcode;
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
   * Performs a new add meta code action
   * @param mcode an object {@link MetaCode}
   * @return an object {@link MetaCodeAction}
   */
  public static MetaCodeAction newAddMetaCodeAction(MetaCode mcode) {
    MetaCodeAction mca = new MetaCodeAction(mcode);
    mca.setMode("ADD");
    return mca;
  }

  /**
   * Performs a new remove meta code action
   * @param mcode an object {@link MetaCode}
   * @return an object {@link MetaCodeAction}
   */
  public static MetaCodeAction newRemoveMetaCodeAction(MetaCode mcode) {
    MetaCodeAction mca = new MetaCodeAction(mcode);
    mca.setMode("REMOVE");
    return mca;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      mds.addMetaCode(mcode);
    else if (mode.equals("REMOVE"))
      mds.removeMetaCode(mcode);
  }

  /**
   * Return the inverse action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    MetaCodeAction mca = null;
    if (mode.equals("ADD"))
      mca = newRemoveMetaCodeAction(mcode);
    else if (mode.equals("REMOVE"))
      mca = newAddMetaCodeAction(mcode);

    mca.setUndoActionOf(this);
    return mca;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("REMOVE"))
      mcode = mds.getMetaCode(mcode.getCode(), mcode.getType());
  }

}