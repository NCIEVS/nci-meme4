/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I8CXR
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
 * Validates those {@link gov.nih.nlm.meme.common.Concept}s that contain
 * MSH "entry term" with C# code matching a <code>MSH/NM</code>
 * in a different {@link gov.nih.nlm.meme.common.Concept} with an approved, releasable
 * XR {@link gov.nih.nlm.meme.common.Relationship} to
 * that {@link gov.nih.nlm.meme.common.Concept} which overrides any
 * other valid {@link gov.nih.nlm.meme.common.Relationship}s.
 *
 * @author MEME Group
 */

public class DT_I8CXR extends DT_I8 {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I8CXR} check.
   */
  public DT_I8CXR() {
    super();
    setName("DT_I8CXR");
    setTermType("NM");
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
        "NM", "C"};
    DT_I8XR.main(params);
  }
}
