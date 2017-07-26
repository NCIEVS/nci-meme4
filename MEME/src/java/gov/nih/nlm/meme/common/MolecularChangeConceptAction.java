/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularChangeConceptAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action changes concept.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
     *     <td>This molecular action is used to change a concept.  Currently the only
 *     thing about a concept that can be changed this way is its status.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Concept to be changed</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source concept, but maintain a separate reference to the object
 *         initialy passed to constructor</li>
 *     <li>If status values are different, update to new value</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Obtain concept reference
 * Concept concept = ... get concept ...
 *
 * // Change status to 'N'
 * concept.setStatus(FV_NEEDS_REVIEW);
 *
 * // Create & configure action
     * MolecularChangeConceptAction mcca = new MolecularChangeConceptAction(concept);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.MolecularChangeConceptAction instead
 */

public class MolecularChangeConceptAction extends gov.nih.nlm.meme.action.
    MolecularChangeConceptAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with a concept.
   * @param concept An object {@link Concept}.
   */
  public MolecularChangeConceptAction(Concept concept) {
    super(concept);
  }

}
