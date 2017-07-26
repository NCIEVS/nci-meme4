/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  AbstractDataConstraint
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Abstract implementation a {@link DataConstraint}.
 * It should be used as the superclass
 * for such checks as {@link DT_M1}.
 *
 * @author MEME Group
 */

public abstract class AbstractDataConstraint extends IntegrityCheck.Default implements
    DataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates an empty {@link AbstractDataConstraint}.
   */
  public AbstractDataConstraint() {
    super();
  }

  //
  // Implementation of DataConstraint
  //

  /**
   * Validate concept.
   * @param source the source {@link Concept}
   * @return <code>true</code> if there are integrity violations,
   *    <code>false</code> otherwise
   */
  public abstract boolean validate(Concept source);

}
