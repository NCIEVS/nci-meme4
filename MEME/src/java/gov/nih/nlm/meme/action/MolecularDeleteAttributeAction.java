/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularDeleteAttributeAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.ActionException;

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
 */

public class MolecularDeleteAttributeAction
    extends MolecularAction {

  //
  // Fields
  //

  Attribute attribute = null;

  //
  // Constructors
  //

  /**
   * No-argument constructor.
   */
  private MolecularDeleteAttributeAction() {
    super();
  }

  /**
   * This constructor initializes the action with an attribute.
   * @param attribute An object {@link Attribute}.
   */
  public MolecularDeleteAttributeAction(Attribute attribute) {
    super();
    this.attribute = attribute;
    setSource(attribute.getConcept());
    setActionName("MOLECULAR_DELETE");
    setAssignCuis(false);
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
   * Returns an attribute to delete.
   * @return An object {@link Attribute}
   */
  public Attribute getAttributeToDelete() {
    return attribute;
  }

  /**
   * Indicates whether or not the action should lock related concept rows.
   * @return false the default value
   */
  public boolean lockRelatedConcepts() {
    return false;
  }

  /**
   * Performs molecular delete attribute action.
   * @throws ActionException if failed while performing
   * molecular delete attribute action.
   */
  public void performAction() throws ActionException {
    AtomicDeleteAction ada = new AtomicDeleteAction(attribute);
    addSubAction(ada);

    // If getChangeStatus, unapprove concept if not approve
    if (getChangeStatus() && !getSource().needsReview()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(getSource());
      acsa.setNewValue("N");
      addSubAction(acsa);
    }
  }

}
