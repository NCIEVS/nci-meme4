/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  WorkLog
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents any kind of work done in the MEME system.
 * Typically this is associated with a {@link Activity}
 *
 * The class is associated with <code>meme_work</code>
 * table in the <i>MID</i>.
 *
 * @author MEME Group
 * @deprecated Use gov.nih.nlm.meme.action.WorkLog instead
 */

public class WorkLog extends gov.nih.nlm.meme.action.WorkLog {

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with a
   * work_id.
   * @param work_id An <code>int</code> work id
   */
  public WorkLog(int work_id) {
    super(work_id);
  }

  /**
   * No-argument constructor.
   */
  public WorkLog() {
    super();
  }

}
