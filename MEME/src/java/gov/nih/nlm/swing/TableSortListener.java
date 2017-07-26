/**************************************************************************
 *
 * Package:    gov.nih.nlm.swing;
 * Interface:  TableSortListener.java
 *
 *************************************************************************/
package gov.nih.nlm.swing;

import javax.swing.event.ListSelectionListener;

/**
 *
 * Generically represents a listener for responding to
 * {@link SortableJTable} {@link TableSortEvent}s.  The interface
 * extends {@link ListSelectionListener} because this listener is
 * designed to allow list selection listeners to ignore
 * <code>valueChanged</code> calls during a table sort.
 *
 * @author Brian Carsen
 **/

public interface TableSortListener extends ListSelectionListener {

  /**
   * Informs the listener that table sorting has begun.
   * @param e the {@link TableSortEvent}
   */
  public void tableSortStarted(TableSortEvent e);

  /**
   * Informs the listener that table sorting has completed.
   * @param e the {@link TableSortEvent}
   */
  public void tableSortFinished(TableSortEvent e);

}
