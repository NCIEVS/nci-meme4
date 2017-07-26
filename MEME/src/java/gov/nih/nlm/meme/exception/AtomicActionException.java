/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  AtomicActionException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

import gov.nih.nlm.meme.action.AtomicAction;

/**
 * Generically represents an exception caused by an attempt
 * to execute an atomic action.  This class is particularly useful
 * because it encodes all of the important details of an atomic
 * action making it easier to track down errors.
 *
 * @author MEME Group
 */
public class AtomicActionException extends ActionException {

  /**
   * Instantiates an {@link AtomicActionException} with the specified message, a null source
   * and a null exception.
       * Makes use of {@link #AtomicActionException(String, AtomicAction, Exception)}.
   * @param message the error message
   * @param action the {@link AtomicAction} that caused the exception
   */
  public AtomicActionException(String message, AtomicAction action) {
    this(message, action, null);
  }

  /**
   * Instantiates an {@link AtomicActionException} with the specified message, source and exception.
   * It inherits fatal and print stack trace flags from the super class; sets
       * the inform user flag to <code>false</code>; and sets the enclosed exception
   * value.
       * Makes use of {@link DeveloperException#DeveloperException(String, Object)}.
   * @param message the error message
   * @param action the {@link AtomicAction} that caused the exception
   * @param exception the {@link Exception} that triggered this one
   */
  public AtomicActionException(String message, AtomicAction action,
                               Exception exception) {
    super(message, action, exception);
    setPrintStackTrace(true);
    setInformUser(false);
    setDetail("table_name", action.getAffectedTable());
    setDetail("row_id", new Integer(action.getRowIdentifier().intValue()));
    setDetail("field_name", action.getField());
    setDetail("old_value", action.getOldValue());
    setDetail("new_value", action.getNewValue());
    setDetail("action_status", new Character(action.getStatus()));
  }

}
