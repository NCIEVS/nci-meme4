/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme
 * Object:  InitializationContext
 *
 *****************************************************************************/

package gov.nih.nlm.meme;

/**
 * Generically allows {@link Initializable} components
 * to register themselves with the application that requested
 * their initialzation.
 *
 * @author MEME Group
 */
public interface InitializationContext {

  /**
   * Provides a mechanism for an application implementing
   * the interface to keep a reference to each
   * {@link Initializable} component.
   * @param init the {@link Initializable} component
   */
  public void addHook(Initializable init);

}
