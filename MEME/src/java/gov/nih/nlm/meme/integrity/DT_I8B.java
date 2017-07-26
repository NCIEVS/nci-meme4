/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.integrity
 * Object:  DT_I8B
 *
 *****************************************************************************/

package gov.nih.nlm.meme.integrity;

/**
 * Validates those {@link gov.nih.nlm.meme.common.Concept} that contain MSH
 * "entry term" with Q# code matching a <code>MSH/TQ</code>
 * in a different {@link gov.nih.nlm.meme.common.Concept} with NO approved, releasable,
 * RT, NT, BT, or non MTH asserted LK {@link gov.nih.nlm.meme.common.Relationship} to
 * that {@link gov.nih.nlm.meme.common.Concept}.
 *
 * @author MEME Group
 */

public class DT_I8B extends DT_I8 {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link DT_I8B} check.
   */
  public DT_I8B() {
    super();
    setName("DT_I8B");
    setTermType("TQ");
  }

  //
  // Methods
  //

  /**
   * The main method performs a self-QA test
   * @param args An array of argument.
   */
  public static void main(String[] args) {
    String[] params = {
        "TQ", "Q"};
    DT_I8.main(params);
  }
}
