/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_H2
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractMergeMoveInhibitor
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.ByStrippedSourceRestrictor;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;

/**
 * Validates merges between two {@link Concept}s where
 * both contain releasable current version <code>MSH</code> {@link Atom}s
 * with different {@link Code}s,
 * specifically (D-C, C-C).
 *
 * @author MEME Group
 */
public class MGV_H2 extends AbstractMergeMoveInhibitor implements MoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_H2} check.
   */
  public MGV_H2() {
    super();
    setName("MGV_H2");
  }

  //
  // Methods
  //

  /**
   * Validates the pair of {@link Concept}s where the specified {@link Atom}s are
   * moving from the source to the target concept.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @param source_atoms {@link Atom}s from the source concept to be moved to target concept
   * @return <code>true</code> if constraint violated, <code>false</code>
   * otherwise
   */
  public boolean validate(Concept source, Concept target, Atom[] source_atoms) {

    //
    // Get MSH atoms from target concept.
    //
    Atom[] target_atoms = (Atom[])
    	getRestrictedAtoms(new ByStrippedSourceRestrictor("MSH"), target.getAtoms());

    Atom[] l_source_atoms = (Atom[])
        getRestrictedAtoms(new ByStrippedSourceRestrictor("MSH"), source_atoms);

    //
    // Find cases where releasable current version MSH atoms with
    // different codes (in the specific combinations) are being merged.
    //
    for (int i = 0; i < l_source_atoms.length; i++) {
      if (l_source_atoms[i].isReleasable() &&
          l_source_atoms[i].getSource().getStrippedSourceAbbreviation().equals(
          "MSH") &&
          l_source_atoms[i].getSource().isCurrent() &&
          (l_source_atoms[i].getCode().toString().startsWith("D") ||
           l_source_atoms[i].getCode().toString().startsWith("C"))) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              target_atoms[j].getSource().isCurrent() &&
              ( (l_source_atoms[i].getCode().toString().startsWith("C") &&
                 target_atoms[j].getCode().toString().startsWith("C")) ||
               (l_source_atoms[i].getCode().toString().startsWith("C") &&
                target_atoms[j].getCode().toString().startsWith("D")) ||
               (l_source_atoms[i].getCode().toString().startsWith("D") &&
                target_atoms[j].getCode().toString().startsWith("C"))) &&
              !target_atoms[j].getCode().equals(l_source_atoms[i].getCode())) {
            return true;
          }
        }
      }
    }

    return false;
  }

}
