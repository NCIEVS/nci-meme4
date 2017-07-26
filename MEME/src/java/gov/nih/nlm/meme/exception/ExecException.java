/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  ExecException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by the attempt to
 * execute a command.
 *
 * @author MEME Group
 */
public class ExecException extends AdministratorException {

  /**
   * Instantiates an {@link ExecException} with the specified message and a null exception.
   * Makes use of {@link #ExecException(String, Exception)}.
   * @param message the error message
   */
  public ExecException(String message) {
    this(message, null);
  }

  /**
   * Instantiates an {@link ExecException} with the specified message and exception.
   * It inherits fatal from the super class; sets the print stack trace and
   * inform user flags to <code>false</code>; and sets the enclosed exception
   * and administrator value.
       * Makes use of {@link AdministratorException#AdministratorException(String)}.
   * @param message the error message
   * @param exception the {@link Exception} that caused this one
   */
  public ExecException(String message, Exception exception) {
    super(message);
    setPrintStackTrace(false);
    setInformUser(false);
    setEnclosedException(exception);
    setAdministrator(SYSTEM_ADMIN);
  }

}
