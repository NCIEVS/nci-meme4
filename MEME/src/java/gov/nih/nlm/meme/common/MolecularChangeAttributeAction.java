/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularChangeAttributeAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action changes attribute.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to change an attribute. Typically this
 *         is used to change releasabiltiy, status, suppressibility, or some
 *         other characteristic of the attribute. </td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Attribute to be changed</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>Source</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Refresh the source concept</li>
 *     <li>Get all attributes, find the one matching the one being changed</li>
 *     <li>If status values are different update to new value</li>
 *     <li>If releasability values are different update to new value</li>
 *     <li>If suppressibility values are different, update to new value</li>
 *     <li>If getChangeStatus unapprove concept</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Obtain attribute object
 * Attribute attribute = ... get attribute ...;
 * attribute.setConcept(concept);
 *
 * // Change releasability to 'Y'
 * attribute.setTobereleased(FV_RELEASABLE);
 *
 * // Change status to 'R'
 * attribute.setStatus(FV_REVIEWED);
 *
 * // Create & configure action
 * MolecularChangeAttributeAction mcaa = new MolecularChangeAttributeAction(attribute)
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
     * @deprecated Use gov.nih.nlm.meme.action.MolecularChangeAttributeAction instead
 */

public class MolecularChangeAttributeAction extends gov.nih.nlm.meme.action.
    MolecularChangeAttributeAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with a attribute.
   * @param attribute An object {@link Attribute}.
   */
  public MolecularChangeAttributeAction(Attribute attribute) {
    super(attribute);
  }

}
