/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_M3
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;


/**
 * Validates those {@link Concept}s without a releasable {@link Atom}
 *
 * @author MEME Group
 */

public class DT_M3 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_M3} check.
   */
  public DT_M3() {
    super();
    setName("DT_M3");
  }

  //
  // Methods
  //

  /**
   * Validates the specified concept.
   * @param source the source {@link Concept}
   * @return <code>true</code> if there is a violation, <code>false</code> otherwise
   */
  public boolean validate(Concept source) {

    //
    // Assume no violation
    //
    boolean violation = true;

    //
    // Get all atoms
    //
    Atom[] atoms = source.getAtoms();

    //
    // Find a releasable atom
    //
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].isReleasable()) {
        violation = false;
        break;
      }
    }
    return violation;
  }

}
