/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  BatchMolecularTransaction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Identifier;

/**
 * Represents a series of {@link MolecularAction}s grouped under the
 * same transaction identifier.  Typically, this class is used
 * as a container class for calling
 * <a href="/MEME/Documentation/plsql_mba.html#batch_action">
 * <tt>MEME_BATCH_ACTIONS.batch_action</tt></a>.
 *
 * @author MEME Group
 */

public class BatchMolecularTransaction
    extends MolecularTransaction
    implements BatchAction {

  //
  // Fields
  //

  private String id_type = null;
  private String table_name = null;
  private String new_value = null;
  private String action_field = "NONE";
  private boolean rank_flag = true;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link BatchMolecularTransaction} with the
   * specified transaction identifier.
   * @param transaction_id the unique identifier for this transaction
   */
  public BatchMolecularTransaction(int transaction_id) {
    this();
    setIdentifier(new Identifier.Default(transaction_id));
  }

  /**
   * Instantiates an empty {@link BatchMolecularTransaction}.
   */
  public BatchMolecularTransaction() {
    super();
  }

  //
  // Methods
  //

  /**
   * Implements {@link BatchAction#getCoreDataType()}.
   */
  public String getCoreDataType() {
    return id_type;
  }

  /**
   * Implements {@link BatchAction#setCoreDataType(String)}.
   */
  public void setCoreDataType(String id_type) {
    this.id_type = id_type;
  }

  /**
   * Implements {@link BatchAction#getTableName()}.
   */
  public String getTableName() {
    return table_name;
  }

  /**
   * Implements {@link BatchAction#setTableName(String)}.
   */
  public void setTableName(String table_name) {
    this.table_name = table_name;
  }

  /**
   * Implements {@link BatchAction#getNewValue()}.
   */
  public String getNewValue() {
    return new_value;
  }

  /**
   * Implements {@link BatchAction#setNewValue(String)}.
   */
  public void setNewValue(String new_value) {
    this.new_value = new_value;
  }

  /**
   * Implements {@link BatchAction#getActionField()}.
   */
  public String getActionField() {
    return action_field;
  }

  /**
   * Implements {@link BatchAction#setActionField(String)}.
   */
  public void setActionField(String action_field) {
    this.action_field = action_field;
  }

  /**
   * Implements {@link BatchAction#getRankFlag()}.
   */
  public boolean getRankFlag() {
    return rank_flag;
  }

  /**
   * Implements {@link BatchAction#setRankFlag(boolean)}.
   */
  public void setRankFlag(boolean rank_flag) {
    this.rank_flag = rank_flag;
  }

  /**
   * Returns the inverse of this action.
   * @return the inverse of this action
   */
  public LoggedAction getInverseAction() {
    LoggedAction la = TransactionAction.newUndoTransactionAction(getIdentifier());
    la.setUndoActionOf(this);
    return la;
  }

}