/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularChangeAtomAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

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
 * @deprecated Use gov.nih.nlm.meme.action.MolecularChangeAtomAction instead
 */

public class MolecularChangeAtomAction extends gov.nih.nlm.meme.action.
    MolecularChangeAtomAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with an atom.
   * @param atom An object {@link Atom}.
   */
  public MolecularChangeAtomAction(Atom atom) {
    super(atom);
  }

}
