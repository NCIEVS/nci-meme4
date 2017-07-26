/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  OverrideVectorAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.meme.integrity.IntegrityVector;
import gov.nih.nlm.meme.sql.MIDDataSource;

/**
 * Represents an override vector action.
 *
 * @author MEME Group
 */

public class OverrideVectorAction
    extends LoggedAction.Default
    implements MIDDataSourceAction {

  //
  // Fields
  //

  private int ic_level = 0;
  private IntegrityVector iv = null;
  private IntegrityVector old_iv = null;
  private IntegrityCheck ic = null;
  private String mode = null;
  private String code = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link OverrideVectorAction} with
   * the specified ic level.
   * @param ic_level the ic level
   */
  private OverrideVectorAction(int ic_level) {
    this.ic_level = ic_level;
  }

  /**
   * Instantiates an {@link OverrideVectorAction} with
   * the specified integrity vector.
   * @param ic_level the ic level
   * @param iv an object {@link IntegrityVector}
   */
  private OverrideVectorAction(int ic_level, IntegrityVector iv) {
    this.ic_level = ic_level;
    this.iv = iv;
  }

  /**
   * Instantiates an {@link OverrideVectorAction} with
   * the specified ic level, integrity check and code.
   * @param ic_level the ic level
   * @param ic an object {@link IntegrityCheck}
   * @param code the code
   */
  private OverrideVectorAction(int ic_level, IntegrityCheck ic, String code) {
    this.ic_level = ic_level;
    this.ic = ic;
    this.code = code;
  }

  /**
   * Instantiates an {@link OverrideVectorAction} with
   * the specified ic level and integrity check.
   * @param ic_level the ic level
   * @param ic an object {@link IntegrityCheck}
   */
  private OverrideVectorAction(int ic_level, IntegrityCheck ic) {
    this.ic_level = ic_level;
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
   * Performs a new add override vector action
   * @param ic_level the ic level
   * @param iv an object {@link IntegrityVector}
   * @return an object {@link OverrideVectorAction}
   */
  public static OverrideVectorAction newAddOverrideVectorAction(
      int ic_level, IntegrityVector iv) {
    OverrideVectorAction iva = new OverrideVectorAction(ic_level, iv);
    iva.setMode("ADD");
    return iva;
  }

  /**
   * Performs a new add override vector action
   * @param ic_level the ic level
   * @param ic an object {@link IntegrityCheck}
   * @param code the code
   * @return an object {@link OverrideVectorAction}
   */
  public static OverrideVectorAction newAddCheckToOverrideVectorAction(
      int ic_level, IntegrityCheck ic, String code) {
    OverrideVectorAction iva = new OverrideVectorAction(ic_level, ic, code);
    iva.setMode("ADD_CHECK");
    return iva;
  }

  /**
   * Performs a new remove override vector action
   * @param ic_level the ic level
   * @return an object {@link OverrideVectorAction}
   */
  public static OverrideVectorAction newRemoveOverrideVectorAction(int ic_level) {
    OverrideVectorAction iva = new OverrideVectorAction(ic_level);
    iva.setMode("REMOVE");
    return iva;
  }

  /**
   * Performs a new remove override vector action
   * @param ic_level the ic level
   * @param ic an object {@link IntegrityCheck}
   * @return an object {@link OverrideVectorAction}
   */
  public static OverrideVectorAction newRemoveCheckFromOverrideVectorAction(
      int ic_level, IntegrityCheck ic) {
    OverrideVectorAction iva = new OverrideVectorAction(ic_level, ic);
    iva.setMode("REMOVE_CHECK");
    return iva;
  }

  /**
   * Performs a new set override vector action
   * @param ic_level the ic_level
   * @param iv an object {@link IntegrityVector}
   * @return an object {@link OverrideVectorAction}
   */
  public static OverrideVectorAction newSetOverrideVectorAction(
      int ic_level, IntegrityVector iv) {
    OverrideVectorAction ova = new OverrideVectorAction(ic_level, iv);
    ova.setMode("SET");
    return ova;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds the data source
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      try {
        mds.addOverrideVector(ic_level, iv);
      } catch (BadValueException bve) {
        throw new DataSourceException(bve.getMessage(), iv, bve);
      }
    else if (mode.equals("ADD_CHECK"))
      mds.addCheckToOverrideVector(ic_level, ic, code);
    else if (mode.equals("REMOVE"))
      mds.removeOverrideVector(ic_level);
    else if (mode.equals("REMOVE_CHECK"))
      mds.removeCheckFromOverrideVector(ic_level, ic);
    else if (mode.equals("SET"))
      mds.setOverrideVector(ic_level, iv);
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    OverrideVectorAction ova = null;
    if (mode.equals("ADD"))
      ova = newRemoveOverrideVectorAction(ic_level);
    else if (mode.equals("ADD_CHECK"))
      ova = newRemoveCheckFromOverrideVectorAction(ic_level, ic);
    else if (mode.equals("REMOVE"))
      ova = newAddOverrideVectorAction(ic_level, iv);
    else if (mode.equals("REMOVE_CHECK"))
      ova = newAddCheckToOverrideVectorAction(ic_level, ic, code);
    else if (mode.equals("SET"))
      ova = newSetOverrideVectorAction(ic_level, old_iv);

    ova.setUndoActionOf(this);
    return ova;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("SET"))
      old_iv = mds.getOverrideVector(ic_level);
    else if (mode.equals("REMOVE"))
      iv = mds.getOverrideVector(ic_level);
    else if (mode.equals("REMOVE_CHECK"))
      code = mds.getOverrideVector(ic_level).getCodeForCheck(ic);
  }

  /**
   * Determines whether or not is synchronized.
       * @return <code>true</code> if it is synchronized; <code>false</code> otherwise
   */
  public boolean isSynchronizable() {
    return false;
  }

}