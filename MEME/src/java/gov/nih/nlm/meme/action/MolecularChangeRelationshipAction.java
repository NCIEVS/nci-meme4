/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularChangeRelationshipAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.ActionException;

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
 */

public class MolecularChangeRelationshipAction
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
  private MolecularChangeRelationshipAction() {
    super();
  }

  /**
   * This constructor initializes the action with a relationship.
   * @param relationship An object {@link Relationship}.
   */
  public MolecularChangeRelationshipAction(Relationship relationship) {
    super();
    setSource(relationship.getConcept());
    this.relationship = relationship;
    setActionName("MOLECULAR_CHANGE");
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
    return new Concept[] {
        getSource()};
  }

  /**
   * Returns a relationship to change.
   * @return An object {@link Relationship}
   */
  public Relationship getRelationshipToChange() {
    return relationship;
  }

  /**
   * Performs molecular change relationship action.
   * @throws ActionException if failed while performing
   * molecular change relationship action.
   */
  public void performAction() throws ActionException {
    Relationship[] rels = getSource().getRelationships();
    for (int i = 0; i < rels.length; i++) {
      if (rels[i].equals(relationship)) {

        if (rels[i].getStatus() != relationship.getStatus()) {
          AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(rels[i]);
          acsa.setNewValue(String.valueOf(relationship.getStatus()));
          addSubAction(acsa);
        }

        // If the relationship is concept level and new status = 'R'
        // then change the relationship name to RT if it is LK or RT?
        if (rels[i].isConceptLevel() &&
            relationship.isApproved() &&
            !rels[i].isApproved() &&
            (rels[i].getName().equals("LK") || rels[i].getName().equals("RT?"))) {
          relationship.setName("RT");
        }

        if ( (relationship.isConceptLevel() ||
              relationship.isSourceAsserted()) &&
            relationship.isApproved()) {
          for (int j = 0; j < rels.length; j++) {
            if (rels[j].getRelatedConcept().equals(rels[i].getRelatedConcept()) &&
                !rels[j].isSourceAsserted() && !rels[j].isConceptLevel()) {
              AtomicDeleteAction ada = new AtomicDeleteAction(rels[j]);
              addSubAction(ada);
            }
          }
        }

        if (!rels[i].getSource().equals(relationship.getSource())) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(rels[i]);
          acfa.setOldValue(rels[i].getSource().toString());
          acfa.setNewValue(relationship.getSource().toString());
          acfa.setField("source");
          addSubAction(acfa);
        }

        if (rels[i].getTobereleased() != relationship.getTobereleased()) {
          AtomicChangeReleasabilityAction acra =
              new AtomicChangeReleasabilityAction(rels[i]);
          acra.setNewValue(String.valueOf(relationship.getTobereleased()));
          addSubAction(acra);
        }

        if (rels[i].getLevel() != relationship.getLevel()) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(rels[i]);
          acfa.setOldValue(String.valueOf(rels[i].getLevel()));
          acfa.setNewValue(String.valueOf(relationship.getLevel()));
          acfa.setField("relationship_level");
          addSubAction(acfa);
        }

        if (!rels[i].getSuppressible().equals(relationship.getSuppressible())) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(rels[i]);
          acfa.setOldValue(rels[i].getSuppressible());
          acfa.setNewValue(relationship.getSuppressible());
          acfa.setField("suppressible");
          addSubAction(acfa);
        }

        if (rels[i].getAtom() != null &&
            !rels[i].getAtom().equals(relationship.getAtom())) {
          AtomicChangeAtomAction acaa = new AtomicChangeAtomAction(rels[i]);
          acaa.setNewAtom(relationship.getAtom());
          addSubAction(acaa);
        }

        if (!rels[i].isInverse() &&
            !rels[i].getName().equals(relationship.getName())) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(rels[i]);
          acfa.setOldValue(rels[i].getName());
          acfa.setNewValue(relationship.getName());
          acfa.setField("relationship_name");
          addSubAction(acfa);
        }

        String rela1 = rels[i].getAttribute() == null ? "" :
            rels[i].getAttribute();
        String rela2 = relationship.getAttribute() == null ? "" :
            relationship.getAttribute();
        if (!rels[i].isInverse() && !rela1.equals(rela2)) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(rels[i]);
          acfa.setOldValue(rels[i].getAttribute());
          acfa.setNewValue(relationship.getAttribute());
          acfa.setField("relationship_attribute");
          addSubAction(acfa);
        }

      }
    }

    // If getChangeStatus, unapprove concept if not approve
    if (getChangeStatus() && !getSource().needsReview()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(getSource());
      acsa.setNewValue("N");
      addSubAction(acsa);
    }
  }

  /**
   * The main method performs a self-QA test
   * @param args An array of arguments.
   */
  public static void main(String[] args) {
    MEMEToolkit.trace("Use memerun.pl gov.nih.nlm.meme.client.ActionClient");
  }
}
