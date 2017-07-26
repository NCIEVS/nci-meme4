/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  MissingPropertyException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

/**
 * Generically represents an exception caused by missing property.
 *
 *
 * @author MEME Group
 */
public class MissingPropertyException extends AdministratorException {

  /**
   * Instantiates a {@link MissingPropertyException} with the specified message and property name.
   * It inherits fatal from the super class; sets the inform  user flag
   * to <code>false</code>; and sets the detail and administrator value.
       * Makes use of {@link AdministratorException#AdministratorException(String)}.
   * @param message the error message
   * @param property the missing property name
   */
  public MissingPropertyException(String message, String property) {
    super(message +
          "\nThis is most likely the results of a property not being set.\n" +
          " Please check the properties file.");
    setInformUser(false);
    setDetail("property", property);
    setAdministrator(SYSTEM_ADMIN);
  }

}
