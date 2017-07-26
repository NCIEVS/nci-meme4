/**************************************************************************
 *
 * Package:    gov.nih.nlm.swing;
 * Interface:  DataChangeListener.java
 *
 *************************************************************************/
package gov.nih.nlm.swing;

/**
 * Generically represents a listener for data changes. This typically used
 * to implement "Save if data changed" functionality.
 *
 * @author Brian Carsen
 */
public interface DataChangeListener {

  /**
   * Informs the listener that data has changed.
   * @param e the {@link DataChangeEvent}
   */
  public void dataChanged(DataChangeEvent e);

}