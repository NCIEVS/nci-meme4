/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_A4
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
 * Validates merges between two {@link Concept}s that
 * were released previously with different CUIs.
 *
 * @author MEME Group
 */
public class MGV_A4 extends AbstractMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_A4} check.
   */
  public MGV_A4() {
    super();
    setName("MGV_A4");
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
    // Find releasable atom from source concept
    // having different last release cui from releasable
    // target concept atom.
    //
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable() &&
          source_atoms[i].getLastReleaseCUI() != null) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              target_atoms[j].getLastReleaseCUI() != null &&
              !target_atoms[j].getLastReleaseCUI().equals(source_atoms[i].
              getLastReleaseCUI())) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
