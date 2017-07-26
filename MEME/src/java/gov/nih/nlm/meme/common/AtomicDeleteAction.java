/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomicDeleteAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents an action to delete some {@link CoreData} element.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.AtomicDeleteAction instead
 */

public class AtomicDeleteAction extends gov.nih.nlm.meme.action.
    AtomicDeleteAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicDeleteAction}.
   */
  public AtomicDeleteAction() {
    super();
  }

  /**
   * Instantiates an {@link AtomicDeleteAction} with the
   * specifed atomic action identifier.
   *@param atomic_action_id the unique identifier for this action
   */
  public AtomicDeleteAction(int atomic_action_id) {
    super(atomic_action_id);
  }

  /**
   * Instantiates an {@link AtomicDeleteAction} with the
   * specifed atom.
   * @param atom the {@link Atom} to delete
   */
  public AtomicDeleteAction(Atom atom) {
    super(atom);
  }

  /**
   * Instantiates an {@link AtomicDeleteAction} with the
   * specifed relationship.
   * @param relationship the {@link Relationship} to delete
   */
  public AtomicDeleteAction(Relationship relationship) {
    super(relationship);
  }

  /**
   * Instantiates an {@link AtomicDeleteAction} with the
   * specifed attribute.
   * @param attribute the {@link Attribute} to delete
   */
  public AtomicDeleteAction(Attribute attribute) {
    super(attribute);
  }

  /**
   * Instantiates an {@link AtomicDeleteAction} with the
   * specifed concept.
   * @param concept the {@link Concept} to delete
   */
  public AtomicDeleteAction(Concept concept) {
    super(concept);
  }

}
