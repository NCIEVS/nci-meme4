/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_MM3
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;

/**
 * Validates those {@link Concept}s which contain a
 * releasable <code>MTH/MM</code> {@link Atom} but no releasable ambiguous strings matching the
 * <code>MTH/MM</code>.
 *
 * @author MEME Group
 */

public class DT_MM3 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_MM3} check.
   */
  public DT_MM3() {
    super();
    setName("DT_MM3");
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
    // Look for MTH/MM atom without matching amgiguous atom
    //
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].getTermgroup().getTermType().equals("MM") &&
          atoms[i].isReleasable() &&
          atoms[i].getSource().getSourceAbbreviation().equals("MTH")) {
        String name1 = atoms[i].getBaseString().toLowerCase();
        boolean found = false;
        for (int j = i + 1; j < atoms.length; j++) {
          if (!atoms[j].getTermgroup().getTermType().equals("MM") &&
              ! (atoms[j].getTermgroup().getTermType().equals("PN") &&
                 atoms[j].getSource().getSourceAbbreviation().equals("MTH")) &&
              atoms[j].isReleasable() && atoms[j].isAmbiguous()) {
            String name2 = atoms[j].getString().toLowerCase();
            if (name1.equals(name2)) {
              found = true;
              break;
            }
          }
        }
        if (!found) {
          return true;
        }
      }
    }
    return false;
  }

}
