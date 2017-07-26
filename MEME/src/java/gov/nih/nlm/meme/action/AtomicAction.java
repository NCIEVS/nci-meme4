/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.action
 * Object:  AtomicAction
 *
 *****************************************************************************/

package gov.nih.nlm.meme.action;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.exception.BadValueException;

import java.text.ParseException;
import java.util.Date;

/**
 * This class generically represents an atomic action, which is
 * typically part of a {@link MolecularAction}.
 *
 * This class is associated with the <code>atomic_actions</code>
 * table in the <i>MID</i>.
 *
 * @author MEME Group
 */

public class AtomicAction
    extends LoggedAction.Default {

  //
  // Fields
  //

  private String table_name = null;
  private Identifier row_id = null;
  private String old_value, new_value = null;
  private String action_field = null;

  //
  // Constructors
  //

  /**
   * Instantiates an {@link AtomicAction} with the specified atomic action
   * identifier.
   * @param atomic_action_id an <code>int</code> representation of the
   *                          atomic action identifier
   */
  public AtomicAction(int atomic_action_id) {
    this();
    setIdentifier(new Identifier.Default(atomic_action_id));
  }

  /**
   * Instantiates an empty {@link AtomicAction}.
   */
  public AtomicAction() {
    super();
  }

  //
  // Additional AtomicAction Methods
  //

  /**
   * Gets the name of the table affected by the action.
   * Looking up the result of this method in <code>code_map</code>
   * where <code>type='table_name'</code>
   * will return the actual name of the table.
   * @return a one or two letter code representing the core table changed
   *         by this action
   */
  public String getAffectedTable() {
    return table_name;
  }

  /**
   * Sets the name of the affected table.
   * @param table_name a one or two letter code representing the
   *        core table changed by this action
   */
  public void setAffectedTable(String table_name) {
    this.table_name = table_name;
  }

  /**
   * Gets the core data id affected by this action.
   * @return the core data {@link Identifier}
   */
  public Identifier getRowIdentifier() {
    return row_id;
  }

  /**
   * Sets the core data id affected by this action.
   * @param row_id the core data {@link Identifier}
   */
  public void setRowIdentifier(Identifier row_id) {
    this.row_id = row_id;
  }

  /**
   * Sets the core data id affected by this action.
   * @param row_id the core data id
   */
  public void setRowIdentifier(int row_id) {
    this.row_id = new Identifier.Default(row_id);
  }

  /**
   * Returns old value of the field changed as a {@link String}.
   * @return old value of the field changed as a {@link String}
   */
  public String getOldValue() {
    return old_value;
  }

  /**
   * Returns the old value of the field changed as an <code>int</code>.
   * @return the old value of the field changed as an <code>int</code>
   */
  public int getOldValueAsInt() {
    if (old_value == null)
      return 0;
    return Integer.valueOf(old_value).intValue();
  }

  /**
   * Returns the old value of the field changed as a {@link Date}.
   * @return the old value of the field changed as a {@link Date}
   * @throws BadValueException if value cannot be parsed as a date
   */
  public Date getOldValueAsDate() throws BadValueException {
    if (old_value == null)
      return null;
    try {
      return MEMEToolkit.getDateFormat().parse(old_value);
    } catch (ParseException pe) {
      BadValueException bve = new BadValueException(
          "Badly formatted date.");
      bve.setDetail("date", old_value);
      throw bve;
    }
  }

  /**
   * Sets the old value of the field changed by this action.
   * @param old_value the old value
   */
  public void setOldValue(String old_value) {
    this.old_value = old_value;
  }

  /**
   * Sets the old value of the field changed by this action.
   * @param old_value the old value
   */
  public void setOldValue(int old_value) {
    this.old_value = String.valueOf(old_value);
  }

  /**
   * Sets the old value of the field changed by this action.
   * @param old_value the old value
   */
  public void setOldValue(Date old_value) {
    this.old_value =
        (old_value == null) ?
        "" : MEMEToolkit.getDateFormat().format(old_value).toLowerCase();
  }

  /**
   * Returns the new value of the field changed as a {@link String}.
   * @return the new value of the field changed as a {@link String}
   */
  public String getNewValue() {
    return new_value;
  }

  /**
   * Returns the new value of the field changed as an </code>int</code>.
   * @return the new value of the field changed as an <code>int</code>
   */
  public int getNewValueAsInt() {
    if (old_value == null)
      return 0;
    return Integer.valueOf(new_value).intValue();
  }

  /**
   * Returns the new value of the field changed as a {@link Date}.
   * @return the new value of the field changed as a {@link Date}
   * @throws BadValueException if error encounters while parsing.
   */
  public Date getNewValueAsDate() throws BadValueException {
    if (new_value == null)
      return null;
    try {
      return MEMEToolkit.getDateFormat().parse(new_value);
    } catch (ParseException pe) {
      BadValueException bve = new BadValueException(
          "Badly formatted date.");
      bve.setDetail("date", new_value);
      throw bve;
    }
  }

  /**
   * Sets the new value of the field changed by this action.
   * @param new_value the new value
   */
  public void setNewValue(String new_value) {
    this.new_value = new_value;
  }

  /**
   * Sets the new value of the field changed by this action.
   * @param new_value the new value
   */
  public void setNewValue(int new_value) {
    this.new_value = String.valueOf(new_value);
  }

  /**
   * Sets the new value of the field changed by this action.
   * @param new_value the new value
   */
  public void setNewValue(Date new_value) {
    this.new_value =
        (new_value == null) ?
        "" : MEMEToolkit.getDateFormat().format(new_value).toLowerCase();
  }

  /**
   * Returns the name of the field affected by this action.
   * @return the name of the field affected by this action
   */
  public String getField() {
    return action_field;
  }

  /**
   * Sets the name of the field affected by this action.
   * @param field the field name
   */
  public void setField(String field) {
    this.action_field = field;
  }

  /**
   * Returns the inverse of this action.
   * @return {@link LoggedAction}
   */
  public LoggedAction getInverseAction() {
    AtomicAction inverse_aa = new AtomicAction();
    inverse_aa.setRowIdentifier(getRowIdentifier());
    inverse_aa.setAffectedTable(getAffectedTable());
    inverse_aa.setField(getField());
    inverse_aa.setNewValue(getOldValue());
    inverse_aa.setOldValue(getNewValue());
    inverse_aa.setStatus(getStatus());
    if (getActionName().equals("I"))
      inverse_aa.setActionName("D");
    else
      inverse_aa.setActionName(getActionName());

    return inverse_aa;

  }
}
