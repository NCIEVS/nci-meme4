/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MVS_A
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Validates splits where the source {@link Concept} contains
 * atoms with the same last released cui values.
 *
 * @author MEME Group
 */
public class MVS_A extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MVS_A} check.
   */
  public MVS_A() {
    super();
    setName("MVS_A");
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
