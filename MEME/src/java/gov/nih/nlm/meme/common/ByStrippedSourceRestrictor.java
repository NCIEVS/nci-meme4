/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ByStrippedSourceRestrictor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Restricts core data to a certain {@link Identifier} or
 * set of {@link Identifier}s.  This {@link CoreDataRestrictor}
 * can be used by any of the {@link Concept} <code>getRestrictedXXX()</code>
 * methods.
 * This is useful for any operation where you want to restrict the view
 * of the data to a particular source or set of sources.
 *
 * @author MEME Group
 */
public class ByStrippedSourceRestrictor implements CoreDataRestrictor {

  //
  // Fields
  //

  private String source = null;
  private String[] sources = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link ByStrippedSourceRestrictor}
   * with the specified stripped source abbreviation.
   * @param source a stripped source abbreviation value to restrict by
   */
  public ByStrippedSourceRestrictor(String source) {
    this.source = source;
  }

  /**
   * Instantiates a {@link ByStrippedSourceRestrictor}
   * with the specified stripped source abbreviations.
   * @param sources the stripped source abbreviations values to restrict by
   */
  public ByStrippedSourceRestrictor(String[] sources) {
    this.sources = sources;
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
   * @return An <code>int</code> indicating the relative ordering
   */
  public int compare(Object o1, Object o2) {
    return ( (CoreData) o2).compareTo(o1);
  }

  /**
   * Dummy equals function.
   * @param object an {@link Object}
   * @return <code>true</code> if the objects are equal,
   *         <code>false</code> otherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) || (! (object instanceof ByStrippedSourceRestrictor))) {
      return false;
    }
    return this.equals(object);
  }

  //
  // Implementation of CoreDataRestrictor interface
  //

  /**
   * Indicates whether or not an element should be kept.
   * @param element the {@link CoreData} element
   * @return <code>true</code> if element should be kept,
   *         <code>false</code> otherwise.
   */
  public boolean keep(CoreData element) {
    if (sources != null) {
      boolean ret_val = false;
      for (int i = 0; i < sources.length; i++) {
        if ( ( (CoreData) element).getSource().getStrippedSourceAbbreviation().
            equals(sources[i])) {
          ret_val = true;
        }
      }
      return ret_val;
    } else {
      return ( (CoreData) element).getSource().getStrippedSourceAbbreviation().
          equals(source);
    }
  }

}
