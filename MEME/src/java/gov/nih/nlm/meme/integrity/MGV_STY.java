/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  MGV_STY
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 							 Extends AbstractBinaryDataMergeMoveInhibitor.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.Source;

import java.util.HashSet;

/**
 * Validates merges between two {@link Concept}s where
 * both contain releasable semantic types from the same
 * {@link Source} and the {@link Source} is in <code>ic_single</code>.
 *
 * @author MEME Group
 */
public class MGV_STY extends AbstractBinaryDataMergeInhibitor {

  //
  // Fields
  //
  private static HashSet sources_set = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link MGV_STY} check.
   */
  public MGV_STY() {
    super();
    setName("MGV_STY");
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
    // Get binary check data
    //
    BinaryCheckData[] data = getCheckData();

    //
    // Get source and target STYs
    //
    ConceptSemanticType[] source_stys = source.getSemanticTypes();
    ConceptSemanticType[] target_stys = target.getSemanticTypes();

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
    // Find cases of merges where source and target stys
    // have sources in the pairs from above
    //
    for (int i = 0; i < source_stys.length; i++) {
      for (int j = 0; j < target_stys.length; j++) {
        if (sources_set.contains(source_stys[i].getValue() +
                                 target_stys[j].getValue()) ||
            sources_set.contains(target_stys[j].getValue() +
                                 source_stys[i].getValue())) {
          return true;
        }
      }
    }
    return false;
  }

}
