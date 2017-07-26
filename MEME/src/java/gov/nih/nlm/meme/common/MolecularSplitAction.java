/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularSplitAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action splits atom out of a concept and create a new concept containing those atoms.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to split atoms out of
     *         one concept and create a new concept containing those atoms.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Concept to be splited</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source concept</li>
 *     <li>No ic checks</li>
 *     <li>Insert a new concept (target)</li>
 *     <li>Move specified atoms to new concept</li>
 *     <li>Unapprove atoms if getChangeStatus is set</li>
 *     <li>If flag is set, copy C level *releasable* relationships as status N</li>
 *     <li>Copy stys (status n)</li>
 *     <li>Insert relationship between source/target</li>
 *     <li>Unapprove target concept if getChangeStatus</li>
 *     <li>MolecularMoveAction</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Create object
 * Concept source_concept = ... get concept ...;<br>
 * Concept target_concept = ... get concept ...;<br>
 * // This atom must come from the source concept.
 * Atom atom_to_split = ... get atom ...;
 * Authority authority = ... get authority ...;
 * Identifier work_id = ... get identifier ...;
 * Identifier transaction_id = ... get identifier ...;<br>
 * // Create & configure action
 * MolecularSplitAction msa = new MolecularSplitAction(source_concept);
 * msa.setSource(source_concept);
 * msa.setTarget(target_concept);
 * msa.setAuthority(authority);
 * msa.setTransactionIdentifier(transaction_id);
 * msa.setWorkIdentifier(wordk_id);
 * msa.setIntegrityVector(null);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.MolecularSplitAction instead
 */

public class MolecularSplitAction extends gov.nih.nlm.meme.action.
    MolecularSplitAction {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MolecularSplitAction} from the specified {@link Concept}.
   * @param concept the source {@link Concept}
   */
  public MolecularSplitAction(Concept concept) {
    super(concept);
  }

}