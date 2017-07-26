/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  BatchAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

/**
 * Generically represents a batch action.  It provides an API
 * for container classes for the parameters necessary to call
 * <a href="/MEME/Documentation/plsql_mba.html#batch_action">
 * <tt>MEME_BATCH_ACTIONS.batch_action</tt></a>.
 *
 * @author MEME Group
 */

public interface BatchAction {

  /**
   * Returns the core data type.
   * @return the core date type
   */
  public String getCoreDataType();

  /**
   * Sets the core data type.
   * @param id_type the core data type
   */
  public void setCoreDataType(String id_type);

  /**
   * Returns the table name.
   * @return the table name
   */
  public String getTableName();

  /**
   * Sets the table name.
   * @param table_name the table name
   */
  public void setTableName(String table_name);

  /**
   * Returns the new value.
   * @return the new value
   */
  public String getNewValue();

  /**
   * Sets the new value.
   * @param new_value the new value
   */
  public void setNewValue(String new_value);

  /**
   * Returns the action field.
   * @return the action field
   */
  public String getActionField();

  /**
   * Sets the action field.
   * @param action_field the action field
   */
  public void setActionField(String action_field);

  /**
   * Returns the rank flag.
   * @return the rank flag
   */
  public boolean getRankFlag();

  /**
   * Sets the rank flag.
   * @param rank_flag the rank flag
   */
  public void setRankFlag(boolean rank_flag);

}
