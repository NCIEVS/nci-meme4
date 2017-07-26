/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  ExecAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.ExecException;
import gov.nih.nlm.meme.sql.MEMEDataSource;

/**
 * Represents an SQL exec action.
 *
 * @author MEME Group
 */

public class ExecAction
    extends LoggedAction.Default
    implements MEMEDataSourceAction {

  //
  // Fields
  //

  private String[] command = null;
  private String[] inverse_command = null;
  private String[] env = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ExecAction} with the specified parameters.
   * @param command the list of command
   * @param env the list of environment
   */
  private ExecAction(String[] command, String[] env) {
    this.command = command;
    this.env = env;
  }

  /**
   * Instantiates a {@link ExecAction} with the specified parameters.
   * @param command the list of command
   * @param inverse_command the list of inverse command
   * @param env the list of environment
   */
  private ExecAction(String[] command, String[] inverse_command, String[] env) {
    this.command = command;
    this.inverse_command = inverse_command;
    this.env = env;
  }

  //
  // Methods
  //

  /**
   * Performs a new exec action
   * @param command the list of command
   * @param env the list of environment
   * @return an object {@link ExecAction}
   */
  public static ExecAction newExecAction(String[] command, String[] env) {
    ExecAction ca = new ExecAction(command, env);
    return ca;
  }

  /**
   * Performs a new exec action
   * @param command the list of command
   * @param inverse_command the list of inverse command
   * @param env the list of environment
   * @return an object {@link ExecAction}
   */
  public static ExecAction newExecAction(String[] command,
                                         String[] inverse_command, String[] env) {
    ExecAction ca = new ExecAction(command, inverse_command, env);
    return ca;
  }

  /**
   * Performs an action using the specified data source.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void performAction(MEMEDataSource mds) throws DataSourceException {
    try {
      int len = (env == null) ? 0 : env.length;
      String[] env2 = new String[len + 1];
      for (int i = 0; i < len; i++) {
        env2[i] = env[i];
      }
      env2[len] = "DB=" + mds.getDataSourceName();
      MEMEToolkit.exec(command, env2);
    } catch (ExecException ee) {
      DataSourceException dse = new DataSourceException(ee.getMessage());
      dse.setEnclosedException(ee);
      throw dse;
    }
  }

  /**
   * Return the inverse of this action.
   * @return {@link LoggedAction}
   * @throws ActionException if failed to get inverse action
   */
  public LoggedAction getInverseAction() throws ActionException {
    ExecAction ea = null;
    if (inverse_command != null)
      ea = newExecAction(inverse_command, command, env);
    else if (inverse_command == null) {
      return new NonOperationAction();
    }

    ea.setUndoActionOf(this);
    return ea;
  }

  /**
   * Return the initial state.
   * @param mds an object {@link MEMEDataSource}
   * @throws DataSourceException if perform action failed
   */
  public void getInitialState(MEMEDataSource mds) throws DataSourceException {}

}