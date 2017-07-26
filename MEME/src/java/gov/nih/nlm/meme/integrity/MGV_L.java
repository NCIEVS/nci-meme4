/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_L
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Validates merges between {@link Concept}s in which one
 * contains one object (STY,source,termgroup) and the other contains an
 * incompatible object as listed in <code>ic_pair</code>.
 *
 * @author MEME Group
 */
public class MGV_L extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_L} check.
   */
  public MGV_L() {
    super();
    setName("MGV_L");
  }

  //
  // Methods
  //

  /**
   * Not implemented.  This is merely a place-holder to be backwards-compatable
   * with MEME3.
   * @param source the source {@link Concept}
   * @param target the source {@link Concept}
       * @return <code>true</code> if validation passed, <code>false</code> otherwise
   */
  public boolean validate(Concept source, Concept target) {
    // Unimplemented
    return false;
  }

}
