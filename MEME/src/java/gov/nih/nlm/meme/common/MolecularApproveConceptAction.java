/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularApproveConceptAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action approves concept.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to approve a concept.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Concept to be approved</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source concept</li>
 *     <li>If concept editing authority is null or does not match action authority<br>
 *         &nbsp;&nbsp;&nbsp;
 *         a. update editing authority
 *     </li>
 *     <li>Update editing timestamp</li>
 *     <li>Update approval_molecule_id</li>
 *     <li>Set status of concept to R</li>
     *     <li>For each atom in the concept approve it if not already approved or if
 *         the authority is like ENG-%
 *     </li>
 *     <li>For each attribute in the concept approve it if not already approved</li>
 *     <li>For each relationship in the concept<br>
 *         &nbsp;&nbsp;&nbsp;
 *         a. If the relationship is concept level,<br>
 *            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *            i. Change the relationship name to RT if it is LK or RT?<br>
 *            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *            ii. Change the status to R if not already approved<br>
 *         &nbsp;&nbsp;&nbsp;
 *         b. If the relationship is P level<br>
 *            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *            i. If a matching concept level relationship is found then delete
 *               the P level relationship<br>
 *            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *            ii. If a matching concept level relationship is NOT found and
 *               the relationship is not a demotion and a matching concept
 *               level relationship has not yet been inserted
 *               then insert a C level relationship mathing the P and delete the P
 *     </li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Create object
 * Concept source = ... get concept to approve ...;
 *
 * // Create & configure action
 * MolecularApproveConceptAction maca = new MolecularApproveConceptAction(source);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
     * @deprecated Use gov.nih.nlm.meme.action.MolecularApproveConceptAction instead
 */

public class MolecularApproveConceptAction extends gov.nih.nlm.meme.action.
    MolecularApproveConceptAction {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MolecularApproveConceptAction}
   * with the specified source {@link Concept}
   * @param concept the source {@link Concept}
   */
  public MolecularApproveConceptAction(Concept concept) {
    super(concept);
  }

}
