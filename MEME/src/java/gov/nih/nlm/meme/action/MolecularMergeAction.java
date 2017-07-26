/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularMergeAction
 *
 * 02/24/2009 BAC (1-GCLNT): Use local ranking algorithm for relationships.
 * 
 *****************************************************************************/
//testing
package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.NonSourceAssertedRestrictor;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 */

public class MolecularMergeAction
    extends MolecularAction {

  //
  // Fields
  //

  private boolean del_dup_sty = false;

  //
  // Constructors
  //

  /**
   * No-argument constructor.
   */
  private MolecularMergeAction() {
    super();
  }

  /**
   * This constructor initializes the action with a source and target.
   * @param source An object {@link Concept}.
   * @param target An object {@link Concept}.
   */
  public MolecularMergeAction(Concept source, Concept target) {
    super();
    setActionName("MOLECULAR_MERGE");
    super.setSource(source);
    super.setTarget(target);
  }

  //
  // Overrides Molecular Action Methods
  //

  /**
   * Overrides {@link MolecularAction#setSource(Concept)}
   * @param source An object {@link Concept}
   */
  public void setSource(Concept source) {
    if (source != null && source.getIdentifier() != null && getTarget() != null
        && source.getIdentifier().equals(getTarget().getIdentifier()))
      throw new IllegalArgumentException(
          "Source and target concepts are the same, "
          + "this is not allowed when merging.");
    super.setSource(source);
  }

  /**
   * Overrides {@link MolecularAction#setTarget(Concept)}
   * @param target An object {@link Concept}
   */
  public void setTarget(Concept target) {
    if (target != null && target.getIdentifier() != null && getSource() != null
        && target.getIdentifier().equals(getSource().getIdentifier()))
      throw new IllegalArgumentException(
          "Target and source concepts are the same, "
          + "this is not allowed when merging.");
    super.setTarget(target);
  }

  //
  // Methods
  //

  /**
   * Returns a list of concepts to refresh.
   * @return An array of object {@link Concept}.
   */
  public Concept[] getConceptsToRefresh() {
    Concept[] concepts = new Concept[2];
    concepts[0] = source;
    concepts[1] = target;
    return concepts;
  }

  /**
   * Returns the violations vector.
   * @return An object {@link ViolationsVector}.
   */
  public ViolationsVector checkFatalIntegrities() {
    EnforcableIntegrityVector eiv = getIntegrityVector();
    if (eiv == null) {
      return new ViolationsVector();
    }
    return eiv.applyMergeInhibitors(getSource(), getTarget());
  }

  /**
   * Gets the value of delete duplicate semantic types.
   * @return <code>true</code> if delete duplicate semantic
   * types is set. <code>false</code> otherwise.
   */
  public boolean getDeleteDuplicateSemanticTypes() {
    return del_dup_sty;
  }

  /**
   * Sets the value of delete duplicate semantic types.
   * @param del_dup_sty An object <code>boolean</code>
   * representation of delete duplicate sty value.
   */
  public void setDeleteDuplicateSemanticTypes(boolean del_dup_sty) {
    this.del_dup_sty = del_dup_sty;
  }

  /**
   * Performs molecular merge action.
   * @throws ActionException if failed while performing
   * molecular merge action.
   */
  public void performAction() throws ActionException {

    // Go through all of the atoms in source
    // and generate atomic actions to move
    // them to the target concept
    Atom[] atoms = getSource().getAtoms();
    for (int i = 0; i < atoms.length; i++) {
      AtomicChangeConceptAction acca = new AtomicChangeConceptAction(atoms[i]);
      acca.setNewConcept(target);
      addSubAction(acca);

      // If change status flag is on, unapprove
      // the atoms as they move
      if (atoms[i].isApproved() && getChangeStatus()) {
        AtomicAction aa = new AtomicChangeStatusAction(atoms[i]);
        aa.setNewValue("N");
        addSubAction(aa);
      }
    }

    // Now, go through the relationships
    // in the source concept
    Relationship[] rels = source.getRelationships();
    for (int i = 0; i < rels.length; i++) {

      // C and P level rels should be deleted if they
      // span the source-target concepts.
      if (!rels[i].isSourceAsserted() &&
          rels[i].getRelatedConcept().equals(target)) {
        AtomicAction aa = new AtomicDeleteAction(rels[i]);
        addSubAction(aa);
        rels[i].setDead(true);

        // C level relationships should be moved
        // from source to target
      } else if (rels[i].isConceptLevel()) {
        AtomicChangeConceptAction acca = new AtomicChangeConceptAction(rels[i]);
        acca.setNewConcept(target);
        addSubAction(acca);

        // If we moved the relationship and it's approved
        // and we are changing status, unapprove the relationship
        if (rels[i].isApproved() && getChangeStatus()) {
          AtomicAction aa = new AtomicChangeStatusAction(rels[i]);
          aa.setNewValue("N");
          rels[i].setStatus('N');
          addSubAction(aa);
        }
      }
    }

    Attribute[] source_attrs = source.getAttributes();
    Attribute[] target_attrs = target.getAttributes();
    for (int i = 0; i < source_attrs.length; i++) {

      boolean delete_sty = false;
      if (getDeleteDuplicateSemanticTypes() &&
          source_attrs[i].getName().equals("SEMANTIC_TYPE")) {
        for (int j = 0; j < target_attrs.length; j++) {
          if (target_attrs[j].getName().equals("SEMANTIC_TYPE") &&
              source_attrs[i].getValue().equals(target_attrs[j].getValue())) {
            delete_sty = true;
            break;
          }
        }
      }

      // If attribute is a duplicate semantic type, delete it
      if (delete_sty) {
        AtomicAction aa = new AtomicDeleteAction(source_attrs[i]);
        addSubAction(aa);
        source_attrs[i].setDead(true);

        // If attribute is C level, move it
      } else if (source_attrs[i].isConceptLevel()) {
        AtomicChangeConceptAction acca = new AtomicChangeConceptAction(
            source_attrs[i]);
        acca.setNewConcept(target);
        addSubAction(acca);

        // If we moved a C level attribute, unapprove it
        // if it's approved and we are changing status
        if (source_attrs[i].isReviewed() && getChangeStatus()) {
          AtomicAction aa = new AtomicChangeStatusAction(source_attrs[i]);
          aa.setNewValue("N");
          addSubAction(aa);
        }
      }
    }

    // Need to delete the source concept
    AtomicAction aa = new AtomicDeleteAction(source);
    addSubAction(aa);

    // Need to unapprove target concept (concept_status=N)
    // or make it an embryo if the source concept was an embryo
    if (!target.isEmbryo()) {
      String new_status = null;
      if (source.isEmbryo())
        new_status = "E";
      else if (getChangeStatus() && !target.needsReview())
        new_status = "N";

      if (new_status != null) {
        aa = new AtomicChangeStatusAction(target);
        aa.setNewValue(new_status);
        addSubAction(aa);
      }
    }

    // Get all non source asserted relationships for the source
    // and target concepts and put these together into a single array
    //
    NonSourceAssertedRestrictor r = new NonSourceAssertedRestrictor();
    Relationship[] source_rels = source.getRestrictedRelationships(r);
    Relationship[] target_rels = target.getRestrictedRelationships(r);

    List c_level_list = new ArrayList(target_rels.length);
    for (int i = 0; i < source_rels.length; i++)
      if (source_rels[i].isConceptLevel() &&
          !source_rels[i].getRelatedConcept().equals(target))
        c_level_list.add(source_rels[i]);
    for (int i = 0; i < target_rels.length; i++)
      if (target_rels[i].isConceptLevel() &&
          !target_rels[i].getRelatedConcept().equals(source))
        c_level_list.add(target_rels[i]);
    Relationship[] concept_rels = (Relationship[])c_level_list.toArray(new
        Relationship[0]);

    List p_level_list = new ArrayList(target_rels.length);
    for (int i = 0; i < source_rels.length; i++)
      if (source_rels[i].getLevel() == 'P' &&
          !source_rels[i].getRelatedConcept().equals(target))
        p_level_list.add(source_rels[i]);
    for (int i = 0; i < target_rels.length; i++)
      if (target_rels[i].getLevel() == 'P' &&
          !target_rels[i].getRelatedConcept().equals(source))
        p_level_list.add(target_rels[i]);
    Relationship[] p_rels = (Relationship[])p_level_list.toArray(new
        Relationship[0]);

    // Compare C and P rels to find concept matches
    for (int i = 0; i < concept_rels.length; i++) {
      for (int j = 0; j < p_rels.length; j++) {

        // If the merged concept has a C and a P
        // relationship to the same related concept
        // either:
        // 1. delete the P (if it is ! demoted)
        // 2. unapprove the C (if the P is demoted)
        if (concept_rels[i].getRelatedConcept().equals(p_rels[j].
            getRelatedConcept())) {

          // Unapprove the C level relatioship
          // if the P is a demotion and the C
          // does not already require review.
          if (p_rels[j].isDemoted() && !concept_rels[i].needsReview() &&
              !concept_rels[i].isDead()) {
            aa = new AtomicChangeStatusAction(concept_rels[i]);
            aa.setNewValue("N");
            addSubAction(aa);
            concept_rels[i].setStatus('N');
          }

          // Delete the P relationship if it is not a demotion
          else if (!p_rels[j].isDemoted() && !p_rels[j].isDead()) {
            aa = new AtomicDeleteAction(p_rels[j]);
            addSubAction(aa);
            p_rels[j].setDead(true);
          }
        }

      }
    }

    // Compare C and C rels to find dups
    for (int i = 0; i < concept_rels.length; i++) {
      for (int j = i + 1; j < concept_rels.length; j++) {

        // If there are two concept level relationships
        // that have the same related concept
        // pick one to delete
        if (concept_rels[i].getRelatedConcept().equals(concept_rels[j].
            getRelatedConcept()) &&
            !concept_rels[i].isDead() && !concept_rels[j].isDead()) {

          // Pick the one with the lowest rank
          // or pick the one with the lowest relationship_id
          // and delete it
          if ( (getRank(concept_rels[i]).compareTo(getRank(concept_rels[j])) ==
                0 &&
                concept_rels[i].getIdentifier().intValue() >
                concept_rels[j].getIdentifier().intValue()) ||
              getRank(concept_rels[i]).compareTo(getRank(concept_rels[j])) <
              0) {
            concept_rels[i].setDead(true);
            aa = new AtomicDeleteAction(concept_rels[i]);
          } else {
            concept_rels[j].setDead(true);
            aa = new AtomicDeleteAction(concept_rels[j]);
          }
          addSubAction(aa);
        }
      }
    }

    // If the source editing timestamp is not null
    // then copy it to the target if either
    // the source editing timestamp is earlier
    // or if the target editing timestamp is null.
    //
    // Also copy the editing authority and
    // approval action.
    //
    if (source.getEditingTimestamp() != null &&
        ( (target.getEditingTimestamp() == null) ||
         (source.getEditingTimestamp().before(
        target.getEditingTimestamp())))) {

      // Change editing authority
      aa = new AtomicChangeFieldAction(target);
      aa.setField("editing_authority");
      aa.setOldValue(target.getEditingAuthority() == null ? "" :
                     target.getEditingAuthority().toString());
      aa.setNewValue(source.getEditingAuthority() == null ? "" :
                     source.getEditingAuthority().toString());
      addSubAction(aa);

      // Change editing timestamp
      aa = new AtomicChangeFieldAction(target);
      aa.setField("editing_timestamp");
      aa.setOldValue(target.getEditingTimestamp());
      aa.setNewValue(source.getEditingTimestamp());
      addSubAction(aa);

      // Change approval action
      aa = new AtomicChangeFieldAction(target);
      aa.setField("approval_molecule_id");
      aa.setOldValue(target.getApprovalAction().getIdentifier().toString());
      aa.setNewValue(source.getApprovalAction().getIdentifier().toString());
      addSubAction(aa);
    }

  }

  /** Simple ranking scheme for C level rels */
  private String getRank(Relationship rel) {
  	if (rel.isReleasable() && rel.getAuthority().toString().startsWith("E-")) {
  		return "99";
  	} else if (rel.isReleasable() && !rel.getAuthority().toString().startsWith("E-")) {
  		return "88";
  	} else if (rel.isWeaklyReleasable() && rel.getAuthority().toString().startsWith("E-")) {
  		return "77";
  	} else if (rel.isWeaklyReleasable() && !rel.getAuthority().toString().startsWith("E-")) {
  		return "66";
  	} else
  	return "0000";
  }
  
  /**
   * Returns any conflicting relationships.
   * @param source An object {@link Concept} representation of source concept.
   * @param target An object {@link Concept} representation of target concept.
   * @return An array of object {@link Relationship}.
   */
  public Relationship[] getConflictingRelationships(Concept source,
      Concept target) {
    Relationship[] source_rels = source.getRelationships();
    Relationship[] target_rels = target.getRelationships();

    Set rels = new HashSet();
    for (int i = 0; i < source_rels.length; i++) {
      boolean c_flag = false;
      // Continue if the relationship is not a S level MSH rel
      // or a concept level relationship
      if ( (!source_rels[i].isConceptLevel() &&
            ! (source_rels[i].getSource().getStrippedSourceAbbreviation().
               equals("MSH") &&
               source_rels[i].isSourceAsserted() &&
               !source_rels[i].getName().equals("SFO/LFO"))) ||
          // Should only look at releasable rels
          !source_rels[i].isReleasable())
        continue;
      // set the flag if it is C level
      if (source_rels[i].isConceptLevel())
        c_flag = true;

      for (int j = 0; j < target_rels.length; j++) {
        if (
            // At least one of the relationships must be C level
            (target_rels[j].isConceptLevel() ||
             (c_flag &&
              target_rels[j].getSource().getStrippedSourceAbbreviation().equals(
            "MSH") &&
              target_rels[j].isSourceAsserted() &&
              !target_rels[j].getName().equals("SFO/LFO"))) &&
            // The related concepts must be the same
            target_rels[j].getRelatedConcept().getIdentifier().equals(
            source_rels[i].getRelatedConcept().getIdentifier()) &&
            // The relationship names must be different
            !target_rels[j].getName().equals(source_rels[i].getName()) &&
            // Should only look at releasable rels
            target_rels[j].isReleasable()) {

          rels.add(target_rels[j]);
          rels.add(source_rels[i]);
        }
      }
    }
    return (Relationship[])rels.toArray(new Relationship[0]);
  }

}
