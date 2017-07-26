/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularChangeConceptAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.ActionException;

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
 */

public class MolecularChangeConceptAction
    extends MolecularAction {

  //
  // Fields
  //

  private Concept concept = null;

  //
  // Constructors
  //

  /**
   * No-argument constructor.
   */
  private MolecularChangeConceptAction() {
    super();
  }

  /**
   * This constructor initializes the action with a concept.
   * @param concept An object {@link Concept}.
   */
  public MolecularChangeConceptAction(Concept concept) {
    super();
    setSource(new Concept.Default(concept.getIdentifier().intValue()));
    this.concept = concept;
    setActionName("MOLECULAR_CHANGE");
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
   * Returns a concept to change.
   * @return An object {@link Concept}
   */
  public Concept getConceptToChange() {
    return concept;
  }

  /**
   * Indicates whether or not the action should lock related concept rows.
   * @return false the default value
   */
  public boolean lockRelatedConcepts() {
    return false;
  }

  /**
   * Performs molecular change concept action.
   * @throws ActionException if failed while performing
   * molecular change concept action.
   */
  public void performAction() throws ActionException {
    if (concept.getStatus() != getSource().getStatus()) {
      AtomicChangeStatusAction acsa = new AtomicChangeStatusAction(source);
      acsa.setNewValue(String.valueOf(concept.getStatus()));
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
