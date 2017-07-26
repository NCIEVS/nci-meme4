/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomicChangeReleasabilityAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents an action to change the releasability of a {@link CoreData}
 * element.
 *
 * @author MEME Group
 */

public class AtomicChangeReleasabilityAction extends gov.nih.nlm.meme.action.
    AtomicChangeReleasabilityAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicChangeReleasabilityAction}.
   */
  public AtomicChangeReleasabilityAction() {
    super();
  }

  /**
   * Instantiates an {@link AtomicChangeReleasabilityAction} with the
   * specified atomic action identifier.
   * @param atomic_action_id the atomic action id.
   */
  public AtomicChangeReleasabilityAction(int atomic_action_id) {
    super(atomic_action_id);
  }

  /**
   * Instantiates an {@link AtomicChangeReleasabilityAction} with the
   * specified atom.
   * @param atom the {@link Atom} to change
   */
  public AtomicChangeReleasabilityAction(Atom atom) {
    super(atom);
  }

  /**
   * Instantiates an {@link AtomicChangeReleasabilityAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to change
   */
  public AtomicChangeReleasabilityAction(Relationship relationship) {
    super(relationship);
  }

  /**
   * Instantiates an {@link AtomicChangeReleasabilityAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to change
   */
  public AtomicChangeReleasabilityAction(Attribute attribute) {
    super(attribute);
  }

  /**
   * Instantiates an {@link AtomicChangeReleasabilityAction} with the
   * specified concept.
   * @param concept the {@link Concept} to change
   */
  public AtomicChangeReleasabilityAction(Concept concept) {
    super(concept);
  }

}
