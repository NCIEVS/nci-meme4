/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ContextPath
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Generically represents a path to the root of a context tree
 * from an {@link Atom}.
 *
 * @author MEME Group
 */

public interface ContextPath {

  /**
   * Returns a {@link String} representation of the path.
   * @return a {@link String} representation of the path
   */
  public String toString();

  /**
   * Adds the specified {@link Atom}.
   * @param atom the{@link Atom} to add
   */
  public void addAtom(Atom atom);

  /**
   * Removes all {@link Atom}s.
   */
  public void clearAtoms();

  /**
   * Removes the specified {@link Atom}.
   * @param atom the {@link Atom} to remove
   */
  public void removeAtom(Atom atom);

  /**
   * Returns the {@link List} of {@link Atom} from the parent to the root.
   * @return the {@link List} of {@link Atom} from the parent to the root.
   */
  public List getPathToRoot();

  //
  // Inner Classes
  //

  /**
   * This inner class serves as a default implementation of the
   * {@link ContextPath} interface.
   */
  public class Default implements ContextPath {

    //
    // Fields
    //

    private List atoms_from_root = new ArrayList();

    //
    // Implementation of ContextPath interface
    //

    /**
     * Implements {@link ContextPath#toString()}.
     */
    public String toString() {
      StringBuffer sb = new StringBuffer();
      String[] auis = (String[]) atoms_from_root.toArray();
      for (int i = 0; i < auis.length; i++) {
        sb.append(auis[i]);
        if (i != auis.length - 1) {
          sb.append(".");
        }
      }
      return sb.toString();
    }

    /**
     * Implements {@link ContextPath#addAtom(Atom)}.
     */
    public void addAtom(Atom atom) {
      atoms_from_root.add(atom.getIdentifier().toString());
    }

    /**
     * Implements {@link ContextPath#clearAtoms()}.
     */
    public void clearAtoms() {
      atoms_from_root.clear();
    }

    /**
     * Implements {@link ContextPath#removeAtom(Atom)}.
     */
    public void removeAtom(Atom atom) {
      atoms_from_root.remove(atom.getIdentifier().toString());
    }

    /**
     * Implements {@link ContextPath#getPathToRoot()}.
     */
    public List getPathToRoot() {
      return atoms_from_root;
    }

  }
}
