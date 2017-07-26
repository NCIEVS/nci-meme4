/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_B
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractUnaryMergeMoveInhibitor.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.BySourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;

/**
 * Validates merges between two {@link Concept}s that
 * both contain releasable {@link Atom}s from the same
 * {@link Source} and that {@link Source} is
 * listed in <code>ic_single</code>.
 *
 * @author MEME Group
 */
public class MGV_B extends AbstractUnaryDataMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_B} check.
   */
  public MGV_B() {
    super();
    setName("MGV_B");
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
    // Get unary check data
    //
    String[] sources = getCheckDataValues();

    if (sources == null) {
      return false;
    }

    //
    // Get target atoms from specified list of sources
    //
    Atom[] target_atoms = (Atom[])
    	getRestrictedAtoms(new BySourceRestrictor(sources), target.getAtoms());

    Atom[] l_source_atoms = (Atom[])
        getRestrictedAtoms(new BySourceRestrictor(sources), source_atoms);
    //
    // Look for cases of within-source merges involving releasable atoms.
    //
    for (int i = 0; i < l_source_atoms.length; i++) {
      if (l_source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              target_atoms[j].getSource().equals(l_source_atoms[i].getSource())) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
