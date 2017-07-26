/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomCluster
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Collections;

/**
 * Represents a cluster of {@link Atom}s associated with an {@link Identifier}.
 *
 * @author MEME Group
 */

public class AtomCluster extends Cluster {

  //
  // Constructors
  //

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
   * Instantiates an {@link AtomCluster} with the specified identifier.
   * @param id the {@link Identifier}
   */
  public AtomCluster(Identifier id) {
    super(id);
  };

  /**
   * Instantiates an empty {@link AtomCluster}.
   */
  public AtomCluster() {
    super();
  };

  //
  // Methods
  //

  /**
   * Returns the {@link Atom}s.
   * @return the {@link Atom}s
   */
  public Atom[] getAtoms() {
    Collections.sort(this);
    return (Atom[]) toArray(new Atom[0]);
  }

  //
  // Overridden Cluster Methods
  //

  /**
   * Adds the object (presumed to be an {@link Atom}).
   * @param atom the {@link Atom}
   * @return a code indicating whether or not the object was successfully added
   * @throws IllegalArgumentException if the object is not an {@link Atom}
   */
  public boolean add(Object atom) {
    if (! (atom instanceof Atom)) {
      throw new IllegalArgumentException(
          "Only Atoms may be added to an AtomCluster");
    }
    return super.add(atom);
  }

}
