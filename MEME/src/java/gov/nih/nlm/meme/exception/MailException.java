/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  MailException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by a failed attempt
 * to send email.  Typically, these exceptions are only thrown {@link gov.nih.nlm.meme.MEMEMail}.
 *
 * @author MEME Group
 */
public class MailException extends DeveloperException {

  /**
       * Instantiates a {@link MailException} with the specified message, a null source
   * and a null exception.
   * Makes use of {@link #MailException(String, Object, Exception)}.
   * @param message the error message
   */
  public MailException(String message) {
    this(message, null, null);
  }

  /**
   * Instantiates a {@link MailException} with the specified message, source and exception.
       * It inherits print stack trace flag from the super class; sets the fatal and
   * inform user flags to <code>false</code>; and sets the enclosed exception value.
       * Makes use of {@link DeveloperException#DeveloperException(String, Object)}.
   * @param message the error message
   * @param source the {@link Object} that caused this exception
   * @param exception the {@link Exception} that caused this one
   */
  public MailException(String message, Object source, Exception exception) {
    super(message, source);
    setFatal(false);
    setInformUser(false);
    setEnclosedException(exception);
  }

}
