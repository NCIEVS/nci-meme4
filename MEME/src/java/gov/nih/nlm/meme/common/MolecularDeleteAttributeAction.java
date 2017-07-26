/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularDeleteAttributeAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action deletes attribute.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to delete attribute.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Attribute to be deleted</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>None</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Do not refresh any concepts (but expect getSource().getStatus() to be correct).</li>
 *     <li>Delete attribute</li>
 *     <li>If getChangeStatus unapprove concept</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Obtain attribute reference
 * Attribute attribute = ... get attribute ...;
 * attribute.setConcept(concept);
 *
 * // Create & configure action
 * MolecularDeleteAttributeAction mdaa = new MolecularDeleteAttributeAction(attribute);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
     * @deprecated Use gov.nih.nlm.meme.action.MolecularDeleteAttributeAction instead
 */

public class MolecularDeleteAttributeAction extends gov.nih.nlm.meme.action.
    MolecularDeleteAttributeAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with an attribute.
   * @param attribute An object {@link Attribute}.
   */
  public MolecularDeleteAttributeAction(Attribute attribute) {
    super(attribute);
  }

}
