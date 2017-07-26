/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularInsertAtomAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.ActionException;

/**
 * This action inserts atom.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to insert atom.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Atom to be inserted</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>None</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Do not refresh any concepts (but expect getSource().getStatus() to be correct).</li>
 *     <li>Insert new atom</li>
 *     <li>If getChangeStatus unapprove concept</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Create atom reference
 * Atom atom = new Atom.Default();
 * atom.setConcept(concept);
 * atom.setSource(source);
 * atom.setTermgroup(termgroup);
 * atom.setCode(new Code("12345"));
 * atom.setString("test atom");
 * atom.setReleased(FV_NOT_RELEASED);
 * atom.setTobereleased(FV_RELEASABLE);
 * atom.setLanguage(language);
 *
 * // Create & configure action
 * MolecularInsertAtomAction miaa = new MolecularInsertAtomAction(atom);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 */

public class MolecularInsertAtomAction
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
  private MolecularInsertAtomAction() {
    super();
  }

  /**
   * This constructor initializes the action with an atom.
   * @param atom An object {@link Atom}.
   */
  public MolecularInsertAtomAction(Atom atom) {
    super();
    this.atom = atom;
    setSource(atom.getConcept());
    setActionName("MOLECULAR_INSERT");
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
   * Returns an atom to insert.
   * @return An object {@link Atom}
   */
  public Atom getAtomToInsert() {
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
   * Performs molecular insert atom action.
   * @throws ActionException if failed while performing
   * molecular insert atom action.
   */
  public void performAction() throws ActionException {
    AtomicInsertAction aia = new AtomicInsertAction(atom);
    addSubAction(aia);

    // If getChangeStatus, unapprove concept if not approve
    if (getChangeStatus() && !getSource().needsReview()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(getSource());
      acsa.setNewValue("N");
      addSubAction(acsa);
    }

  }

}
