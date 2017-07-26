/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_MM2
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.BySourceRestrictor;
import gov.nih.nlm.meme.common.Concept;

/**
 * Validates those {@link Concept}s which contain
 * multiple releasable MTH/MM {@link Atom}s with the same base string but different
 * bracket numbers (e.g. "Apple <1>" and "apple <2>".
 *
 * @author MEME Group
 */

public class DT_MM2 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_MM2} check.
   */
  public DT_MM2() {
    super();
    setName("DT_MM2");
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
    // Get MTH atoms
    //
    Atom[] atoms = source.getRestrictedAtoms(new BySourceRestrictor("MTH"));

    //
    // Find MTH/MM atoms with different bracket numbers but the same base string
    //
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].getTermgroup().getTermType().equals("MM") &&
          atoms[i].isReleasable()) {
        String name1 = atoms[i].getBaseString().toLowerCase();
        for (int j = i + 1; j < atoms.length; j++) {
          if (atoms[j].getTermgroup().getTermType().equals("MM") &&          		
              atoms[j].isReleasable()) {
            String name2 = atoms[j].getBaseString().toLowerCase();
            if (name1.equals(name2)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

}
