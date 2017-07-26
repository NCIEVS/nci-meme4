/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_MM
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractMergeMoveInhibitor
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.BySourceRestrictor;
import gov.nih.nlm.meme.common.Concept;

/**
 * Validates merges between {@link Concept}s where both contain
 * releasable <code>MTH/MM</code> {@link Atom}s.
 *
 * @author MEME Group
 */
public class MGV_MM extends AbstractMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_MM} check.
   */
  public MGV_MM() {
    super();
    setName("MGV_MM");
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
    // Get MTH atoms from target
    //
    Atom[] target_atoms = (Atom[])
    	getRestrictedAtoms(new BySourceRestrictor("MTH"), target.getAtoms());
    Atom[] l_source_atoms = (Atom[])
        getRestrictedAtoms(new BySourceRestrictor("MTH"), source_atoms);

    //
    // Find releasable MTH/MM atoms in both source and target
    //
    for (int i = 0; i < l_source_atoms.length; i++) {
      if (l_source_atoms[i].getTermgroup().getTermType().equals("MM") &&
          l_source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[i].getTermgroup().getTermType().equals("MM") &&
              target_atoms[i].isReleasable()) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
