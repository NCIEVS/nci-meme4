/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  ReflectionException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by the use of the
 * Java reflection API.  For example, a piece of code that dynamically tries
 * to access a method that does not exist would trigger this exception.
 *
 * @author MEME Group
 */
public class ReflectionException extends DeveloperException {

  /**
   * Instantiates a {@link ReflectionException} with the specified message, a null source
   * and a null exception.
   * Makes use of {@link #ReflectionException(String, Object, Exception)}.
   * @param message the error message
   */
  public ReflectionException(String message) {
    this(message, null, null);
  }

  /**
   * Instantiates a {@link ReflectionException} with the specified message, source and exception.
   * It inherits fatal and print stack trace flags from the super class; sets
       * the inform user flag to <code>false</code>; and sets the enclosed exception
   * value.
       * Makes use of {@link DeveloperException#DeveloperException(String, Object)}.
   * @param message the error message
   * @param source the {@link Object} that caused this exception
   * @param exception the {@link Exception} that caused this one
   */
  public ReflectionException(String message, Object source, Exception exception) {
    super(message, source);
    setInformUser(false);
    setEnclosedException(exception);
  }

}
