/**************************************************************************
 *
 * Package:	gov.nih.nlm.swing
 * Object:	SortButtonRenderer.java
 *
 **************************************************************************/
package gov.nih.nlm.swing;

import java.util.Hashtable;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * {@link TableCellRenderer} for use as s column header in a {@link SortableJTable}.
 * Displays a {@link BevelArrowIcon} that indicates if sorting should
 * occur in an ascending or descending order.
 */
public class SortButtonRenderer implements TableCellRenderer {
  public static final int NONE = 0;
  public static final int DOWN = 1;
  public static final int UP = 2;

  Hashtable state;
  DefaultTableCellRenderer downButton, upButton, noneButton;

  /**
   * Instantiates a {@link SortButtonRenderer}
   * Sets the current icon to {@link BlankIcon} until
   * the user chooses to sort a column.
   */
  public SortButtonRenderer() {
    state = new Hashtable();

    downButton = new DefaultTableCellRenderer();
    downButton.setHorizontalTextPosition(SwingConstants.LEFT);
    downButton.setIcon(new BevelArrowIcon(BevelArrowIcon.DOWN, false, false));
    upButton = new DefaultTableCellRenderer();
    upButton.setHorizontalTextPosition(SwingConstants.LEFT);
    upButton.setIcon(new BevelArrowIcon(BevelArrowIcon.UP, false, false));
  }

  /**
   * Returns a {@link JButton} that holds the text and {@link BevelArrowIcon}.
   * @param table the {@link JTable}
   * @param value the value
   * @param isSelected indicates whether or not it is selected
   * @param hasFocus indicates whether or not it has focus
   * @param row row index
   * @param column column index
   * @return a {@link JButton} that holds the text and {@link BevelArrowIcon}
   */
  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus, int row,
                                                 int column) {
    JButton button = new JButton(value.toString());
    button.setMargin(new Insets(0, 0, 0, 0));
    Object obj = state.get(new Integer(column));
    if (obj != null) {
      if ( ( (Integer) obj).intValue() == DOWN) {
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        button.setIcon(new BevelArrowIcon(BevelArrowIcon.DOWN, false, false));
      } else {
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        button.setIcon(new BevelArrowIcon(BevelArrowIcon.UP, false, false));
      }
    }
    Font f = button.getFont();
    button.setFont(new Font(f.getName(), Font.PLAIN, f.getSize()));
    Container wrapper = new Container();
    wrapper.add(button);
    FontSizeManager.adjustFontSize(wrapper);
    return button;
  }

  /**
   * Sets the state of a column for either ascending or descending
   * sorting.
   * @param col a column index
       * @param asc <code>true</code> if ascending, <code>false</code> if descending
   */
  public void setState(int col, boolean asc) {
    state.clear();
    state.put(new Integer(col), new Integer(asc ? UP : DOWN));
  }
}