/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  ProtocolException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by an error in handling
 * of a network protocol.  For example, an HTTP response with a 400 code would
 * throw this exception.
 *
 * @author MEME Group
 */

public class ProtocolException extends DeveloperException {

  /**
   * Instantiates a {@link ProtocolException} with the specified message.
       * It inherits print stack trace flag from the super class; and sets the fatal
   * and inform user flags to <code>false</code>.
   * Makes use of {@link DeveloperException#DeveloperException(String)}.
   * @param message the error message
   */
  public ProtocolException(String message) {
    super(message);
    setInformUser(false);
  }

}
