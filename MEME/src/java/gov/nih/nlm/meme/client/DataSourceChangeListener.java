/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  DataSourceChangeListener
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.swing.GlassPaneListener;

import java.util.EventListener;

/**
 * Listens for changes in the data source being used.
 *
 * @author BAC, RBE
 */
public interface DataSourceChangeListener extends EventListener,
    GlassPaneListener {

  /**
   * Indicates that the data source has changed.
   * @param dsce the event
   */
  public void dataSourceChanged(DataSourceChangeEvent dsce);
}
