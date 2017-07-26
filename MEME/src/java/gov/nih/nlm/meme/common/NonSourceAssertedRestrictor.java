/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  NonSourceAssertedRestrictor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;


/**
 * Restricts {@link CoreData} retrieved from a {@link Concept} to just MTH
 * asserted elements.
 *
 * @author MEME Group
 */

public class NonSourceAssertedRestrictor implements CoreDataRestrictor {


  //
  // Constructors
  //

  public NonSourceAssertedRestrictor() {
    super();
  }

  //
  // Implementation of Comparator
  //

  /**
   * Sort order based on natural {@link CoreData} sort ordering.
   * @param o1 first object to compare
   * @param o2 second object to compare
   * @return an <code>int</codE> indicating relative sort order
   */
  public int compare(Object o1, Object o2) {
    return ( (CoreData) o1).compareTo(o2);
  }

  /**
   * Dummy equals function.
   * @param object object to compare to
   * @return <code>true</code> if objects are same class
   */
  public boolean equals(Object object) {
    if ( (object == null) || (! (object instanceof NonSourceAssertedRestrictor))) {
      return false;
    }
    return this.equals(object);
  }

  //
  // Implementation of CoreDataRestrictor interface
  //

  /**
   * Indicatess whether or not a {@link CoreData} element should be kept.
   * @param element the {@link CoreData} element to check
   * @return <code>true</code> if element must be kept, <code>false</code>
   * otherwise.
   */
  public boolean keep(CoreData element) {
    return!element.isSourceAsserted();
  }
}
