/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  BatchMolecularTransaction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a series of {@link MolecularAction}s grouped under the
 * same transaction identifier.  Typically, this class is used
 * as a container class for calling
 * <a href="/MEME/Documentation/plsql_mba.html#batch_action">
 * <tt>MEME_BATCH_ACTIONS.batch_action</tt></a>.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.BatchMolecularTransaction instead
 */

public class BatchMolecularTransaction extends gov.nih.nlm.meme.action.
    BatchMolecularTransaction {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link BatchMolecularTransaction} with the
   * specified transaction identifier.
   * @param transaction_id the unique identifier for this transaction
   */
  public BatchMolecularTransaction(int transaction_id) {
    super(transaction_id);
  }

  /**
   * Instantiates an empty {@link BatchMolecularTransaction}.
   */
  public BatchMolecularTransaction() {
    super();
  }

}