/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_M1
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;

/**
 * Validates those {@link Concept}s lacking an approved,
 * releasable {@link ConceptSemanticType}.
 *
 * @author MEME Group
 */

public class DT_M1 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_M1} check.
   */
  public DT_M1() {
    super();
    setName("DT_M1");
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
    // Assume no violation
    //
    boolean violation = true; // Until proven false

    //
    // Get semantic types
    //
    ConceptSemanticType[] stys = source.getSemanticTypes();

    //
    // Violation if there are none
    //
    if (stys.length == 0) {
      return true;
    }

    //
    // Check that there is a releaseable, approved STY
    //
    for (int i = 0; i < stys.length; i++) {
      if (stys[i].isReleasable()) {
        violation = false;
        break;
      }
    }
    return violation;
  }

}
