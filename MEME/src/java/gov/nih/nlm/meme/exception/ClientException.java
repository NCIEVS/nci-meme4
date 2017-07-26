/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  ClientException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception that was caused
 * by a client request.  This class is officially <i>deprecated</i> and should
 * not be used.
 *
 * @author MEME Group
 */

public class ClientException extends MEMEException {

  /**
   * Instantiates an empty {@link ClientException}.
   */
  public ClientException() {};

  /**
   * Instantiates a {@link ClientException} with the specified message and a null source.
   * Makes use of {@link #ClientException(String, Object)}.
   * @param message  the error message
   */
  public ClientException(String message) {
    this(message, null);
  }

  /**
   * Instantiates a {@link ClientException} with the specified message and specified
   * source.  It inherits the super class.
   * Makes use of {@link MEMEException#MEMEException(String, Object)}.
   * @param message the error message
   * @param source the object that caused the exception
   */
  public ClientException(String message, Object source) {
    super(message, source);
  }

}
