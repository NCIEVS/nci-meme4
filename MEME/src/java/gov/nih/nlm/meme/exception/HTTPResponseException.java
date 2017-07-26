/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  HTTPResponseException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by a HTTP response
 * with a status code other than 200.  This means, either the request was badly
 * formatted, the server was unavailable or there was a server error.
 *
 * @author MEME Group
 */
public class HTTPResponseException extends ProtocolException {

  /**
   * Instantiates an {@link HTTPResponseException} with the specified message.
       * It inherits print stack trace flag from the super class; and sets the fatal
   * and inform user flags to <code>false</code>.
   * Makes use of {@link ProtocolException#ProtocolException(String)}.
   * @param message the error message
   * @param code the <code>int</code> HTTP error code
   */
  public HTTPResponseException(String message, int code) {
    super(message);
    setFatal(false);
    setDetail("code", String.valueOf(code));
  }

}
