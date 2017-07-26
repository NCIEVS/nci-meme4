/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I11
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;

/**
 * Validates those {@link Concept}s that contain an
 * SFO/LFO relationship to a different {@link Concept}
 *
 * @author MEME Group
 */

public class DT_I11 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I11} check.
   */
  public DT_I11() {
    super();
    setName("DT_I11");
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
    // Get relationships
    //
    Relationship[] rels = source.getRelationships();

    //
    // Look for different related concept
    //
    for (int i = 0; i < rels.length; i++) {
      if (rels[i].getName().equals("SFO/LFO") &&
          rels[i].isApproved() &&
          !rels[i].getConcept().getIdentifier().equals(rels[i].
          getRelatedConcept().getIdentifier())) {
        return true;
      }
    }
    return false;
  }

}
