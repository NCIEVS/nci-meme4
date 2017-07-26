/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_F
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractMergeMoveInhibitor
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.ByStrippedSourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Relationship;

/**
 * Validates merging between two {@link Concept}s.
 * It is connected by an approved, releasable, non RT?, SY, LK, SFO/LFO, BT?, or
 * NT? current version <code>MSH</code> {@link Relationship}.
 *
 * @author MEME Group
 */
public class MGV_F extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_F} check.
   */
  public MGV_F() {
    super();
    setName("MGV_F");
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
    // Obtain MSH rels
    //
    Relationship[] rels =
        source.getRestrictedRelationships(new ByStrippedSourceRestrictor("MSH"));

    //
    // Find current version MSH rel connecting source and target
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
          rels[i].getSource().isCurrent() &&
          rels[i].getRelatedConcept().equals(target)) {
        return true;
      }
    }
    return false;
  }

}
