/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  IntegrityCheckAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.meme.sql.MIDDataSource;

/**
 * Represents an integrity check action.
 *
 * @author MEME Group
 */

public class IntegrityCheckAction
    extends LoggedAction.Default
    implements MIDDataSourceAction {

  //
  // Fields
  //

  private IntegrityCheck ic = null;
  private IntegrityCheck old_ic = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link IntegrityCheckAction} with
   * the specified integrity check.
   * @param ic an object {@link IntegrityCheck}
   */
  private IntegrityCheckAction(IntegrityCheck ic) {
    this.ic = ic;
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
   * Performs a new add integrity check action
   * @param ic an object {@link IntegrityCheck}
   * @return an object {@link IntegrityCheckAction}
   */
  public static IntegrityCheckAction newAddIntegrityCheckAction(IntegrityCheck
      ic) {
    IntegrityCheckAction ica = new IntegrityCheckAction(ic);
    ica.setMode("ADD");
    return ica;
  }

  /**
   * Performs a new remove integrity check action
   * @param ic an object {@link IntegrityCheck}
   * @return an object {@link IntegrityCheckAction}
   */
  public static IntegrityCheckAction newRemoveIntegrityCheckAction(
      IntegrityCheck ic) {
    IntegrityCheckAction ica = new IntegrityCheckAction(ic);
    ica.setMode("REMOVE");
    return ica;
  }

  /**
   * Performs a new set integrity check action
   * @param ic an object {@link IntegrityCheck}
   * @return an object {@link IntegrityCheckAction}
   */
  public static IntegrityCheckAction newSetIntegrityCheckAction(IntegrityCheck
      ic) {
    IntegrityCheckAction ica = new IntegrityCheckAction(ic);
    ica.setMode("SET");
    return ica;
  }

  /**
   * Performs a new set activate integrity check action
   * @param ic an object {@link IntegrityCheck}
   * @return an object {@link IntegrityCheckAction}
   */
  public static IntegrityCheckAction newSetActivateIntegrityCheckAction(
      IntegrityCheck ic) {
    IntegrityCheckAction ica = new IntegrityCheckAction(ic);
    ica.setMode("ACTIVATE");
    return ica;
  }

  /**
   * Performs a new set deactivate integrity check action
   * @param ic an object {@link IntegrityCheck}
   * @return an object {@link IntegrityCheckAction}
   */
  public static IntegrityCheckAction newSetDeactivateIntegrityCheckAction(
      IntegrityCheck ic) {
    IntegrityCheckAction ica = new IntegrityCheckAction(ic);
    ica.setMode("DEACTIVATE");
    return ica;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      try {
        mds.addIntegrityCheck(ic);
      } catch (BadValueException bve) {
        throw new DataSourceException(bve.getMessage(), ic, bve);
      }
    else if (mode.equals("REMOVE"))
      mds.removeIntegrityCheck(ic);
    else if (mode.equals("SET"))
      mds.setIntegrityCheck(ic);
    else if (mode.equals("ACTIVATE"))
      mds.activateIntegrityCheck(ic);
    else if (mode.equals("DEACTIVATE"))
      mds.deactivateIntegrityCheck(ic);
  }

  /**
   * Return the inverse action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    IntegrityCheckAction ica = null;
    if (mode.equals("ADD"))
      ica = newRemoveIntegrityCheckAction(ic);
    else if (mode.equals("REMOVE"))
      ica = newAddIntegrityCheckAction(ic);
    else if (mode.equals("SET"))
      ica = newSetIntegrityCheckAction(old_ic);
    else if (mode.equals("ACTIVATE"))
      ica = newSetDeactivateIntegrityCheckAction(ic);
    else if (mode.equals("DEACTIVATE"))
      ica = newSetActivateIntegrityCheckAction(ic);

    ica.setUndoActionOf(this);
    return ica;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("SET"))
      old_ic = mds.getIntegrityCheck(ic.getName());
    else if (mode.equals("REMOVE"))
      ic = mds.getIntegrityCheck(ic.getName());
  }

  /**
   * Determines whether or not is synchronized.
       * @return <code>true</code> if it is synchronized; <code>false</code> otherwise
   */
  public boolean isSynchronizable() {
    return false;
  }

}