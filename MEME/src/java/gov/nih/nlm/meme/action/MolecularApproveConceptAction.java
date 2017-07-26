/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularApproveConceptAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.NonSourceAssertedRestrictor;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;

import java.util.HashSet;

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
 */

public class MolecularApproveConceptAction
    extends MolecularAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link MolecularApproveConceptAction}.
   */
  private MolecularApproveConceptAction() {
    super();
  }

  /**
   * Instantiates an {@link MolecularApproveConceptAction}
   * with the specified source {@link Concept}
   * @param concept the source {@link Concept}
   */
  public MolecularApproveConceptAction(Concept concept) {
    super();
    setActionName("MOLECULAR_CONCEPT_APPROVAL");
    setSource(concept);
  }

  //
  // Methods
  //

  /**
   * Returns the source {@link Concept}.
   * @return a {@link Concept}<code>[]</code> containing
   *         the source {@link Concept}
   */
  public Concept[] getConceptsToRefresh() {
    return new Concept[] {
        getSource()};
  }

  /**
   * Returns the {@link ViolationsVector} resulting from the failure
   * of any fatal integrity checks.
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector checkFatalIntegrities() {
    EnforcableIntegrityVector eiv = getIntegrityVector();
    if (eiv == null) {
      return new ViolationsVector();
    }
    return eiv.applyDataConstraints(getSource());
  }

  /**
   * Performs the molecular approve concept action.
   * @throws ActionException if failed while performing
   */
  public void performAction() throws ActionException {

    // Source concepts
    if (source == null)
      throw new ActionException(
          "Failed to perform molecular approve concept action.");

    Concept concept = getSource();
    AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(concept);

    if (concept.getEditingAuthority() == null ||
        !concept.getEditingAuthority().equals(getAuthority())) {

      // Change editing authority to the action authority
      acfa.setRowIdentifier(concept.getIdentifier());
      acfa.setField("editing_authority");
      acfa.setOldValue(concept.getEditingAuthority().toString());
      acfa.setNewValue(getAuthority() == null ? "" : getAuthority().toString());
      addSubAction(acfa);
    }

    // Change editing timestamp to the action timestamp
    acfa = new AtomicChangeFieldAction(concept);
    acfa.setRowIdentifier(concept.getIdentifier());
    acfa.setField("editing_timestamp");
    acfa.setOldValue(concept.getEditingTimestamp());
    acfa.setNewValue(getTimestamp());
    addSubAction(acfa);

    // Change approval action to this action
    acfa = new AtomicChangeFieldAction(concept);
    acfa.setRowIdentifier(concept.getIdentifier());
    acfa.setField("approval_molecule_id");
    acfa.setOldValue(concept.getApprovalAction().getIdentifier().toString());
    acfa.setNewValue(getIdentifier().toString());
    addSubAction(acfa);

    // Change status of the concept to 'R'
    AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(concept);
    acsa.setNewValue("R");
    addSubAction(acsa);

    // Go through all of the atoms
    // change status to 'R' where !isApproved()
    // Or the authority is like 'ENG-%'
    Atom[] atoms = concept.getAtoms();
    for (int i = 0; i < atoms.length; i++) {
      if (!atoms[i].isApproved()) {
        acsa = new AtomicChangeStatusAction(atoms[i]);
        acsa.setNewValue("R");
        addSubAction(acsa);
      }
    }

    // Go through all of the attributes
    // and change status to 'R' where !isApproved()
    Attribute[] attrs = concept.getAttributes();
    for (int i = 0; i < attrs.length; i++) {
      if (!attrs[i].isApproved()) {
        acsa = new AtomicChangeStatusAction(attrs[i]);
        acsa.setNewValue("R");
        addSubAction(acsa);
      }
    }

    HashSet hs = new HashSet();

    // Go through all of the relationships
    // The algorithm here is a bit more complicated.
    NonSourceAssertedRestrictor r = new NonSourceAssertedRestrictor();
    Relationship[] rels = concept.getRestrictedRelationships(r);
    for (int i = 0; i < rels.length; i++) {

      // If the relationship is concept level,
      //   a. change the relationship name to RT if it is LK or RT?
      //   b. change the status to R
      if (rels[i].isConceptLevel()) {
        if (rels[i].getName().equals("LK") || rels[i].getName().equals("RT?")) {
          acfa = new AtomicChangeFieldAction(rels[i]);
          acfa.setField("relationship_name");
          acfa.setNewValue("RT");
          acfa.setOldValue(rels[i].getName());
          addSubAction(acfa);
        }
        if (!rels[i].isApproved()) {
          acsa = new AtomicChangeStatusAction(rels[i]);
          acsa.setNewValue("R");
          addSubAction(acsa);
        }

      }

      // If the relationship is P level
      //   If a matching concept level relationship is found
      //     then delete the P level rel.
      //   If a matching concept level rel is NOT found and
      //   the rel is not a demotion and a matching concept
      //   level relationship has not yet been inserted
      //     then insert a C level rel matching the P and delete the P
      // level relationship is found, delete the P
      else {

        AtomicDeleteAction ada = new AtomicDeleteAction(rels[i]);
        addSubAction(ada);

        boolean found = false;
        for (int j = 0; j < rels.length; j++) {
          if (rels[j].isConceptLevel() &&
              rels[i].getRelatedConcept().equals(rels[j].getRelatedConcept())) {
            found = true;
            break;
          }
        }

        if (!found && !rels[i].isDemoted() &&
            !hs.contains(rels[i].getRelatedConcept())) {
          Relationship new_rel = new Relationship.Default();
          new_rel.setLevel('C');
          new_rel.setStatus('R');
          new_rel.setGenerated(true);
          new_rel.setReleased('N');
          new_rel.setTobereleased('Y');
          new_rel.setSuppressible("N");
          new_rel.setName(rels[i].getName());
          if (rels[i].getName().equals("LK") || rels[i].getName().equals("RT?"))
            new_rel.setName("RT");
          new_rel.setSource(new Source.Default(getAuthority() == null ? "" :
                                               getAuthority().toString()));
          new_rel.setSourceOfLabel(new Source.Default(getAuthority() == null ?
              "" : getAuthority().toString()));
          new_rel.setConcept(concept);
          new_rel.setRelatedConcept(rels[i].getRelatedConcept());
          AtomicInsertAction aia = new AtomicInsertAction(new_rel);
          this.addSubAction(aia);
          hs.add(rels[i].getRelatedConcept());
        }
      }
    }
  }

}
