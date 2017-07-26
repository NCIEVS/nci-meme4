/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  ExternalResourceException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception that was caused
 * by a failure external to the system.  For example, if a network connection
 * breaks because of internet problem, this exception will be thrown.
 *
 * @author MEME Group
 */
public class ExternalResourceException extends MEMEException {

  /**
   * Instantiates an {@link ExternalResourceException} with the specified message and a null source.
   * Makes use of {@link #ExternalResourceException(String, Exception)}.
   * @param message the error message
   */
  public ExternalResourceException(String message) {
    this(message, null);
  }

  /**
   * Instantiates an {@link ExternalResourceException} with the specified message and source.
   * It inherits the super class and additionally sets the fatal and inform
   * user flags to <code>true</code>; and sets enclosed exception value.
   * Makes use of {@link MEMEException#MEMEException(String, Object)}.
   * @param message the error message
   * @param exception the {@link Exception} that caused this one
   */
  public ExternalResourceException(String message, Exception exception) {
    super(message);
    setInformUser(true);
    setInformAdministrator(true);
    setAdministrator(MEME_ADMIN);
    setEnclosedException(exception);
  }

}
