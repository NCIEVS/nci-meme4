/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Cluster
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.ArrayList;

/**
 * Generically represents an ordered cluster of objects
 * with an associated {@link Identifier}.
 *
 * @author MEME Group
 */

public class Cluster extends ArrayList implements Comparable {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//
  // Fields
  //
  protected Identifier id = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link Cluster} with the specified {@link Identifier}.
   * @param id the {@link Identifier}
   */
  public Cluster(Identifier id) {
    super();
    this.id = id;
  };

  /**
   * Instantiates an empty {@link Cluster}.
   */
  public Cluster() {
    super();
  };

  //
  // Methods
  //

  /**
   * Returns the cluster {@link Identifier}.
   * @return the cluster {@link Identifier}
   */
  public Identifier getIdentifier() {
    return id;
  }

  /**
   * Sets the cluster {@link Identifier}.
   * @param id the cluster {@link Identifier}
   */
  public void setIdentifier(Identifier id) {
    this.id = id;
  }

  //
  // Comparable implementation
  //

  /**
   * Implements an ordering function based on {@link Identifier}s.
   * This function orders higher identifiers after lower ones.
   * @param o an {@link Object}
   * @return an <code>int</code> indicating the relative ordering
   */
  public int compareTo(Object o) {
    if (o == null) {
      throw new NullPointerException();
    }
    if (! (o instanceof Cluster)) {
      throw new ClassCastException();
    } else {
      return getIdentifier().compareTo( ( (Cluster) o).getIdentifier());
    }
  }

  //
  // Overridden Object methods
  //

  /**
   * Implements an equality test based on {@link Identifier}s.
   * @param o an {@link Object} to compare to
   * @return <code>true</code> if the objects are equal,
   *         <code>false</code> otherwise
   */
  public boolean equals(Object o) {
    if (o == null || ! (o instanceof Cluster)) {
      return false;
    } else {
      return getIdentifier().equals( ( (Cluster) o).getIdentifier());
    }
  }

  /**
   * Returns an <code>int</code> hashcode based on
   * the cluster {@link Identifier}.
   * @return an <code>int</code> hashcode
   */
  public int hashCode() {
    return getIdentifier().hashCode();
  }

  //
  // Overridden ArrayList Methods
  //

  /**
   * Adds the specified element and sets the cluster {@link Identifier}
   * if it is core data.
   * @param obj the specified element to add
   * @return a <code>boolean</code> representation of status
   */
  public boolean add(Object obj) {
    if (obj instanceof CoreData) {
      ( (CoreData) obj).setClusterIdentifier(id);
    }
    return super.add(obj);
  }

}
