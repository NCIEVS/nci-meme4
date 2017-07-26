/**************************************************************************
 *
 * Package:    gov.nih.nlm.swing;
 * Interface:  TableSortEvent.java
 *
 *************************************************************************/
package gov.nih.nlm.swing;

import java.util.EventObject;

/**
 *
 * Represents an event used with {@link SortableJTable} to inform
 * users of the table that a sort is taking place.
 * This is useful because it allows any selection listeners
 * for the class to ignore <code>valueChanged</code> events while
 * the table is sorting.
 *
 * @author Brian Carsen
 **/
public class TableSortEvent extends EventObject {

  //
  // Constructors
  //

  /**
   * Instantiates a {@link TableSortEvent}.
   * @param source the source of the event
   */
  public TableSortEvent(Object source) {
    super(source);
  };

}
