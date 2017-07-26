/*****************************************************************************
 *
 * Package:    gov.nih.nlm.swing
 * Object:     TableMap.java
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting
 * a TableMap which has not been subclassed into a chain of table filters
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne
 */
public class TableMap extends AbstractTableModel implements TableModelListener {
  protected TableModel model;

  /**
   * Returns the model underlying the map.
   * @return the model underlying the map
   */
  public TableModel getModel() {
    return model;
  }

  /**
   * Sets the model underlying the map.
   * @param model the {@link TableModel}
   */
  public void setModel(TableModel model) {
    this.model = model;
    model.addTableModelListener(this);
  }

  // By default, implement TableModel by forwarding all messages
  // to the model.

  /**
   * Returns the value for the specified cell from the underlying model.
   * @param r A row index
   * @param c A colum index
   * @return a value for cell from underlying model
   */
  public Object getValueAt(int r, int c) {
    return model.getValueAt(r, c);
  }

  /**
   * Sets the value for a cell in the underlying mode.
   * @param value An {@link Object} value
   * @param r A row index
   * @param c A colum index
   */
  public void setValueAt(Object value, int r, int c) {
    model.setValueAt(value, r, c);
  }

  /**
   * Returns the row count from the underlying model.
   * @return the row count from the underlying model
   */
  public int getRowCount() {
    return (model == null) ? 0 : model.getRowCount();
  }

  /**
   * Returns the column count from the underlying model.
   * @return the column count from the underlying model
   */
  public int getColumnCount() {
    return (model == null) ? 0 : model.getColumnCount();
  }

  /**
   * Returns the column name from the underlying model.
   * @param c A column index
   * @return the column name from the underlying model
   */
  public String getColumnName(int c) {
    return model.getColumnName(c);
  }

  /**
   * Returns the column class from the underlying model.
   * @param aColumn A column index
   * @return the column class from the underlying model
   */
  public Class getColumnClass(int aColumn) {
    return model.getColumnClass(aColumn);
  }

  /**
   * Returns a flag from the underlying model indicating
   * whether or not the cell is editable.
   * @param r A row index
   * @param c A column index
   * @return a flag from the underlying model indicating
   * whether or not the cell is editable
   */
  public boolean isCellEditable(int r, int c) {
    return model.isCellEditable(r, c);
  }

  //
  // Implementation of the TableModelListener interface,
  //

  // By default forward all events to all the listeners.

  /**
   * Forwards a table changed event to the table.
   * @param e a {@link TableModelEvent}
   */
  public void tableChanged(TableModelEvent e) {
    fireTableChanged(e);
  }

}
