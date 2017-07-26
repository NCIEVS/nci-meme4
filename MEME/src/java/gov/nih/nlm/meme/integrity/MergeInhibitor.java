/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MergeInhibitor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Generically represents an {@link IntegrityCheck} that takes two {@link
 * Concept}s and determines whether or not merging them would create an illegal state.
 *
 * @author MEME Group
 */
public interface MergeInhibitor extends IntegrityCheck {

  //
  // Methods
  //

  /**
   * Performs the integrity check on the specified source and
   * target {@link Concept}s.
   * @param source the source {@link Concept} to validate
   * @param target the target {@link Concept} to validate
   * @return <code>true</code> if a violation is found, <code>false</code> otherwise
   */
  public boolean validate(Concept source, Concept target);

}
