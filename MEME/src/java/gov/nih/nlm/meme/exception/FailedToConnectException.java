/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  FailedToConnectException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Represents an inability to connecdt to the server.
 *
 * @author MEME Group
 */
public class FailedToConnectException extends ExternalResourceException {

  /**
   * Instantiates an {@link FailedToConnectException} with the specified message and a null source.
   * Makes use of {@link #FailedToConnectException(String, Exception)}.
   * @param message the error message
   */
  public FailedToConnectException(String message) {
    this(message, null);
  }

  /**
   * Instantiates an {@link FailedToConnectException} with the specified message and source.
   * It inherits the super class and additionally sets the fatal and inform
   * user flags to <code>true</code>; and sets enclosed exception value.
   * Makes use of {@link MEMEException#MEMEException(String, Object)}.
   * @param message the error message
   * @param exception the {@link Exception} that caused this one
   */
  public FailedToConnectException(String message, Exception exception) {
    super(message, exception);
  }

}
