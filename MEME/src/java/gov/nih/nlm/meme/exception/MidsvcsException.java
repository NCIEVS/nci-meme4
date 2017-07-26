/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  MidsvcsException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

import gov.nih.nlm.meme.MEMEToolkit;

/**
 * Generically represents an exception caused by the attempt to
 * use the {@link gov.nih.nlm.meme.MIDServices}.
 *
 * @author MEME Group
 */
public class MidsvcsException extends AdministratorException {

  /**
   * Instantiates a {@link MidsvcsException} with the specified message and null exception.
   * Makes use of {@link #MidsvcsException(String, Exception)}.
   * @param message the error message
   */
  public MidsvcsException(String message) {
    this(message, null);
  }

  /**
   * Instantiates a {@link MidsvcsException} with the specified message and exception.
   * It inherits fatal and print stack trace from the super class; sets the
   * inform user flag to <code>false</code>; and sets the enclosed exception
   * and administrator value.
       * Makes use of {@link AdministratorException#AdministratorException(String)}.
   * @param message the error message
   * @param exception the {@link Exception} that caused this one
   */
  public MidsvcsException(String message, Exception exception) {
    super(message);
    setInformUser(false);
    setEnclosedException(exception);
    setAdministrator(MIDSVCS_ADMIN);
    setDetail("host", MEMEToolkit.getProperty(MIDSVCS_HOST));
    setDetail("port", MEMEToolkit.getProperty(MIDSVCS_PORT));
  }

}
