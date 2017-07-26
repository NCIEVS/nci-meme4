/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a molecular action and is
 * typically has a {@link MolecularTransaction} as a parent and
 * one or more {@link AtomicAction}s as children.
 *
 * There is a contract for using a molecular action.  It requires
 * that applications instantiating actions (for use in the <i>MID</i>)
 * must set the work identifier, transaction identifierd, authority,
 * integrity vector, the change status flag, and the source/target concepts.
 *
 * Subclasses may have specific additional requirements.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.MolecularAction instead
 */

public class MolecularAction extends gov.nih.nlm.meme.action.MolecularAction {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MolecularAction} with the specified
   * molecule id.
   * @param molecule_id the <code>int</code> molecule id
   */
  public MolecularAction(int molecule_id) {
    super(molecule_id);
  }

  /**
   * Instantiates an empty {@link MolecularAction}.
   */
  public MolecularAction() {
    super();
  }

}
