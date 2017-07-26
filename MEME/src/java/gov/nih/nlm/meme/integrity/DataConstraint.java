/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DataConstraint
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Generically represents an integrity check that takes a single
 * {@link Concept} and validates
 * some aspect of its content.  Checks like {@link DT_M1} are data constraints.
 *
 * @author MEME Group
 */

public interface DataConstraint extends IntegrityCheck {

  //
  // Methods
  //

  /**
   * Performs the integrity check on the specified {@link Concept}.
   * @param source the source{@link Concept} to validate
   * @return <code>true</code> if a violation is found, <code>false</code> otherwise
   */
  public boolean validate(Concept source);

}
