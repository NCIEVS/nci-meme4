/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularInsertAttributeAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.ActionException;

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
 */

public class MolecularInsertAttributeAction
    extends MolecularAction {

  //
  // Fields
  //

  private Attribute attribute = null;

  //
  // Constructors
  //

  /**
   * No-argument constructor.
   */
  private MolecularInsertAttributeAction() {
    super();
  }

  /**
   * This constructor initializes the action with a attribute.
   * @param attribute An object {@link Attribute}.
   */
  public MolecularInsertAttributeAction(Attribute attribute) {
    super();
    this.attribute = attribute;
    setSource(attribute.getConcept());
    setActionName("MOLECULAR_INSERT");
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
   * Returns an attribute to insert.
   * @return An object {@link Attribute}
   */
  public Attribute getAttributeToInsert() {
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
   * Performs molecular insert attribute action.
   * @throws ActionException if failed while performing
   * molecular insert attribute action.
   */
  public void performAction() throws ActionException {
    AtomicInsertAction aia = new AtomicInsertAction(attribute);
    addSubAction(aia);

    // If getChangeStatus, unapprove concept if not approve
    if (getChangeStatus() && !getSource().needsReview()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(getSource());
      acsa.setNewValue("N");
      addSubAction(acsa);
    }
  }

}
