/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_MM1
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

import gov.nih.nlm.meme.common.Concept;

/**
 * Validates those {@link Concept}s that contain a releasable
 * ambiguous string but at least once concept in this ambiguous cluster is
 * lacking an approved (or unreviewed), releasable
 * {@link gov.nih.nlm.meme.common.Relationship} to one of the
 * other {@link Concept}s in the cluster.
 *
 * @author MEME Group
 */

public class DT_MM1 extends AbstractDataConstraint {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_MM1} check.
   */
  public DT_MM1() {
    super();
    setName("DT_MM1");
  }

  //
  // Methods
  //

  /**
   * Not implemented.  This is merely a place-holder to be backwards-compatable
   * with MEME3.
   * @param source the source {@link Concept}
   * @return <code>true</code> if there is a violation, <code>false</code> otherwise
   */
  public boolean validate(Concept source) {
    // unimplemented
    return false;
  }

}
