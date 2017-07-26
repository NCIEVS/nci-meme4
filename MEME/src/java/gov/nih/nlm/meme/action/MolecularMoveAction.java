/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularMoveAction
 * 
 * 01/03/2007 BAC (1-D60C5): When considering moving foreign atoms, check if concept id is null first.
 * 08/14/2006 BAC (1-BMLM5) : Additional bug fix to support movement of translation_of atom
 * 07/07/2006 RBE (1-BMLM5) : Bug fixes on moving atom from one concept to 
 *  						  another
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.NonSourceAssertedRestrictor;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.ViolationsVector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This action moves atoms from one concept to another.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to move atoms from one concept to another.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Concept to be moved into target</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>Concept where source to be moved</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source and target</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source and target</li>
 *     <li>Apply move inhibitors?</li>
 *     <li>Move all specified atoms from source to target, this should
 *         already move attributes/relationships
 *     </li>
 *     <li>Unapprove atoms if getChangeStatus is set</li>
 *     <li>If moving atoms creates self-referential P relationships, delete them</li>
     *     <li>If moving atoms causes a P level relationships to match a C, delete it
 *         if status N, unapprove the C if status D.
 *     </li>
 *     <li>Unapprove target concept if getChangeStatus</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Create object
 * Concept source_concept = ... get concept ...;
 * Concept target_concept = ... get concept ...;<br>
 * // this atom must come from the source concept
 * Atom atom_to_move = ... get atom ...;
 * Authority authority = ... get authority ...;
 * Identifier work_id = ... get identifier ...;
 * Identifier transaction_id = ... get identifier ...;<br>
 * // Create & configure action
 * MolecularMoveAction mma = new MolecularMoveAction(source_concept, target_concept);
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

