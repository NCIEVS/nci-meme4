/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  SourceComparator
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Comparator;

/**
 * Implements a {@link Source} ordering function based on source abbreviations.
 *
 * @author MEME Group
 */

public class SourceComparator implements Comparator {

  //
  // Constructors
  //

  public SourceComparator() {};

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
   * Comparison function based on source abbreviation.
   * @param o1 first object to compare
   * @param o2 second object to compare
   * @return an <code>int</code> code indicating relative sort order
   */
  public int compare(Object o1, Object o2) {
    return ( (CoreData) o1).getSource().getSourceAbbreviation().compareTo( ( (
        CoreData) o2).getSource().getSourceAbbreviation());
  }

  /**
   * Dummy equality function.
   * @param object the object to compare to
   * @return <code>true</code> if equal, <code>false</code> otherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) || (! (object instanceof SourceComparator))) {
      return false;
    }
    return this.equals(object);
  }

}
