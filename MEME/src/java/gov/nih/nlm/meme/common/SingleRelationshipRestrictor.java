/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  SingleRelationshipRestrictor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.HashMap;

/**
 * Restriction function that keeps only preferred {@link Relationship}s of
 * a {@link Concept}.  Used in a call to
 * {@link Concept#getRestrictedRelationships(CoreDataRestrictor)}.
 *
 * @author MEME Group
 */

public class SingleRelationshipRestrictor implements CoreDataRestrictor {

  //
  // Fields
  //

  private HashMap to_keep = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link SingleRelationshipRestrictor} for the specified
   * {@link Concept}
   * @param concept the {@link Concept}
   */
  public SingleRelationshipRestrictor(Concept concept) {

    to_keep = new HashMap();

    Relationship[] rels = concept.getRelationships();
    for (int i = 0; i < rels.length; i++) {
      Identifier concept_id_2 = rels[i].getRelatedConcept().getIdentifier();
      Relationship rel = (Relationship) to_keep.get(concept_id_2);

      if (rel == null || rank(rels[i]) > rank(rel)) {
        to_keep.put(concept_id_2, rels[i]);

      }
    }

  };

  //
  // Overriden Object Methods
  //

  /**
   * Returns an <code>int</code> hashcode
   * @return an <code>int</code> hashcode
   */
  public int hashCode() {
    return toString().hashCode();
  }

  //
  // Methods
  //

  /**
   * Returns the {@link Relationship} rank.
   * @param rel the {@link Relationship}.
   * @return the {@link Relationship} rank
   */
  private long rank(Relationship rel) {
    StringBuffer rank = new StringBuffer(20);

    String r = rel.getSource().getRank().toString();
    r = "0000".substring(r.length()) + r;
    rank.append(r);
    r = rel.getIdentifier().toString();
    r = "0000000000".substring(r.length()) + r;
    rank.append(r);

    return Long.valueOf(rank.toString()).longValue();

  }

  //
  // Implementation of Comparator
  //

  /**
   * Comparison function.
   * @param o1 first object to compare
   * @param o2 second object to compare
   * @return an <code>int<code> indicating the relative sort order
   */
  public int compare(Object o1, Object o2) {
    Relationship rel1 = (Relationship) o1;
    Relationship rel2 = (Relationship) o2;

    int ret_val = rel1.getRelatedConcept().getIdentifier().intValue() -
        rel2.getRelatedConcept().getIdentifier().intValue();

    // If concept_ids are equal, then sort by rank.
    if (ret_val == 0) {
      long lrank = rank(rel2) - rank(rel1);
      if (lrank == 0) {
        return 0;
      }
      return (int) (lrank / Math.abs(lrank));
    } else {
      return ret_val;
    }

  }

  /**
   * Equality function.
   * @param object object to compare to.
   * @return <code>true</code> if equal, <code>false</code> otherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) || (! (object instanceof SingleRelationshipRestrictor))) {
      return false;
    }
    return this.equals(object);
  }

  //
  // Implementation of CoreDataRestrictor interface
  //

  /**
   * Indicates whether or not the specified {@link CoreData} element should be kept.
   * @param element the {@link CoreData} element
   * @return <code>true</code> if element should be kept, <code>false</code>
   * otherwise.
   */
  public boolean keep(CoreData element) {
    Identifier id =
        ( (Relationship) element).getRelatedConcept().getIdentifier();
    return element.equals(to_keep.get(id));
  }
}
