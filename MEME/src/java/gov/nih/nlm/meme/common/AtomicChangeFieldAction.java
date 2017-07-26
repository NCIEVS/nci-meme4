/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomicChangeFieldAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents an action to change some {@link CoreData} field.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.AtomicChangeFieldAction instead
 */
public class AtomicChangeFieldAction extends gov.nih.nlm.meme.action.
    AtomicChangeFieldAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicChangeFieldAction}.
   */
  public AtomicChangeFieldAction() {
    super();
  }

  /**
   * Instantiates an {@link AtomicChangeFieldAction} with the specified
   * atomic action identifier.
   * @param atomic_action_id the unique identifier for this action
   */
  public AtomicChangeFieldAction(int atomic_action_id) {
    super(atomic_action_id);
  }

  /**
   * Instantiates an {@link AtomicChangeFieldAction} with the
   * specified atom.
   * @param atom the {@link Atom} to change
   */
  public AtomicChangeFieldAction(Atom atom) {
    super(atom);
  }

  /**
   * Instantiates an {@link AtomicChangeFieldAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to change
   */
  public AtomicChangeFieldAction(Relationship relationship) {
    super(relationship);
  }

  /**
   * Instantiates an {@link AtomicChangeFieldAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to change
   */
  public AtomicChangeFieldAction(Attribute attribute) {
    super(attribute);
  }

  /**
   * Instantiates an {@link AtomicChangeFieldAction} with the
   * specified concept.
   * @param concept the {@link Concept} to change
   */
  public AtomicChangeFieldAction(Concept concept) {
    super(concept);
  }

}
