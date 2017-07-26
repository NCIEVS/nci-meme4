/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  UserInputException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused during a user
 * interaction.
 *
 * @author MEME Group
 */
public class UserInputException extends ApplicationException {

  /**
   * Instantiates an {@link UserInputException} with the specified message and a null source.
   * Makes use of {@link #UserInputException(String, Object)}.
   * @param message the error message.
   */
  public UserInputException(String message) {
    this(message, null);
  }

  /**
   * Instantiates an {@link UserInputException} with the specified message and source.
       * It inherits the fatal, print stack trace and inform user flags from the super
   * class.
   * Makes use of {@link ApplicationException#ApplicationException(String, Object)}.
   * @param message the error message
   * @param source the {@link Object} that caused this exception
   */
  public UserInputException(String message, Object source) {
    super(message, source);
  }

}
