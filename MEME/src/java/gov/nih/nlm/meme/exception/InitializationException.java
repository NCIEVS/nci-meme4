/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  InitializationException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception that was caused
 * by initialization process.  These errors will be fatal.  As much information
 * as possible should be provided in the details so that the problem can be
 * easily diagnosed.
 *
 * @author MEME Group
 */
public class InitializationException extends MEMEException {

  /**
   * Instantiates an {@link InitializationException} with the specified message and a null
   * exception.
   * Makes use of {@link #InitializationException(String, Exception)}.
   * @param message the error message
   */
  public InitializationException(String message) {
    this(message, null);
  }

  /**
   * Instantiates an {@link InitializationException }with the specified message and exception.
   * It inherits the super class and additionally sets the fatal flag to
   * <code>true</code> and the enclosed exception value by default.
   * Makes use of {@link MEMEException#MEMEException(String)}.
   * @param message the error message
   * @param exception the {@link Exception} that caused this one
   */
  public InitializationException(String message, Exception exception) {
    super(message);
    setFatal(true);
    setEnclosedException(exception);
  }

}
