/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_B2
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractBinaryDataMergeMoveInhibitor.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Source;

import java.util.HashSet;

/**
 * Validates merges between two {@link Concept}s where
 * both contain releasable {@link Atom}s from the same
 * {@link Source} and the {@link Source} is in <code>ic_pair</code>.
 *
 * @author MEME Group
 */
public class MGV_B2 extends AbstractBinaryDataMergeMoveInhibitor {

  //
  // Fields
  //
  private static HashSet sources_set = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_B2} check.
   */
  public MGV_B2() {
    super();
    setName("MGV_B2");
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
    // Get binary check data
    //
    BinaryCheckData[] data = getCheckData();

    //
    // Get target atoms
    //
    Atom[] target_atoms = target.getAtoms();
    
    //
    // Get source pairs
    //
    if (sources_set == null) {
      sources_set = new HashSet(data.length);
      for (int i = 0; i < data.length; i++) {
        sources_set.add(data[i].getValue1() + data[i].getValue2());
        sources_set.add(data[i].getValue2() + data[i].getValue1());
      }
    }

    //
    // Find cases of merges where source and target atoms
    // have sources in the pairs from above
    //
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
                                    getSourceAbbreviation()))) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
