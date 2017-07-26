/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_PN4
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
 * a releasable <code>MTH/PN</code> {@link Atom}
 * that has a string matching a releasable <code>MTH/PN</code>
 * {@link Atom} in a different {@link Concept}.
 *
 * @author MEME Group
 */

public class DT_PN4 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_PN4} check.
   */
  public DT_PN4() {
    super();
    setName("DT_PN4");
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
    // Look for ambiguous MTH/PN
    //
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].isAmbiguous() && atoms[i].isReleasable() &&
          atoms[i].getTermgroup().getTermType().equals("PN")) {
        return true;
      }
    }
    return false;
  }

}
