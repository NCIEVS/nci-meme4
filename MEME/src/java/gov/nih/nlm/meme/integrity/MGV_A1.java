/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_A1
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Validates merges between two {@link Concept}s where
 * both contain "released as approved" atoms.
 *
 * @author MEME Group
 */
public class MGV_A1 extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_A1} check.
   */
  public MGV_A1() {
    super();
    setName("MGV_A1");
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
    // unimplemented
    return false;
  }

}
