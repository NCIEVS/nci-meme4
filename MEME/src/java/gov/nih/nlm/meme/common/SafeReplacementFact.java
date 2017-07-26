/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  SafeReplacementFact
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a fact that connects two atoms
 * by virtue of having one be the update source's
 * version of the other one.  In this case,
 * the "connected" atom is the replacement.
 *
 * This class roughly corresponds to <code>mom_safe_replacement</code>.
 *
 * @author MEME Group
 */

public class SafeReplacementFact extends AtomFact.Default {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link SafeReplacementFact}.
   */
  public SafeReplacementFact() {
    super();
  };

  //
  // Convenience Accessor Methods
  //

  /**
   * Sets the old atom.
   * @param atom the old atom
   */
  public void setOldAtom(Atom atom) {
    setAtom(atom);
  }

  /**
   * Returns the old, or <i>replaced</i>, atom.
   * @return the old, or <i>replaced</i>, atom
   */
  public Atom getOldAtom() {
    return getAtom();
  }

  /**
   * Sets the new atom.
   * @param atom the new atom
   */
  public void setNewAtom(Atom atom) {
    setConnectedAtom(atom);
  }

  /**
   * Returns the new, or <i>replacement</i>, atom.
   * @return the new, or <i>replacement</i>, atom
   */
  public Atom getNewAtom() {
    return getConnectedAtom();
  }
}
