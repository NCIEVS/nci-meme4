/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AtomChecklist
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Collections;

/**
 * Represents a clustered checklist of {@link Atom}s.
 *
 * @author MEME Group
 */

public class AtomChecklist extends Checklist {

  //
  // Constructors
  //

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
   * Instantiates an empty {@link AtomChecklist}.
   */
  public AtomChecklist() {
    super();
  };

  //
  // Methods
  //

  /**
   * Returns the atoms contained in the checklist.
   * @return the {@link Atom}s contained in the checklist
   */
  public Atom[] getAtoms() {
    Collections.sort(this);
    return (Atom[]) toArray(new Atom[0]);
  }

  //
  // Overridden Checklist Methods
  //

  /**
   * Adds specified {@link Atom}s.
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
   * Adds specified object (presumed to be an {@link Atom} to the checklist.
   * Overrides superclass method.
   * @param object the {@link Atom}
   * @return a status value indicating whether or not the object was added
   * @throws IllegalArgumentException if object is not an {@link Atom}
   */
  public boolean add(Object object) {
    if (! (object instanceof Atom)) {
      throw new IllegalArgumentException(
          "Only Atoms and AtomClusters may be added to an AtomChecklist");
    }
    return super.add(object);
  }

  /**
   * Adds the {@link Cluster}.
   * @param cluster the {@link Cluster}
       * @throws IllegalArgumentException if the cluster is not an {@link AtomCluster}
   */
  public void addCluster(Cluster cluster) {
    if (! (cluster instanceof AtomCluster)) {
      throw new IllegalArgumentException(
          "Only Atoms and AtomClusters may be added to an AtomChecklist");
    }
    super.addCluster(cluster);
  }

}
