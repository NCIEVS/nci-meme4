/*****************************************************************************
 *
 * Package:    gov.nih.nlm.swing
 * Object:     TableMap.java
 *
 *****************************************************************************/
package gov.nih.nlm.swing;

import java.util.Date;
import java.util.Vector;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Sorter for {@link TableModel}s. The sorter has a model (conforming to {@link TableModel})
 * and itself implements {@link TableModel}. {@link TableSorter} does not store or copy
 * the data in the {@link TableModel}, instead it maintains an array of
 * integers which it keeps the same size as the number of rows in its
 * model. When the model changes it notifies the sorter that something
 * has changed eg. "rowsAdded" so that its internal array of integers
 * can be reallocated. As requests are made of the sorter (like
 * getValueAt(row, col) it redirects them to its model via the mapping
 * array. That way the {@link TableSorter} appears to hold another copy of the table
 * with the rows in a different order. The sorting algorthm used is stable
 * which means that it does not move around rows when its comparison
 * function returns 0 to denote that they are equivalent.
 *
 * @version 1.5 12/17/97
 * @author Philip Milne
 */
public class TableSorter extends TableMap {

  //
  // Private fields
  //

  private int indexes[];
  private Vector sorting_columns = new Vector();
  private boolean ascending = true;
  private int compares;
  private int column;
  private boolean header_clicked = false;

  /**
   * Instantiates an empty {@link TableSorter}.
   */
  public TableSorter() {
    indexes = new int[0]; // for consistency
  }

  /**
   * Instantiates a {@link TableSorter} with the specified model.
   * @param model the {@link TableModel}
   */
  public TableSorter(TableModel model) {
    setModel(model);
  }

  /**
   * Sets the model and resets the map.
   * @param model the {@link TableModel}
   */
  public void setModel(TableModel model) {
    super.setModel(model);
    reallocateIndexes();
  }

  /**
   * Resets the sorting mechanism to wait until a header is
   * clicked before sorting the table.
   */
  public void resetSort() {
    header_clicked = false;
  }

  /**
   * Compares data in sorted column by type.  If the type is
   * an object type, it sorts by string value.
   * @param row1 A table row
   * @param row2 Another table row
   * @param column A table column
   * @return a value indicating the relative sort order
   * of <code>row1</code> and <code>row2</code> for the
   * specified column
   */
  public int compareRowsByColumn(int row1, int row2, int column) {
    Class type = model.getColumnClass(column);
    TableModel data = model;

    //
    // Check for nulls.
    //
    Object o1 = data.getValueAt(row1, column);
    Object o2 = data.getValueAt(row2, column);

    //
    // If both values are null, return 0.
    //
    if (o1 == null && o2 == null) {
      return 0;
    } else if (o1 == null) { // Define null less than everything.
      return -1;
    } else if (o2 == null) {
      return 1;
    }

    //
    // We copy all returned values from the getValue call in case
    // an optimised model is reusing one object to return many
    // values.  The Number subclasses in the JDK are immutable and
    // so will not be used in this way but other subclasses of
    // Number might want to do this to save space and avoid
    // unnecessary heap allocation.
    //
    if (type.getSuperclass() == java.lang.Number.class) {
      Number n1 = (Number) data.getValueAt(row1, column);
      double d1 = n1.doubleValue();
      Number n2 = (Number) data.getValueAt(row2, column);
      double d2 = n2.doubleValue();

      if (d1 < d2) {
        return -1;
      } else if (d1 > d2) {
        return 1;
      } else {
        return 0;
      }
    } else if (type == java.util.Date.class) {
      Date d1 = (Date) data.getValueAt(row1, column);
      long n1 = d1.getTime();
      Date d2 = (Date) data.getValueAt(row2, column);
      long n2 = d2.getTime();

      if (n1 < n2) {
        return -1;
      } else if (n1 > n2) {
        return 1;
      } else {
        return 0;
      }
    } else if (type == String.class) {
      String s1 = (String) data.getValueAt(row1, column);
      String s2 = (String) data.getValueAt(row2, column);
      int result = s1.compareTo(s2);

      if (result < 0) {
        return -1;
      } else if (result > 0) {
        return 1;
      } else {
        return 0;
      }
    } else if (type == Boolean.class) {
      Boolean bool1 = (Boolean) data.getValueAt(row1, column);
      boolean b1 = bool1.booleanValue();
      Boolean bool2 = (Boolean) data.getValueAt(row2, column);
      boolean b2 = bool2.booleanValue();

      if (b1 == b2) {
        return 0;
      } else if (b1) { // Define false < true
        return 1;
      } else {
        return -1;
      }
    } else {
      Object v1 = data.getValueAt(row1, column);
      String s1 = v1.toString();
      Object v2 = data.getValueAt(row2, column);
      String s2 = v2.toString();
      int result = s1.compareTo(s2);

      if (result < 0) {
        return -1;
      } else if (result > 0) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  /**
   * Perform comparison on the sorting column.
   * @param row1 A table row
   * @param row2 Another table row
   * @return an value indicating the relative sort order of row1 and row2
   */
  public int compare(int row1, int row2) {
    compares++;
    for (int level = 0; level < sorting_columns.size(); level++) {
      Integer column = (Integer) sorting_columns.elementAt(level);
      int result = compareRowsByColumn(row1, row2, column.intValue());
      if (result != 0) {
        return ascending ? result : -result;
      }
    }
    return 0;
  }

  /**
   * Resets the map so that the first row of the table
   * corresponds to the first index of the underlying model.
   */
  public void reallocateIndexes() {
    int rowCount = model.getRowCount();
    //
    // Set up a new array of indexes with the right number of elements
    // for the new data model.
    //
    indexes = new int[rowCount];

    //
    // Initialise with the identity mapping.
    //
    for (int row = 0; row < rowCount; row++) {
      indexes[row] = row;
    }
  }

  /**
   * Handles the table change event.
   * @param e the {@link TableModelEvent}
   */
  public void tableChanged(TableModelEvent e) {
    if (e.getType() != TableModelEvent.UPDATE) {
      reallocateIndexes();
    }
    if (header_clicked) {
      this.sortByColumn(column, ascending);
    }

    super.tableChanged(e);
  }

  /**
   * Validates the model.  This simple check verifies that
   * the index map has the same row count as the underlying model.
   */
  public void checkModel() {
    if (indexes.length != model.getRowCount()) {
      System.err.println("Sorter not informed of a change in model.");
    }
  }

  /**
   * Sorts the table.  Actually, it sorts the appropriate column
   * and re-orders the index map to produce a sorted table column.
   * @param sender {@link Object}
   */
  public void sort(Object sender) {
    checkModel();
    compares = 0;
    shuttlesort( (int[]) indexes.clone(), indexes, 0, indexes.length);
  }


  /**
   * This is a home-grown implementation which we have not had time
   * to research - it may perform poorly in some circumstances. It
   * requires twice the space of an in-place algorithm and makes
   * NlogN assigments shuttling the values between the two
   * arrays. The number of compares appears to vary between N-1 and
   * NlogN depending on the initial order but the main reason for
   * using it here is that, unlike qsort, it is stable.
   * @param from source
   * @param to target
   * @param low low value
   * @param high high value
   */
  public void shuttlesort(int from[], int to[], int low, int high) {
    if (high - low < 2) {
      return;
    }
    int middle = (low + high) / 2;
    shuttlesort(to, from, low, middle);
    shuttlesort(to, from, middle, high);

    int p = low;
    int q = middle;

    //
    // This is an optional short-cut; at each recursive call,
    // check to see if the elements in this subset are already
    // ordered.  If so, no further comparisons are needed; the
    // sub-array can just be copied.  The array must be copied rather
    // than assigned otherwise sister calls in the recursion might
    // get out of sinc.  When the number of elements is three they
    // are partitioned so that the first set, [low, mid), has one
    // element and and the second, [mid, high), has two. We skip the
    // optimisation when the number of elements is three or less as
    // the first compare in the normal merge will produce the same
    // sequence of steps. This optimisation seems to be worthwhile
    // for partially ordered lists but some analysis is needed to
    // find out how the performance drops to Nlog(N) as the initial
    // order diminishes - it may drop very quickly.

    if (high - low >= 4 && compare(from[middle - 1], from[middle]) <= 0) {
      for (int i = low; i < high; i++) {
        to[i] = from[i];
      }
      return;
    }
    //
    // A normal merge.
    //
    for (int i = low; i < high; i++) {
      if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
        to[i] = from[p++];
      } else {
        to[i] = from[q++];
      }
    }
  }

  /**
   * Swap two elements of the map.
   * @param i first index to swap
   * @param j second index to swap
   */
  public void swap(int i, int j) {
    int tmp = indexes[i];
    indexes[i] = indexes[j];
    indexes[j] = tmp;
  }

  /**
   * Returns the value at the specified row and column from
   * the underlying model. This operation is performed by
   * mapping the row index through the map.
   * @param r a table row
   * @param c a table column
   * @return the value at the specified row and column from
   * the underlying model
   */
  public Object getValueAt(int r, int c) {
    checkModel();
    return model.getValueAt(indexes[r], c);
  }

  /**
   * Sets the value at the specified row and column from
   * the underlying model. This operation is performed by
   * mapping the row index through the map.
   * @param r a table row
   * @param c a table column
   * @param value an {@link Object} value.
   */
  public void setValueAt(Object value, int r, int c) {
    checkModel();
    model.setValueAt(value, indexes[r], c);
  }

  /**
   * Sorts table by a column (ascending by default).
   * @param column the column number
   */
  public void sortByColumn(int column) {
    sortByColumn(column, true);
  }

  /**
   * Sorts table by a column.
   * @param column the column number
   * @param ascending flag indicating sort order
   */
  public void sortByColumn(int column, boolean ascending) {
    this.ascending = ascending;
    sorting_columns.removeAllElements();
    sorting_columns.addElement(new Integer(column));
    sort(this);
    super.tableChanged(new TableModelEvent(this));
  }

  /**
   * Returns the mapped index for the specified table row index.
   * @param i a table row
   * @return the index of the underlying model
   */
  public int mapIndex(int i) {
    return indexes[i];
  }

  /**
   * Returns the table row index for the underlying model index.
   * @param i the underlying index of the model
   * @return the corresponding table row index
   */
  public int reverseMapIndex(int i) {
    for (int j = 0; j < indexes.length; j++) {
      if (indexes[j] == i) {
        return j;
      }
    }
    return -1;
  }

  /**
   * Debug method.
   */
  public void dump() {
  }

  /**
   * Adds a mouse listener to the Table to trigger a table sort
   * when a column heading is clicked in the JTable.
   * @param table the sortable {@link JTable}
   */
  public void addMouseListenerToHeaderInTable(JTable table) {
    final TableSorter sorter = this;
    final JTable tableView = table;
    final JTableHeader th = tableView.getTableHeader();

    final SortButtonRenderer renderer = new SortButtonRenderer();

    th.setDefaultRenderer(renderer);
    tableView.setColumnSelectionAllowed(false);
    MouseAdapter listMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        header_clicked = true;
        TableColumnModel columnModel = tableView.getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
        column = tableView.convertColumnIndexToModel(viewColumn);
        if (e.getClickCount() == 1 && column != -1) {
          int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
          boolean ascending = (shiftPressed == 0);
          renderer.setState(column, ascending);
          sorter.sortByColumn(column, ascending);
          th.resizeAndRepaint();
        }
      }
    };

    th.addMouseListener(listMouseListener);
  }

}
