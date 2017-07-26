/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  SystemStatusAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.sql.MIDDataSource;

/**
 * Represents a system status action.
 *
 * @author MEME Group
 */

public class SystemStatusAction
    extends LoggedAction.Default
    implements MIDDataSourceAction {

  //
  // Fields
  //

  private String key = null;
  private String value = null;
  private String old_value = null;
  private String mode = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link SystemStatusAction} with no parameter.
   */
  private SystemStatusAction() {}

  /**
   * Instantiates a {@link SystemStatusAction} with
   * the specified source and target concept.
   * @param key the key
   * @param value the value
   */
  private SystemStatusAction(String key, String value) {
    this.key = key;
    this.value = value;
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
   * Performs a new set system status action
   * @param key the key
   * @param value the value
   * @return an object {@link SystemStatusAction}
   */
  public static SystemStatusAction newSetSystemStatusAction(String key,
      String value) {
    SystemStatusAction ssa = new SystemStatusAction(key, value);
    ssa.setMode("SET");
    return ssa;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("SET"))
      mds.setSystemStatus(key, value);

  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    SystemStatusAction ssa = null;
    if (mode.equals("SET"))
      ssa = newSetSystemStatusAction(key, old_value);

    ssa.setUndoActionOf(this);
    return ssa;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MIDDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MIDDataSource mds) throws DataSourceException {
    if (mode.equals("SET"))
      old_value = mds.getSystemStatus(key);
  }

}