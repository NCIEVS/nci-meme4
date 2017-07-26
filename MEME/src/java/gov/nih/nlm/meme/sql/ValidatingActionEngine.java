/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  ValidatingActionEngine
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.action.MolecularChangeAtomAction;
import gov.nih.nlm.meme.action.MolecularChangeAttributeAction;
import gov.nih.nlm.meme.action.MolecularChangeConceptAction;
import gov.nih.nlm.meme.action.MolecularChangeRelationshipAction;
import gov.nih.nlm.meme.action.MolecularDeleteAtomAction;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.action.MolecularDeleteConceptAction;
import gov.nih.nlm.meme.action.MolecularDeleteRelationshipAction;
import gov.nih.nlm.meme.action.MolecularInsertAtomAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertConceptAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.action.MolecularMergeAction;
import gov.nih.nlm.meme.action.MolecularMoveAction;
import gov.nih.nlm.meme.action.MolecularSplitAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.ActionException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.StaleDataException;

/**
 * {@link ActionEngine} implementation that validates each {@link MolecularAction}
 * after it is performed.
 *
 * @author MEME Group
 */
public class ValidatingActionEngine extends MIDActionEngine {

  /**
       * Instantiates a {@link ValidatingActionEngine} from the specified data source.
   * @param data_source the {@link MEMEDataSource}
   */
  public ValidatingActionEngine(MIDDataSource data_source) {
    super(data_source);
  }

