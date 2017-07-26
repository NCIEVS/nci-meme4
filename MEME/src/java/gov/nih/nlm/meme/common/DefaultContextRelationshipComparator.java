/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  DefaultContextRelationshipComparator
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Comparator;

/**
 * Implements the default {@link ContextRelationship} ordering function.
 *
 * @author MEME Group
 */

public class DefaultContextRelationshipComparator implements Comparator {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link DefaultContextRelationshipComparator}.
   */
  public DefaultContextRelationshipComparator() {};

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
   * Implements an ordering function based on relationship name.
   * PAR less than CHD less than SIB
   * @param o1 the first {@link ContextRelationship} to compare
   * @param o2 the second {@link ContextRelationship} to compare
   * @return an <code>int</code> indicating the relative ordering
   */
  public int compare(Object o1, Object o2) {
    return (int) ( (ContextRelationship) o1).getName().charAt(1) -
        (int) ( (ContextRelationship) o2).getName().charAt(1);
  }

  /**
   * Dummy equality function.
   * @param object the {@link Object} to compare
   * @return <code>true</code> if the objects are equal,
   *         <code>false</code> otherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) ||
        (! (object instanceof DefaultContextRelationshipComparator))) {
      return false;
    }
    return this.equals(object);
  }
}