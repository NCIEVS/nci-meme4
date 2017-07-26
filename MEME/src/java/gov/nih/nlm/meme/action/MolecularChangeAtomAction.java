/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularChangeAtomAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.ActionException;

/**
 * This action changes atom.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to change atom.  Typically this
 *         is used to change releasabiltiy, status, suppressibility, or some
 *         other characteristic of the atom.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Atom to be changed</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source concept</li>
 *     <li>Get all atoms, find the one matching the one being changed</li>
 *     <li>If status values are different update status to new value</li>
 *     <li>If releasability values are different update releasability to new value<br>
 *         &nbsp;&nbsp;&nbsp;
 *         a. Change connected attribute tbr to new value<br>
 *         &nbsp;&nbsp;&nbsp;
 *         b. Change connected relationship tbr to new value
 *     </li>
 *     <li>If suppressibility values are different, update suppressibility</li>
 *     <li>If getChangeStatus unapprove concept</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Obtain atom reference
 * Atom atom = ... get atom ...;
 * atom.setConcept(concept);
 *
 * // Change releasabiltiy to 'Y'
 * atom.setTobereleased(FV_RELEASABLE);
 *
 * // Change status to 'R'
 * atom.setStatus(FV_REVIEWED);
 *
 * // Create & configure action
 * MolecularChangeAtomAction mcaa = new MolecularChangeAtomAction(atom);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 */

public class MolecularChangeAtomAction
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
  private MolecularChangeAtomAction() {
    super();
  }

  /**
   * This constructor initializes the action with an atom.
   * @param atom An object {@link Atom}.
   */
  public MolecularChangeAtomAction(Atom atom) {
    super();
    setSource(atom.getConcept());
    this.atom = atom;
    setActionName("MOLECULAR_CHANGE");
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
   * Returns an atom to change.
   * @return An object {@link Atom}
   */
  public Atom getAtomToChange() {
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
   * Performs molecular change atom action.
   * @throws ActionException if failed while performing
   * molecular change atom action.
   */
  public void performAction() throws ActionException {

    Atom[] atoms = getSource().getAtoms();
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].equals(atom)) {

        if (atoms[i].getStatus() != atom.getStatus()) {
          AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(atoms[i]);
          acsa.setNewValue(String.valueOf(atom.getStatus()));
          addSubAction(acsa);
        }

        if (atoms[i].getTobereleased() != atom.getTobereleased()) {
          AtomicChangeReleasabilityAction acra = new
              AtomicChangeReleasabilityAction(atoms[i]);
          acra.setNewValue(String.valueOf(atom.getTobereleased()));
          addSubAction(acra);
        }

        if (!atoms[i].getSuppressible().equals(atom.getSuppressible())) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(atoms[i]);
          acfa.setOldValue(atoms[i].getSuppressible());
          acfa.setNewValue(atom.getSuppressible());
          acfa.setField("suppressible");
          addSubAction(acfa);
        }

        if (!atoms[i].getSource().equals(atom.getSource())) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(atoms[i]);
          acfa.setOldValue(atoms[i].getSource().toString());
          acfa.setNewValue(atom.getSource().toString());
          acfa.setField("source");
          addSubAction(acfa);
        }

        if (!atoms[i].getTermgroup().equals(atom.getTermgroup())) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(atoms[i]);
          acfa.setOldValue(atoms[i].getTermgroup().getTermType());
          acfa.setNewValue(atom.getTermgroup().getTermType());
          acfa.setField("tty");
          addSubAction(acfa);

          acfa = new AtomicChangeFieldAction(atoms[i]);
          acfa.setOldValue(atoms[i].getTermgroup().toString());
          acfa.setNewValue(atom.getTermgroup().toString());
          acfa.setField("termgroup");
          addSubAction(acfa);
        }

        if (!atoms[i].getCode().equals(atom.getCode())) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(atoms[i]);
          acfa.setOldValue(atoms[i].getCode().toString());
          acfa.setNewValue(atom.getCode().toString());
          acfa.setField("code");
          addSubAction(acfa);
        }

        if (!atoms[i].getSUI().equals(atom.getSUI())) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(atoms[i]);
          acfa.setOldValue(atoms[i].getSUI().toString());
          acfa.setNewValue(atom.getSUI().toString());
          acfa.setField("sui");
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
