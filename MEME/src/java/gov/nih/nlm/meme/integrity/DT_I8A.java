/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I8A
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
 * Validates those {@link gov.nih.nlm.meme.common.Concept}s that contain
 * MSH "entry term" with D# code matching a  <code>MSH/MH</code>
 * in a different {@link gov.nih.nlm.meme.common.Concept} with NO approved, releasable, RT, NT, BT,
 * or non MTH asserted LK {@link gov.nih.nlm.meme.common.Relationship} to
 * that {@link gov.nih.nlm.meme.common.Concept}.
 *
 * @author MEME Group
 */

public class DT_I8A extends DT_I8 {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I8A} check.
   */
  public DT_I8A() {
    super();
    setXR(false);
    setName("DT_I8A");
    setTermType("MH");
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
    DT_I8.main(params);
  }
}
