/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I8AXR
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
 * Validates those {@link gov.nih.nlm.meme.common.Concept} that contain MSH
 * "entry term" with D# code matching a <code>MSH/MH</code>
 * in a different {@link gov.nih.nlm.meme.common.Concept} with an approved, releasable
 * XR {@link gov.nih.nlm.meme.common.Relationship} to that {@link gov.nih.nlm.meme.common.Concept} which
 * overrides any other valid relationships.
 *
 * @author MEME Group
 */

public class DT_I8AXR extends DT_I8 {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I8AXR} check.
   */
  public DT_I8AXR() {
    super();
    setName("DT_I8AXR");
    setTermType("MH");
    setXR(true);
  }

  //
  // Methods
  //

  /**
   * Self-qa test.
   * @param args command line arguments
   */
  public static void main(String[] args) {
    String[] params = {
        "MH", "D"};
    DT_I8XR.main(params);
  }
}