  /**
   * Processes a {@link MolecularAction} and validates the result.
   * @param ma the{@link MolecularAction} to process
   * @throws ActionException if failed to process action
   * @throws StaleDataException if failed due to stale data
   */
  public void processAction(MolecularAction ma) throws ActionException,
      StaleDataException {

    //
    // Perform action (done by superclass)
    //
    MEMEToolkit.trace(
        "ValidatingActionEngine.processAction(): - Calling super class.");
    super.processAction(ma);
    MEMEToolkit.trace(
        "ValidatingActionEngine.processAction(): - Performing validation.");

    //
    // re-read concepts involved
    //
    Ticket ticket = Ticket.getActionsTicket();
    ticket.setExpandLongAttributes(true);
    if (!ma.getActionName().equals("MOLECULAR_MERGE") &&
        ! (ma instanceof MolecularDeleteConceptAction) &&
        ! (ma instanceof MolecularInsertConceptAction)) {
      MEMEToolkit.trace(
          "ValidatingActionEngine.processAction(): - Reading source data.");
      try {
        data_source.populateConcept(ma.getSource(), ticket);
      } catch (MEMEException me) {
        throw new ActionException("Failed to get source.", ma, me);
      }
    }

    if (ma.getTarget() != null && !ma.getActionName().equals("UNDO_SPLIT")) {
      MEMEToolkit.trace(
          "ValidatingActionEngine.processAction(): - Reading target data.");
      try {
        data_source.populateConcept(ma.getTarget(), ticket);
      } catch (MEMEException me) {
        throw new ActionException("Failed to get target.", ma, me);
      }
    }

    //
    // Below we determine which kind of action was performed and verify
    // the state of the concept after the action matches what we expect
    //

    //
    // If insert action was performed, data inserted must be found in the concept
    //
    if (ma instanceof MolecularInsertAtomAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular insert atom action.");
      Atom atom = ( (MolecularInsertAtomAction) ma).getAtomToInsert();
      Atom[] atoms = ma.getSource().getAtoms();
      boolean found = false;
      for (int i = 0; i < atoms.length; i++) {
        if (atoms[i].equals(atom)) {
          found = true;
          if (!atomDeepEquals(atom, atoms[i])) {
            throw new ActionException(
                "Action validation failed in insert atom: atom are not equal.");
          }
        }
      }
      if (!found) {
        throw new ActionException(
            "Action validation failed in insert atom: atom not found.");
      }
    } else if (ma instanceof MolecularInsertRelationshipAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular insert relationship action.");
      Relationship rel = ( (MolecularInsertRelationshipAction) ma).
          getRelationshipToInsert();
      Relationship[] rels = ma.getSource().getRelationships();
      boolean found = false;
      for (int i = 0; i < rels.length; i++) {
        if (rels[i].equals(rel)) {
          found = true;
          if (!relDeepEquals(rel, rels[i])) {
            throw new ActionException(
                "Action validation failed in insert relationship: relationship are not equal.");
          }
        }
      }
      if (!found) {
        throw new ActionException(
            "Action validation failed in insert relationship: relationship not found.");
      }
    } else if (ma instanceof MolecularInsertAttributeAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular insert attribute action.");
      Attribute attr = ( (MolecularInsertAttributeAction) ma).
          getAttributeToInsert();
      Attribute[] attrs = ma.getSource().getAttributes();
      boolean found = false;
      for (int i = 0; i < attrs.length; i++) {
        if (attrs[i].equals(attr)) {
          found = true;
          if (!attrDeepEquals(attr, attrs[i])) {
            throw new ActionException(
                "Action validation failed in insert attribute: attribute are not equal.");
          }
        }
      }
      if (!found) {
        throw new ActionException(
            "Action validation failed in insert attribute: attribute not found.");
      }
    } else if (ma instanceof MolecularInsertConceptAction) {
      try {
        data_source.populateConcept(ma.getSource(), ticket);
      } catch (MEMEException me) {
        throw new ActionException("Action validation failed in insert concept.");
      }
    }

    //
    // If delete action was performed, removed data must now be gone
    //
    else if (ma instanceof MolecularDeleteAtomAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular delete atom action.");
      Atom atom = ( (MolecularDeleteAtomAction) ma).getAtomToDelete();
      Atom[] atoms = ma.getSource().getAtoms();
      for (int i = 0; i < atoms.length; i++) {
        if (atoms[i].equals(atom)) {
          throw new ActionException("Action validation failed in delete atom.");
        }
      }
    } else if (ma instanceof MolecularDeleteRelationshipAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular delete relationship action.");
      Relationship rel = ( (MolecularDeleteRelationshipAction) ma).
          getRelationshipToDelete();
      Relationship[] rels = ma.getSource().getRelationships();
      for (int i = 0; i < rels.length; i++) {
        if (rels[i].equals(rel)) {
          throw new ActionException(
              "Action validation failed in delete relationship.");
        }
      }
    } else if (ma instanceof MolecularDeleteAttributeAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular delete attribute action.");
      Attribute attr = ( (MolecularDeleteAttributeAction) ma).
          getAttributeToDelete();
      Attribute[] attrs = ma.getSource().getAttributes();
      for (int i = 0; i < attrs.length; i++) {
        if (attrs[i].equals(attr)) {
          throw new ActionException(
              "Action validation failed in delete attribute.");
        }
      }
    } else if (ma instanceof MolecularDeleteConceptAction) {
      boolean found = true;
      try {
        data_source.populateConcept(ma.getSource(), ticket);
      } catch (MEMEException me) {
        found = false;
      }
      if (found) {
        throw new ActionException("Action validation failed in delete concept.");
      }
    }

    //
    // If change action was performed, changed must be found and equal.
    //
    else if (ma instanceof MolecularChangeAtomAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular change atom action.");
      Atom atom = ( (MolecularChangeAtomAction) ma).getAtomToChange();
      Atom[] atoms = ma.getSource().getAtoms();
      boolean found = false;
      for (int i = 0; i < atoms.length; i++) {
        if (atoms[i].equals(atom)) {
          found = true;
          if (!atomDeepEquals(atom, atoms[i])) {
            throw new ActionException(
                "Action validation failed in change atom: atom are not equal.");
          }
        }
      }
      if (!found) {
        throw new ActionException(
            "Action validation failed in change atom: atom not found.");
      }
    } else if (ma instanceof MolecularChangeRelationshipAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular change relationship action.");
      Relationship rel = ( (MolecularChangeRelationshipAction) ma).
          getRelationshipToChange();
      Relationship[] rels = ma.getSource().getRelationships();
      boolean found = false;
      for (int i = 0; i < rels.length; i++) {
        if (rels[i].equals(rel)) {
          found = true;
          if (!relDeepEquals(rel, rels[i])) {
            throw new ActionException(
                "Action validation failed in change relationship: relationship are not equal.");
          }
        }
      }
      if (!found) {
        throw new ActionException(
            "Action validation failed in change relationship: relationship not found.");
      }
    } else if (ma instanceof MolecularChangeAttributeAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular change attribute action.");
      Attribute attr = ( (MolecularChangeAttributeAction) ma).
          getAttributeToChange();
      Attribute[] attrs = ma.getSource().getAttributes();
      boolean found = false;
      for (int i = 0; i < attrs.length; i++) {
        if (attrs[i].equals(attr)) {
          found = true;
          if (!attrDeepEquals(attr, attrs[i])) {
            throw new ActionException(
                "Action validation failed in change attribute: attribute are not equal.");
          }
        }
      }
      if (!found) {
        throw new ActionException(
            "Action validation failed in change attribute: attribute not found.");
      }
    } else if (ma instanceof MolecularChangeConceptAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular change concept action.");
      Concept concept = ( (MolecularChangeConceptAction) ma).getConceptToChange();
      if (!conceptDeepEquals(concept, ma.getSource())) {
        throw new ActionException("Action validation failed in change concept.");
      }
    }

    //
    // Validate split action
    //
    else if (ma instanceof MolecularSplitAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular split action.");
      Atom[] splitted_atoms = ( (MolecularSplitAction) ma).getAtomsToSplit();
      Atom[] atoms = ma.getSource().getAtoms();

      // perform validation
      for (int i = 0; i < atoms.length; i++) {
        for (int j = 0; j < splitted_atoms.length; j++) {
          if (atoms[i].equals(splitted_atoms[j])) {
            throw new ActionException(
                "Action validation failed in split: atom still in source.");
          }
        }
      }
      atoms = ma.getTarget().getAtoms();
      for (int i = 0; i < splitted_atoms.length; i++) {
        boolean found = false;
        for (int j = 0; j < atoms.length; j++) {
          if (splitted_atoms[i].equals(atoms[j])) {
            found = true;
            break;
          }
        }
        if (!found) {
          throw new ActionException(
              "Action validation failed in split: atom not in target.");
        }
      }
    }

    //
    // Validate move action
    //
    else if (ma instanceof MolecularMoveAction) {
      MEMEToolkit.trace("ValidatingActionEngine.processAction(): "
                        + "- Validating molecular move action.");
      Atom[] moved_atoms = ( (MolecularMoveAction) ma).getAtomsToMove();
      Atom[] atoms = ma.getSource().getAtoms();

      // perform validation
      for (int i = 0; i < atoms.length; i++) {
        for (int j = 0; j < moved_atoms.length; j++) {
          if (atoms[i].equals(moved_atoms[j])) {
            throw new ActionException(
                "Action validation failed in move: atom still in source.");
          }
        }
      }
      atoms = ma.getTarget().getAtoms();
      for (int i = 0; i < moved_atoms.length; i++) {
        boolean found = false;
        for (int j = 0; j < atoms.length; j++) {
          if (moved_atoms[i].equals(atoms[j])) {
            found = true;
            break;
          }
        }
        if (!found) {
          throw new ActionException(
              "Action validation failed in move: atom not in target.");
        }
      }
    }

    //
    // Validate merge action
    //
    else if (ma instanceof MolecularMergeAction) {
      boolean found = true;
      try {
        data_source.populateConcept(ma.getSource(), ticket);
      } catch (MEMEException me) {
        found = false;
      }
      if (found) {
        throw new ActionException(
            "Action validation failed in merge: source still exist.");
      }

      Relationship[] source_rels = ma.getSource().getRelationships();
      Relationship[] target_rels = ma.getTarget().getRelationships();
      for (int i = 0; i < source_rels.length; i++) {
        for (int j = 0; j < target_rels.length; j++) {
          if (source_rels[i].isConceptLevel() &&
              (target_rels[j].isConceptLevel() ||
               (!target_rels[j].isSourceAsserted() && !target_rels[j].isDemoted()) &&
               source_rels[i].getRelatedConcept().equals(target_rels[j].
              getRelatedConcept()))) {
            throw new ActionException("Action validation failed in merge.");
          }
        }
      }

    }

  } // end processAction

