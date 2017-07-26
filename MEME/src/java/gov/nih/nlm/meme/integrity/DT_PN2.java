/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_PN2
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
 * multiple releasable <code>MTH/PN</code> {@link Atom}s.
 *
 * @author MEME Group
 */

public class DT_PN2 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_PN2} check.
   */
  public DT_PN2() {
    super();
    setName("DT_PN2");
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
    Atom[] atoms = source.getAtoms();

    //
    // Count releasable MTH/PN atoms
    //
    int l_ctr = 0;
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].getSource().getSourceAbbreviation().equals("MTH") &&
          atoms[i].getTermgroup().getTermType().equals("PN") &&
          atoms[i].isReleasable()) {
        l_ctr++;
      }
    }

    //
    // Are there more than one?
    //
    if (l_ctr > 1) {
      return true;
    }

    return false;
  }

}
