/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularMoveAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action moves atoms from one concept to another.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to move atoms from one concept to another.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Concept to be moved into target</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>Concept where source to be moved</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source and target</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source and target</li>
 *     <li>Apply move inhibitors?</li>
 *     <li>Move all specified atoms from source to target, this should
 *         already move attributes/relationships
 *     </li>
 *     <li>Unapprove atoms if getChangeStatus is set</li>
 *     <li>If moving atoms creates self-referential P relationships, delete them</li>
     *     <li>If moving atoms causes a P level relationships to match a C, delete it
 *         if status N, unapprove the C if status D.
 *     </li>
 *     <li>Unapprove target concept if getChangeStatus</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Create object
 * Concept source_concept = ... get concept ...;
 * Concept target_concept = ... get concept ...;<br>
 * // this atom must come from the source concept
 * Atom atom_to_move = ... get atom ...;
 * Authority authority = ... get authority ...;
 * Identifier work_id = ... get identifier ...;
 * Identifier transaction_id = ... get identifier ...;<br>
 * // Create & configure action
 * MolecularMoveAction mma = new MolecularMoveAction(source_concept, target_concept);
 * mma.setTransactionIdentifier(transaction_id);
 * mma.setWorkIdentifier(work_id);
 * mma.setSource(source_concept);
 * mma.setTarget(target_concept);
 * mma.setAuthority(authority);
 * mma.setIntegrityVector(null);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.MolecularMoveAction instead
 */

public class MolecularMoveAction extends gov.nih.nlm.meme.action.
    MolecularMoveAction {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MolecularMoveAction} from the specified source
   * and target {@link Concept}s.
   * @param source the source {@link Concept}.
   * @param target the target {@link Concept}.
   */
  public MolecularMoveAction(Concept source, Concept target) {
    super(source, target);
  }

}
