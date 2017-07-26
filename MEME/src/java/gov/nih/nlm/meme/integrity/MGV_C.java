/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_C
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractMergeMoveInhibitor
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;

/**
 * Validates merges between two {@link Concept}s where
 * both contain releasable current version <code>MSH</code> {@link Atom}s.
 *
 * @author MEME Group
 */
public class MGV_C extends AbstractMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates the {@link MGV_C} check.
   */
  public MGV_C() {
    super();
    setName("MGV_C");
  }

  //
  // Methods
  //

  /**
   * Validates the pair of {@link Concept}s.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @param source_atoms the {@link Atom}s being moved
   * @return <code>true</code> if constraint violated, <code>false</code>
   * otherwise
   */
  public boolean validate(Concept source, Concept target, Atom[] source_atoms) {

	//
    // Obtain target atoms
    //
    Atom[] target_atoms = target.getAtoms();

    //
    // Look for cases where both source and target contain
    // releasable current-version MSH atoms.
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable()) {
            if (source_atoms[i].getSource().getStrippedSourceAbbreviation().
                equals("MSH") &&
                source_atoms[i].getSource().isCurrent() &&
                target_atoms[j].getSource().getStrippedSourceAbbreviation().
                equals("MSH") &&
                target_atoms[j].getSource().isCurrent()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

}
