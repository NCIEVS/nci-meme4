/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Activity
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Use {@link gov.nih.nlm.meme.action.Activity} instead.
 * @author MEME Group
 * @deprecated Use {@link gov.nih.nlm.meme.action.Activity} instead
 */

public class Activity extends gov.nih.nlm.meme.action.Activity {

  /**
   * Instantiates an {@link Activity} with the specified row sequence.
   * @param row_sequence An <code>int</code> representing the
   *                     ordering of this logged action within its parent.
   */
  public Activity(int row_sequence) {
    super(row_sequence);
  }

  /**
   * Instantiates an empty {@link Activity}.
   */
  public Activity() {
    super();
  }

}
