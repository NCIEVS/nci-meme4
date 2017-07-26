/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  PasswordAuthenticator
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Implements an authentication hand-shake with {@link PasswordAuthentication}
 * to provide username/password access to resources.
 * It extends the {@link Authenticator} functionality with the
 * {@link PasswordAuthenticator#setUsernameAndPassword(String, char[])} method
 * which allows an {@link Authentication} object to provide the necessary
 * information for authentication.
 *
 * @see PasswordAuthentication
 * @see Authentication
 *
 * @author MEME Group
 */

public class PasswordAuthenticator implements Authenticator {

  //
  // Fields
  //

  private String username = null;
  private String reason_failed = null;
  private char[] password;
  private boolean failed = false;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link PasswordAuthenticator}.
   */
  public PasswordAuthenticator() {}

  //
  // Methods
  //

  /**
   * Sets the username and password.
   * @param username the username
   * @param password the password
   */
  public void setUsernameAndPassword(String username, char[] password) {
    this.username = username;
    this.password = password;
  }

  /**
   * Returns the username.
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Returns the password.
   * @return the password.
   */
  public char[] getPassword() {
    return password;
  }

  /**
   * Clears the password.
   */
  public void clearPassword() {
    //for (int i = 0; i < password.length; i++) {
    //  password[i] = (char) 0;
    //}
    password = null;
  }

  //
  // Implementation of Authenticator interface.
  //

  /**
   * Implements {@link Authenticator#authenticationFailed(String)}.
   * Sets the reason_failed field and failed field to true.
   * @param reason the reason for failure
   */
  public void authenticationFailed(String reason) {
    this.reason_failed = reason;
    this.failed = true;
  }

  /**
   * Implements {@link Authenticator#failed()}.  It returns
   * <code>true</code> if not authentic; <code>false</code> otherwise.
   * @return a flag indicating whether authentication failed
   */
  public boolean failed() {
    return failed;
  }

  /**
   * Implements {@link Authenticator#getReasonFailed()}.
   * Returns the reason for failure.
   * @return the reason for failure.
   */
  public String getReasonFailed() {
    return reason_failed;
  }

}
