/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularInsertRelationshipAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.ActionException;

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
 */

public class MolecularInsertRelationshipAction
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
  private MolecularInsertRelationshipAction() {
    super();
  }

  /**
   * This constructor initializes the action with a relationship.
   * @param relationship An object {@link Relationship}.
   */
  public MolecularInsertRelationshipAction(Relationship relationship) {
    super();
    this.relationship = relationship;
    setSource(relationship.getConcept());
    setTarget(relationship.getRelatedConcept());
    setActionName("MOLECULAR_INSERT");
    setAssignCuis(false);
  }

  //
  // Methods
  //

  /**
   * Returns a concept to refresh.
   * @return An array of object {@link Relationship}
   */
  public Concept[] getConceptsToRefresh() {
    if (relationship.isSourceAsserted() &&
        !relationship.getName().equals("SFO/LFO"))
      return new Concept[0];
    else
      return new Concept[] {
          getSource()};
  }

  /**
   * Returns a relationship to insert.
   * @return An object {@link Relationship}
   */
  public Relationship getRelationshipToInsert() {
    return relationship;
  }

  /**
   * Performs molecular insert relationship action.
   * @throws ActionException if failed while performing
   * molecular insert relationship action.
   */
  public void performAction() throws ActionException {

    // Delete matching C level or P level relationships

    // When inserting a concept level relationship
    // Remove any existing P or C level relationships
    // that have a matching related concept
    if (relationship.isConceptLevel()) {
      Relationship[] rels = getSource().getRelationships();
      for (int i = 0; i < rels.length; i++) {
        if (!rels[i].isSourceAsserted() &&
            rels[i].getRelatedConcept().equals(relationship.getRelatedConcept())) {
          AtomicDeleteAction ada = new AtomicDeleteAction(rels[i]);
          addSubAction(ada);
        }
      }
    }

    // When inserting a concept level or P level relationship
    // do nothing if the relationship is self-referential
    // (meaning that the concept and related concept are the same)
    boolean insert_flag = true;
    if ( (relationship.isMTHAsserted() || relationship.getLevel() == 'P') &&
        relationship.getConcept().getIdentifier().equals(
        relationship.getRelatedConcept().getIdentifier())) {
      insert_flag = false;
    }

    // When inserting a P level relationship
    // check for matching concept level relationship.
    else if (relationship.getLevel() == 'P') {
      Relationship[] rels = getSource().getRelationships();
      for (int i = 0; i < rels.length; i++) {
        if (rels[i].isConceptLevel() &&
            rels[i].getRelatedConcept().equals(relationship.getRelatedConcept())) {

          // If the relationship is level D
          // then unapprove the concept level relationship (if necessary)
          // and insert the demotion
          if (relationship.isDemoted()) {
            if (!rels[i].needsReview()) {
              AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(rels[
                  i]);
              acsa.setNewValue("N");
              addSubAction(acsa);
            }
          }

          // Otherwise, do not insert the relationship
          else {
            insert_flag = false;
          }
        }
      } // end for
    } // end else if (relationship.getLevel() == 'P')

    // Insert the relationship if logic dictates so
    if (insert_flag) {
      AtomicInsertAction aia = new AtomicInsertAction(relationship);
      addSubAction(aia);
    }

    // If getChangeStatus, unapprove concept if not approve
    if (getChangeStatus() && !getSource().needsReview()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(getSource());
      acsa.setNewValue("N");
      addSubAction(acsa);
    }

  }

}
