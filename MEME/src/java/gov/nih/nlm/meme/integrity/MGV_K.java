/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_K
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractBinaryDataMergeMoveInhibitor.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;

import java.util.HashSet;

/**
 * Validates merges between merging two {@link Concept}s in such
 * a way to produce an {@link MGV_J} violation, unless the merge will not increase
 * the distinct {@link Code} count in the target {@link Concept}.  In other words, if all of
 * the {@link Code}s from the source {@link Concept}'s {@link Atom}s appear already in the target
 * {@link Concept}, there is not violation.
 *
 * @author MEME Group
 */
public class MGV_K extends AbstractBinaryDataMergeMoveInhibitor {

  //
  // Fields
  //
  private static HashSet sources_set = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_K} check.
   */
  public MGV_K() {
    super();
    setName("MGV_K");
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
	// Acquire check data
	//
	BinaryCheckData[] data = getCheckData();

	//
	// Get target atoms
	//
	Atom[] target_atoms = target.getAtoms();

    // Create a set of SAB/CODE values from
    // the releasable target atoms.  Because we are
    // often operating across sources, we need to
    // use the stripped source abbreviation
    HashSet code_set = new HashSet();
    for (int i = 0; i < target_atoms.length; i++) {
      if (target_atoms[i].isReleasable()) {
        code_set.add(target_atoms[i].getSource().getStrippedSourceAbbreviation() +
                     target_atoms[i].getCode());
      }
    }

    // Create a set of SAB values to which this integrity check applies
    if (sources_set == null) {
      sources_set = new HashSet(data.length);
      for (int i = 0; i < data.length; i++) {
        sources_set.add(data[i].getValue1() + data[i].getValue2());
        sources_set.add(data[i].getValue2() + data[i].getValue1());
      }
    }

    for (int i = 0; i < source_atoms.length; i++) {
      if (source_atoms[i].isReleasable()) {
        for (int j = 0; j < target_atoms.length; j++) {
          if (target_atoms[j].isReleasable() &&
              (sources_set.contains(source_atoms[i].getSource().
                                    getSourceAbbreviation() +
                                    target_atoms[j].getSource().
                                    getSourceAbbreviation()) ||
               sources_set.contains(target_atoms[j].getSource().
                                    getSourceAbbreviation() +
                                    source_atoms[i].getSource().
                                    getSourceAbbreviation())) &&
              !target_atoms[j].getCode().equals(source_atoms[i].getCode()) &&
              !code_set.contains(source_atoms[i].getSource().
                                 getStrippedSourceAbbreviation() +
                                 source_atoms[i].getCode())) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
