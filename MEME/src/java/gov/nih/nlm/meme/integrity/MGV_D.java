/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_D
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 *                           Extend AbstractMergeMoveInhibitor
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.MeshEntryTerm;
import gov.nih.nlm.meme.common.Relationship;

import java.util.HashSet;

/**
 * Validates merges between two {@link Concept}s
 * that contain different releasable {@link Relationship}s to the same latest version
 * <code>MSH/MH</code>.
 *
 * @author MEME Group
 */
public class MGV_D extends AbstractMergeInhibitor {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_D} check.
   */
  public MGV_D() {
    super();
    setName("MGV_D");
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
  public boolean validate(Concept source, Concept target) {

    //
    // Collect main headings for each entry term in target concepts
    //
    HashSet mh_atoms = new HashSet();
    Atom[] source_atoms = source.getAtoms();
    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i] instanceof MeshEntryTerm) {
        Atom mh = ( (MeshEntryTerm) source_atoms[i]).getMainHeading();
        mh_atoms.add(mh);
      }
    }
    Atom[] target_atoms = target.getAtoms();
    for (int i = 0; i < target_atoms.length; i++) {
      if (target_atoms[i] instanceof MeshEntryTerm) {
        Atom mh = ( (MeshEntryTerm) target_atoms[i]).getMainHeading();
        mh_atoms.add(mh);
      }
    }

    //
    // Get all relationships
    //
    Relationship[] source_rels = source.getRelationships();
    Relationship[] target_rels = target.getRelationships();

    //
    // Find cases where the source concept has a
    // relationship to the same main heading as the target
    // concept where the relationship names are different.
    //
    for (int i = 0; i < source_rels.length; i++) {
      if (source_rels[i].isSourceAsserted() &&
          source_rels[i].isReleasable() &&
          (!source_rels[i].getName().equals("RT?") &&
           !source_rels[i].getName().equals("BT?") &&
           !source_rels[i].getName().equals("NT?") &&
           !source_rels[i].getName().equals("SY")) &&
          mh_atoms.contains(source_rels[i].getRelatedAtom())) {
        for (int j = 0; j < target_rels.length; j++) {
          if (target_rels[j].isSourceAsserted() &&
              target_rels[j].isReleasable() &&
              (!target_rels[j].getName().equals("RT?") &&
               !target_rels[j].getName().equals("BT?") &&
               !target_rels[j].getName().equals("NT?") &&
               !target_rels[j].getName().equals("SY")) &&
              mh_atoms.contains(target_rels[j].getRelatedAtom()) &&
              source_rels[i].getRelatedAtom().equals(target_rels[j].
              getRelatedAtom()) &&
              !source_rels[i].getName().equals(target_rels[j].getName())) {
            return true;
          }
        }
      }
    }

    return false;
  }

}
