/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_SDUI
 *
 * 06/07/2006 RBE (1-BEYL1): File created
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.BySourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;

/**
 * Validates merges between two {@link Concept}s that
 * both contain releasable {@link Atom}s from the same {@link Source} but different
 * source concept descriptor and that {@link Source} is listed in <code>ic_single</code>.
 *
 * @author MEME Group
 */
public class MGV_SDUI extends AbstractUnaryDataMergeMoveInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_SDUI} check.
   */
  public MGV_SDUI() {
    super();
    setName("MGV_SDUI");
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
    // Get sources list
    //
    String[] sources = getCheckDataValues();
    if (sources == null) {
      return false;
    }

    //
    // Get target atoms with source values in the list
    //
    Atom[] target_atoms = (Atom[])
    getRestrictedAtoms(new BySourceRestrictor(sources), target.getAtoms());

    Atom[] l_source_atoms = (Atom[])
    getRestrictedAtoms(new BySourceRestrictor(sources), source_atoms);

    //
    // Find cases of merges where the sources are the same but SDUI are
    // different.
    //
    for (int i = 0; i < l_source_atoms.length; i++) {
      if (l_source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              target_atoms[j].getSource().equals(l_source_atoms[i].getSource()) &&
              target_atoms[j].getSourceDescriptorIdentifier() != null &&
              !target_atoms[j].getSourceDescriptorIdentifier().equals(l_source_atoms[i].getSourceDescriptorIdentifier())) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
