/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  AdministratorException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception that was caused
 * by an administrator mistake.  Typically, these errors will be fatal and will
 * require the stack trace to be dumped so that the location of the error can
 * be easily determined.  As much information as possible should be provided in
 * the details so that the problem can be easily diagnosed.
 *
 * @author MEME Group
 */
public class AdministratorException extends MEMEException {

  /**
   * Instantiates an {@link AdministratorException} with the specified message, a null source and
   * a null exception.
   * Makes use of {@link #AdministratorException(String, Object)}.
   * @param message the error message
   */
  public AdministratorException(String message) {
    this(message, null);
  }

  /**
   * Instantiates an {@link AdministratorException} with the specified message and source.
   * It inherits the super class and additionally sets the inform user and
   * administrator flags to <code>true</code> by default.
   * Makes use of {@link MEMEException#MEMEException(String, Object)}.
   * @param message the error message
   * @param source the {@link Object} that caused the exception
   */
  public AdministratorException(String message, Object source) {
    super(message, source);
    setInformUser(true);
    setInformAdministrator(true);
    setAdministrator(MEME_ADMIN);
  }

}
