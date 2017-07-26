/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  MolecularInsertAttributeAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * This action inserts attribute.
 * <br>
 * <table width="100%" border>
 * <tr bgcolor="#ffffcc">
 *     <td colspan="2"><center><b>Action Details</b></center></td></tr>
 * <tr><td><b>Description</b></td>
 *     <td>This molecular action is used to insert attribute.</td></tr>
 * <tr><td><b>Source</b></td>
 *     <td>Attribute to be inserted</td></tr>
 * <tr><td><b>Target</b></td>
 *     <td>N/A</td></tr>
 * <tr><td><b>Concepts To Refresh</b></td>
 *     <td>None</td></tr>
 * <tr><td><b>Integrities</b></td>
 *     <td>N/A</td></tr>
 * <tr><td valign="top"><b>Algorithm</b></td>
 *     <td>
 *     <li>Do not refresh any concepts (but expect getSource().getStatus() to be correct).</li>
 *     <li>Insert new attribute</li>
 *     <li>If getChangeStatus unapprove concept</li>
 *     </td></tr>
 * <tr><td valign="top"><b>Sample Usage</b></td>
 *     <td>
 * <pre><br>
 * // Create attribute reference
 * Attribute attribute = new Attribute.Default();
 * attribute.setConcept(concept);
 * attribute.setAtom(atom);
 * attribute.setSource(source);
 * attribute.setName("DEFINITION");
 * attribute.setValue("This is a definition.");
 * attribute.setLevel('S');
 * attribute.setReleased(FV_NOT_RELEASED);
 * attribute.setTobereleased(FV_RELEASABLE);
 *
 * // Create & configure action
 * MolecularInsertAttributeAction miaa = new MolecularInsertAttributeAction(attribute);
 * </pre>
 *         The action is now ready to use
 *     </td></tr>
 * </table>
 *
 * @author MEME Group
     * @deprecated Use gov.nih.nlm.meme.action.MolecularInsertAttributeAction instead
 */

public class MolecularInsertAttributeAction extends gov.nih.nlm.meme.action.
    MolecularInsertAttributeAction {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with a attribute.
   * @param attribute An object {@link Attribute}.
   */
  public MolecularInsertAttributeAction(Attribute attribute) {
    super(attribute);
  }

}