  //
  // Private methods
  //

  /**
   * Determines if two atoms are exactly the same.
   * @param atom1 the first {@link Atom}
   * @param atom2 the second {@link Atom}
   * @return <code>true</code> if both atoms are exactly the same;
   *   otherwise <code>false</code>
   */
  private boolean atomDeepEquals(Atom atom1, Atom atom2) {
    MEMEToolkit.trace("ValidatingActionEngine.atomDeepEquals(): "
                      + "- Looking for exactly the same atom.");

    String atom1_code = atom1.getCode() == null ? "" : atom1.getCode().toString();
    String atom2_code = atom2.getCode() == null ? "" : atom2.getCode().toString();

    MEMEToolkit.trace("TRACE:atom1=" + atom1);
    MEMEToolkit.trace("TRACE:atom2=" + atom2);
    MEMEToolkit.trace("TRACE:atom1.getReleased()=" + atom1.getReleased());
    MEMEToolkit.trace("TRACE:atom2.getReleased()=" + atom2.getReleased());
    MEMEToolkit.trace("TRACE:atom1.getTobereleased()=" + atom1.getTobereleased());
    MEMEToolkit.trace("TRACE:atom2.getTobereleased()=" + atom2.getTobereleased());
    MEMEToolkit.trace("TRACE:atom1.getTermgroup()=" + atom1.getTermgroup());
    MEMEToolkit.trace("TRACE:atom2.getTermgroup()=" + atom2.getTermgroup());
    MEMEToolkit.trace("TRACE:atom1.getStatus()=" + atom1.getStatus());
    MEMEToolkit.trace("TRACE:atom2.getStatus()=" + atom2.getStatus());
    MEMEToolkit.trace("TRACE:atom1_code=" + atom1_code);
    MEMEToolkit.trace("TRACE:atom2_code=" + atom2_code);
    MEMEToolkit.trace("TRACE:atom1.getConcept()=" + atom1.getConcept());
    MEMEToolkit.trace("TRACE:atom2.getConcept()=" + atom2.getConcept());
    MEMEToolkit.trace("TRACE:atom1.getString()=" + atom1.getString());
    MEMEToolkit.trace("TRACE:atom2.getString()=" + atom2.getString());
    MEMEToolkit.trace("TRACE:atom1.getSource()=" + atom1.getSource());
    MEMEToolkit.trace("TRACE:atom2.getSource()=" + atom2.getSource());

    // Compare atom1 to atom2
    if (atom1.getReleased() == atom2.getReleased() &&
        atom1.getTobereleased() == atom2.getTobereleased() &&
        atom1.getTermgroup().equals(atom2.getTermgroup()) &&
        atom1.getStatus() == atom2.getStatus() &&
        atom1_code.equals(atom2_code) &&
        atom1.getConcept().equals(atom2.getConcept()) &&
        atom1.getString().equals(atom2.getString()) &&
        atom1.getSource().equals(atom2.getSource())) {
      return true;
    }
    return false;
  }

