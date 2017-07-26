/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomicInsertAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents an action to insert some {@link CoreData} element.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.AtomicInsertAction instead
 */

public class AtomicInsertAction extends gov.nih.nlm.meme.action.
    AtomicInsertAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicInsertAction}.
   */
  public AtomicInsertAction() {
    super();
  }

  /**
   * Instantiates an {@link AtomicInsertAction} with the
   * specified atomic action identifier.
   * @param atomic_action_id the unique identifier for this action
   */
  public AtomicInsertAction(int atomic_action_id) {
    super(atomic_action_id);
  }

  /**
   * Instantiates an {@link AtomicInsertAction} with the
   * specified atom.
   * @param atom the {@link Atom} to insert
   */
  public AtomicInsertAction(Atom atom) {
    super(atom);
  }

  /**
   * Instantiates an {@link AtomicInsertAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to insert
   */
  public AtomicInsertAction(Relationship relationship) {
    super(relationship);
  }

  /**
   * Instantiates an {@link AtomicInsertAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to insert
   */
  public AtomicInsertAction(Attribute attribute) {
    super(attribute);
  }

  /**
   * Instantiates an {@link AtomicInsertAction} with the
   * specified concept.
   * @param concept the {@link Concept} to insert
   */
  public AtomicInsertAction(Concept concept) {
    super(concept);
  }

}
