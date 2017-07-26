/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  DefaultAtomComparator
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Comparator;

/**
 * Implements the default {@link Atom} ordering function.
 *
 * To get the atoms into the correct order is to find the order of the LUIs
 * by rank, then the order of the SUIs by rank.
 * {@link ReportsAtomComparator} is the class that implements these
 * process.
 *
 * @author MEME Group
 */

public class DefaultAtomComparator implements Comparator {

  //
  // Constructors
  //

  public DefaultAtomComparator() {};

  //
  // Overriden Object Methods
  //

  /**
   * Returns an <code>int</code> hashcode.
   * @return an <code>int</code> hashcode
   */
  public int hashCode() {
    return toString().hashCode();
  }

  //
  // Implementation of Comparator interface
  //

  /**
   * Implements an ordering function based on rank.
   * @param o1 the first {@link Atom} to compare
   * @param o2 the second {@link Atom} to compare
   * @return an <code>int</code> indicating the relative ordering
   */
  public int compare(Object o1, Object o2) {
    Atom a2 = (Atom) o2;
    return a2.compareTo(o1);
  }

  /**
   * Dummy equality function.
   * @param object the {@link Object} to compare to
   * @return <code>true</code> if the objects are equal,
   *         <code>false</code> otherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) || (! (object instanceof DefaultAtomComparator))) {
      return false;
    }
    return this.equals(object);
  }

}
