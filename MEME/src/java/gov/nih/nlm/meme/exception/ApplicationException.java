/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  ApplicationException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception that was caused
 * by a user interaction.  These are cases where the user is asked to interact
 * with the application and either provides an illegal value, or closes a
 * window prematurely, or in some other way screws up the execution stream in a
 * a way that is not the result of faulty administration or buggy code.
 *
 * @author MEME Group
 */
public class ApplicationException extends MEMEException {

  /**
   * Instantiates an {@link ApplicationException} with the specified message and a null source.
   * Makes use of {@link #ApplicationException(String, Object)}.
   * @param message the error message
   */
  public ApplicationException(String message) {
    this(message, null);
  }

  /**
   * Instantiates ann {@link ApplicationException} with the specified message and source.
   * It inherits the super class and additionally sets the inform user flag to
   * <code>true</code> by default.
   * Makes use of {@link MEMEException#MEMEException(String, Object)}.
   * @param message the error message
   * @param source the {@link Object} that caused the exception
   */
  public ApplicationException(String message, Object source) {
    super(message, source);
    setInformUser(true);
  }

}
