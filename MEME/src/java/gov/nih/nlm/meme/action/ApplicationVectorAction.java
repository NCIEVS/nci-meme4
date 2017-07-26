/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  ApplicationVectorAction
 *
 * 02/07/2006 RBE (1-763IU): Changes to performAction to handle 
 * 													 BadValueException in add mode.
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
 * Represents an application vector action.
 *
 * @author MEME Group
 */

public class ApplicationVectorAction
    extends LoggedAction.Default
    implements MIDDataSourceAction {

  //
  // Fields
  //

  private String application = null;
  private IntegrityVector iv = null;
  private IntegrityVector old_iv = null;
  private IntegrityCheck ic = null;
  private String code = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link ApplicationVectorAction} with
   * the specified application.
   * @param application the application
   */
  private ApplicationVectorAction(String application) {
    this.application = application;
  }

  /**
   * Instantiates an {@link ApplicationVectorAction} with
   * the specified application and integrity vector.
   * @param application the application
   * @param iv an object {@link IntegrityVector}
   */
  private ApplicationVectorAction(String application, IntegrityVector iv) {
    this.application = application;
    this.iv = iv;
  }

  /**
   * Instantiates an {@link ApplicationVectorAction} with
   * the specified application, integrity check and code.
   * @param application the application
   * @param ic an object {@link IntegrityCheck}
   * @param code the code
   */
  private ApplicationVectorAction(String application, IntegrityCheck ic,
                                  String code) {
    this.application = application;
    this.ic = ic;
    this.code = code;
  }

  /**
   * Instantiates an {@link ApplicationVectorAction} with
   * the specified application and integrity check.
   * @param application the application
   * @param ic an object {@link IntegrityCheck}
   */
  private ApplicationVectorAction(String application, IntegrityCheck ic) {
    this.application = application;
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
   * Performs a new add application vector action
   * @param application the application
   * @param iv an object {@link IntegrityVector}
   * @return an object {@link ApplicationVectorAction}
   */
  public static ApplicationVectorAction newAddApplicationVectorAction(
      String application, IntegrityVector iv) {
    ApplicationVectorAction ava = new ApplicationVectorAction(application, iv);
    ava.setMode("ADD");
    return ava;
  }

  /**
   * Performs a new add check to application vector action
   * @param application the application
   * @param ic an object {@link IntegrityCheck}
   * @param code the code
   * @return an object {@link ApplicationVectorAction}
   */
  public static ApplicationVectorAction newAddCheckToApplicationVectorAction(
      String application, IntegrityCheck ic, String code) {
    ApplicationVectorAction ava = new ApplicationVectorAction(application, ic,
        code);
    ava.setMode("ADD_CHECK");
    return ava;
  }

  /**
   * Performs a new remove application vector action
   * @param application the application
   * @return an object {@link ApplicationVectorAction}
   */
  public static ApplicationVectorAction newRemoveApplicationVectorAction(String
      application) {
    ApplicationVectorAction ava = new ApplicationVectorAction(application);
    ava.setMode("REMOVE");
    return ava;
  }

  /**
   * Performs a new remove check from application vector action
   * @param application the application
   * @param ic an object {@link IntegrityCheck}
   * @return an object {@link ApplicationVectorAction}
   */
  public static ApplicationVectorAction
      newRemoveCheckFromApplicationVectorAction(
      String application, IntegrityCheck ic) {
    ApplicationVectorAction ava = new ApplicationVectorAction(application, ic);
    ava.setMode("REMOVE_CHECK");
    return ava;
  }

  /**
   * Performs a new set application vector action
   * @param application the application
   * @param iv an object {@link IntegrityCheck}
   * @return an object {@link ApplicationVectorAction}
   */
  public static ApplicationVectorAction newSetApplicationVectorAction(
      String application, IntegrityVector iv) {
    ApplicationVectorAction ava = new ApplicationVectorAction(application, iv);
    ava.setMode("SET");
    return ava;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds the data source
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      try {
        mds.addApplicationVector(application, iv);
      } catch (BadValueException bve) {
        throw new DataSourceException(bve.getMessage(), iv, bve);
      }
    else if (mode.equals("ADD_CHECK"))
      mds.addCheckToApplicationVector(application, ic, code);
    else if (mode.equals("REMOVE"))
      mds.removeApplicationVector(application);
    else if (mode.equals("REMOVE_CHECK"))
      mds.removeCheckFromApplicationVector(application, ic);
    else if (mode.equals("SET"))
      mds.setApplicationVector(application, iv);
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    ApplicationVectorAction ava = null;
    if (mode.equals("ADD"))
      ava = newRemoveApplicationVectorAction(application);
    else if (mode.equals("ADD_CHECK"))
      ava = newRemoveCheckFromApplicationVectorAction(application, ic);
    else if (mode.equals("REMOVE"))
      ava = newAddApplicationVectorAction(application, iv);
    else if (mode.equals("REMOVE_CHECK"))
      ava = newAddCheckToApplicationVectorAction(application, ic, code);
    else if (mode.equals("SET"))
      ava = newSetApplicationVectorAction(application, old_iv);

    ava.setUndoActionOf(this);
    return ava;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("SET"))
      old_iv = mds.getApplicationVector(application);
    else if (mode.equals("REMOVE"))
      iv = mds.getApplicationVector(application);
    else if (mode.equals("REMOVE_CHECK"))
      code = mds.getApplicationVector(application).getCodeForCheck(ic);
  }

  /**
   * Determines whether or not is synchronized.
       * @return <code>true</code> if it is synchronized; <code>false</code> otherwise
   */
  public boolean isSynchronizable() {
    return false;
  }

}