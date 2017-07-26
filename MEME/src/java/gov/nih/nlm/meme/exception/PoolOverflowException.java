/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  PoolOverflowException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by a pool overflow.
 *
 * @author MEME Group
 */
public class PoolOverflowException extends ApplicationException {

  /**
   * Instantiates a {@link PoolOverflowException} with the specified message.  It inherits the
   * fatal, print stack trace and inform user flags from the super class.
   * Makes use of {@link #PoolOverflowException(String)}.
   * @param message the error message
   */
  public PoolOverflowException(String message) {
    super(message);
  }

}
