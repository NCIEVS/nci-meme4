/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomElement
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents something connected to an {@link Atom}
 * (and by inheritance, a {@link Concept}).
 *
 * @author MEME Group
 */

public interface AtomElement extends ConceptElement {

  /**
   * Returns the {@link Atom} to which this element is connected.
   * @return the {@link Atom} to which this element is connected
   */
  public Atom getAtom();

  /**
   * Sets the {@link Atom} to which this element is connected.
   * @param atom the {@link Atom} to which this element is connected
   */
  public void setAtom(Atom atom);

}
