/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  ServerChangeListener
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.swing.GlassPaneListener;

import java.util.EventListener;

/**
 * Listens for changes in the server being used.
 *
 * @author BAC, RBE
 *
 */

public interface ServerChangeListener extends EventListener, GlassPaneListener {

  /**
   * Indicates that the server information has changed.
   * @param dsce the event
   */
  public void serverChanged(ServerChangeEvent dsce);
}
