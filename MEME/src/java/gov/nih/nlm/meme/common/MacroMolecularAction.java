/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MacroMolecularAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a batch of {@link AtomicAction}s grouped under the
 * same molecule identifier.  Typically, this class is used
 * as a container class for calling
 * <a href="/MEME/Documentation/plsql_mba.html#macro_action">
 * <tt>MEME_BATCH_ACTIONS.macro_action</tt></a>.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.MacroMolecularAction instead
 */

public class MacroMolecularAction extends gov.nih.nlm.meme.action.
    MacroMolecularAction {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MacroMolecularAction} with
   * the specified molecule id.
   * @param molecule_id the molecule id
   */
  public MacroMolecularAction(int molecule_id) {
    super(molecule_id);
  }

  /**
   * Instantiates an empty {@link MacroMolecularAction}.
   */
  public MacroMolecularAction() {
    super();
  }

}