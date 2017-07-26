/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_PN3
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;

/**
 * Validates those {@link Concept}s which contain
 * a releasable <code>MTH/PN</code> {@link Atom} but no releasable ambiguous strings.
 *
 * @author MEME Group
 */

public class DT_PN3 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_PN3} check.
   */
  public DT_PN3() {
    super();
    setName("DT_PN3");
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
    // Assume no MTH/PN
    //
    boolean found = false;

    //
    // Get atoms
    //
    Atom[] atoms = source.getAtoms();

    //
    // look for MTH/PN
    //
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].getSource().getSourceAbbreviation().equals("MTH") &&
          atoms[i].getTermgroup().getTermType().equals("PN") &&
          atoms[i].isReleasable()) {
        found = true;
        break;
      }
    }

    //
    // Is there a MTH/PN
    //
    if (!found) {
      return false;
    }

    //
    // look for ambiguous atom
    //
    found = false;
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].isAmbiguous() &&
          ! (atoms[i].getSource().getSourceAbbreviation().equals("MTH") &&
             atoms[i].getTermgroup().getTermType().equals("PN")) &&
          atoms[i].isReleasable()) {
        found = true;
        break;
      }
    }

    //
    // Is there an MTH/PN AND a releasable atom?
    //
    if (!found) {
      return true;
    }

    return false;
  }

}
