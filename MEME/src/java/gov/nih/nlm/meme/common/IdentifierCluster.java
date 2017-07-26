/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  IdentifierCluster
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.ArrayList;

/**
 * Represents a cluster of {@link Identifier}s that has been assigned an id itself.
 *
 * @author MEME Group
 */

public class IdentifierCluster {

  //
  // Fields
  //
  private Identifier cluster_id = null;
  private ArrayList ids = new ArrayList();

  //
  // Constructors
  //

  /**
   * Instantiates an {@link IdentifierCluster}
   * with the specified cluster id.
   * @param cluster_id an <code>int</code> cluster id
   */
  public IdentifierCluster(int cluster_id) {
    this.cluster_id = new Identifier.Default(cluster_id);
  }

  /**
   * Instantiates a {@link IdentifierCluster}
   * with the specified cluster {@link Identifier}.
   * Note, the parameter is not cloned, the actual object is used.
   * @param cluster_id an {@link Identifier} cluster id.
   */
  public IdentifierCluster(Identifier cluster_id) {
    this.cluster_id = cluster_id;
  }

  //
  // Accessor methods
  //

  /**
   * Returns the cluster {@link Identifier}.
   * @return the cluster {@link Identifier}
   */
  public Identifier getClusterIdentifier() {
    return cluster_id;
  }

  /**
   * Sets the cluster {@link Identifier}.
   * @param cluster_id the cluster {@link Identifier}
   */
  public void setClusterIdentifier(Identifier cluster_id) {
    this.cluster_id = cluster_id;
  }

  /**
   * Returns the {@link Identifier} elements of the cluster.
   * @return an {@link Identifier}<code>[]</code>
   */
  public Identifier[] getIdentifiers() {
    return (Identifier[]) ids.toArray(new Identifier[0]);
  }

  /**
   * Adds the specified {@link Identifier} to the cluster.
   * @param id the {@link Identifier} to add
   */
  public void addIdentifier(Identifier id) {
    ids.add(id);
  }

  /**
   *
   * Removes the specified {@link Identifier} from the cluster.
   * @param id the {@link Identifier} to remove
   */
  public void removeIdentifier(Identifier id) {
    ids.remove(id);
  }

  /**
   * Empties the cluster.
   */
  public void clearIdentifiers() {
    ids.clear();
  }

  //
  // Overridden Object methods
  //

  /**
   * Returns a {@link String} representation of the cluster.
   * @return a {@link String} representation of the cluster
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(100);
    sb.append("Cluster ");
    sb.append(cluster_id);
    sb.append(": ");
    sb.append(ids);
    return sb.toString();
  }

  /**
   * Implements an equality function based on the contents
   * of the cluster.
   * @param o the {@link IdentifierCluster} to compare to
   * @return <code>true</code> the clusters being compared
   *     have the same {@link Identifier}s,
   *     <code>false</code> otherwise
   */
  public boolean equals(Object o) {
    if (o == null || ! (o instanceof IdentifierCluster)) {
      return false;
    }
    // If strings are equal, they are equal
    return ids.toString().equals(o.toString());
  }

  /**
   * Returns an <code>int</code> hashcode.
   * @return an <code>int</code> hashcode
   */
  public int hashCode() {
    return toString().hashCode();
  }

}
