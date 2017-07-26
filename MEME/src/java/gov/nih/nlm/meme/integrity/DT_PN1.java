/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_PN1
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Validates those {@link Concept}s that contain an ambiguous
 * string but at least one {@link Concept} in this ambiguous
 * cluster lacks a releasable <code>MTH/PN</code>.
 *
 * @author MEME Group
 */

public class DT_PN1 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_PN1} check.
   */
  public DT_PN1() {
    super();
    setName("DT_PN1");
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
