/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  DataSourceConnectionException
 *
 * 05/24/2006 RBE (1-BA55P) : Preventing certain errors from sending mail
 * 
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by an attempt to
 * connect to a {@link gov.nih.nlm.meme.sql.MEMEDataSource}.  For example, a piece of code that attempts to
 * connect to a data source that did not exist would cause this exception.
 *
 * @author MEME Group
 */
public class DataSourceConnectionException extends DeveloperException {

  /**
   * Instantiates a {@link DataSourceConnectionException} with the specified message, a null source
   * and a null exception.
   * Makes use of {@link #DataSourceConnectionException(String, Exception)}.
   * @param message the error message
   */
  public DataSourceConnectionException(String message) {
    this(message, null);
  }

  /**
   * Instantiates a {@link DataSourceConnectionException} with the specified message and exception.
   * It sets the fatal, print stack trace and inform user flags to
   * <code>false</code>; and the enclosed exception value.
       * Makes use of {@link DeveloperException#DeveloperException(String, Object)}.
   * @param message the error message
   * @param exception the {@link Exception} that caused this one
   */
  public DataSourceConnectionException(String message, Exception exception) {
    super(message, exception);
    setFatal(false);
    setPrintStackTrace(false);
    setInformUser(false);
    setInformAdministrator(false);    
    setEnclosedException(exception);
  }

}
