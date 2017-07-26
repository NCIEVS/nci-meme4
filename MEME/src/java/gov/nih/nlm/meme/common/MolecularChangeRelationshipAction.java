/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularChangeRelationshipAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action changes relationship.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to change a relationship.   Typically this
 *         is used to change releasabiltiy, status, suppressibility, or some
 *         other characteristic of the attribute.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Relationship to be changed</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source concept</li>
 *     <li>Get all relationships, find the one matching the one being changed</li>
 *     <li>If status values are different update status to new value<br>
 *         &nbsp;&nbsp;&nbsp;
 *         a. If concept level and new status is R delete any matching P relationship,
 *            change relationship name from RT?, LK to RT<br>
 *         &nbsp;&nbsp;&nbsp;
 *         b. If source level and new status is R delete any matching P relationship
 *     </li>
 *     <li>If releasability values are different, update to new value</li>
 *     <li>If suppressibility values are different, update to new value</li>
 *     <li>If relationship name values are different, update to new value</li>
 *     <li>If relationship attribute values are different, update to new value</li>
 *     <li>If getChangeStatus unapprove concept</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Obtain relationship reference
 * Relationship relationship = ... get relationship ...;
 * relationship.setConcept(concept);
 *
 * // Change releasability to 'Y'
 * relationship.setTobereleased(FV_RELEASABLE);
 *
 * // Change status to 'R'
 * relationship.setStatus(FV_REVIEWED);
 *
 * // Create & configure action
 * MolecularChangeRelationshipAction mcra = new MolecularChangeRelationshipAction(relationship);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.MolecularChangeRelationshipAction instead
 */

public class MolecularChangeRelationshipAction extends gov.nih.nlm.meme.action.
    MolecularChangeRelationshipAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with a relationship.
   * @param relationship An object {@link Relationship}.
   */
  public MolecularChangeRelationshipAction(Relationship relationship) {
    super(relationship);
  }

}
