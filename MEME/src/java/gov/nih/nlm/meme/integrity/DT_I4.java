/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I4
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;

/**
 * Validates those {@link Concept}s that contain
 * at least one {@link Relationship} with {@link Authority} like 'PIR%'.
 *
 * @author MEME Group
 */

public class DT_I4 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiate a {@link DT_I4} check.
   */
  public DT_I4() {
    super();
    setName("DT_I4");
  }

  //
  // Methods
  //

  /**
   * Validates the specified concept.
   * @param source the sourced {@link Concept}
   * @return <code>true</code> if there is a violation, <code>false</code> otherwise
   */
  public boolean validate(Concept source) {
    //
    // Assume no violation
    //
    boolean violation = false;

    //
    // Get all relationships
    //
    Relationship[] rels = source.getRelationships();

    //
    // Look for relationship with PIR authority
    //
    for (int i = 0; i < rels.length; i++) {
      if (rels[i].getAuthority().toString().startsWith("PIR")) {
        violation = true;
        break;
      }
    }
    return violation;
  }

}
