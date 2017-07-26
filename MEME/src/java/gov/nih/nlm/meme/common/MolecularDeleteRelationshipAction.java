/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularDeleteRelationshipAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action deletes relationship.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to delete relationship.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Relationship to be deleted</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>None</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign ="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Do not refresh any concepts (but expect getSource().getStatus() to be correct).</li>
 *     <li>Delete relationship</li>
 *     <li>If getChangeStatus unapprove concept</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Obtain relationshipreference
 * Relationship relationship = ... get relationship ...;
 * relationship.setConcept(concept);
 *
 * // Create & configure action
 * MolecularDeleteRelationshipAction mdra = new MolecularDeleteRelationshipAction(relationship);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.MolecularDeleteRelationshipAction instead
 */

public class MolecularDeleteRelationshipAction extends gov.nih.nlm.meme.action.
    MolecularDeleteRelationshipAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with a relationship.
   * @param relationship An object {@link Relationship}.
   */
  public MolecularDeleteRelationshipAction(Relationship relationship) {
    super(relationship);
  }

}
