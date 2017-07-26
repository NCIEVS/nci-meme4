/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_E
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.BySourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;

/**
 * Validates merging between two {@link Concept}s.
 * It is connected by an approved, releasable, non RT?, SY, LK, SFO/LFO, BT?, or
 * NT? {@link Relationship} whose source is not <code>MSH</code> and is
 * not listed in <code>ic_single</code>.
 *
 * @author MEME Group
 */

public class MGV_E extends AbstractUnaryDataMergeInhibitor {

  //
  // Constructors
  //

  /**
   * instantiates the {@link MGV_E} check.
   */
  public MGV_E() {
    super();
    setName("MGV_E");
  }

  //
  // Methods
  //

  /**
   * Validates the pair of {@link Concept}s.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @return <code>true</code> if constraint violated, <code>false</code>
   * otherwise
   */
  public boolean validate(Concept source, Concept target) {
    //
    // Obtain relationships from sources in the list (from ic_single)
    //
    Relationship[] rels =
        source.getRestrictedRelationships(new BySourceRestrictor(
        getCheckDataValues(), true));

    //
    // Find a relationship connecting the source and target concepts
    // with a valid relationship name.
    //
    for (int i = 0; i < rels.length; i++) {
      if (rels[i].isReleasable() &&
          !rels[i].getName().equals("SFO/LFO") &&
          !rels[i].getName().equals("RT?") &&
          !rels[i].getName().equals("BT?") &&
          !rels[i].getName().equals("NT?") &&
          !rels[i].getName().equals("SY") &&
          !rels[i].getName().equals("LK") &&
          rels[i].isApproved() &&
          !rels[i].getSource().getStrippedSourceAbbreviation().equals("MSH") &&
          rels[i].getRelatedConcept().equals(target)) {
        return true;
      }
    }
    return false;
  }

}
