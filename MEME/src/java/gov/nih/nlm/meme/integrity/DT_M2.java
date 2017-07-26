/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_M2
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Validates those {@link Concept}s lacking an approved, releasable
 * {@link gov.nih.nlm.meme.common.Relationship} to
 * another {@link Concept} (including hierarchical relationships).
 *
 * @author MEME Group
 */

public class DT_M2 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_M2} check.
   */
  public DT_M2() {
    super();
    setName("DT_M2");
  }

  //
  // Methods
  //

  /**
   * Not implemented.  This is merely a place-holder to be backwards-compatable
   * with MEME3.
   * @param source the source {@link Concept}
   * @return <code>true</code> if there is a violation, <code>false</code> otherwise
   */
  public boolean validate(Concept source) {
    // unimplemented
    return false;
  }

}
