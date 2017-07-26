/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MoveInhibitor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;

/**
 * Generically represents an {@link IntegrityCheck} that takes two {@link
 * Concept}s and determines whether or not moving {@link Atom}s from
 * one to the other would create an illegal state.
 *
 * @author MEME Group
 */

public interface MoveInhibitor extends IntegrityCheck {

  //
  // Methods
  //

  /**
   * Performs the integrity check on the specified source and
   * target {@link Concept}s and the list of {@link Atom}s.
   * @param source the source {@link Concept} to validate
   * @param target the target {@link Concept} to validate
   * @param source_atoms the {@link Atom}s to move
   * @return <code>true</code> if a violation is found, <code>false</code> otherwise
   */
  public boolean validate(Concept source, Concept target, Atom[] source_atoms);

}
