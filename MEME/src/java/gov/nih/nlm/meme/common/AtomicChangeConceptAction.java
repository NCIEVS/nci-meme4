/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomicChangeConceptAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents an action to change the atom that an {@link ConceptElement}
     * is connected to.  Typically this moves an {@link Atom}, {@link Attribute}, or
 * {@link Relationship} from one {@link Concept} to another.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.AtomicChangeConceptAction instead
 */

public class AtomicChangeConceptAction extends gov.nih.nlm.meme.action.
    AtomicChangeConceptAction {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AtomicChangeConceptAction}.
   */
  public AtomicChangeConceptAction() {
    super();
  }

  /**
   * Instantiates an {@link AtomicChangeConceptAction} with the
   * specified atomic action identifier.
   * @param atomic_action_id the unique identifier for this action
   */
  public AtomicChangeConceptAction(int atomic_action_id) {
    super(atomic_action_id);
  }

  /**
   * Instantiates an {@link AtomicChangeConceptAction} with the
   * specified atom.
   * @param atom the {@link Atom} to change
   */
  public AtomicChangeConceptAction(Atom atom) {
    super(atom);
  }

  /**
   * Instantiates an {@link AtomicChangeConceptAction} with the
   * specified relationship.
   * @param relationship the {@link Relationship} to change
   */
  public AtomicChangeConceptAction(Relationship relationship) {
    super(relationship);
  }

  /**
   * Instantiates an {@link AtomicChangeConceptAction} with the
   * specified attribute.
   * @param attribute the {@link Attribute} to change
   */
  public AtomicChangeConceptAction(Attribute attribute) {
    super(attribute);
  }

}
