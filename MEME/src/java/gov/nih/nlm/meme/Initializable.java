/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme
 * Object:  Initializable
 *
 *****************************************************************************/

package gov.nih.nlm.meme;

import gov.nih.nlm.meme.exception.InitializationException;

/**
 * Generically allows an application to give components
 * known to require initialization an opportunity to do so
 * before normal application processing proceeds.  In this
 * model, the application itself must implement
 * {@link InitializationContext}.
 *
 * @author MEME Group
 */
public interface Initializable {

  /**
   * Generically allow components to be initialized.
   * @param ic a reference to the context initializing the component.  Typically
   *           this will be the application itself.
   * @throws InitializationException if initialization fails
   */
  public void initialize(InitializationContext ic) throws
      InitializationException;

}
