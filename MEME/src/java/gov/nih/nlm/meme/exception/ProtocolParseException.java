/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  ProtocolParseException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by a badly formatted
 * network protocol request.  For example, an HTTP request that used an illegal
 * request method would throw this exception.
 *
 * @author MEME Group
 */

public class ProtocolParseException extends ProtocolException {

  /**
   * Instantiates a {@link ProtocolParseException} with the specified message.
       * It inherits print stack trace flag from the super class; and sets the fatal
   * and inform user flags to <code>false</code>.
   * @param message the error message
   */
  public ProtocolParseException(String message) {
    super(message);
    setInformUser(false);
    setInformAdministrator(false);
  }

}
