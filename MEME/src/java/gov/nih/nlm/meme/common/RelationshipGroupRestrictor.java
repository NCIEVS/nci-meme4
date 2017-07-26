/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  RelationshipGroupRestrictor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Implements a {@link Relationship} restriction function used for
 * displaying group relationships.
 *
 * @author MEME Group
 */

public class RelationshipGroupRestrictor implements CoreDataRestrictor {

  //
  // Fields
  //

  private Identifier group_id = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link RelationshipGroupRestrictor}.
   * @param relationship the {@link Relationship} whose group should be used to restrict
   */
  public RelationshipGroupRestrictor(Relationship relationship) {
    group_id = relationship.getGroupIdentifier();
  }

  //
  // Implementation of Comparator
  //

  /**
   * Comparison function.
   * @param o1 first object to compare
   * @param o2 second object to compare
   * @return an <code>int</code> representing relative sort order
   */
  public int compare(Object o1, Object o2) {
    return 0;
  }

  //
  // Implementation of CoreDataRestrictor interface
  //

  /**
   * Indicatess whether or not the specified
   * {@link CoreData} element should be kept.
   * @param element the {@link CoreData} element
   * @return <code>true</code> if element must be kept. <code>false</code>
   * otherwise.
   */
  public boolean keep(CoreData element) {
    return group_id != null &&
        group_id.equals( ( (Relationship) element).getGroupIdentifier());
  }
}
