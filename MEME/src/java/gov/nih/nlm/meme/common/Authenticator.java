/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Authenticator
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Generically represents a way to obtain authentication information.
 * An application will request that an {@link Authentication} object provide
 * its authentication information to an Authenticator.  These two classes must
 * be written together with knowledge of how the other one works.  A sample
 * exchange would work like this,
 * <pre>
 * Authenticator authenticator = <obtain authenticator instance>;
 * Authentication authentication = <obtain authentication instance>;
 *
 * // pass info
 * authentication.provideAuthentication(authenticator);
 *
 * // check and see if it was successful,
 * if (authentication.failed()) {
 *   System.out.println("Authentication failed: " + authenticator.getReasonFailed();
 * }
 * </pre>
 *
 *
 * @see Authentication
 *
 * @author MEME Group
 */

public interface Authenticator {

  /**
   * Informs {@link Authenticator} that authentication has failed. It indicates
   * that it could not provide the necessary authentication information.
   * @param reason the reason for authentication failure.
   */
  public void authenticationFailed(String reason);

  /**
   * Returns the reason for authentication failure.
   * @return the reason for authentication failure
   */
  public String getReasonFailed();

  /**
   * Indicates whether authentication failed or passed.
   * @return <code>true</code> if authentication failed;
   * <code>false</code> otherwise.
   */
  public boolean failed();

}
