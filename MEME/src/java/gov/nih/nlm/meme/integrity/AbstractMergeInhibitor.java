/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  AbstractMergeInhibitor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Abstract implementation of a {@link MergeInhibitor}.
 * Should be used as the superclass for such checks as {@link MGV_E}.
 *
 * @author MEME Group
 */

public abstract class AbstractMergeInhibitor extends IntegrityCheck.Default implements
    MergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AbstractMergeInhibitor}.
   */
  public AbstractMergeInhibitor() {
    super();
  }

  //
  // Implementation of MergeInhibitor
  //

  /**
   * Validate the {@link Concept}s.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @return <code>true</code> if merging the source and target
   * concepts would cause an integrity violation, <code>false</code>
   * otherwise
   */
  public abstract boolean validate(Concept source, Concept target);

}
