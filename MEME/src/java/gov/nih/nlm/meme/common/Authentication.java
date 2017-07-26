/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.common
 * Object:  Authentication
 *
 *****************************************************************************/

package gov.nih.nlm.meme.common;

/**
 * Represents a generic way to allow an {@link Authenticator} to provide its
 * authentication information.
 * Any implementation of this interface will have to define methods
 * in addition to {@link #provideAuthentication(Authenticator)} that
 * would be recognized by the {@link Authenticator} being passed in.
 * <p>
 * In other words, implementations of this interface and {@link Authenticator}
 * should be written together and have knowledge of how the other one works.
 *
 * @see Authenticator
 *
 * @author MEME Group
 */

public interface Authentication {

  /**
   * This method takes an {@link Authenticator} object and attempts to provide
   * the necessary information that the authenticator expects.  If it cannot or some
   * other problem arises then it informs the authenticator that the authentication failed.
   * @param authenticator An object {@link Authenticator} which represents
   * authenticator that use to determine its authentication.
   */
  public void provideAuthentication(Authenticator authenticator);

}
