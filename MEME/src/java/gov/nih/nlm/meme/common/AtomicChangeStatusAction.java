/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomicChangeStatusAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents an action to change the status of a {@link CoreData}
 * element.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.AtomicChangeStatusAction instead
 */

public class AtomicChangeStatusAction extends gov.nih.nlm.meme.action.
    AtomicChangeStatusAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicChangeStatusAction}.
   */
  public AtomicChangeStatusAction() {
    super();
  }

  /**
   * Instantiates an {@link AtomicChangeStatusAction} with the
   * specified atomic action identifier.
   * @param atomic_action_id the unique identifier for this acction
   */
  public AtomicChangeStatusAction(int atomic_action_id) {
    super(atomic_action_id);
  }

  /**
   * Instantiates an {@link AtomicChangeStatusAction} with the
   * specified atom.
   * @param atom the {@link Atom} to change
   */
  public AtomicChangeStatusAction(Atom atom) {
    super(atom);
  }

  /**
   * Instantiates an {@link AtomicChangeStatusAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to change
   */
  public AtomicChangeStatusAction(Relationship relationship) {
    super(relationship);
  }

  /**
   * Instantiates an {@link AtomicChangeStatusAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to change
   */
  public AtomicChangeStatusAction(Attribute attribute) {
    super(attribute);
  }

  /**
   * Instantiates an {@link AtomicChangeStatusAction} with the
   * specified concept.
   * @param concept the {@link Concept} to change
   */
  public AtomicChangeStatusAction(Concept concept) {
    super(concept);
  }

}
