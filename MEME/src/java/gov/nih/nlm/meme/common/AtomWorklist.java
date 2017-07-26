/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomWorklist
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Collections;

/**
 * Represents a clustered worklist of {@link Atom}s.
 *
 * @author MEME Group
 */

public class AtomWorklist extends Worklist {

  //
  // Constructors
  //

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
   * Instantiates an empty {@link AtomWorklist}.
   */
  public AtomWorklist() {
    super();
  };

  //
  // Methods
  //

  /**
   * Returns the atoms contained in the worklist.
   * @return the {@link Atom} objects contained in the worklist.
   */
  public Atom[] getAtoms() {
    Collections.sort(this);
    return (Atom[]) toArray(new Atom[0]);
  }

  //
  // Overridden Worklist Methods
  //

  /**
   * Adds the specified {@link Atom}s.
   * @param atoms the {@link Atom}s
   */
  public void add(Atom[] atoms) {
    if (atoms != null) {
      for (int i = 0; i < atoms.length; i++) {
        super.add(atoms[i]);
      }
    }
  }

  /**
   * Adds the object (presumed to be an {@link Atom}.
   * @param atom an {@link Atom} to add to the worklist
   * @return a status code indicating whether or not the add was successful
   * @throws IllegalArgumentException if the object is not an {@link Atom}
   */
  public boolean add(Object atom) {
    if (! (atom instanceof Atom)) {
      throw new IllegalArgumentException(
          "Only Atoms and AtomClusters may be added to an AtomWorklist");
    }
    return super.add(atom);
  }

  /**
       * Adds the specified {@link Cluster} (presumed to be an {@link AtomCluster}).
   * @param cluster the {@link AtomCluster} to add to the worklist
       * @throws IllegalArgumentException if the cluster is not an {@link AtomCluster}
   */
  public void addCluster(Cluster cluster) {
    if (! (cluster instanceof AtomCluster)) {
      throw new IllegalArgumentException(
          "Only Atoms and AtomClusters may be added to an AtomWorklist");
    }
    super.addCluster(cluster);
  }

}
