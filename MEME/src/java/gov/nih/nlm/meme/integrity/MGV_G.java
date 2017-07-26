/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_G
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractMergeMoveInhibitor
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;

/**
 * Validates merges between two {@link Concept}s
 * where one contains a previous version <code>MSH/MH</code>
 * {@link Atom} and the other contains a releasable, latest
 * version <code>MSH</code> {@link Atom} and their {@link Code}s are different.
 *
 * @author MEME Group
 */

public class MGV_G extends AbstractMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_G} check.
   */
  public MGV_G() {
    super();
    setName("MGV_G");
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
    // Find case where source and target both have current or previous version MSH
    // MH atoms with different codes.  One should have current and the other previous.
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].getSource().getStrippedSourceAbbreviation().equals(
          "MSH") &&
          source_atoms[i].getTermgroup().getTermType().equals("MH")) {
        //source_atoms[i].getSource().isPrevious() ) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].getSource().getStrippedSourceAbbreviation().
              equals("MSH") &&
              target_atoms[j].getTermgroup().getTermType().equals("MH") &&
              ( (target_atoms[j].getSource().isCurrent() &&
                 source_atoms[i].getSource().isPrevious() &&
                 target_atoms[j].isReleasable()) ||
               (source_atoms[i].getSource().isCurrent() &&
                target_atoms[j].getSource().isPrevious() &&
                source_atoms[i].isReleasable())) &&
              !target_atoms[j].getCode().equals(source_atoms[i].getCode())) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
