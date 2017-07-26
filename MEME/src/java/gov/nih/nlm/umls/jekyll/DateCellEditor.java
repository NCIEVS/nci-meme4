/**
 * DateCellEditor.java
 */

package gov.nih.nlm.umls.jekyll;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;

/**
 * @see <a href="src/DateCellEditor.java.html">source </a>
 */
public class DateCellEditor extends DefaultCellEditor {
    private JTextField tf = null;

    // Date format
    private SimpleDateFormat dateFormat = new SimpleDateFormat(
            "EEE MMM dd HH:mm:ss z yyyy");

    public DateCellEditor(JTextField textField) {
        super(textField);
        tf = textField;
    }

    //Override DefaultCellEditor's getCellEditorValue method
    //to return a Date, not a String.
    public Object getCellEditorValue() {
        try {
            return dateFormat.parse(tf.getText());
        } catch (Exception e) {
            e.printStackTrace(JekyllKit.getLogWriter());
            return new Date();
        }
    }
}