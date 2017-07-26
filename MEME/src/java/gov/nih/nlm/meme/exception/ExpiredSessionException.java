/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  ExpiredSessionException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by an
 * attempt to access an expried session.
 *
 * @author MEME Group
 */
public class ExpiredSessionException extends ApplicationException {

  /**
   * Instantiates an {@link ExpiredSessionException} with the specified message.  It inherits the
   * fatal, print stack trace and inform user flags from the super class.
   * Makes use of {@link #ExpiredSessionException(String)}.
   * @param message the error message
   */
  public ExpiredSessionException(String message) {
    super(message);
  }

}
