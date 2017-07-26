/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  ReportsRelationshipRestrictor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Implements a {@link Relationship} restriction function used for
 * displaying {@link Relationship}s in the {@link Concept} reports.
 *
 * @author MEME Group
 */

public class ReportsRelationshipRestrictor implements CoreDataRestrictor {

  //
  // Fields
  //

  private Relationship[] rels = null;
  private HashSet to_keep = null;
  private HashSet demotions_id_2 = null;
  private HashSet xrs_id_2 = null;
  private HashMap work;

  //
  // Constructors
  //
  /**
   * Instantiates a {@link ReportsRelationshipRestrictor} for the specified
   * {@link Concept}
   * @param concept the {@link Concept}
   */
  public ReportsRelationshipRestrictor(Concept concept) {

    to_keep = new HashSet();
    demotions_id_2 = new HashSet();
    xrs_id_2 = new HashSet();
    work = new HashMap();

    rels = concept.getRelationships();
    for (int i = 0; i < rels.length; i++) {
      Identifier concept_id_2 = rels[i].getRelatedConcept().getIdentifier();
      Relationship rel = (Relationship) work.get(concept_id_2);

      if (rel == null || rank(rels[i]) > rank(rel)) {
        work.put(concept_id_2, rels[i]);

      }
      if (rels[i].isDemoted()) {
        demotions_id_2.add(concept_id_2.toString());

      }
      if (rels[i].getName().equals("XR")) {
        xrs_id_2.add(concept_id_2.toString());
      }
    }

    Collection report_rels = work.values();
    Iterator iterator = report_rels.iterator();
    while (iterator.hasNext()) {
      Relationship relationship = (Relationship) iterator.next();
      to_keep.add(relationship);
    }

  };

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
  // Methods
  //

  /**
   * Indicates whether or not the relationships has the same related
   * concept as a demotion in this concept.
   * @param rel the {@link Relationship}
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean hasSameRelatedConceptAsDemotion(Relationship rel) {
    return demotions_id_2.contains(
        rel.getRelatedConcept().getIdentifier().toString());
  }

  /**
   * Indicates whether or not the relationship has the same related
   * concept as an XR relationship in this concept.
   * @param rel the {@link Relationship}
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean hasSameRelatedConceptAsXR(Relationship rel) {
    return xrs_id_2.contains(
        rel.getRelatedConcept().getIdentifier().toString());
  }

  /**
   * Returns the {@link Relationship} rank.
   * @param rel the {@link Relationship}
   * @return the {@link Relationship} rank
   */
  private long rank(Relationship rel) {
    StringBuffer rank = new StringBuffer(20);

    if (rel.isDemoted()) {
      rank.append("90");
    } else if (rel.isMTHAsserted()) {
      rank.append("80");
    } else if (rel.needsReview()) {
      rank.append("60");
    } else if (rel.isSourceAsserted()) {
      if (rel.needsReview()) {
        rank.append("5");
      } else if (rel.isReviewed()) {
        rank.append("4");
      } else if (rel.isUnreviewed()) {
        rank.append("3");
      } else {
        rank.append("0");

      }
      if (rel.isWeaklyReleasable()) {
        rank.append("7");
      } else if (rel.isReleasable()) {
        rank.append("9");
      } else if (rel.isWeaklyUnreleasable()) {
        rank.append("5");
      } else if (rel.isUnreleasable()) {
        rank.append("3");
      } else {
        rank.append("0");
      }
    } else {
      rank.append("00");

    }
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
   * @return an <code>int</code> code indicating the relative sort order
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
   * Equality funciton.
   * @param object object to compare
   * @return <code>true</code> equal, <codE>false</code> otherwise
   */
  public boolean equals(Object object) {
    if ( (object == null) ||
        (! (object instanceof ReportsRelationshipRestrictor))) {
      return false;
    }
    return this.equals(object);
  }

  //
  // Implementation of CoreDataRestrictor interface
  //

  /**
   * Indicatess whether or not the specified {@link CoreData} element should be kept.
   * @param element the {@link CoreData} element
   * @return <code>true</code> if element must be kept, <code>false</code>
   * otherwise
   */
  public boolean keep(CoreData element) {
    String id =
        ( (Relationship) element).getRelatedConcept().getIdentifier().toString();
    return
        to_keep.contains(element) ||
        element.isDemoted() ||
        demotions_id_2.contains(id) ||
        xrs_id_2.contains(id);
  }
}
