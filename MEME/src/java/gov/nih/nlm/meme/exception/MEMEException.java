/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.exception
 * Object:  MEMEException
 *
 *****************************************************************************/

package gov.nih.nlm.meme.exception;

import gov.nih.nlm.meme.MEMEConstants;

import java.io.PrintWriter;
import java.util.HashMap;

/**
 * This is a wrapper around {@link Exception} which is the base class for the
 * <code>MEME</code> exception architecture.  It provides useful methods for
 * obtaining more information about the nature of the actual problem.
 *
 * @author MEME Group
 */

public class MEMEException extends Exception implements MEMEConstants {

  //
  // Fields
  //

  private HashMap details = null;
  private Object source = null;
  private Exception enclosed_exception = null;
  private boolean fatal = false;
  private boolean inform_user = false;
  private boolean inform_client = false;
  private boolean inform_administrator = false;
  private String administrator = null;
  private boolean print_stack_trace = false;
  private String message = "";

  /**
   * Instantiates an empty {@link MEMEException}.
   */
  public MEMEException() {}

  /**
   * Instantiates a {@link MEMEException} with the specified message and a null
   * source.  Makes use of {@link MEMEException#MEMEException(String,Object)}.
   * @param message the error message
   */
  public MEMEException(String message) {
    this(message, null);
  }

  /**
       * Instantiates a {@link MEMEException} with the specified message and specified
   * source.  Makes use of {@link Exception}.
   * @param message the error message
   * @param source the {@link Object} that caused this exception
   */
  public MEMEException(String message, Object source) {
    super();
    this.message = message;
    this.source = source;
    details = new HashMap();
  }

  //
  // Accessor Methods
  //

  /**
   * Returns the error message.
   * @return the error message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the error message
   * @param message the error message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Returns the administrator email property.
   * @return the property name that should be
   * used to look up the administrator's email.
   */
  public String getAdministrator() {
    return administrator;
  }

  /**
   * Sets the administrator email property.
   * @param admin_prop the property name used to
   * look up the administrator's email.
   */
  public void setAdministrator(String admin_prop) {
    this.administrator = admin_prop;
  }

  /**
   * Returns the enclosed {@link Exception} if there is one.
   * @return the enclosed {@link Exception} if there is one
   */
  public Exception getEnclosedException() {
    return enclosed_exception;
  }

  /**
   * Sets the enclosed {@link Exception}.
   * @param enclosed_exception the enclosed {@link Exception}
   */
  public void setEnclosedException(Exception enclosed_exception) {
    this.enclosed_exception = enclosed_exception;
  }

  /**
   * Returns the {@link Object} that caused the exception.
   * @return the {@link Object} that caused the exception
   */
  public Object getSource() {
    return source;
  }

  /**
   * Sets the source of the exception.
   * @param source the {@link Object} that caused the exception
   */
  public void setSource(Object source) {
    this.source = source;
  }

  /**
   * Indicates whether or not the exception is fatal.
   * @return <code>true</code> if the exception is fatal;
   * <code>false</code> otherwise.
   */
  public boolean isFatal() {
    return fatal;
  }

  /**
   * Sets the flag indicating whether or not the stack trace should be
   * printed when <code>super.printStackTrace()</code> is called.
   * @param pst <code>true</code> if the exception is fatal;
   * <code>false</code> otherwise.
   */
  public void setPrintStackTrace(boolean pst) {
    this.print_stack_trace = pst;
  }

  /**
   * Sets the flag indicating whether or not the user should be informed
   * of the exception.
   * @param inform_user <code>true</code> if user should be informed;
   * <code>false</code> otherwise.
   */
  public void setInformUser(boolean inform_user) {
    this.inform_user = inform_user;
  }

  /**
   * Sets the flag indicating whether or not the client should be informed
   * of the exception.
   * @param inform_client <code>true</code> if client should be informed;
   * <code>false</code> otherwise.
   */
  public void setInformClient(boolean inform_client) {
    this.inform_client = inform_client;
  }

  /**
   * Gets the flag indicating whether or not the user should be informed
   * of the exception.
   * @return <code>true</code> if user should be informed;
   * <code>false</code> otherwise.
   */
  public boolean informUser() {
    return inform_user;
  }

  /**
   * Indicates whether or not the client should be informed
   * of the exception.
   * @return <code>true</code> if client should be informed;
   * <code>false</code> otherwise.
   */
  public boolean informClient() {
    return inform_client;
  }

  /**
       * Sets the flag indicating whether or not an administrator should be informed
   * of the exception the user.
   * @param inform_administrator <code>true</code> if user should be informed;
   * <code>false</code> otherwise.
   */
  public void setInformAdministrator(boolean inform_administrator) {
    this.inform_administrator = inform_administrator;
  }

  /**
   * Indicates whether or not an administrator should be informed
   * of the exception.
   * @return <code>true</code> if user should be informed;
   * <code>false</code> otherwise.
   */
  public boolean informAdministrator() {
    return inform_administrator;
  }

  /**
   * Sets the flag indicating whether or not the exception is fatal.
   * @param fatal <code>true</code> if the exception is fatal;
   * <code>false</code> otherwise.
   */
  public void setFatal(boolean fatal) {
    this.fatal = fatal;
  }

  /**
   * Returns any additional details about the exception.
   * @return any details as a {@link HashMap}
   */
  public HashMap getDetails() {
    return details;
  }

  /**
   * Adds a detail to the exception.
   * @param key a {@link String}  key
   * @param value an {@link Object} value
   */
  public void setDetail(String key, Object value) {
    details.put(key, value);
  }

  /**
   * Adds details to the exception.
   * @param details a {@link HashMap} of details
   */
  public void setDetails(HashMap details) {
    this.details = details;
  }

  /**
   * Returns a {@link String} representation.
   * @return a {@link String} representation
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(100);
    if (fatal) {
      sb.append("(FATAL) ");
    }
    sb.append(message);
    sb.append("\n\t");
    sb.append(details == null ? "" : details.toString());

    if (enclosed_exception != null && this != enclosed_exception &&
        (! (enclosed_exception instanceof MEMEException) ||
         ( (MEMEException) enclosed_exception).getEnclosedException() != this)) {

      sb.append("\nEnclosed exception: ");
      sb.append(enclosed_exception.toString());
    }

    return sb.toString();
  }

  //
  // Overridden Exception methods
  //

  /**
   * Print the stack trace if <code>setPrintStackTrace(true)</code> has been called.
   */
  public void printStackTrace() {
    if (print_stack_trace) {
      super.printStackTrace();
    }

    if (enclosed_exception != null && this != enclosed_exception) {
      enclosed_exception.printStackTrace();
    }
  }

  /**
   * Print the stack trace to the specified {@link PrintWriter} if
   * <code>setPrintStackTrace(true)</code> has beencalled.  Recursively
   * print stack trace for all enclosed exceptions.
   * @param log the {@link PrintWriter} log
   */
  public void printStackTrace(PrintWriter log) {

    if (print_stack_trace) {
      super.printStackTrace(log);
    }

    if (enclosed_exception != null && enclosed_exception != this) {
      enclosed_exception.printStackTrace(log);
    }
  }
}
