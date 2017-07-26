/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  DeveloperException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception that was caused
 * by a developer mistake.  Any kind of exception caused by code in a compiled
 * class that is not the result of a configuration or administration error or
 * by user input can be considered a developer exception.  Typically, these
 * errors will be fatal and will require the stack trace to be dumped so that
 * the location of the error can be easily determined.  As much information as
 * possible should be provided in the details so that the problem can be easily
 * diagnosed.
 *
 * @author MEME Group
 */
public class DeveloperException extends MEMEException {

  /**
   * Instantiates a {@link DeveloperException} with the specified message and a null source.
   * Makes use of {@link #DeveloperException(String, Object)}.
   * @param message the error message
   */
  public DeveloperException(String message) {
    this(message, null);
  }

  /**
   * Instantiates a {@link DeveloperException} with the specified message and specified
   * source.  It inherits the super class and additionally sets the fatal,
   * print stack trace and inform user flags to <code>true</code> by default.
   * Makes use of {@link MEMEException#MEMEException(String, Object)}.
   * @param message the error message
   * @param source the {@link Object} that caused this exception
   */
  public DeveloperException(String message, Object source) {
    super(message, source);
    setFatal(true);
    setPrintStackTrace(true);
    setInformUser(true);
    setInformAdministrator(true);
    setAdministrator(MEME_ADMIN);

  }

}
