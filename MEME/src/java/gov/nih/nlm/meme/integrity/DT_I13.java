/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I13
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
     * current version <code>MSH/PM</code> (permuted term) without a current version
 * same-code <code>MSH/EN</code>, <code>EP</code>, or <code>MH</code>.
 *
 * @author MEME Group
 */

public class DT_I13 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I13} check.
   */
  public DT_I13() {
    super();
    setName("DT_I13");
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
    Atom[] atoms = source.getAtoms();
    Atom pm = null;

    // Find the PM
    for (int i = 0; i < atoms.length; i++) {
      if (atoms[i].getSource().isCurrent() &&
          atoms[i].getTermgroup().getTermType().equals("PM") &&
          atoms[i].getCode().toString().startsWith("D") &&
          atoms[i].getSource().getStrippedSourceAbbreviation().equals("MSH")) {
        pm = atoms[i];

        boolean found = false;
        // Find EP, EN, MH with matching code
        for (int j = 0; j < atoms.length; j++) {
          if (atoms[j].getSource().isCurrent() &&
              (atoms[j].getTermgroup().getTermType().equals("EP") ||
               atoms[j].getTermgroup().getTermType().equals("EN") ||
               atoms[j].getTermgroup().getTermType().equals("MH")) &&
              atoms[j].getCode().equals(pm.getCode()) &&
              atoms[j].getSource().getStrippedSourceAbbreviation().equals("MSH")) {
            found = true;
            break;
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
