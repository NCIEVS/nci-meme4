/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I3
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;

/**
 * Validates those {@link Concept}s that contain at least
 * one demoted {@link Relationship}.
 *
 * @author MEME Group
 */

public class DT_I3 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I3} check.
   */
  public DT_I3() {
    super();
    setName("DT_I3");
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
    // Look for demotions.
    //
    Relationship[] rels = source.getDemotions();
    if (rels.length > 0) {
      return true;
    } else {
      return false;
    }
  }

}
