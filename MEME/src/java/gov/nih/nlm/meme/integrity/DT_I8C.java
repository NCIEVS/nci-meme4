/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I8C
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
 * Validates those {@link gov.nih.nlm.meme.common.Concept}s that contain MSH
 * "entry term" with C# code matching a <code>MSH/NM</code>
 * in a different {@link gov.nih.nlm.meme.common.Concept} with NO approved (or
 * unreviewed), releasable, RT, NT, BT, or non MTH asserted LK
 * {@link gov.nih.nlm.meme.common.Relationship} to
 * that {@link gov.nih.nlm.meme.common.Concept}.
 *
 * @author MEME Group
 */

public class DT_I8C extends DT_I8 {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I8C} check.
   */
  public DT_I8C() {
    super();
    setName("DT_I8C");
    setTermType("NM");
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
    DT_I8.main(params);
  }
}
