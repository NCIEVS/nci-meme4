/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  MolecularDeleteConceptAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.exception.ActionException;

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
 */

public class MolecularDeleteConceptAction
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
  private MolecularDeleteConceptAction() {
    super();
  }

  /**
   * This constructor initializes the action with a concept.
   * @param concept An object {@link Concept}.
   */
  public MolecularDeleteConceptAction(Concept concept) {
    super();
    this.concept = concept;
    setSource(concept);
    setActionName("MOLECULAR_DELETE");
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
   * Returns a concept to delete.
   * @return An object {@link Concept}
   */
  public Concept getConceptToDelete() {
    return concept;
  }

  /**
   * Performs molecular delete concept action.
   * @throws ActionException if failed while performing
   * molecular delete concept action.
   */
  public void performAction() throws ActionException {

    AtomicDeleteAction ada = null;

    Attribute[] attrs = concept.getAttributes();
    for (int i = 0; i < attrs.length; i++) {
      ada = new AtomicDeleteAction(attrs[i]);
      addSubAction(ada);
    }

    Relationship[] rels = concept.getRelationships();
    for (int i = 0; i < rels.length; i++) {
      ada = new AtomicDeleteAction(rels[i]);
      addSubAction(ada);
    }

    Atom[] atoms = concept.getAtoms();
    for (int i = 0; i < atoms.length; i++) {
      ada = new AtomicDeleteAction(atoms[i]);
      addSubAction(ada);
    }

    ada = new AtomicDeleteAction(getSource());
    addSubAction(ada);

  }

}
