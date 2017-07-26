/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  BadValueException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by a variable in
 * some class that has an invalid or badly formatted value.
 *
 * @author MEME Group
 */
public class BadValueException extends DeveloperException {

  /**
   * Instantiates an {@link BadValueException} with the specified message and a null source.
   * Makes use of {@link #BadValueException(String, Object)}.
   * @param message the error message
   */
  public BadValueException(String message) {
    this(message, null);
  }

  /**
   * Instantiates an {@link BadValueException} with the specified message and source.
   * It inherits fatal and print stack trace flags from the super class; and
   * sets the inform user flag to <code>false</code>.
       * Makes use of {@link DeveloperException#DeveloperException(String, Object)}.
   * @param message the error message
   * @param source the {@link Object} that caused this exception
   */
  public BadValueException(String message, Object source) {
    super(message, source);
    setInformUser(false);
  }

}
