/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  BySourceRestrictor
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

public class BySourceRestrictor implements CoreDataRestrictor {

  //
  // Fields
  //

  private String source = null;
  private String[] sources = null;
  private boolean negate = false;

  //
  // Costructors
  //

  /**
   * Instantiates a {@link BySourceRestrictor} with the
   * specified source.
   * @param source the abbreviation of the source to restricty by
   */
  public BySourceRestrictor(String source) {
    this.source = source;
    this.negate = false;
  }

  /**
   * Instantiates a {@link BySourceRestrictor} with the
   * specified source.
   * @param source the abbreviation of the source to restricty by
   * @param negate <code>true</code> if {@link CoreData} matching
   *        the source should be removed, <code>false</code> otherwise
   */
  public BySourceRestrictor(String source, boolean negate) {
    this.source = source;
    this.negate = negate;
  }

  /**
   * Instantiates a {@link BySourceRestrictor} with the
   * specified sources.
   * @param sources the source abbreviations to restrict by
   */
  public BySourceRestrictor(String[] sources) {
    this.sources = sources;
    this.negate = false;
  }

  /**
   * Instantiates a {@link BySourceRestrictor} with the
   * specified sources.
   * @param sources the source abbreviations to restricty by
   * @param negate <code>true</code> if {@link CoreData} matching
   *        the sources should be removed, <code>false</code> otherwise
   */
  public BySourceRestrictor(String[] sources, boolean negate) {
    this.sources = sources;
    this.negate = negate;
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
   **/
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
    if ( (object == null) || (! (object instanceof BySourceRestrictor))) {
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
   * @return <code>true</code> if element must be kept. <code>false</code>
   * otherwise.
   * Following is the truth table for this method. <code>found</code>
   * means that the source of the {@link CoreData} element matches either
   * the list or the single source in the restrictor.
   * <pre>
   * found           negate          keep?
   * ------          ------          ------
   * true            true            false
   * true            false           true
   * false           true            true
   * false           false           false
   * </pre>
   */
  public boolean keep(CoreData element) {
    // constructed with multiple sources
    if (sources != null) {
      boolean found = false;
      for (int i = 0; i < sources.length; i++) {
        if ( ( (CoreData) element).getSource().getSourceAbbreviation().equals(
            sources[i])) {
          found = true;
          break;
        }
      }
      return ( (found && !negate) || (!found && negate));
    } else {

      // constructed with a single source
      return
          ( ( (CoreData) element).getSource().getSourceAbbreviation().equals(
          source)
           && !negate) ||
          (! ( (CoreData) element).getSource().getSourceAbbreviation().equals(
          source)
           && negate);
    }
  }

}
