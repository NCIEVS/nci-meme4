/*
 * ResizableJTable.java
 */

package gov.nih.nlm.umls.jekyll.swing;

import gov.nih.nlm.swing.SortButtonRenderer;

import java.awt.FontMetrics;
import java.awt.event.MouseListener;

import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class ResizableJTable extends SortableJTable {

    private JTableHeader table_header = null;

    private TableSorter table_sorter = null;
    
    // ========================
    // Constructors
    //  ========================
    
    public ResizableJTable() {
        super();
        initValues();
     }

    public ResizableJTable(TableModel tm) {
        super(tm);
        initValues();
    }

    public void tableChanged(TableModelEvent e) {
        int type = e.getType();

        super.tableChanged(e);     
        /* Soma Lanka: Make sure to resize the comments only when loading (inserting) the data
         * 
         */
        if (type == 1)
         resizeColumns();
    }

    public void setSortState(int col, boolean asc) {
        if (col != -1) {
            SortButtonRenderer renderer = (SortButtonRenderer) table_header
                    .getDefaultRenderer();
            renderer.setState(col, asc);
            table_sorter.sortByColumn(col, asc);
            table_header.resizeAndRepaint();
        } else {
            // 	    table_header.setDefaultRenderer(new SortButtonRenderer());
            MouseListener[] listeners = table_header.getMouseListeners();
            table_header.removeMouseListener(listeners[listeners.length - 1]);

            table_sorter.addMouseListenerToHeaderInTable(this);
            this.resetSort();
        }
    }

    // ========================
    // Private Methods
    //  ========================
    
    private void initValues() {
        table_sorter = (TableSorter) this.getModel();
        table_header = this.getTableHeader();
    }

    private void resizeColumns() {
        try {
            if (getFont() == null) {
                return;
            }

            TableColumn tc = null;
            FontMetrics fm = getFontMetrics(getFont());
            int cw = 0;
            for (int i = 0; i < getColumnCount(); i++) {
                // 		tc = getColumn(getColumnName(i));
                tc = columnModel.getColumn(i);

                cw = determineColumnWidth(tc, fm);
                tc.setPreferredWidth(cw);
               /*
                * Soma Lanka: Set the minimum width to 75 pixels. The users can reduce the column width
                * upto 20 pixels. This will effect all the screens. Problem: on sorting the column
                * the tableChanged method will be called and resets the column width to preferred width.
                * may have to modify the the way sorting is handled to not change the column width.
                */
                if (cw < 20) {
                	tc.setMinWidth(cw);
                } else {
                	tc.setMinWidth(20);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    } // resizeColumns()

    private int determineColumnWidth(TableColumn col, FontMetrics fm) {
        int headerWidth = fm.stringWidth((String) col.getHeaderValue());
        int columnNumber = col.getModelIndex();
        int max = headerWidth;
        int columnWidth = 0;
        String cell = "";
        for (int i = 0; i < dataModel.getRowCount(); i++) {
            cell = dataModel.getValueAt(i, columnNumber).toString();
            columnWidth = fm.stringWidth(cell) + 5;
            if (columnWidth > max) {
                max = columnWidth;
            }
        }

        return max + 25;
    } // determineColumnWidth()
}