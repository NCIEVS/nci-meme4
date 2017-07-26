/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_NCIPN
 *
 *****************************************************************************/
 
package gov.nih.nlm.meme.integrity;
 
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.BySourceRestrictor;
import gov.nih.nlm.meme.common.Concept;
 
/**
 * Validates merges between {@link Concept}s where both contain
 * releasable <code>MTH/NCIPN</code> {@link Atom}s.
 *
 * @author MEME Group
 */
public class MGV_NCIPN extends AbstractMergeMoveInhibitor {
 
  //
  // Constructors
  //
 
  /**
   * Instantiates an {@link MGV_NCIPN} check.
   */
  public MGV_NCIPN() {
    super();
    setName("MGV_NCIPN");
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
    // Get NCIMTH atoms from target
    //
    
            Atom[] target_atoms = (Atom[])
                        getRestrictedAtoms(new BySourceRestrictor("NCIMTH"), target.getAtoms());
 
            Atom[] l_source_atoms = (Atom[])
                        getRestrictedAtoms(new BySourceRestrictor("NCIMTH"), source_atoms);
    //
    // Find releasable MTH/NCIPNs in both source and target concepts
    //
    for (int i = 0; i < l_source_atoms.length; i++) {
      if (l_source_atoms[i].getTermgroup().getTermType().equals("PN") &&
          l_source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].getTermgroup().getTermType().equals("PN") &&
              target_atoms[j].isReleasable()) {
            return true;
          }
        }
      }
    }
    return false;
  }
 
}