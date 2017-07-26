/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_MUI
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractMergeMoveInhibitor
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.ByStrippedSourceRestrictor;
import gov.nih.nlm.meme.common.Concept;

/**
 * Validate merges between two {@link Concept}s where
 * both contain current version <code>MSH</code> {@link Atom}s with
 * different MUIs.
 *
 * @author MEME Group
 */
public class MGV_MUI extends AbstractMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates an {@link MGV_MUI} check.
   */
  public MGV_MUI() {
    super();
    setName("MGV_MUI");
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
    // Obtain MSH atoms from target
    //
	Atom[] target_atoms = (Atom[])
    	getRestrictedAtoms(new ByStrippedSourceRestrictor("MSH"), target.getAtoms());
    Atom[] l_source_atoms = (Atom[])
        getRestrictedAtoms(new ByStrippedSourceRestrictor("MSH"), source_atoms);

    //
    // Find cross-MUI merges among current-version MSH atoms
    //
    for (int i = 0; i < l_source_atoms.length; i++) {
      if (l_source_atoms[i].isReleasable() &&
          l_source_atoms[i].getSource().isCurrent()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              target_atoms[j].getSource().isCurrent() &&
              l_source_atoms[i].getSourceConceptIdentifier() !=
              target_atoms[j].getSourceConceptIdentifier()) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
