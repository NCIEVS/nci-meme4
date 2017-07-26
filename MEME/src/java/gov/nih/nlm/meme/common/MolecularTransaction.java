/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularTransaction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a collection of {@link MolecularAction}s
 * grouped by a transaction identifier.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.MolecularTransaction instead
 */

public class MolecularTransaction extends gov.nih.nlm.meme.action.
    MolecularTransaction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with an
   * transaction_id.
   * @param transaction_id An <code>int</code> representation of the
   * unique id representing this action.
   */
  public MolecularTransaction(int transaction_id) {
    super(transaction_id);
  }

  /**
   * No-argument constructor.
   */
  public MolecularTransaction() {
    super();
  }

}
