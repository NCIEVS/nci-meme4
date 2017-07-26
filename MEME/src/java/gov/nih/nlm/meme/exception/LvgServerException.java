/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  LvgServerException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

import gov.nih.nlm.meme.MIDServices;

/**
 * Generically represents an exception caused by the attempt to
 * normalize a string via the {@link gov.nih.nlm.meme.MIDServices}.
 *
 * @author MEME Group
 */
public class LvgServerException extends MidsvcsException {

  /**
   * Instantiates a {@link LvgServerException} with the specified message and null exception.
   * Makes use of {@link #LvgServerException(String, Exception)}.
   * @param message the error message
   */
  public LvgServerException(String message) {
    this(message, null);
  }

  /**
   * Instantiates a {@link LvgServerException} with the specified message and exception.
   * It inherits fatal and print stack trace from the super class; sets the
   * inform user flag to <code>false</code>; and sets the enclosed exception
   * and administrator value.
       * Makes use of {@link AdministratorException#AdministratorException(String)}.
   * @param message the error message
   * @param exception the {@link Exception} that caused this one
   */
  public LvgServerException(String message, Exception exception) {
    super(message);
    setInformUser(false);
    setEnclosedException(exception);
    setAdministrator(MIDSVCS_ADMIN);
    try {
      setDetail("host", MIDServices.getService("lvg-server-host"));
      setDetail("port", MIDServices.getService("lvg-server-port"));
    } catch (MidsvcsException me) {}
  }

}
