/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  AbstractMergeMoveInhibitor
 *
 * 06/19/2006 BAC (1-AV8WP): MergeMoveInhibitor
 * 04/07/2006 RBE (1-AV8WP): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Abstract implementation of {@link AbstractMergeMoveInhibitor}
 *
 * @author MEME Group
 */

public abstract class AbstractMergeMoveInhibitor 
  extends AbstractMoveInhibitor implements MergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AbstractMergeMoveInhibitor}.
   */
  public AbstractMergeMoveInhibitor() {
    super();
  }

  //
  // Implementation of AbstractMergeMoveInhibitor
  //

  /**
   * Validate the {@link Concept}s.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @return <Code>true</code> if moving the specified atoms from the source
   * concept to the target concept would produce an integrity violation,
   * <code>false</code> otherwise
   */
  public boolean validate(Concept source, Concept target) {
	  return validate(source, target, source.getAtoms());
  }

  
}