public class MolecularMoveAction
    extends MolecularAction {

  //
  // Fields
  //

  private List atoms_to_move = null;
  private List attrs_to_move = null;
  private List rels_to_move = null;

  //
  // Constructors
  //
  /**
   * Used for ObjectXMLSerializer.
   */
  private MolecularMoveAction() {
    super();
  }

  /**
   * Instantiates a {@link MolecularMoveAction} from the specified source
   * and target {@link Concept}s.
   * @param source the source {@link Concept}.
   * @param target the target {@link Concept}.
   */
  public MolecularMoveAction(Concept source, Concept target) {
    super();
    setActionName("MOLECULAR_MOVE");
    setSource(source);
    setTarget(target);
    atoms_to_move = new ArrayList();
    attrs_to_move = new ArrayList();
    rels_to_move = new ArrayList();
  }

  //
  // Methods
  //

  /**
   * Returns a list of concepts to refresh.
   * @return a {@link Concept}<code>[]</code> of concepts to refresh
   */
  public Concept[] getConceptsToRefresh() {
    Concept[] concepts = new Concept[2];
    concepts[0] = source;
    concepts[1] = target;
    return concepts;
  }

  /**
   * Returns a list of atom to move.
   * @return an {@link Atom}<code>[]<?code> of atoms to move
   */
  public Atom[] getAtomsToMove() {
    return (Atom[])atoms_to_move.toArray(new Atom[0]);
  }

  /**
   * Adds an {@link Atom} to the list of atoms to move.
   * The {@link Concept} of the atom must equal the source {@link Concept}.
   * @param atom the {@link Atom} to move
   * @throws BadValueException if the concepts do not match
   */
  public void addAtomToMove(Atom atom) throws BadValueException {
    if (!atom.getConcept().equals(getSource())) {
      BadValueException bve = new BadValueException(
          "Atom does not belong to source concept.");
      bve.setDetail("source_concept_id", getSource().getIdentifier());
      bve.setDetail("atom_concept_id", atom.getConcept().getIdentifier());
      throw bve;
    }
    atoms_to_move.add(atom);
  }

  /**
   * Returns a list of {@link Attribute}s to move.
   * @return an {@link Attribute}<code>[]</code> of attributes to move
   */
  public Attribute[] getAttributesToMove() {
    return (Attribute[])attrs_to_move.toArray(new Attribute[0]);
  }

  /**
   * Adds an {@link Attribute} to the list of attributes to move.
       * The {@link Concept} of the attribute must equal the source {@link Concept}.
   * @param attribute the {@link Attribute} to move
   * @throws BadValueException if the concepts do not match, or if the
   * attribute is not concept level
   */
  public void addAttributeToMove(Attribute attribute) throws BadValueException {
    if (!attribute.getConcept().equals(getSource())) {
      BadValueException bve = new BadValueException(
          "Attribute does not belong to source concept.");
      bve.setDetail("source_concept_id", getSource().getIdentifier());
      bve.setDetail("attribute_concept_id",
                    attribute.getConcept().getIdentifier());
      throw bve;
    }

    if (!attribute.isConceptLevel()) {
      BadValueException bve = new BadValueException(
          "Attribute must be concept level");
      bve.setDetail("attribute_level", String.valueOf(attribute.getLevel()));
      throw bve;
    }

    attrs_to_move.add(attribute);
  }

  /**
   * Returns a list of {@link Relationship}s to move.
   * @return an {@link Relationship}<code>[]</code> of relationships to move
   */
  public Relationship[] getRelationshipsToMove() {
    return (Relationship[])rels_to_move.toArray(new Relationship[0]);
  }

  /**
   * Adds an {@link Relationship} to the list of relationships to move.
       * The {@link Concept} of the relationship must equal the source {@link Concept}.
   * @param relationship the {@link Relationship} to move
   * @throws BadValueException if the concepts do not match, or if the
   * relationship is not concept level
   */
  public void addRelationshipToMove(Relationship relationship) throws
      BadValueException {
    if (!relationship.getConcept().equals(getSource())) {
      BadValueException bve = new BadValueException(
          "Relationship does not belong to source concept.");
      bve.setDetail("source_concept_id", getSource().getIdentifier());
      bve.setDetail("relationship_concept_id",
                    relationship.getConcept().getIdentifier());
      throw bve;
    }

    if (!relationship.isConceptLevel()) {
      BadValueException bve = new BadValueException(
          "Relationship must be concept level");
      bve.setDetail("relationship_level", String.valueOf(relationship.getLevel()));
      throw bve;
    }

    rels_to_move.add(relationship);
  }

  /**
   * Returns the violations vector.
   * @return the {@link ViolationsVector}
   */
  public ViolationsVector checkFatalIntegrities() {
    EnforcableIntegrityVector eiv = getIntegrityVector();
    if (eiv == null) {
      return new ViolationsVector();
    }
    return eiv.applyMoveInhibitors(getSource(), getTarget(), getAtomsToMove());

  }

  /**
   * Performs molecular move action.
   * @throws ActionException if failed while performing the action
   */
  public void performAction() throws ActionException {

    Relationship[] source_rels = getSource().getRelationships();

    // Go through all of the atoms in source
    // and generate atomic actions to move
    // them to the target concept
    
    Set atom_set = new HashSet();
    Atom[] atoms = getSource().getAtoms();
    for (int i=0; i<atoms.length; i++) {
      if (atoms_to_move.contains(atoms[i]))
        atom_set.add(atoms[i]);
    }
    atoms = (Atom[]) atom_set.toArray(new Atom[0]);
    
    for (int i = 0; i < atoms.length; i++) {
      AtomicChangeConceptAction acca = new AtomicChangeConceptAction(atoms[i]);
      acca.setNewConcept(target);
      atoms[i].setConcept(target);
      addSubAction(acca);

      // If change status flag is on, unapprove
      // the atoms as they move
      if (atoms[i].isApproved() && getChangeStatus()) {
        AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(atoms[i]);
        acsa.setNewValue("N");
        addSubAction(acsa);
      }

      //
      // Move translation atoms if in same concept
      //
      // Foreign atoms do not have concept populated
      // if they are in a different concept 
      //
      Atom[] foreign_atoms = atoms[i].getTranslationAtoms();
      for (int x = 0; x < foreign_atoms.length; x++) {
        if (foreign_atoms[x].getConcept() != null &&
        		foreign_atoms[x].getConcept().getIdentifier().equals(getSource().
            getIdentifier()) && !atoms_to_move.contains(foreign_atoms[x])) {
          acca = new AtomicChangeConceptAction(foreign_atoms[x]);
          acca.setNewConcept(target);
          addSubAction(acca);
        }
      }

      NonSourceAssertedRestrictor r = new NonSourceAssertedRestrictor();
      Relationship[] target_rels = target.getRestrictedRelationships(r);

      //
      // Special logic for P level relationships connected to the current atom
      //
      for (int j = 0; j < source_rels.length; j++) {

        if (source_rels[j].getLevel() == 'P' &&
            source_rels[j].getAtom().equals(atoms[i])) {

          //
          // P level rels should be deleted if they
          // span the source-target concepts and the source
          // atom is moving to the target.
          //
          if (source_rels[j].getRelatedConcept().equals(target)) {
            AtomicDeleteAction ada = new AtomicDeleteAction(source_rels[j]);
            addSubAction(ada);
            source_rels[j].setDead(true);
            continue;
          }

          //
          // If the moving will produce a situation where there is
          // a P and a C between the target and the same third concept
          // either unapprove the C or delete the P
          //
          for (int k = 0; k < target_rels.length; k++) {
            // C level relationships should be deleted
            if (target_rels[k].isConceptLevel() &&
                source_rels[j].getRelatedConcept().equals(target_rels[k].
                getRelatedConcept())) {

              // If demoted, unapprove any matching C level rels
              // Otherwise, delete the P
              if (source_rels[j].isDemoted() && !source_rels[j].isDead() &&
                  !target_rels[k].needsReview()) {
                AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(
                    target_rels[k]);
                acsa.setNewValue("N");
                target_rels[k].setStatus('N');
                addSubAction(acsa);
              } else if (!source_rels[j].isDead()) {
                AtomicDeleteAction ada = new AtomicDeleteAction(source_rels[j]);
                addSubAction(ada);
                source_rels[j].setDead(true);
              }
            } // end for ... target_rels ...
          } // end if (p level)
        } // end for ... source_rels ...
      } // end for ... atoms ...
    }

    // Go through all of the attributes in source
    // and generate atomic actions to move
    // them to the target concept
    Attribute[] attrs = (Attribute[])attrs_to_move.toArray(new Attribute[0]);
    for (int i = 0; i < attrs.length; i++) {
      AtomicChangeConceptAction acca = new AtomicChangeConceptAction(attrs[i]);
      acca.setNewConcept(target);
      addSubAction(acca);

      // If change status flag is on, unapprove
      // the attributes as they move
      if (attrs[i].isApproved() && getChangeStatus()) {
        AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(attrs[i]);
        acsa.setNewValue("N");
        addSubAction(acsa);
      }

      NonSourceAssertedRestrictor r = new NonSourceAssertedRestrictor();
      Relationship[] target_rels = target.getRestrictedRelationships(r);

      //
      // Special logic for P level relationships connected to the current attribute
      //
      for (int j = 0; j < source_rels.length; j++) {

        if (source_rels[j].getLevel() == 'P' &&
            source_rels[j].getAttribute().equals(attrs[i])) {

          //
          // P level rels should be deleted if they
          // span the source-target concepts and the source
          // attribute is moving to the target.
          //
          if (source_rels[j].getRelatedConcept().equals(target)) {
            AtomicDeleteAction ada = new AtomicDeleteAction(source_rels[j]);
            addSubAction(ada);
            source_rels[j].setDead(true);
            continue;
          }

          //
          // If the moving will produce a situation where there is
          // a P and a C between the target and the same third concept
          // either unapprove the C or delete the P
          //
          for (int k = 0; k < target_rels.length; k++) {
            // C level relationships should be deleted
            if (target_rels[k].isConceptLevel() &&
                source_rels[j].getRelatedConcept().equals(target_rels[k].
                getRelatedConcept())) {

              // If demoted, unapprove any matching C level rels
              // Otherwise, delete the P
              if (source_rels[j].isDemoted() && !source_rels[j].isDead() &&
                  !target_rels[k].needsReview()) {
                AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(
                    target_rels[k]);
                acsa.setNewValue("N");
                target_rels[k].setStatus('N');
                addSubAction(acsa);
              } else if (!source_rels[j].isDead()) {
                AtomicDeleteAction ada = new AtomicDeleteAction(source_rels[j]);
                addSubAction(ada);
                source_rels[j].setDead(true);
              }
            } // end for ... target_rels ...
          } // end if (p level)
        } // end for ... source_rels ...
      } // end for ... attrs ...
    }

    // Go through all of the relationships in source
    // and generate atomic actions to move
    // them to the target concept
    Relationship[] rels = (Relationship[])rels_to_move.toArray(new
        Relationship[0]);
    for (int i = 0; i < rels.length; i++) {
      AtomicChangeConceptAction acca = new AtomicChangeConceptAction(rels[i]);
      acca.setNewConcept(target);
      addSubAction(acca);

      // If change status flag is on, unapprove
      // the attributes as they move
      if (rels[i].isApproved() && getChangeStatus()) {
        AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(rels[i]);
        acsa.setNewValue("N");
        addSubAction(acsa);
      }

      NonSourceAssertedRestrictor r = new NonSourceAssertedRestrictor();
      Relationship[] target_rels = target.getRestrictedRelationships(r);

      //
      // Special logic for P level relationships connected to the current attribute
      //
      for (int j = 0; j < source_rels.length; j++) {

        if (source_rels[j].getLevel() == 'P' &&
            source_rels[j].getAttribute().equals(attrs[i])) {

          //
          // P level rels should be deleted if they
          // span the source-target concepts and the source
          // attribute is moving to the target.
          //
          if (source_rels[j].getRelatedConcept().equals(target)) {
            AtomicDeleteAction ada = new AtomicDeleteAction(source_rels[j]);
            addSubAction(ada);
            source_rels[j].setDead(true);
            continue;
          }

          //
          // If the moving will produce a situation where there is
          // a P and a C between the target and the same third concept
          // either unapprove the C or delete the P
          //
          for (int k = 0; k < target_rels.length; k++) {
            // C level relationships should be deleted
            if (target_rels[k].isConceptLevel() &&
                source_rels[j].getRelatedConcept().equals(target_rels[k].
                getRelatedConcept())) {

              // If demoted, unapprove any matching C level rels
              // Otherwise, delete the P
              if (source_rels[j].isDemoted() && !source_rels[j].isDead() &&
                  !target_rels[k].needsReview()) {
                AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(
                    target_rels[k]);
                acsa.setNewValue("N");
                target_rels[k].setStatus('N');
                addSubAction(acsa);
              } else if (!source_rels[j].isDead()) {
                AtomicDeleteAction ada = new AtomicDeleteAction(source_rels[j]);
                addSubAction(ada);
                source_rels[j].setDead(true);
              }
            } // end for ... target_rels ...
          } // end if (p level)
        } // end for ... source_rels ...
      } // end for ... rels ...
    }

    if (getChangeStatus() && !getTarget().needsReview()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(getTarget());
      acsa.setNewValue("N");
      addSubAction(acsa);
    }

    if (getChangeStatus() && !getSource().needsReview()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(getSource());
      acsa.setNewValue("N");
      addSubAction(acsa);
    }
  }

  /**
   * Returns the information of this class
   * @return the information of this class
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(100);
    sb.append(getClass().getName()).append(": id=").append(getIdentifier()).
        append(", sid=")
        .append(getSourceIdentifier()).append(", tid=").append(
        getTargetIdentifier());
    if (atoms_to_move != null) {
      for (int i = 0; i < atoms_to_move.size(); i++) {
        if (i > 0)
          sb.append(",");
        else
          sb.append(", atom_ids=");
        sb.append( ( (Atom)atoms_to_move.get(i)).getIdentifier());
      }
    }
    return sb.toString();
  }

}
