/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I9
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Validates those {@link gov.nih.nlm.meme.common.Concept}s that contain a current
 * version MSH "entry term" and the same approved, releasable {@link gov.nih.nlm.meme.common.Relationship} to
 * its MSH MH as another {@link gov.nih.nlm.meme.common.Concept} which also has a current version MSH
 * "entry term" with a matching code.  The "same relationship" means one
 * concept has a BT, NT, or RT relationship and the other either has a
 * {@link gov.nih.nlm.meme.common.Relationship} with the same name or it has an RT? relationship.
 *
 * @author MEME Group
 */

public class DT_I9 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I9} check.
   */
  public DT_I9() {
    super();
    setName("DT_I9");
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
