/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  ConcurrencyException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception that was caused
 * by two or more things happening at the same time.  For example, if reentrant
     * service is requested while it is still running, then this exception is thrown.
 *
 * @author MEME Group
 */
public class ConcurrencyException extends ApplicationException {

  /**
   * Instantiates a {@link ConcurrencyException} with the specified message and a null source.
   * Makes use of {@link #ConcurrencyException(String, Object)}.
   * @param message the error message
   */
  public ConcurrencyException(String message) {
    this(message, null);
  }

  /**
   * Instantiates a {@link ConcurrencyException} with the specified message and source.
   * Makes use of {@link ApplicationException#ApplicationException(String, Object)}.
   * @param message the error message
   * @param source the object that caused the exception
   */
  public ConcurrencyException(String message, Object source) {
    super(message, source);
  }

}
