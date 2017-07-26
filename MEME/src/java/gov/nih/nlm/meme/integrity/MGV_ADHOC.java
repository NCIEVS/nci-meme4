/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_ADHOC
 *
 *****************************************************************************/


package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.ByStrippedSourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;

/**
 * Validates merges between two {@link Concept}s
 * where one contains a releasable, current version <code>MDR</code> {@link Atom}
 * and the other has a chemical {@link ConceptSemanticType}.
 *
 * @author MEME Group
 */
public class MGV_ADHOC extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_ADHOC} check.
   */
  public MGV_ADHOC() {
    super();
    setName("MGV_ADHOC");
  }

  //
  // Methods
  //

  /**
   * Validates the pair of {@link Concept}s.
   * @param source the source {@link Concept}
   * @param target the target {@link Concept}
   * @return <code>true</code> if constraint violated, <code>false</code>
   * otherwise
   */
  public boolean validate(Concept source, Concept target) {

    //
    // If source has a chemical semantic type, look
    // for releasable MDR atoms in target
    //
    if (source.hasChemicalSemanticType()) {
      Atom[] atoms = target.getRestrictedAtoms(new ByStrippedSourceRestrictor(
          "MDR"));
      for (int i = 0; i < atoms.length; i++) {
        if (atoms[i].isReleasable()) {
          return true;
        }
      }
    }

    //
    // If target has a chemical semantic type, look
    // for releasable MDR atoms in source
    //
    if (target.hasChemicalSemanticType()) {
      Atom[] atoms = source.getRestrictedAtoms(new ByStrippedSourceRestrictor(
          "MDR"));
      for (int i = 0; i < atoms.length; i++) {
        if (atoms[i].isReleasable()) {
          return true;
        }
      }
    }
    return false;
  }

}
