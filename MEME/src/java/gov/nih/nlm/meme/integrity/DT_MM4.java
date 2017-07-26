/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_MM4
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Validates those {@link Concept}s where <code>MTH/MM</code>
 * {@link gov.nih.nlm.meme.common.Atom}s are misaligned.
 *
 *
 * @author MEME Group
 */

public class DT_MM4 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_MM4} check.
   */
  public DT_MM4() {
    super();
    setName("DT_MM4");
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