  /**
   * Determines if two relationship are exactly the same.
   * @param rel1 the first {@link Relationship}
   * @param rel2 the second {@link Relationship}
   * @return <code>true</code> if both relationship are exactly the same;
   *   otherwise <code>false</code>
   */
  private boolean relDeepEquals(Relationship rel1, Relationship rel2) {
    MEMEToolkit.trace("ValidatingActionEngine.relDeepEquals(): "
                      + "- Looking for exactly the same relationship.");

    int rel1_atom_id_1 = rel1.getAtom() == null ? 0 :
        rel1.getAtom().getIdentifier().intValue();
    int rel2_atom_id_1 = rel2.getAtom() == null ? 0 :
        rel2.getAtom().getIdentifier().intValue();
    int rel1_atom_id_2 = rel1.getRelatedAtom() == null ? 0 :
        rel1.getRelatedAtom().getIdentifier().intValue();
    int rel2_atom_id_2 = rel2.getRelatedAtom() == null ? 0 :
        rel2.getRelatedAtom().getIdentifier().intValue();
    String rel1_value = rel1.getAttribute() == null ? "" : rel1.getAttribute();
    String rel2_value = rel2.getAttribute() == null ? "" : rel2.getAttribute();

    MEMEToolkit.trace("TRACE:rel1_atom_id_1=" + rel1_atom_id_1);
    MEMEToolkit.trace("TRACE:rel2_atom_id_1=" + rel2_atom_id_1);
    MEMEToolkit.trace("TRACE:rel1_atom_id_2=" + rel1_atom_id_2);
    MEMEToolkit.trace("TRACE:rel2_atom_id_2=" + rel2_atom_id_2);
    MEMEToolkit.trace("TRACE:rel1.getLevel()=" + rel1.getLevel());
    MEMEToolkit.trace("TRACE:rel2.getLevel()=" + rel2.getLevel());
    MEMEToolkit.trace("TRACE:rel1.getReleased()=" + rel1.getReleased());
    MEMEToolkit.trace("TRACE:rel2.getReleased()=" + rel2.getReleased());
    MEMEToolkit.trace("TRACE:rel1.getStatus()=" + rel1.getStatus());
    MEMEToolkit.trace("TRACE:rel2.getStatus()=" + rel2.getStatus());
    MEMEToolkit.trace("TRACE:rel1_value=" + rel1_value);
    MEMEToolkit.trace("TRACE:rel2_value=" + rel2_value);
    MEMEToolkit.trace("TRACE:rel1.getTobereleased()=" + rel1.getTobereleased());
    MEMEToolkit.trace("TRACE:rel2.getTobereleased()=" + rel2.getTobereleased());
    MEMEToolkit.trace("TRACE:rel1.getConcept()=" + rel1.getConcept());
    MEMEToolkit.trace("TRACE:rel2.getConcept()=" + rel2.getConcept());
    MEMEToolkit.trace("TRACE:rel1.getRelatedConcept()=" +
                      rel1.getRelatedConcept());
    MEMEToolkit.trace("TRACE:rel2.getRelatedConcept()=" +
                      rel2.getRelatedConcept());
    MEMEToolkit.trace("TRACE:rel1.getName()=" + rel1.getName());
    MEMEToolkit.trace("TRACE:rel2.getName()=" + rel2.getName());
    MEMEToolkit.trace("TRACE:rel1.getSource()=" + rel1.getSource());
    MEMEToolkit.trace("TRACE:rel2.getSource()=" + rel2.getSource());

    // Compare relationship to rels[i]
    if (rel1_atom_id_1 == rel2_atom_id_1 &&
        rel1_atom_id_2 == rel2_atom_id_2 &&
        rel1.getLevel() == rel2.getLevel() &&
        rel1.getReleased() == rel2.getReleased() &&
        rel1.getStatus() == rel2.getStatus() &&
        rel1_value.equals(rel2_value) &&
        rel1.getTobereleased() == rel2.getTobereleased() &&
        rel1.getConcept().equals(rel2.getConcept()) &&
        rel1.getRelatedConcept().equals(rel2.getRelatedConcept()) &&
        rel1.getName().equals(rel2.getName()) &&
        rel1.getSource().equals(rel2.getSource())) {
      return true;
    }
    return false;
  }

