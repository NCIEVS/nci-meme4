/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I8BXR
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
 * Validates those concept that contain MSH
 * "entry term" with Q# code matching a <code>MSH/TQ</code>
 * in a different {@link gov.nih.nlm.meme.common.Concept} with an approved, releasable
 * XR {@link gov.nih.nlm.meme.common.Relationship} to
 * that {@link gov.nih.nlm.meme.common.Concept} which overrides any
 * other valid {@link gov.nih.nlm.meme.common.Relationship}s.
 *
 * @author MEME Group
 */

public class DT_I8BXR extends DT_I8 {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I8BXR} check.
   */
  public DT_I8BXR() {
    super();
    setName("DT_I8BXR");
    setTermType("TQ");
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
        "TQ", "Q"};
    DT_I8XR.main(params);
  }
}
