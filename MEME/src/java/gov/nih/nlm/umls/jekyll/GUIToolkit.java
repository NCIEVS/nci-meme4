/*
 * GUIToolkit.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.exception.IntegrityViolationException;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.umls.jekyll.swing.ResizableJTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Collection of frequently used components.
 * 
 * @see <a href="src/GUIToolkit.java.html">source </a>
 */
public class GUIToolkit {

    // Constants

    /**
     * Border for rendering table cells.
     */
    public static final Border EMPTY_BORDER = new EmptyBorder(50, 0, 50, 0);

    /**
     * Light brown color.
     */
    public static final Color LIGHT_BROWN = new Color(204, 153, 051);

    /**
     * Font used for buttons.
     */
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.PLAIN, 14);

    /**
     * Font used for table data and text areas.
     */
    protected static final Font DIALOG_FONT_14 = new Font("Dialog", Font.PLAIN,
            14);

    protected static final Font DIALOG_FONT_13 = new Font("Dialog", Font.PLAIN,
            13);

    protected static final Font DIALOG_FONT_BOLD_14 = new Font("Dialog",
            Font.BOLD, 14);

    protected static final Font ARIAL_BOLD_14 = new Font("Arial", Font.BOLD, 14);

    protected static final Font LUCIDA_SANS_BOLD_14 = new Font("Lucida Sans",
            Font.BOLD, 14);

    protected static final Font ARIAL_BOLD_16 = new Font("Arial", Font.BOLD, 16);

    /**
     * Gets a button configured using the specified action.
     * 
     * @param a
     *                  <code>Action</code> object to use
     * @return a <code>JButton</code>
     */
    public static JButton getButton(Action a) {
        return new Button(a);
    }

    public static JLabel getLabel() {
        JLabel label = new JLabel();
        label.setFont(LUCIDA_SANS_BOLD_14);
        return label;
    }

    public static JLabel getHeaderLabel() {
        JLabel label = new JLabel();
        label.setFont(ARIAL_BOLD_16);
        return label;
    }

    public static JTextField getField(int columns) {
        JTextField field = new JTextField(columns);
        return field;
    }

    public static JTextField getNonEditField() {
        JTextField textfield = new JTextField() {
            public void setText(String t) {
                super.setText(t);
                setCaretPosition(0);
            }
        };
        textfield.setEditable(false);
        textfield.setBackground(Color.YELLOW);

        return textfield;
    }

    public static void showIntegrityViolationDialog(Component parentComponent,
            IntegrityViolationException ex) {
        IntegrityCheck[] warnings = ex.getViolationsVector().getWarnings()
                .getChecks();
        IntegrityCheck[] errors = ex.getViolationsVector().getViolations()
                .getChecks();

        StringBuffer sb = new StringBuffer(500);

        if (errors.length == 0) {
            sb.append(ex.getMessage());
            sb.append("\nPossible cause(s):\n");
            for (int i = 0; i < warnings.length; i++) {
                sb.append("Check name: " + warnings[i].getName());
                sb.append("\n\t" + warnings[i].getShortDescription());
                sb.append("\n\n");
            }

            JOptionPane.showMessageDialog(parentComponent, sb.toString(),
                    "Integrity Failure", JOptionPane.WARNING_MESSAGE);
        } else if (warnings.length == 0) {
            sb.append(ex.getMessage());
            sb.append("\nPossible cause(s):\n");
            for (int i = 0; i < errors.length; i++) {
                sb.append("Check name: " + errors[i].getName());
                sb.append("\n\t" + errors[i].getShortDescription());
                sb.append("\n\n");
            }

            JOptionPane.showMessageDialog(parentComponent, sb.toString(),
                    "Integrity Failure", JOptionPane.ERROR_MESSAGE);
        } else {
            sb.append(ex.getMessage());
            sb.append("\nPossible cause(s):\n");
            for (int i = 0; i < errors.length; i++) {
                sb.append("Check name: " + errors[i].getName());
                sb.append("\n\t" + errors[i].getShortDescription());
                sb.append("\n\n");
            }

            for (int i = 0; i < warnings.length; i++) {
                sb.append("Check name: " + warnings[i].getName());
                sb.append("\n\t" + warnings[i].getShortDescription());
                sb.append("\n\n");
            }

            JOptionPane.showMessageDialog(parentComponent, sb.toString(),
                    "Integrity Failure", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static class NonEditTextArea extends JTextArea {

        /**
         * Default constructor.
         */
        public NonEditTextArea(boolean wrap) {
            super();

            setFont(DIALOG_FONT_14);
            setEditable(false);

            setLineWrap(wrap);
            setWrapStyleWord(wrap);
        }
    }

    public static ResizableJTable getTable(TableModel model) {
        ResizableJTable table = new ResizableJTable(model){
        	/* 
             * Soma Lanka:  Adding a tool tip to the existing getBoldTable Method.
             * Did not changed the original method as we want to provide the tool tip 
             * functionality to only few screens. 
            */
            public String getToolTipText(MouseEvent e) {
            	String tip = null;
            	java.awt.Point p = e.getPoint();
            	int rowIndex = rowAtPoint(p);
            	int colIndex = columnAtPoint(p);
            	colIndex = convertColumnIndexToModel(colIndex);
            	
            	javax.swing.table.TableColumnModel colMod = getColumnModel();
            	javax.swing.table.TableColumn colName = colMod.getColumn(colIndex);
            	FontMetrics fm = getFontMetrics(getFont());
            	//if (colIndex == 1)
            	  Object obj = getValueAt(rowIndex,colIndex);
            	  if (fm.stringWidth((String)obj) + 5 > colName.getPreferredWidth())  
            		tip = (String)obj; 
            	return tip;
            }
        };
        table.setFont(DIALOG_FONT_14);
        table.setAutoscrolls(false);
        //    int row_height = table.getFontMetrics(table.getFont()).getHeight();
        //    table.setRowHeight(row_height - 1);

        return table;
    }
    
    /**
     * With extra space between rows.
     */
    public static ResizableJTable getBoldTable(TableModel model) {
        ResizableJTable table = new ResizableJTable(model) {

            public void setRowHeight(int rowHeight) {
                super.setRowHeight(rowHeight);
                TableModel this_model = this.getModel();
                ((AbstractTableModel) this_model).fireTableRowsInserted(0, 1);
                ((AbstractTableModel) this_model).fireTableDataChanged();
            }
            /* 
             * Soma Lanka:  Adding a tool tip to the existing getBoldTable Method.
             * Did not changed the original method as we want to provide the tool tip 
             * functionality to only few screens. 
            */
            public String getToolTipText(MouseEvent e) {
            	String tip = null;
            	java.awt.Point p = e.getPoint();
            	int rowIndex = rowAtPoint(p);
            	int colIndex = columnAtPoint(p);
            	colIndex = convertColumnIndexToModel(colIndex);
            	
            	javax.swing.table.TableColumnModel colMod = getColumnModel();
            	javax.swing.table.TableColumn colName = colMod.getColumn(colIndex);
            	FontMetrics fm = getFontMetrics(getFont());
            	//if (colIndex == 1)
            	  Object obj = getValueAt(rowIndex,colIndex);
            	  if (fm.stringWidth((String)obj) + 5 > colName.getPreferredWidth())  
            		tip = (String)obj; 
            	return tip;
            }
        };
        table.setFont(DIALOG_FONT_BOLD_14);
        int font_height = table.getFontMetrics(table.getFont()).getHeight();
        table.setRowHeight(font_height + 5);

        table.setAutoscrolls(false);
        return table;
    }
    // -----------------------------
    // Inner Classes
    // -----------------------------

    private static class Button extends JButton {

        // Constructors
        public Button() {
            setFont(BUTTON_FONT);
        }

        public Button(Action action) {
            super(action);
            if (action.getValue("Background") != null) {
                setBackground((Color) action.getValue("Background"));
            }
            setFont(BUTTON_FONT);

            // 	    this.addMouseListener(new MouseAdapter() {
            // 		public void mouseClicked(MouseEvent e) {
            // 		    if (e.getClickCount() == 1 ) {
            // 			Button.this.fireActionPerformed(new ActionEvent(e.getSource(),
            // 										      0,
            // 										      ""));
            // 		    } else {
            // 			System.out.println("Ignoring...");
            // 		    }
            // 		}
            // 	    });
        }
    }
}