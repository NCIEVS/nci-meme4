/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularInsertRelationshipAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action inserts relationship.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to insert relationship.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Relationship to be inserted</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source, if the relationship to insert is level C or P, nothing otherwise.</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source concept (because we need to compare against other rels)</li>
 *     <li>If relationship is self-referential and C or P level,
 *         do nothing, Else insert relationship</li>
 *     <li>If relationship if C level, delete any P or C relationship matching it</li>
 *     <li>If getChangeStatus unapprove concept</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Create relationship reference
 * Relationship relationship = new Relationship.Default();
 * relationship.setConcept(concept);
 * relationship.setRelatedConcept(concept2);
 * relationship.setLevel('C');
 * relationship.setRelationshipName("RT");
 * relationship.setRelationshipAttribute(null);
 * relationship.setTobereleased(CoreData.FV_RELEASABLE);
 * relationship.setStatus(CoreData.FV_NEEDS_REVIEW);
 * relationship.setReleased(CoreData.FV_RELEASED_AS_APPROVED);
 * relationship.setSource(...);
 * relationship.setSourceOfLabel(...);
 * ...
 *
 * // Create & configure action
 * MolecularInsertRelationshipAction mira = new MolecularInsertRelationshipAction(relationship);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.MolecularInsertRelationshipAction instead
 */

public class MolecularInsertRelationshipAction extends gov.nih.nlm.meme.action.
    MolecularInsertRelationshipAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with a relationship.
   * @param relationship An object {@link Relationship}.
   */
  public MolecularInsertRelationshipAction(Relationship relationship) {
    super(relationship);
  }

}
