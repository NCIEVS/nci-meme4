/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I3B
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;

/**
 * Validates those {@link Concept}s which contain
 * at least one demoted {@link Relationship} without a matching
 * releasable <code>MTH</code> asserted {@link Relationship}.
 *
 * @author MEME Group
 */

public class DT_I3B extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I3B} check.
   */
  public DT_I3B() {
    super();
    setName("DT_I3B");
  }

  //
  // Methods
  //

  /**
   * Validates the specified concept.
   * @param source the source {@link Concept}
   * @return <code>true</code> if there is a violation, <code>false</code> otherwise
   */
  public boolean validate(Concept source) {

    //
    // Get demotions
    //
    Relationship[] dems = source.getDemotions();

    //
    // Get all relationships
    //
    Relationship[] rels = source.getRelationships();

    //
    // Assume no violation
    //
    boolean found = true;

    //
    // Scan for violations
    //
    for (int i = 0; i < dems.length; i++) {
      //
      // We've found a demotion, now look for a matching C level rel
      //
      found = false;
      for (int j = 0; j < rels.length; j++) {
        if (dems[i].getRelatedConcept().equals(rels[j].getRelatedConcept()) &&
            rels[j].isConceptLevel() && rels[j].isReleasable()) {
          //
          // OK, we found a matching C level rel, check the next case
          //
          found = true;
          break;
        }
      }
      //
      // If we did not find a matching C level rel, VIOLATION!
      //
      if (!found) {
        return true;
      }
    }
    return false;
  }

}
