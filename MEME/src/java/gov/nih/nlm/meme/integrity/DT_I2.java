/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I2
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Concept;

/**
 * Validates those {@link Concept}s that contain at least
 * one releasable {@link Atom} merged by the merge engine,
 * indicated by an ENG-% {@link Authority}.
 *
 * @author MEME Group
 */

public class DT_I2 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I2} check.
   */
  public DT_I2() {
    super();
    setName("DT_I2");
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
    // Get all atoms
    //
    boolean violation = false;
    Atom[] atoms = source.getAtoms();

    //
    // Find one with an ENG authority
    //
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].getAuthority().toString().startsWith("ENG-") &&
          atoms[i].isReleasable()) {
        violation = true;
        break;
      }
    }
    return violation;
  }

}