  /**
   * Determines if two attribute are exactly the same.
   * @param attr1 the first {@link Attribute}
   * @param attr2 the second {@link Attribute}
   * @return <code>true</code> if both attribute are exactly the same;
   *   otherwise <code>false</code>
   */
  private boolean attrDeepEquals(Attribute attr1, Attribute attr2) {
    MEMEToolkit.trace("ValidatingActionEngine.attrDeepEquals(): "
                      + "- Looking for exactly the same attribute.");

    int attr1_atom_id = attr1.getAtom() == null ? 0 :
        attr1.getAtom().getIdentifier().intValue();
    int attr2_atom_id = attr2.getAtom() == null ? 0 :
        attr2.getAtom().getIdentifier().intValue();
    String attr1_value = attr1.getValue() == null ? "" : attr1.getValue();
    String attr2_value = attr2.getValue() == null ? "" : attr2.getValue();

    MEMEToolkit.trace("TRACE:attr1_atom_id=" + attr1_atom_id);
    MEMEToolkit.trace("TRACE:attr2_atom_id=" + attr2_atom_id);
    MEMEToolkit.trace("TRACE:attr1.getLevel()=" + attr1.getLevel());
    MEMEToolkit.trace("TRACE:attr2.getLevel()=" + attr2.getLevel());
    MEMEToolkit.trace("TRACE:attr1.getReleased()=" + attr1.getReleased());
    MEMEToolkit.trace("TRACE:attr2.getReleased()=" + attr2.getReleased());
    MEMEToolkit.trace("TRACE:attr1.getValue()=" + attr1.getValue());
    MEMEToolkit.trace("TRACE:attr2.getValue()=" + attr2.getValue());
    MEMEToolkit.trace("TRACE:attr1.getTobereleased()=" + attr1.getTobereleased());
    MEMEToolkit.trace("TRACE:attr2.getTobereleased()=" + attr2.getTobereleased());
    MEMEToolkit.trace("TRACE:attr1.getConcept()=" + attr1.getConcept());
    MEMEToolkit.trace("TRACE:attr2.getConcept()=" + attr2.getConcept());
    MEMEToolkit.trace("TRACE:attr1.getName()=" + attr1.getName());
    MEMEToolkit.trace("TRACE:attr2.getName()=" + attr2.getName());
    MEMEToolkit.trace("TRACE:attr1.getSource()=" + attr1.getSource());
    MEMEToolkit.trace("TRACE:attr2.getSource()=" + attr2.getSource());

    // Compare attribute to attrs[i]
    if (attr1_atom_id == attr2_atom_id &&
        attr1.getLevel() == attr2.getLevel() &&
        attr1.getReleased() == attr2.getReleased() &&
        attr1_value.equals(attr2_value) &&
        attr1.getTobereleased() == attr2.getTobereleased() &&
        attr1.getConcept().equals(attr2.getConcept()) &&
        attr1.getName().equals(attr2.getName()) &&
        attr1.getSource().equals(attr2.getSource())) {
      return true;
    }
    return false;
  }

  /**
   * Determines if two concept are exactly the same.
   * @param concept1 the first {@link Concept}
   * @param concept2 the second {@link Concept}
   * @return <code>true</code> if both concept are exactly the same;
   *   otherwise <code>false</code>
   */
  private boolean conceptDeepEquals(Concept concept1, Concept concept2) {
    MEMEToolkit.trace("ValidatingActionEngine.conceptDeepEquals(): "
                      + "- Looking for exactly the same concept.");
    MEMEToolkit.trace("ValidatingActionEngine.conceptDeepEquals(): "
                      + "concept1=" + concept1);
    MEMEToolkit.trace("ValidatingActionEngine.conceptDeepEquals(): "
                      + "concept2=" + concept2);
    MEMEToolkit.trace("ValidatingActionEngine.conceptDeepEquals(): "
                      + "concept1.getStatus()=" + concept1.getStatus());
    MEMEToolkit.trace("ValidatingActionEngine.conceptDeepEquals(): "
                      + "concept2.getStatus()=" + concept2.getStatus());
    return (concept1.getStatus() == concept2.getStatus() &&
            concept1.equals(concept2));
  }

}
