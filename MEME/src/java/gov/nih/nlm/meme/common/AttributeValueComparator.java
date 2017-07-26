/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  AttributeValueComparator
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Comparator;

/**
 * Implements a comparison function that sorts {@link Attribute}s
 * by their values.  The comparison is done by calling
 * {@link Attribute#getValue()}.
 *
 * @see Attribute
 * @author MEME Group
 */

public class AttributeValueComparator implements Comparator {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AttributeValueComparator}.
   */
  public AttributeValueComparator() {};

  //
  // Overriden Object Methods
  //

  /**
   * Returns an <code>int</code> hashcode.
   * @return An <code>int</code> hashcode
   */
  public int hashCode() {
    return toString().hashCode();
  }

  //
  // Implementation of Comparator interface
  //

  /**
   * Implements an ordering function based on attribute value.
   * If attribute values are the same, sort on status.
   * @param o1 the first {@link Attribute} to be compared
   * @param o2 the second {@link Attribute} to be compared
   * @return an <code>int</code> indicating the relative ordering
   */
  public int compare(Object o1, Object o2) {
    Attribute a1 = (Attribute) o1;
    Attribute a2 = (Attribute) o2;
    int comp = a1.getValue().compareTo(a2.getValue());
    if (comp == 0) {
      return a1.getStatus() - a2.getStatus();
    }
    return comp;
  }

  /**
   * Dummy equals function.
   * @param object an {@link Object} to compare to
   * @return <code>true</code> if the objects are equal,
   *         <code>false</code> otherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) || (! (object instanceof AttributeValueComparator))) {
      return false;
    }
    return this.equals(object);
  }

}
