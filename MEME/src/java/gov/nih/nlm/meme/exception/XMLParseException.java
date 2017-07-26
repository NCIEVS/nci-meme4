/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  XMLParseException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused while attempting to
 * parse an XML document.  Examples include attempting to parse an XML file
 * that does not exist or attempting to parse a badly formatted XML document.
 *
 * @author MEME Group
 */

public class XMLParseException extends DeveloperException {

  /**
   * Instantiates an {@link XMLParseException} with the specified message, a null source
   * and a null exception.
   * Makes use of {@link #XMLParseException(String, Object, Exception)}.
   * @param message the error message
   */
  public XMLParseException(String message) {
    this(message, null, null);
  }

  /**
   * Instantiates an {@link XMLParseException} with the specified message, source and exception.
   * It inherits fatal and print stack trace flags from the super class; sets
       * the inform user flag to <code>false</code>; and sets the enclosed exception
   * value.
       * Makes use of {@link DeveloperException#DeveloperException(String, Object)}.
   * @param message the error message
   * @param source the {@link Object} that caused this exception
   * @param exception the {@link Exception} that caused this one
   */
  public XMLParseException(String message, Object source, Exception exception) {
    super(message, source);
    setInformUser(false);
    setEnclosedException(exception);
  }

}
