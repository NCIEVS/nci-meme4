/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularInsertConceptAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.ActionException;

/**
 * This action inserts concept.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to insert concept.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Concept to be inserted</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>None</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Do not refresh any concepts</li>
 *     <li>Set source to the concept being inserted</li>
 *     <li>Insert concept (we should set editing_authority, editing_timestamp,
 *         and approval_molecule_id). done by aproc_insert_cs</li>
 *     <li>Insert any atoms attached to concept object</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Create concept and atom references
 * Concept concept = new Concept.Default();
 * Atom atom = new Atom.Default();
 * atom.setConcept(concept);
 * atom.setSource(source);
 * atom.setTermgroup(termgroup)
 * atom.setCode(new Code("12345"));
 * atom.setString("Test atom");
 * atom.setReleased(FV_NOT_RELEASED);
 * atom.setTobereleased(FV_RELEASABLE);
 * atom.setLanguage(language);
 * concept.addAtom(atom);
 *
 * // Create & configure action
     * MolecularInsertConceptAction mica = new MolecularInserConceptAction(concept);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 */

public class MolecularInsertConceptAction
    extends MolecularAction {

  //
  // Constructors
  //

  /**
   * No-argument constructor.
   */
  private MolecularInsertConceptAction() {
    super();
  }

  /**
   * This constructor initializes the action with a concept.
   * @param concept An object {@link Concept}.
   */
  public MolecularInsertConceptAction(Concept concept) {
    super();
    setSource(concept);
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
    return new Concept[0];
  }

  /**
   * Returns a concept to insert.
   * @return An object {@link Concept}
   */
  public Concept getConceptToInsert() {
    return getSource();
  }

  /**
   * Indicates whether or not the action should lock related concept rows.
   * @return false the default value
   */
  public boolean lockRelatedConcepts() {
    return false;
  }

  /**
   * Performs molecular insert concept action.
   * @throws ActionException if failed while performing
   * molecular insert concept action.
   */
  public void performAction() throws ActionException {

    AtomicInsertAction aia = new AtomicInsertAction(getSource());
    addSubAction(aia);

    // Go through all of the atoms in concept
    Atom[] atoms = getSource().getAtoms();
    for (int i = 0; i < atoms.length; i++) {
      aia = new AtomicInsertAction(atoms[i]);
      addSubAction(aia);
    }

    Attribute[] attributes = getSource().getAttributes();
    for (int i = 0; i < attributes.length; i++) {
      aia = new AtomicInsertAction(attributes[i]);
      addSubAction(aia);
    }

  }

}
