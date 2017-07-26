/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularDeleteRelationshipAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.ActionException;

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
 */

public class MolecularDeleteRelationshipAction
    extends MolecularAction {

  //
  // Fields
  //

  private Relationship relationship = null;

  //
  // Constructors
  //

  /**
   * No-argument constructor.
   */
  private MolecularDeleteRelationshipAction() {
    super();
  }

  /**
   * This constructor initializes the action with a relationship.
   * @param relationship An object {@link Relationship}.
   */
  public MolecularDeleteRelationshipAction(Relationship relationship) {
    super();
    this.relationship = relationship;
    setSource(relationship.getConcept());
    setActionName("MOLECULAR_DELETE");
    setAssignCuis(false);
  }

  //
  // Methods
  //

  /**
   * Returns a concept to refresh.
   * @return An array of object {@link Concept}
   */
  public Concept[] getConceptsToRefresh() {
    if (getChangeStatus() && !getSource().needsReview())
      return new Concept[] {
          getSource()};
    return new Concept[0];
  }

  /**
   * Returns a relationship to delete.
   * @return An object {@link Relationship}
   */
  public Relationship getRelationshipToDelete() {
    return relationship;
  }

  /**
   * Performs molecular delete relationship action.
   * @throws ActionException if failed while performing
   * molecular delete relationship action.
   */
  public void performAction() throws ActionException {

    Relationship[] rels = getSource().getRelationships();
    for (int i = 0; i < rels.length; i++) {
      if (rels[i].equals(relationship)) {
        Attribute[] attrs = rels[i].getAttributes();
        for (int j = 0; j < attrs.length; j++) {
          AtomicDeleteAction ada = new AtomicDeleteAction(attrs[j]);
          addSubAction(ada);
        }
        break;
      }
    }

    AtomicDeleteAction ada = new AtomicDeleteAction(relationship);
    addSubAction(ada);

    // If getChangeStatus, unapprove concept if not approve
    if (getChangeStatus() && !getSource().needsReview()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(getSource());
      acsa.setNewValue("N");
      addSubAction(acsa);
    }

  }

}
