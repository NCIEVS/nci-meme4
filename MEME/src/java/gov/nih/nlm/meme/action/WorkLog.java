/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  WorkLog
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Identifier;

/**
 * Generically represents any kind of work done in the MEME system.
 * Typically this is associated with a {@link Activity}
 *
 * The class is associated with <code>meme_work</code>
 * table in the <i>MID</i>.
 *
 * @author MEME Group
 */

public class WorkLog
    extends LoggedAction.Default {

  //
  // Fields
  //

  // Associated with meme_work.type
  protected String type = null;

  // Associated with meme_work.description
  protected String description = null;

  //
  // Constructors
  //

  /**
   * This constructor initializes the action with a
   * work_id.
   * @param work_id An <code>int</code> work id
   */
  public WorkLog(int work_id) {
    this();
    setIdentifier(new Identifier.Default(work_id));
  }

  /**
   * No-argument constructor.
   */
  public WorkLog() {
    super();
  }

  /**
   * This class has no parent
   * @param action An object {@link LoggedAction}.
   */
  public void setParent(LoggedAction action) {
    throw new IllegalArgumentException(
        "A WorkLog has no parent");
  }

  /**
   * Returns the type.
   * @return the type.
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type.
   * @param type the value of type to be set.
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Returns the description.
   * @return the description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   * @param dsc the value of description to be set.
   */
  public void setDescription(String dsc) {
    description = dsc;
  }

}
