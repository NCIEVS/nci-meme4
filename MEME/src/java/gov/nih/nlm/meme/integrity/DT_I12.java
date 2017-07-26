/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I12
 *
 * 04/07/2006 RBE (1-AV8WP): Removed self-qa test. Test for this check is
 * 							 implemented in gov.nih.nlm.meme.qa.ic package.
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import java.util.HashSet;

/**
 * Validates those {@link Concept}s that contain a
 * releasable <code>NON_HUMAN</code> attribute without valid semantic type (as defined by
 * <code>nhsty</code>).
 *
 * @author MEME Group
 */

public class DT_I12 extends AbstractDataConstraint {

  //
  // Fields
  //

  private static HashSet member = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I12} check.
   */
  public DT_I12() {
    super();
    setName("DT_I12");
    if (member == null) {
      cache_member();
    }
  }

  //
  // Methods
  //

  /**
   * Validates the specified {@link Concept}.
   * @param source the source {@link Concept}
   * @return <code>true</code> if there is a violation, <code>false</code> otherwise
   */
  public boolean validate(Concept source) {

    //
    // Is it a non human concept?
    //
    if (!source.isNonHuman()) {
      return false;
    }

    //
    // Check for valid STY
    //
    boolean violation = true;
    ConceptSemanticType[] stys = source.getSemanticTypes();
    for (int i = 0; i < stys.length; i++) {
      if (member.contains(stys[i].getValue()) && stys[i].isReleasable()) {
        violation = false;
        break;
      }
    }
    return violation;
  }

  /**
   * Cache valid NH semantic types.
   */
  private void cache_member() {
    member = new HashSet();
    member.add("Anatomical Structure");
    member.add("Embryonic Structure");
    member.add("Anatomical Abnormality");
    member.add("Congenital Abnormality");
    member.add("Acquired Abnormality");
    member.add("Fully Formed Anatomical Structure");
    member.add("Body Part, Organ, or Organ Component");
    member.add("Tissue");
    member.add("Cell");
    member.add("Cell Component");
    member.add("Gene or Genome");
    member.add("Body Substance");
    member.add("Behavior");
    member.add("Social Behavior");
    member.add("Individual Behavior");
    member.add("Natural Phenomenon or Process");
    member.add("Biologic Function");
    member.add("Physiologic Function");
    member.add("Organism Function");
    member.add("Mental Process");
    member.add("Organ or Tissue Function");
    member.add("Cell Function");
    member.add("Molecular Function");
    member.add("Genetic Function");
    member.add("Pathologic Function");
    member.add("Disease or Syndrome");
    member.add("Mental or Behavioral Dysfunction");
    member.add("Neoplastic Process");
    member.add("Cell or Molecular Dysfunction");
    member.add("Experimental Model of Disease");
  }

}
