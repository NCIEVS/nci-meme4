/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  MissingDataException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by missing
 * data in the database.
 *
 * @author MEME Group
 */
public class MissingDataException extends DataSourceException {

  /**
   * CInstantiates a {@link MissingDataException} with the specified message and a null source.
   * Makes use of {@link #MissingDataException(String)}.
   * @param message the error message
   */
  public MissingDataException(String message) {
    this(message, null, null);
  }

  /**
   * Instantiates a {@link MissingDataException} with the specified message and source.
   * It inherits fatal and print stack trace flags from the super class.
   * This is just a data
   * error and so does not merit sending mail to the administrator.
       * Makes use of {@link DeveloperException#DeveloperException(String, Object)}.
   * @param message the error message
   * @param source the {@link Object} that caused this exception
   * @param exception the {@link Exception} that caused this one
   */
  public MissingDataException(String message, Object source,
                              Exception exception) {
    super(message, source, exception);
    setInformAdministrator(false);
    setPrintStackTrace(true);
  }

}
