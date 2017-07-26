/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularDeleteAtomAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.ActionException;

/**
 * This action deletes atom.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to delete atom.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Atom to be deleted</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source concept</li>
 *     <li>Get all atoms, find the one matching the one to delete</li>
 *     <li>Delete all attributes connected to it</li>
 *     <li>Delete all relationships connected to it</li>
 *     <li>Delete the atom itself</li>
 *     <li>If getChangeStatus unapprove source</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Obtain atom reference
 * Atom atom = ... get atom ...;
 * atom.setConcept(... get concept ...);<br>
 * // Create & configure action
 * MolecularDeleteAtomAction mdaa = new MolecularDeleteAtomAction(atom);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 */

public class MolecularDeleteAtomAction
    extends MolecularAction {

  //
  // Fields
  //

  private Atom atom = null;

  //
  // Constructors
  //

  /**
   * No-argument constructor.
   */
  private MolecularDeleteAtomAction() {
    super();
  }

  /**
   * This constructor initializes the action with an atom.
   * @param atom An object {@link Atom}.
   */
  public MolecularDeleteAtomAction(Atom atom) {
    super();
    this.atom = atom;
    setSource(atom.getConcept());
    setActionName("MOLECULAR_DELETE");
  }

  //
  // Methods
  //

  /**
   * Returns a concept to refresh.
   * @return An array of object {@link Concept}
   */
  public Concept[] getConceptsToRefresh() {
    return new Concept[] {
        getSource()};
  }

  /**
   * Returns an atom to delete.
   * @return An object {@link Atom}
   */
  public Atom getAtomToDelete() {
    return atom;
  }

  /**
   * Indicates whether or not the action should lock related concept rows.
   * @return false the default value
   */
  public boolean lockRelatedConcepts() {
    return false;
  }

  /**
   * Performs molecular delete atom action.
   * @throws ActionException if failed while performing
   * molecular delete atom action.
   */
  public void performAction() throws ActionException {

    Atom[] atoms = getSource().getAtoms();
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].equals(atom)) {
        Attribute[] attrs = atoms[i].getAttributes();
        for (int j = 0; j < attrs.length; j++) {
          AtomicDeleteAction ada = new AtomicDeleteAction(attrs[j]);
          addSubAction(ada);
        }
        Relationship[] rels = atoms[i].getRelationships();
        for (int j = 0; j < rels.length; j++) {
          AtomicDeleteAction ada = new AtomicDeleteAction(rels[j]);
          addSubAction(ada);
        }
        Atom[] foreign_atoms = atoms[i].getTranslationAtoms();
        for (int j = 0; j < foreign_atoms.length; j++) {
          AtomicDeleteAction ada = new AtomicDeleteAction(foreign_atoms[j]);
          addSubAction(ada);
        }

        AtomicDeleteAction ada = new AtomicDeleteAction(atoms[i]);
        addSubAction(ada);
        break;

      }
    }

    // If getChangeStatus, unapprove concept if not approve
    if (getChangeStatus() && !getSource().needsReview()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(getSource());
      acsa.setNewValue("N");
      addSubAction(acsa);
    }
  }

}
