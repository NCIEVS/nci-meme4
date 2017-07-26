/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ByNameAttributeRestrictor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Restricts {@link Attribute}s to those with a specified name or
 * set of names.  This {@link CoreDataRestrictor}
 * can be used by {@link Concept#getRestrictedAttributes(CoreDataRestrictor)}
 * method. This is useful for operations like "Give me all of the SOS
 * attributes in this concept".
 *
 * @author MEME Group
 */

public class ByNameAttributeRestrictor implements CoreDataRestrictor {

  //
  // Fields
  //

  private String attribute_name = null;
  private String[] attribute_names = null;

  //
  // Costructors
  //

  /**
   * Instantiates a {@link ByNameAttributeRestrictor} with a
   * specified attribute name.
   * @param attribute_name the attribute name to restricty by
   */
  public ByNameAttributeRestrictor(String attribute_name) {
    this.attribute_name = attribute_name;
  }

  /**
   * Instantiates a {@link ByNameAttributeRestrictor} with the
   * specified attribute names.
   * @param attribute_names the attribute names to restricty by
   */
  public ByNameAttributeRestrictor(String[] attribute_names) {
    this.attribute_names = attribute_names;
  }

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
  // Implementation of Comparator
  //

  /**
   * Implements an ordering function based on rank.
   * @param o1 the first {@link Attribute} to compare
   * @param o2 the second {@link Attribute} to compare
   * @return an <code>int</code> indicating the relative ordering
   */
  public int compare(Object o1, Object o2) {
    return ( (CoreData) o2).compareTo(o1);
  }

  /**
   * Dummy equals function.
   * @param object an {@link Object}
   * @return <code>true</code> if the objects are equal,
   *        <codE>false</code> otherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) || (! (object instanceof ByNameAttributeRestrictor))) {
      return false;
    }
    return this.equals(object);
  }

  //
  // Implementation of CoreDataRestrictor interface
  //

  /**
   * Indicates whether or not to keep the specified {@link Attribute}.
   * @param element the {@link Attribute} to keep or remove
   * @return <code>true</code> if element should be kept, <code>false</code>
   * otherwise
   */
  public boolean keep(CoreData element) {
    if (attribute_names != null) {
      boolean ret_val = false;
      for (int i = 0; i < attribute_names.length; i++) {
        if ( ( (Attribute) element).getName().equals(attribute_names[i])) {
          ret_val = true;
        }
      }
      return ret_val;
    } else {
      return ( (Attribute) element).getName().equals(attribute_name);
    }
  }

}
