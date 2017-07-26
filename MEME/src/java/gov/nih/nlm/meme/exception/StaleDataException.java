/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  StaleDataException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception that was caused
 * an editing client attempting to change core data based on stale in-memory
 * objects.  For example, if any actions have been performed since the client last read
 * the data, this exception will be thrown.
 *
 * @author MEME Group
 */
public class StaleDataException extends ApplicationException {

  /**
   * Instantiates a {@link StaleDataException} with the specified message and a null source.
   * Makes use of {@link #StaleDataException(String, Object)}.
   * @param message the error message
   */
  public StaleDataException(String message) {
    this(message, null);
  }

  /**
   * Instantiates a {@link StaleDataException} with the specified message and source.
   * It inherits the super class and additionally sets the inform user flag to
   * <code>true</code> by default.
   * Makes use of {@link MEMEException#MEMEException(String, Object)}.
   * @param message thhe error message
   * @param source the {@link Object} that caused this exception
   */
  public StaleDataException(String message, Object source) {
    super(message, source);
    setPrintStackTrace(true);
  }

}
