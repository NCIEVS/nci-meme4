/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  ClientProgressListener
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import java.util.EventListener;

/**
 * Listens for changes in the data source being used.
 *
 * @author BAC, RBE
 */
public interface ClientProgressListener extends EventListener {

  /**
   * Indicates progress made with a message.
   * @param cpe the event
   */
  public void progressUpdated(ClientProgressEvent cpe);
}
