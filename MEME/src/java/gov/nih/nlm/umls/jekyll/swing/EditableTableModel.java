/*
 * @(#)EditableTableModel.java
 */

package gov.nih.nlm.umls.jekyll.swing;

import javax.swing.table.DefaultTableModel;

public class EditableTableModel extends DefaultTableModel {

    //
    // Constructors
    //
    int column = 0; 
    
    /**
     * Constructs a default <code>EditableTableModel</code> which is a table
     * of zero columns and zero rows.
     */
    public EditableTableModel() {
        super();
    }

    public EditableTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    public Class getColumnClass(int c) {
        if (c != -1) {
            column = c;
        }
        
        return getValueAt(0, column).getClass();
    }
}