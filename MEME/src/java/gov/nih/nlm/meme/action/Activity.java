/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  Activity
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.common.Identifier;

/**
 * This class generically represents any kind of activity done in the
 * MEME system.  Typically this is associated with a
 * {@link MolecularTransaction} and/or a {@link WorkLog} object.
 *
 * This class is associated with <code>activity_log</code> in the
 * <i>MID</i>.  The identifier is the <code>row_sequence</code>
 * the parent is the {@link WorkLog} associated with <code>work_id</code>
 * The <code>activity</code> and <code>detail</code> fields are
 * represeted as long and short descriptions.  <code>timestamp</code>
 * and </code>authority</code> are handled by the superclass.
 * and there is one sub action, if the <code>transaction_id</code> is set.
 *
 * @author MEME Group
 */

public class Activity
    extends LoggedAction.Default {

  //
  // Fields
  //

  // associated with activity_log.activity
  private String short_description = null;

  // associated with activity_log.detail
  private String long_description = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link Activity} with the specified row sequence.
   * @param row_sequence An <code>int</code> representing the
   *                     ordering of this logged action within its parent.
   */
  public Activity(int row_sequence) {
    this();
    setIdentifier(new Identifier.Default(row_sequence));
  }

  /**
   * Instantiates an empty {@link Activity}.
   */
  public Activity() {
    super();
  }

  //
  // Accessor Methods
  //

  /**
   * Returns the short description.
   * @return short decription
   */
  public String getShortDescription() {
    return short_description;
  }

  /**
   * Sets the short description.
   * @param dsc short description
   */
  public void setShortDescription(String dsc) {
    short_description = dsc;
  }

  /**
   * Returns the long description.
   * @return long description
   */
  public String getDescription() {
    return long_description;
  }

  /**
   * Sets the long description.
   * @param dsc long description.
   */
  public void setDescription(String dsc) {
    long_description = dsc;
  }

}
