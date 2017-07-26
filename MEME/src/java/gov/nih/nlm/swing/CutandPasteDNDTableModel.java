/**************************************************************************
 *
 * Package:	gov.nih.nlm.swing
 * Object:	CutandPasteDNDTableModel.java
 *
 **************************************************************************/
package gov.nih.nlm.swing;

import javax.swing.table.TableModel;

/**
 * Model for use with {@link CutandPasteDNDTable}.  It is recommeded
 * that when using this interface you subclass a rich implementation
 * of {@link TableModel} such as {@link DefaultTableModel} and
 * merely <code>implement</code> the three methods defined here.
 *
 * @author Deborah Shapiro
 */
public interface CutandPasteDNDTableModel extends TableModel {

  /**
   * Moves the rows at the specified drag indexes to
   * the specified drop index.  All rows are inserted
   * in the order in which they previously appeared in
   * the table.  For example, if rows 3,4, and 6 were
   * dragged and dropped at index 2, then row 3 would get
   * index 2, row 4 would get index 3, and row 6 would
   * get index 4.
   * @param drag_row_index an <code>int[]</code> of indexes
   *                   from the table that were dragged to
   *                   a new location and dropped
   * @param drop_row_index the index of the table where the
   *                   drop operation took place
   */
  public void moveRow(
      int[] drag_row_index,
      int drop_row_index);

  /**
   * Removes rows at the specified indices from the table.
   * @param rows_to_cut an <code>int[]</code> of table indexes
   */
  public void cutRow(int[] rows_to_cut);

  /**
   * Indicates the location where previously cut row
   * should be pasted.  The implementor of this interface
   * is required to keep track of the rows cut during
   * a {@link #cutRow(int[])} operation so that they can
   * be properly pasted in here.
   * @param paste_location the index where previously cut rows
   *                    should now be pasted
   */
  public void pasteRow(int paste_location);

}