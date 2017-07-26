/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  DataSourceException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by the use of a
 * {@link gov.nih.nlm.meme.sql.MEMEDataSource}.  For example, a piece of code that attempts to
 * execute a badly formatted query would trigger this exception.
 *
 * @author MEME Group
 */
public class DataSourceException extends DeveloperException {

  /**
   * Instantiates a {@link DataSourceException} with the specified message, a null source
   * and a null exception.
   * Makes use of {@link #DataSourceException(String, Object, Exception)}.
   * @param message the error message
   */
  public DataSourceException(String message) {
    this(message, null, null);
  }

  /**
   * Instantiates a {@link DataSourceException} with the specified message, source and exception.
   * It inherits fatal and print stack trace flags from the super class; sets
       * the inform user flag to <code>false</code>; and sets the enclosed exception
   * value.
       * Makes use of {@link DeveloperException#DeveloperException(String, Object)}.
   * @param message the error message
   * @param source the {@link Object} that caused this exception
   * @param exception the {@link Exception} that caused this one
   */
  public DataSourceException(String message, Object source, Exception exception) {
    super(message, source);
    setPrintStackTrace(true);
    setInformUser(false);
    setEnclosedException(exception);
  }

}
