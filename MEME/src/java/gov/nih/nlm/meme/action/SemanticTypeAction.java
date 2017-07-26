/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  SemanticTypeAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents a semantic type action.
 *
 * @author MEME Group
 */

public class SemanticTypeAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private SemanticType sty = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link SemanticTypeAction} with
   * the specified semantic type.
   * @param sty an object {@link SemanticType}
   */
  private SemanticTypeAction(SemanticType sty) {
    this.sty = sty;
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
   * Performs a new add SemanticType action
   * @param sty an object {@link SemanticType}
   * @return an object {@link SemanticTypeAction}
   */
  public static SemanticTypeAction newAddSemanticTypeAction(SemanticType sty) {
    SemanticTypeAction ca = new SemanticTypeAction(sty);
    ca.setMode("ADD");
    return ca;
  }

  /**
   * Performs a new remove SemanticType action
   * @param sty an object {@link SemanticType}
   * @return an object {@link SemanticTypeAction}
   */
  public static SemanticTypeAction newRemoveSemanticTypeAction(SemanticType sty) {
    SemanticTypeAction ca = new SemanticTypeAction(sty);
    ca.setMode("REMOVE");
    return ca;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      mds.addValidSemanticType(sty);
    else if (mode.equals("REMOVE"))
      mds.removeValidSemanticType(sty);
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    SemanticTypeAction sta = null;
    if (mode.equals("ADD"))
      sta = newRemoveSemanticTypeAction(sty);
    else if (mode.equals("REMOVE"))
      sta = newAddSemanticTypeAction(sty);

    sta.setUndoActionOf(this);
    return sta;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {
    try {
      if (mode.equals("REMOVE"))
        sty = mds.getSemanticType(sty.getValue());
    } catch (BadValueException bve) {
      DataSourceException dse = new DataSourceException(
          "Failed to initialize state.", sty, bve);
      throw dse;

    }
  }

}