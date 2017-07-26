/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularInsertConceptAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

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
 * @deprecated Use gov.nih.nlm.meme.action.MolecularInsertConceptAction instead
 */

public class MolecularInsertConceptAction extends gov.nih.nlm.meme.action.
    MolecularInsertConceptAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with a concept.
   * @param concept An object {@link Concept}.
   */
  public MolecularInsertConceptAction(Concept concept) {
    super(concept);
  }

}
