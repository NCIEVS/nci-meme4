/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ByIdentifierRestrictor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Restricts core data to a certain {@link Identifier} or
 * set of {@link Identifier}s.  This {@link CoreDataRestrictor}
 * can be used by any of the {@link Concept} <code>getRestrictedXXX()</code>
 * methods.
 *
 * @author MEME Group
 */

public class ByIdentifierRestrictor implements CoreDataRestrictor {

  //
  // Fields
  //

  private Identifier identifier_name = null;
  private Identifier[] identifier_names = null;

  //
  // Costructors
  //

  /**
   * Instantiates a {@link ByIdentifierRestrictor} with the
   * specified identifier.
   * @param identifier_name an {@link Identifier} to restrict by
   */
  public ByIdentifierRestrictor(Identifier identifier_name) {
    this.identifier_name = identifier_name;
  }

  /**
   * Instantiates a {@link ByIdentifierRestrictor} with the
   * specified set of identifiers.
   * @param identifier_names {@link Identifier}s to restrict by
   */
  public ByIdentifierRestrictor(Identifier[] identifier_names) {
    this.identifier_names = identifier_names;
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
   * @param o1 the first {@link CoreData} object to compare
   * @param o2 the second {@link CoreData} object to compare
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
    if ( (object == null) || (! (object instanceof ByIdentifierRestrictor))) {
      return false;
    }
    return this.equals(object);
  }

  //
  // Implementation of CoreDataRestrictor interface
  //

  /**
   * Indicates whether or not an element should be kept.
   * @param element the {@link CoreData} to test
   * @return <code>true</code> if the element has one of
   * the specified identifiers, <code>false</code> otherwise
   */
  public boolean keep(CoreData element) {
    if (identifier_names != null) {
      boolean ret_val = false;
      for (int i = 0; i < identifier_names.length; i++) {
        if ( (element).getIdentifier().equals(identifier_names[i])) {
          ret_val = true;
        }
      }
      return ret_val;
    } else {
      return (element).getIdentifier().equals(identifier_name);
    }
  }

}
