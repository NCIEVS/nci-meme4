/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I10
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;

/**
 * Validates those {@link Concept}s that contain a
 * current version <code>MSH/MH</code> where the same-code
 * previous year <code>MSH/MH</code> is in a different {@link Concept}.
 *
 * @author MEME Group
 */

public class DT_I10 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I10} check.
   */
  public DT_I10() {
    super();
    setName("DT_I10");
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
    // Verify that the concept contains a current version MSH main heading
    //
    if (!source.isCurrentMeSHMainHeading()) {
      return false;
    }

    //
    // Set initial variables
    //
    boolean violation = true; // Until proven false;
    Atom[] atoms = source.getAtoms();
    Atom mh = null;

    //
    // Find current MH atom
    //
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].getSource().getStrippedSourceAbbreviation().equals("MSH") &&
          atoms[i].getTermgroup().getTermType().equals("MH") &&
          atoms[i].getSource().isCurrent()) {
        mh = atoms[i];
        break;
      }
    }

    //
    // assert, mh != null
    //

    //
    // Find previous year MH atom
    //
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].getSource().getStrippedSourceAbbreviation().equals("MSH") &&
          atoms[i].getTermgroup().getTermType().equals("MH") &&
          atoms[i].getSource().isPrevious() &&
          atoms[i].getSUI().equals(mh.getSUI()) &&
          atoms[i].getCode().equals(mh.getCode())) {
        violation = false;
        break;
      }
    }

    return violation;
  }

}
