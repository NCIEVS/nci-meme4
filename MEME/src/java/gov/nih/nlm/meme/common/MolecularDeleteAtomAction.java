/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularDeleteAtomAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

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
 * @deprecated Use gov.nih.nlm.meme.action.MolecularDeleteAtomAction instead
 */

public class MolecularDeleteAtomAction extends gov.nih.nlm.meme.action.
    MolecularDeleteAtomAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with an atom.
   * @param atom An object {@link Atom}.
   */
  public MolecularDeleteAtomAction(Atom atom) {
    super(atom);
  }

}
