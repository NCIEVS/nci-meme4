/************************************************************************
 *
 * Package:     gov.nih.nlm.umls.jekyll.swing
 * Object:      NonEditableTableModel
 *
 * Author:      Vladimir Olenichev
 *
 * Remarks:     
 *
 * Change History: 
 *
 ***********************************************************************/

package gov.nih.nlm.umls.jekyll.swing;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * <p>
 * {@link <a href="/vlad-doc/jekyll/src_files/Swing/NonEditableTableModel.java.html">Browse Source</a>}
 */
public class NonEditableTableModel extends DefaultTableModel {

    //
    // Constructors
    //
    public NonEditableTableModel() {
	super();
    }

    public NonEditableTableModel(int rowCount, int columnCount) {
	super(rowCount, columnCount);
    }

    public NonEditableTableModel(Object[][] data, Object[] columnNames) {
	super(data, columnNames);
    }

    public NonEditableTableModel(Vector data, Vector columnNames) {
	super(data, columnNames);
    }

    //
    // Public methods
    //
    public Class getColumnClass(int c) {
	return getValueAt(0, c).getClass();
    }

    // Making all cells are non-editable
    public boolean isCellEditable(int row, int col) {
	return false;
    }
}
