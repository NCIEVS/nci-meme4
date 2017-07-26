/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  PasswordAuthentication
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents user/password authentication information.
 *
 * @see PasswordAuthenticator
 * @see Authenticator
 *
 * @author MEME Group
 */

public class PasswordAuthentication implements Authentication {

  //
  // Fields
  //

  private String name = null;
  private char[] password;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link PasswordAuthentication} from the specified name
   * and password.
   * @param name the name
   * @param password the password
   */
  public PasswordAuthentication(String name, char[] password) {
    this.name = name;
    this.password = password;
  }

  //
  // Implementation of Authentication interface
  //

  /**
   * Implementation of {@link Authentication} interface.
   * This method expects a {@link PasswordAuthenticator} object to be
   * passed in as the {@link Authenticator}.  If the instance of the
   * object passed in is not a {@link PasswordAuthenticator}, then the
   * {@link Authenticator#authenticationFailed(String)}
   * method is called, indicating that this
   * {@link Authentication} cannot provide the necessary information.
   * @param authenticator An object {@link Authenticator} representation of
   * password authenticator.
   */
  public void provideAuthentication(Authenticator authenticator) {
    if (authenticator instanceof PasswordAuthenticator) {
      ( (PasswordAuthenticator) authenticator).setUsernameAndPassword(name,
          password);
    } else {
      authenticator.authenticationFailed("Expecting PasswordAuthenticator");
    }
  }

  /**
   * Returns the name.
   * @return the name
   */
  public String toString() {
    return name;
  }
}
