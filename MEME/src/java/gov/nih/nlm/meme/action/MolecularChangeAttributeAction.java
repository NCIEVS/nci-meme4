/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularChangeAttributeAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.ActionException;

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
 */

public class MolecularChangeAttributeAction
    extends MolecularAction {

  //  // Fields
  //

  private Attribute attribute = null;

  //
  // Constructors
  //

  /**
   * No-argument constructor.
   */
  private MolecularChangeAttributeAction() {
    super();
  }

  /**
   * This constructor initializes the action with a attribute.
   * @param attribute An object {@link Attribute}.
   */
  public MolecularChangeAttributeAction(Attribute attribute) {
    super();
    setSource(attribute.getConcept());
    this.attribute = attribute;
    setActionName("MOLECULAR_CHANGE");
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
    return new Concept[] {
        getSource()};
  }

  /**
   * Returns an attribute to change.
   * @return An object {@link Attribute}
   */
  public Attribute getAttributeToChange() {
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
   * Performs molecular change attribute action.
   * @throws ActionException if failed while performing
   * molecular change attribute action.
   */
  public void performAction() throws ActionException {

    Attribute[] attrs = getSource().getAttributes();
    for (int i = 0; i < attrs.length; i++) {
      if (attrs[i].equals(attribute)) {

        if (attrs[i].getStatus() != attribute.getStatus()) {
          AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(attrs[i]);
          acsa.setNewValue(String.valueOf(attribute.getStatus()));
          addSubAction(acsa);
        }

        if (attrs[i].getTobereleased() != attribute.getTobereleased()) {
          AtomicChangeReleasabilityAction acra = new
              AtomicChangeReleasabilityAction(attrs[i]);
          acra.setNewValue(String.valueOf(attribute.getTobereleased()));
          addSubAction(acra);
        }

        if (!attrs[i].getSuppressible().equals(attribute.getSuppressible())) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(attrs[i]);
          acfa.setOldValue(attrs[i].getSuppressible());
          acfa.setNewValue(attribute.getSuppressible());
          acfa.setField("suppressible");
          addSubAction(acfa);
        }

        if (!attrs[i].getSource().equals(attribute.getSource())) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(attrs[i]);
          acfa.setOldValue(attrs[i].getSource().toString());
          acfa.setNewValue(attribute.getSource().toString());
          acfa.setField("source");
          addSubAction(acfa);
        }

        if (!attrs[i].getName().equals(attribute.getName())) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(attrs[i]);
          acfa.setOldValue(attrs[i].getName());
          acfa.setNewValue(attribute.getName());
          acfa.setField("attribute_name");
          addSubAction(acfa);
        }

        if (attrs[i].getLevel() != attribute.getLevel()) {
          AtomicChangeFieldAction acfa = new AtomicChangeFieldAction(attrs[i]);
          acfa.setOldValue(String.valueOf(attrs[i].getLevel()));
          acfa.setNewValue(String.valueOf(attribute.getLevel()));
          acfa.setField("attribute_level");
          addSubAction(acfa);
        }

        if (attrs[i].getAtom() != null &&
            !attrs[i].getAtom().equals(attribute.getAtom())) {
          AtomicChangeAtomAction acaa = new AtomicChangeAtomAction(attrs[i]);
          acaa.setNewAtom(attribute.getAtom());
          addSubAction(acaa);
        }

      }
    }

    // If getChangeStatus, unapprove concept if not approve
    if (getChangeStatus() && !getSource().needsReview()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(getSource());
      acsa.setNewValue("N");
      addSubAction(acsa);
    }
  }

  /**
   * The main method performs a self-QA test
   * @param args An array of arguments.
   */
  public static void main(String[] args) {
    MEMEToolkit.trace("Use memerun.pl gov.nih.nlm.meme.client.ActionClient");
  }
}
