/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomicAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This class generically represents an atomic action, which is
 * typically part of a {@link MolecularAction}.
 *
 * This class is associated with the <code>atomic_actions</code>
 * table in the <i>MID</i>.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.AtomicAction instead
 */

public class AtomicAction extends gov.nih.nlm.meme.action.AtomicAction {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link AtomicAction} with the specified atomic action
   * identifier.
   * @param atomic_action_id an <code>int</code> representation of the
   *                          atomic action identifier
   */
  public AtomicAction(int atomic_action_id) {
    super(atomic_action_id);
  }

  /**
   * Instantiates an empty {@link AtomicAction}.
   */
  public AtomicAction() {
    super();
  }

}
