/*
 * AttributesFrame.java
 *  * Modfied: Soma Lanka : Changes related to Attribute printing
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularChangeAttributeAction;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;

import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.exception.StaleDataException;
import gov.nih.nlm.swing.DecreaseFontAction;
import gov.nih.nlm.swing.FontSizeManager;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.swing.IncreaseFontAction;
import gov.nih.nlm.umls.jekyll.swing.EditableTableModel;
import gov.nih.nlm.umls.jekyll.swing.ResizableJTable;
import gov.nih.nlm.util.HTMLDocumentRenderer;
import gov.nih.nlm.util.SystemToolkit;
import gov.nih.nlm.umls.jekyll.swing.TestReportFrame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.SocketException;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import samples.accessory.StringGridBagLayout;

/**
 * Window for displaying and editing various concept attributes.
 * 
 * TODO:
 * 
 * "Atom Id" column may not be sorted correctly since atom_ids populated into
 * table model as <code>String</code> s rather than <code>Integer</code>s.
 * This is done in order to display an empty string in case when there is no
 * atom_id for an attribute.
 * 
 * @see <a href="src/AttributesFrame.java.html">source </a>
 * @see <a href="src/AtomNotesFrame.properties.html">properties </a>
 * @author Vladimir Olenichev
 */
public class AttributesFrame extends JFrame implements Refreshable,
        JekyllConstants {
	 static private double TITLE_SCALE = 1.5;
    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // integer values for columns
    private final int TABLE_LEVEL_COLUMN = 0;

    private final int TABLE_ATOM_ID_COLUMN = 1;

    private final int TABLE_ATTRIBUTE_NAME_COLUMN = 2;

    private final int TABLE_ATTRIBUTE_VALUE_COLUMN = 3;

    private final int TABLE_SOURCE_COLUMN = 4;

    private final int TABLE_STATUS_COLUMN = 5;

    private final int TABLE_TBR_COLUMN = 6;
    private JTable _printItem;

	private String Footer = null;

	private String Header = null;

    // Various components
    private GlassComponent glass_comp = null;

    private JTextField conceptNameTF = null;

    private JTextField conceptIdTF = null;

    private JTextField targetConceptIdTF = null;

    private JTextField countTF = null;
    
    final HTMLDocumentRenderer hdr = new HTMLDocumentRenderer();
    private EditableTableModel attrsModel = null;

    private ResizableJTable attrsTable = null;

    // Fields
    CloseAction close_action = new CloseAction(this);
    PrintAction print_action = new PrintAction(this);
    private Concept current_concept = null;

    private Vector listOfAttrs = new Vector();

    /**
     * Default constructor.
     */
    public AttributesFrame() {
        this.setName("AttributesFrame");
        initResources();
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle
                .getBundle("bundles.AttributesFrameResources");
    }

    private void initComponents() {
        Box b = null;
        String columnName = null;
        TableColumn column = null;
        JScrollPane sp = null;

        setTitle(resources.getString("window.title"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        // set properties on this frame
        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);

        // concept name textfield
        conceptNameTF = GUIToolkit.getNonEditField();

        // current concept_id textfield
        conceptIdTF = new JTextField(10);
        conceptIdTF.setEditable(false);
        conceptIdTF.setBackground(LABEL_BKG);
        conceptIdTF.setMinimumSize(conceptIdTF.getPreferredSize());
        conceptIdTF.setMaximumSize(conceptIdTF.getPreferredSize());

        // "Close" button
        JButton closeButton = GUIToolkit.getButton(close_action);

        // box container
        b = Box.createHorizontalBox();
        b.add(conceptNameTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(conceptIdTF);
        b.add(Box.createHorizontalStrut(12));
        b.add(closeButton);

        contents
                .add(
                        "gridx=0,gridy=0,gridwidth=2,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        // label for target concept_id textfield
        JLabel targetConceptIdLabel = new JLabel();
        targetConceptIdLabel.setText(resources
                .getString("targetConceptId.label"));

        // target concept_id textfield
        targetConceptIdTF = new JTextField(10);
        targetConceptIdTF.setMinimumSize(conceptIdTF.getPreferredSize());
        targetConceptIdTF.setEnabled(false);

        // box container
        b = Box.createHorizontalBox();
        b.add(targetConceptIdLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(targetConceptIdTF);

        contents.add(
                "gridx=0,gridy=1,fill=NONE,anchor=WEST,insets=[12,12,0,0]", b);

        // label for count textfield
        JLabel countLabel = new JLabel();
        countLabel.setText(resources.getString("count.label"));

        // count textfield
        countTF = new JTextField(5);
        countTF.setEditable(false);
        countTF.setForeground(LABEL_FG);
        countTF.setMinimumSize(countTF.getPreferredSize());
        countTF.setText(resources.getString("countTextField.text"));
//      "Print" button

        JButton printButton = GUIToolkit.getButton(print_action);

        // box container
        b = Box.createHorizontalBox();
        b.add(countLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(countTF);
        b.add(printButton);
        contents.add(
                "gridx=1,gridy=1,fill=NONE,anchor=EAST,insets=[12,12,0,11]", b);

        attrsModel = new EditableTableModel();
        attrsModel.setColumnCount(7);
        attrsModel.setRowCount(0);
        attrsTable = GUIToolkit.getTable(attrsModel);

        // table columns settings
        columnName = attrsTable.getColumnName(TABLE_LEVEL_COLUMN);
        column = attrsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("attrsTable.level.label"));

        columnName = attrsTable.getColumnName(TABLE_ATOM_ID_COLUMN);
        column = attrsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("attrsTable.atomId.label"));

        columnName = attrsTable.getColumnName(TABLE_ATTRIBUTE_NAME_COLUMN);
        column = attrsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("attrsTable.attributeName.label"));

        columnName = attrsTable.getColumnName(TABLE_ATTRIBUTE_VALUE_COLUMN);
        column = attrsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("attrsTable.attributeValue.label"));

        columnName = attrsTable.getColumnName(TABLE_SOURCE_COLUMN);
        column = attrsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("attrsTable.source.label"));

        columnName = attrsTable.getColumnName(TABLE_STATUS_COLUMN);
        column = attrsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("attrsTable.status.label"));
        column.setIdentifier(StringTableCellRenderer.STATUS_IDENTIFIER);

        // making TBR column invisible
        columnName = attrsTable.getColumnName(TABLE_TBR_COLUMN);
        column = attrsTable.getColumn(columnName);
        attrsTable.removeColumn(column);

        attrsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTextField tf = new JTextField();
        tf.setEditable(false);
        attrsTable.setDefaultEditor(String.class, new DefaultCellEditor(tf));
        StringTableCellRenderer renderer = new StringTableCellRenderer(
                attrsTable, attrsModel);
        renderer.setColumnIndex(StringTableCellRenderer.TBR_IDENTIFIER,
                TABLE_TBR_COLUMN);
        attrsTable.setDefaultRenderer(String.class, renderer);
        sp = new JScrollPane(attrsTable);
        contents
        .add(
                "gridx=0,gridy=2,gridwidth=2,fill=BOTH,anchor=CENTER,weightx=1.0,weighty=1.0,insets=[12,12,12,11]",
                sp);
        
        // adding a menu       
        setJMenuBar(buildMenuBar());
    } // initComponents()

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = null;
        JMenu submenu = null;
        JMenuItem item = null;

        // File
        menu = new JMenu();
        menu.setText(resources.getString("fileMenu.label"));
        menu.setMnemonic(resources.getString("fileMenu.mnemonic").charAt(0));

        // file->close
        item = new JMenuItem(close_action);
        menu.add(item);

        menuBar.add(menu);

        // Edit
        menu = new JMenu();
        menu.setText(resources.getString("editMenu.label"));
        menu.setMnemonic(resources.getString("editMenu.mnemonic").charAt(0));

        // edit->move
        item = new JMenuItem("Move selected attributes");
        item.setEnabled(false);
        menu.add(item);

        // edit->delete
        item = new JMenuItem(new DeleteAttrsAction(this));
        menu.add(item);

        // New Rel submenu
        submenu = new JMenu("Status");

        item = new JMenuItem("Reviewed");
        item.setEnabled(false);
        submenu.add(item);

        item = new JMenuItem("Unreviewed");
        item.setEnabled(false);
        submenu.add(item);

        item = new JMenuItem("Needs review");
        item.setEnabled(false);
        submenu.add(item);

        item = new JMenuItem("Suggested");
        item.setEnabled(false);
        submenu.add(item);

        menu.add(submenu);

        menuBar.add(menu);

        // Options
        menu = new JMenu();
        menu.setText(resources.getString("optionsMenu.label"));
        menu.setMnemonic(resources.getString("optionsMenu.mnemonic").charAt(0));

        // options->increase font
        item = new JMenuItem(new IncreaseFontAction());
        menu.add(item);

        // options->decrease font
        item = new JMenuItem(new DecreaseFontAction());
        menu.add(item);

        menuBar.add(menu);

        return menuBar;
    } // buildMenuBar()

    // clears content
    private void clearContent() {
        conceptNameTF.setText(null);
        conceptIdTF.setText(null);
        attrsTable.clearSelection();
        attrsModel.getDataVector().clear();
        attrsModel.fireTableRowsInserted(0, 1);
        attrsModel.fireTableDataChanged();
        listOfAttrs.clear();
    }

    /**
     * Sets the content of this window.
     */
    public void setContent(Concept concept) {
        ActionLogger logger = new ActionLogger(this.getClass().getName() + ".setContent()",
                true);

        try {
            current_concept = concept;

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            clearContent();

            conceptNameTF.setText(current_concept.getPreferredAtom()
                    .getString()); // concept name
            conceptIdTF.setText(current_concept.getIdentifier().toString()); // concept_id

            Attribute[] attrs = current_concept.getAttributes();

            Vector dataVector = new Vector();
            for (int i = 0; i < attrs.length; i++) {
                Vector row = new Vector();

                row.add(String.valueOf(attrs[i].getLevel())); // level
                Atom atom = attrs[i].getAtom();
                row.add((atom == null) ? "" : atom.getIdentifier().toString()); // atom_id
                row.add(attrs[i].getName()); // attribute name
                row.add(attrs[i].getValue()); // attribute value
                row.add(attrs[i].getSource().toString()); // source
                row.add(String.valueOf(attrs[i].getStatus())); // status
                row.add(String.valueOf(attrs[i].getTobereleased())); // tbr

                dataVector.add(row);

                listOfAttrs.add(attrs[i]);
            }

            for (Iterator i = dataVector.iterator(); i.hasNext();) {
                attrsModel.addRow((Vector) i.next());
            }

            countTF.setText(String.valueOf(attrs.length));
        } catch (Exception ex) {
            MEMEToolkit
                    .notifyUser(
                            this,
                            "Failed to load attributes for concept: "
                                    + ((current_concept == null) ? " "
                                            : current_concept.getIdentifier()
                                                    .toString())
                                    + "\nConsole/Log file may contain additional information.");
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logger.logElapsedTime();
        }
    } // setContent()

    public Dimension getPreferredSize() {
        return new Dimension(900, 450);
    }

    // --------------------------------
    // Inner Classes
    // --------------------------------
    class PrintAction extends AbstractAction {
    	//
    	//    	 Private Fields
    	//
    	private Component target = null;
   
    	// Constructor

    	public PrintAction(Component comp) {
    		putValue(Action.NAME, "Print");
    		putValue(Action.SHORT_DESCRIPTION, "Print sorted Attributes");
    		putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
    		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,Event.CTRL_MASK));
    		putValue("Background", Color.orange);
    		target = comp;

    	}
    	public void actionPerformed(ActionEvent e){
    		try {
    		     attrsTable.setSortState(2,true);
    		     printTable();
    		   } catch (Exception ex) {
    				MEMEToolkit.notifyUser( glass_comp,ex.toString());
    			}
    	}
    }
    /**
     * Deletes selected attribute(s).
     * 
     * @see AbstractAction
     */
    class DeleteAttrsAction extends AbstractAction {

        private Component target = null;

        public DeleteAttrsAction(Component comp) {
            putValue(Action.NAME, "Delete selected attributes");
            putValue(Action.SHORT_DESCRIPTION, "delete selected attribute(s)");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (attrsTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target, "There are no selected row(s).");
                return;
            }

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(DeleteAttrsAction.this
                            .getClass().getName(), true);

                    try {
                        boolean source_level = false;
                        boolean need_refresh = false;

                        int[] selected_rows = attrsTable.getSelectedRows();

                        for (int i = 0; i < selected_rows.length; i++) {
                            Attribute attr = (Attribute) listOfAttrs
                                    .get(attrsTable.mapIndex(selected_rows[i]));

                            if (attr.getLevel() == SOURCE_LEVEL) {
                                source_level = true;
                                continue;
                            }

                            MolecularDeleteAttributeAction mcra = new MolecularDeleteAttributeAction(
                                    attr);
                            JekyllKit.getDefaultActionClient().processAction(
                                    mcra);

                            MEMEToolkit.logComment("Attribute "
                                    + attr.getName() + " with value "
                                    + attr.getValue()
                                    + " has successfully been deleted.", true);
                            need_refresh = true;

                            if (i == 0) {
                                current_concept
                                        .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
                            }
                        } // i loop

                        if (source_level) {
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "You have attempted to delete one\n"
                                                    + "or more source level attributes.\n"
                                                    + "This is not allowed. The status for these\n"
                                                    + "attributes will remain unchanged.");
                        }

                        // refresh
                        if (need_refresh) {
                            JekyllKit.getConceptSelector().refreshConcept();

                        }
                    } catch (StaleDataException sde) {
                        try {
                            setContent(JekyllKit.getCoreDataClient()
                                    .getConcept(current_concept));
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "You have attempted to perform an action on a"
                                                    + "\nconcept that has changed since it was last read."
                                                    + "\nThe concept was automatically re-read for you."
                                                    + "\nPlease try the intended action again.");
                        } catch (Exception ex) {
                            if (ex instanceof MissingDataException) {
                                MEMEToolkit.notifyUser(target, "Concept "
                                        + current_concept.getIdentifier()
                                                .toString() + " is no longer"
                                        + "\na valid concept in the database.");
                            } else {
                                ex.printStackTrace(JekyllKit.getLogWriter());
                            }
                        }
                    } catch (Exception ex) {
                        if (ex instanceof MEMEException
                                && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                            MEMEToolkit.reportError(target,
                                    "There was a network error."
                                            + "\nPlease try the action again.",
                                    false);
                        } else {
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "Failed to delete attribute(s)."
                                                    + "\nConsole/Log file may contain more information.");
                        }
                        ex.printStackTrace(JekyllKit.getLogWriter());
                    } finally {
                        glass_comp.setVisible(false);
                        target.setCursor(Cursor
                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                        logger.logElapsedTime();
                    }
                }
            });

            t.start();
        } // actionPerformed()

        public boolean isEnabled() {
            // 	    if (JekyllKit.getEditorLevel() == 5) {
            // 		return true;
            // 	    } else {
            // 		return false;
            // 	    }

            return false;
        }

    } // DeleteAttrsAction

    /**
     * Changes status for selected attribute(s).
     * 
     * @see AbstractAction
     */
    class ChangeStatusAction extends AbstractAction {
        private Component target = null;

        private String action_cmd = null;

        public ChangeStatusAction(Component comp) {
            putValue(Action.NAME, "Status");
            putValue(Action.SHORT_DESCRIPTION,
                    "change status for selected attribute(s)");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (attrsTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target, "There are no selected row(s).");
                return;
            }

            action_cmd = e.getActionCommand();

            JekyllKit.enableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(ChangeStatusAction.this
                            .getClass().getName(), true);
                    try {
                        char status;

                        if (action_cmd.equals(String
                                .valueOf(CoreData.FV_STATUS_REVIEWED))) {
                            status = CoreData.FV_STATUS_REVIEWED;
                        } else {
                            status = CoreData.FV_STATUS_NEEDS_REVIEW;
                        }

                        int[] selected_rows = attrsTable.getSelectedRows();

                        for (int i = 0; i < selected_rows.length; i++) {
                            Attribute attr = (Attribute) listOfAttrs
                                    .get(attrsTable.mapIndex(selected_rows[i]));

                            attr.setStatus(status);

                            MolecularChangeAttributeAction mcaa = new MolecularChangeAttributeAction(
                                    attr);
                            JekyllKit.getDefaultActionClient().processAction(
                                    mcaa);

                            MEMEToolkit.logComment(
                                    "Status has been successfully changed for attribute "
                                            + attr.getName()
                                            + " with the value: "
                                            + attr.getValue(), true);
                        }

                        JekyllKit.getConceptSelector().refreshConcept();
                    } catch (StaleDataException sde) {
                        try {
                            setContent(JekyllKit.getCoreDataClient()
                                    .getConcept(current_concept));
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "You have attempted to perform an action on a"
                                                    + "\nconcept that has changed since it was last read."
                                                    + "\nThe concept was automatically re-read for you."
                                                    + "\nPlease try the intended action again.");
                        } catch (Exception ex) {
                            if (ex instanceof MissingDataException) {
                                MEMEToolkit.notifyUser(target, "Concept "
                                        + current_concept.getIdentifier()
                                                .toString() + " is no longer"
                                        + "\na valid concept in the database.");

                            } else {
                                ex.printStackTrace(JekyllKit.getLogWriter());
                            }
                        }
                    } catch (Exception ex) {
                        if (ex instanceof MEMEException
                                && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                            MEMEToolkit.reportError(target,
                                    "There was a network error."
                                            + "\nPlease try the action again.",
                                    false);
                        } else {
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "Failed to change status of attribute(s)."
                                                    + "\nConsole/Log file may contain more information.");
                        }
                        ex.printStackTrace(JekyllKit.getLogWriter());
                    } finally {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                glass_comp.setVisible(false);
                                target
                                        .setCursor(Cursor
                                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                                JekyllKit.enableFrames();
                            }
                        });

                        logger.logElapsedTime();
                    }
                }
            });

            t.start();
        }
    } // ChangeStatusAction

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            attrsTable.setSortState(-1, false);
        }
    }
    
    

	private void printTable() {
		final StringBuffer strbuf = new StringBuffer();
		strbuf.setLength(0);
	    strbuf.append("<table width=\"800\"><tr><td align=\"center\"><B> <FONT SIZE=\"30\"> Concept ID " + conceptIdTF.getText() + "</FONT></B></TD></TR></TABLE>" );
	    strbuf.append("<p>");
	    strbuf.append("<table border=\"2\" align=\"left\" width=\"800\">");
	    // Create a Header Row
	    strbuf.append("<tr>");
	    int colcount = attrsTable.getColumnCount();
	    int j=0;
	    String width;
		for (int i=0; i < colcount; i++) {
			
			if (i == 3) {
				width = "200";
			} else {
				width = "150";
			}
			strbuf.append("<td align=\"center\" colspan=\"1\" width=\"" + width + "\">");
			strbuf.append("<B><FONT SIZE=\"4\">" + attrsTable.getColumnModel().getColumn(i).getHeaderValue() + "</FONT></B></td>");
		}
		strbuf.append("</tr>");
	    for (int row = 0; row < attrsTable.getRowCount(); row++) {
	    	if (attrsTable.getValueAt(row,2)!= null &&
	    			attrsTable.getValueAt(row,2).toString() != null
	    			&& !attrsTable.getValueAt(row,2).toString().trim().equalsIgnoreCase("CONTEXT")) {
		    	strbuf.append("<tr>");
		    		for (int col = 0; col < attrsTable.getColumnCount(); col++) {
						if (col == 3) {
							width = "200";
						} else {
							width = "150";
						}
						strbuf.append("<td align=\"left\" colspan=\"1\" style=\"WORD-BREAK:BREAK-ALL;\" width=\"" + width + "\">");
						Object temp = attrsTable.getValueAt(row, col);
						if (temp == null) {
							strbuf.append(" ");
						}
						else {
							strbuf.append( "<FONT SIZE=\"2\">" + temp.toString() + "</FONT>");
						}
						strbuf.append("</td>");
					} 
					strbuf.append("</tr>");
	    	}
		}
	    strbuf.append("</table>");
	    
	    Thread t = new Thread(new Runnable() {
            public void run() {
                TestReportFrame tr_frame = null;
                String stylesText = "table.color1 {";
                stylesText += " color: #ffffff;";
                stylesText += " background-color: #435772;";
                stylesText += "}";
                stylesText += "td.color2 {";
                stylesText += " color: #ffffff;";
                stylesText += " background-color: #cc3300;";
                stylesText += "}";
                StyleSheet styles = new StyleSheet();
                styles.addRule(stylesText);

                HTMLDocument doc = new HTMLDocument();
                doc.setParser(new javax.swing.text.html.parser.ParserDelegator());
                try {
                    tr_frame = JekyllKit.getReportFrame();
                    tr_frame.enableGlassPane();
                    hdr.setOnePagePerSheet();
                    hdr.setTitle(conceptIdTF.getText());
                    JEditorPane editor = new JEditorPane();
                    editor.setContentType("text/html");
                    //editor.setDocument(doc);
                    editor.setText(
                            "<html><head><style> a {text-decoration: none; }</style></head><body>" +
                            strbuf.toString()
                            + "</body></html>");
                    editor
                            .setText("<html><head></head><body>"
                                    + strbuf.toString()
                                    + "</body></html>");
                    //HTMLDocument doc = (HTMLDocument)editor.getDocument();
                    hdr.print(editor);
                    tr_frame.disableGlassPane();     
                } catch (Exception e) {
                    if (tr_frame != null) {
                        tr_frame.disableGlassPane();
                    }
                    MEMEToolkit.handleError(e);
                }
                ;
            }
        });
        t.start();
	}
}