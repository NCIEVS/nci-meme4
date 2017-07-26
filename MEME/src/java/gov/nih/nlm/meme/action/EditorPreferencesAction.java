/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  EditorPreferencesAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MIDDataSource;

/**
 * Represents an editor preferences action.
 *
 * @author MEME Group
 */

public class EditorPreferencesAction
    extends LoggedAction.Default
    implements MIDDataSourceAction {

  //
  // Fields
  //

  private EditorPreferences ep = null;
  private EditorPreferences old_ep = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link EditorPreferencesAction} with
   * the specified editor preferences.
   * @param ep an object {@link EditorPreferences}
   */
  private EditorPreferencesAction(EditorPreferences ep) {
    this.ep = ep;
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
   * Performs a new add editor preferences action
   * @param ep an object {@link EditorPreferences}
   * @return an object {@link EditorPreferencesAction}
   */
  public static EditorPreferencesAction newAddEditorPreferencesAction(
      EditorPreferences ep) {
    EditorPreferencesAction epa = new EditorPreferencesAction(ep);
    epa.setMode("ADD");
    return epa;
  }

  /**
   * Performs a new remove editor preferences action
   * @param ep an object {@link EditorPreferences}
   * @return an object {@link EditorPreferencesAction}
   */
  public static EditorPreferencesAction newRemoveEditorPreferencesAction(
      EditorPreferences ep) {
    EditorPreferencesAction epa = new EditorPreferencesAction(ep);
    epa.setMode("REMOVE");
    return epa;
  }

  /**
   * Performs a new set editor preferences action
   * @param ep an object {@link EditorPreferences}
   * @return an object {@link EditorPreferencesAction}
   */
  public static EditorPreferencesAction newSetEditorPreferencesAction(
      EditorPreferences ep) {
    EditorPreferencesAction epa = new EditorPreferencesAction(ep);
    epa.setMode("SET");
    return epa;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("ADD"))
      try {
        mds.addEditorPreferences(ep);
      } catch (BadValueException bve) {
        throw new DataSourceException(bve.getMessage(), ep, bve);
      }
    else if (mode.equals("REMOVE"))
      mds.removeEditorPreferences(ep);
    else if (mode.equals("SET"))
      mds.setEditorPreferences(ep);
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    EditorPreferencesAction epa = null;
    if (mode.equals("ADD"))
      epa = newRemoveEditorPreferencesAction(ep);
    else if (mode.equals("REMOVE"))
      epa = newAddEditorPreferencesAction(ep);
    else if (mode.equals("SET"))
      epa = newSetEditorPreferencesAction(old_ep);

    epa.setUndoActionOf(this);
    return epa;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MIDDataSource mds) throws DataSourceException {
    try {
      if (mode.equals("SET"))
        old_ep = mds.getEditorPreferencesByUsername(ep.getUserName());
      else if (mode.equals("REMOVE"))
        ep = mds.getEditorPreferencesByUsername(ep.getUserName());
    } catch (BadValueException bve) {
      throw new DataSourceException(bve.getMessage(), ep, bve);
    }
  }

  /**
   * Determines whether or not is synchronized.
       * @return <code>true</code> if it is synchronized; <code>false</code> otherwise
   */
  public boolean isSynchronizable() {
    return false;
  }

}