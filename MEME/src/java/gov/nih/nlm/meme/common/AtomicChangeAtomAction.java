/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomicChangeAtomAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents an action to change the atom that an {@link AtomElement}
 * is connected to.  Typically this moves an {@link Attribute} or
 * {@link Relationship} from one {@link Atom} to another.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.AtomicChangeAtomAction instead
 */

public class AtomicChangeAtomAction extends gov.nih.nlm.meme.action.
    AtomicChangeAtomAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicChangeAtomAction}.
   */
  public AtomicChangeAtomAction() {
    super();
  }

  /**
   * Instantiates an {@link AtomicChangeAtomAction} with the specified
   * atomic action identifier.
   * @param atomic_action_id the unique id of this action
   */
  public AtomicChangeAtomAction(int atomic_action_id) {
    super(atomic_action_id);
  }

  /**
   * Instantiates an {@link AtomicChangeAtomAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to change
   */
  public AtomicChangeAtomAction(Relationship relationship) {
    super(relationship);
  }

  /**
   * Instantiates an {@link AtomicChangeAtomAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to change
   */
  public AtomicChangeAtomAction(Attribute attribute) {
    super(attribute);
  }

}
