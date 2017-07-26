/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularTransaction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;

/**
 * Represents a collection of {@link MolecularAction}s
 * grouped by a transaction identifier.
 *
 * @author MEME Group
 */

public class MolecularTransaction
    extends LoggedAction.Default {

  //
  // Fields
  //
  protected EnforcableIntegrityVector eiv = null;
  protected Identifier work_id = null;

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
    this();
    setIdentifier(new Identifier.Default(transaction_id));
  }

  /**
   * No-argument constructor.
   */
  public MolecularTransaction() {
    super();
  }

  /**
   * Gets the sub actions as {@link MolecularAction} objects.
   * @return An array of {@link MolecularAction}
   */
  public MolecularAction[] getMolecularActions() {
    if (sub_actions == null)
      return new MolecularAction[0];
    else
      return (MolecularAction[])sub_actions.toArray(new MolecularAction[0]);
  }

  /**
   * Gets the work id.  Although, in the model, the parent
   * of the parent of this action is a {@link WorkLog} which
   * is the object represented by the work id. This method exists
   * for convenience.
   * @return A work {@link Identifier}
   */
  public Identifier getWorkIdentifier() {
    return work_id;
  }

  /**
   * Sets the work identifier.  Exists for convenience as the parent
   * is a {@link WorkLog}.
   * @param work_id The work {@link Identifier}
   */
  public void setWorkIdentifier(Identifier work_id) {
    this.work_id = work_id;
    if (getParent() != null) {
      getParent().setIdentifier(work_id);
      getParent().clearSubActions();
    }
  }

  /**
   * Set parent action and populate local work identifier.
   * @param la the parent action
   */
  public void setParent(LoggedAction la) {
    super.setParent(la);
    if (la != null)
      this.work_id = la.getIdentifier();
  }

  /**
   * Gets the integrity vector.
   * @return An object {@link EnforcableIntegrityVector}
   */
  public EnforcableIntegrityVector getIntegrityVector() {
    return eiv;
  }

  /**
   * Sets the integrity vector.
   * @param eiv An object {@link EnforcableIntegrityVector}
   */
  public void setIntegrityVector(EnforcableIntegrityVector eiv) {
    this.eiv = eiv;
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
