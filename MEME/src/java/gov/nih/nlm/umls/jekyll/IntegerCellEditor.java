/**
 * IntegerCellEditor.java
 */

package gov.nih.nlm.umls.jekyll;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;

/**
 * @see <a href="src/IntegerCellEditor.java.html">source </a>
 */
public class IntegerCellEditor extends DefaultCellEditor {
    private JTextField tf = null;

    public IntegerCellEditor(JTextField textField) {
        super(textField);
        tf = textField;
    }

    //Override DefaultCellEditor's getCellEditorValue method
    //to return an Integer, not a String.
    public Object getCellEditorValue() {
        try {
            return Integer.valueOf(tf.getText());
        } catch (Exception e) {
            e.printStackTrace(JekyllKit.getLogWriter());
            return new Integer(0);
        }
    }
}