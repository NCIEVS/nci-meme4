/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  ActionException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

import gov.nih.nlm.meme.action.LoggedAction;

/**
 * Generically represents an exception caused by a failure
 * of the {@link gov.nih.nlm.meme.sql.ActionEngine} to process an action.
 *
 * @author MEME Group
 */
public class ActionException extends DeveloperException {

  /**
   * Instantiates an empty {@link ActionException}.
   */
  public ActionException() {
    this("Action Error", null, null);
  }

  /**
   * Instantiates an {@link ActionException} with the specified message and a null source.
   * Makes use of {@link #ActionException(String, LoggedAction)}.
   * @param message the {@link String} error message
   */
  public ActionException(String message) {
    this(message, null, null);
  }

  /**
       * Instantiates an {@link ActionException} with the specified message and source.
   * It inherits fatal and print stack trace flags from the super class; and
   * sets the inform user flag to <code>false</code>.
   * Makes use of {@link #ActionException(String, LoggedAction, Exception)}.
   * @param message the error message
   * @param source the {@link LoggedAction} resulting in this error
   */
  public ActionException(String message, LoggedAction source) {
    this(message, source, null);
  }

  /**
       * Instantiates an {@link ActionException} with the specified message and source.
   * It inherits fatal and print stack trace flags from the super class; and
   * sets the inform user flag to <code>false</code>.
   * Makes use of {@link DeveloperException#DeveloperException(String)}.
   * @param message the error message
   * @param source the {@link LoggedAction} resulting in this error
   * @param exception the {@link Exception} that triggered this one
   */
  public ActionException(String message, LoggedAction source,
                         Exception exception) {
    super(message);
    setSource(source);
    setEnclosedException(exception);
  }

}
