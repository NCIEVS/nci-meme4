/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularDeleteConceptAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action deletes concept.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to delete concept.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Concept to be deleted</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh source concept</li>
 *     <li>Get all attributes and delete them</li>
 *     <li>Get all relationships and delete them</li>
 *     <li>Get all atoms and delete them</li>
 *     <li>Delete the concept itself</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Obtain concept reference
 * Concept concept = ... get concept ...;
 *
 * // Create & configure action
     * MolecularDeleteConceptAction mdca = new MolecularDeleteConceptAction(concept);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.MolecularDeleteConceptAction instead
 */

public class MolecularDeleteConceptAction extends gov.nih.nlm.meme.action.
    MolecularDeleteConceptAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with a concept.
   * @param concept An object {@link Concept}.
   */
  public MolecularDeleteConceptAction(Concept concept) {
    super(concept);
  }

}
