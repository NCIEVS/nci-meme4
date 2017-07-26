/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularMergeAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action merges two concepts together.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to merge two concepts together.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Concept merging into target</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>Concept where source to be merged</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source and Target</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source and target</li>
 *     <li>Move atoms from source to target<br>
 *         &nbsp;&nbsp;&nbsp;
 *         - this causes relationships/attributes connected to atom to move
 *     </li>
 *     <li>Unapprove each atom that moves (if getChangeStatus)</li>
 *     <li>Delete any C or P relationships that will become self-referential
 *         as a result of the merge. i.e. they point from source to target
 *     </li>
 *     <li>Move concept level relationship</li>
 *     <li>Unapprove each C level relationship that moves (if getChangeStatus)</li>
 *     <li>Delete stys from source that are already in target</li>
 *     <li>Move any C level attributes</li>
 *     <li>Unapprove any attributes that move (if getChangeStatus)</li>
 *     <li>Delete source concept</li>
 *     <li>Set status of target<br>
 *         &nbsp;&nbsp;&nbsp;
 *         - if source/target is E, target is E<br>
 *         &nbsp;&nbsp;&nbsp;
 *         - else if getChangeStatus, target is R
 *     </li>
 *     <li>If there are duplicate C level relationships, delete lower ranking ones</li>
 *     <li>Delete all P:N relationships matching C relationships and unapprove any C
 *         relationships matching P:D relationships
 *     </li>
 *     <li>If source.editing_timestamp < target.editing_timestamp<br>
 *         &nbsp;&nbsp;&nbsp;
 *         a. set target.editing_timestamp = source.editing_timestamp<br>
 *         &nbsp;&nbsp;&nbsp;
 *         b. set target.editing_authority = source.editing_authority<br>
 *         &nbsp;&nbsp;&nbsp;
 *         c. set target.editing_approval_molecule_id = source.approval_molecule_id
 *     </li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Create object
 * Concept source_concept = ... get concept ...;
 * Concept target_concept = ... get concept ...;
 * Authority authority = ... get authority ...;
 * Identifier transaction_id = ... get identifier ...;
 * Identifier work_id = ... get identifier ...;<br>
 * // Create & configure action
 * MolecularMergeAction mma = new MolecularMergeAction(source_concept, target_concept);
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
 * @deprecated Use gov.nih.nlm.meme.action.MolecularMergeAction instead
 */

public class MolecularMergeAction extends gov.nih.nlm.meme.action.
    MolecularMergeAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with a source and target.
   * @param source An object {@link Concept}.
   * @param target An object {@link Concept}.
   */
  public MolecularMergeAction(Concept source, Concept target) {
    super(source, target);
  }

}
