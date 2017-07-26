/*
 * STYEditor.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularChangeAttributeAction;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.exception.StaleDataException;
import gov.nih.nlm.swing.DecreaseFontAction;
import gov.nih.nlm.swing.FontSizeManager;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.swing.IncreaseFontAction;
import gov.nih.nlm.umls.jekyll.swing.EditableTableModel;
import gov.nih.nlm.umls.jekyll.swing.ResizableJTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.SocketException;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import samples.accessory.StringGridBagLayout;

/**
 * The screen displays semantic type(s) for current concept. Here, user can add,
 * delete or replace semantic type(s).
 * 
 * @see <a href="src/STYEditor.java.html">source </a>
 * @see <a href="src/STYEditorResources.properties.html">properties </a>
 */
public class STYEditor extends JFrame implements JekyllConstants, Refreshable,
        StateChangeListener {

    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // integer values for columns
    private final int TABLE_NAME_COLUMN = 0;

    private final int TABLE_STATUS_COLUMN = 1;

    private final int TABLE_AUTHORITY_COLUMN = 2;

    private final int TABLE_AUTHORIZED_ON_COLUMN = 3;

    private final String REPLACE_ALL_CMD = "replace.all.cmd";

    // Various components
    private GlassComponent glass_comp = null;

    private JTextField conceptNameTF = null;

    private JTextField conceptIdTF = null;

    private EditableTableModel stysModel = null;

    private ResizableJTable stysTable = null;

    private JTextField tuiTF = null;

    private JTextField styTF = null;

    private JTextField styNameTF = null;

    private JMenu editMenu = null;

    private JButton apprNextButton, tuiButton, styButton = null;

    // Actions
    CloseAction close_action = new CloseAction(this);

    ChangeStatusAction change_status_action = new ChangeStatusAction(this);

    AddSTYAction add_sty_action = new AddSTYAction(this);

    // Core data
    private Concept current_concept = null;

    private String semantic_type = null;

    private Vector listOfSTYs = new Vector();

    /**
     * Default constructor.
     */
    public STYEditor() {
        initResources();
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle.getBundle("bundles.STYEditorResources");
    }

    private void initComponents() {
        Action a = null;
        Box b = null;
        String columnName = null;
        TableColumn column = null;
        JScrollPane sp = null;

        setTitle(resources.getString("window.title"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        // set properties on this window
        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);

        // concept name text field
        conceptNameTF = GUIToolkit.getNonEditField();

        // concept_id text field
        conceptIdTF = new JTextField(10);
        conceptIdTF.setEditable(false);
        conceptIdTF.setBackground(LABEL_BKG);
        conceptIdTF.setMinimumSize(conceptIdTF.getPreferredSize());
        conceptIdTF.setMaximumSize(conceptIdTF.getPreferredSize());

        // box container
        b = Box.createHorizontalBox();
        b.add(conceptNameTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(conceptIdTF);

        contents
                .add(
                        "gridx=0,gridy=0,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,0]",
                        b);

        stysModel = new EditableTableModel();
        stysModel.setColumnCount(4);
        stysModel.setRowCount(0);
        stysTable = GUIToolkit.getTable(stysModel);

        // table columns settings
        columnName = stysTable.getColumnName(TABLE_NAME_COLUMN);
        column = stysTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("stysTable.name.label"));

        columnName = stysTable.getColumnName(TABLE_STATUS_COLUMN);
        column = stysTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("stysTable.status.label"));
        column.setIdentifier(StringTableCellRenderer.STATUS_IDENTIFIER);

        columnName = stysTable.getColumnName(TABLE_AUTHORITY_COLUMN);
        column = stysTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("stysTable.authority.label"));

        columnName = stysTable.getColumnName(TABLE_AUTHORIZED_ON_COLUMN);
        column = stysTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("stysTable.authorizedOn.label"));

        stysTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTextField tf = new JTextField();
        tf.setEditable(false);
        stysTable.setDefaultEditor(String.class, new DefaultCellEditor(tf));
        stysTable.setDefaultEditor(Date.class, new DateCellEditor(tf));
        StringTableCellRenderer renderer = new StringTableCellRenderer(
                stysTable, stysModel);
        stysTable.setDefaultRenderer(String.class, renderer);
        stysTable.setDefaultRenderer(Date.class, renderer);
        // 	stysTable.getSelectionModel().addListSelectionListener(new
        // ListSelectionListener() {
        // 		public void valueChanged(ListSelectionEvent e) {
        // 		    if (e.getValueIsAdjusting() ||
        // 			stysTable.getSelectionModel().isSelectionEmpty()) return;

        // 		    String name = (String)
        // stysModel.getValueAt(stysTable.mapIndex(stysTable.getSelectedRow()),
        // TABLE_NAME_COLUMN);
        // 		    styNameTF.setText(name);
        // 			}
        // 	    });
        sp = new JScrollPane(stysTable);

        contents
                .add(
                        "gridx=0,gridy=1,fill=BOTH,anchor=CENTER,weightx=1.0,weighty=1.0,insets=[12,12,12,0]",
                        sp);

        // "Close" button
        JButton closeButton = new JButton(close_action);
        closeButton.setFont(BUTTON_FONT);
        closeButton.setBackground((Color) close_action.getValue("Background"));

        // "Approve/Next" button
        a = new ApproveNextAction(this);
        apprNextButton = new JButton(a);
        apprNextButton.setFont(BUTTON_FONT);
        apprNextButton.setBackground((Color) a.getValue("Background"));

        // "Not Approve/Next" button
        a = new NotApproveNextAction(this);
        JButton notApprButton = new JButton(a);
        notApprButton.setFont(BUTTON_FONT);
        notApprButton.setBackground((Color) a.getValue("Background"));

        // "Show All" button
        a = new ShowAllAction(this);
        JButton showAllButton = new JButton(a);
        showAllButton.setFont(BUTTON_FONT);
        showAllButton.setBackground((Color) a.getValue("Background"));

        // "DEF" button
        a = new STYDefAction(this);
        JButton defButton = new JButton(a);
        defButton.setFont(BUTTON_FONT);
        defButton.setBackground((Color) a.getValue("Background"));

        Vector buttons = new Vector();
        buttons.add(closeButton);
        buttons.add(apprNextButton);
        buttons.add(notApprButton);
        buttons.add(showAllButton);
        buttons.add(defButton);
        gov.nih.nlm.umls.jekyll.swing.SwingUtilities
                .equalizeComponentSizes(buttons);
        buttons.clear();

        // box container
        b = Box.createVerticalBox();
        b.add(closeButton);
        b.add(Box.createVerticalStrut(5));
        b.add(apprNextButton);
        b.add(Box.createVerticalStrut(5));
        b.add(notApprButton);
        b.add(Box.createVerticalStrut(5));
        b.add(showAllButton);
        b.add(Box.createVerticalStrut(5));
        b.add(defButton);

        contents
                .add(
                        "gridx=1,gridy=0,gridheight=2,fill=NONE,anchor=CENTER,insets=[12,12,0,11]",
                        b);

        // "TUI" button
        tuiButton = new JButton(add_sty_action);
        tuiButton.setText(resources.getString("tuiButton.label"));
        tuiButton.setFont(BUTTON_FONT);
        tuiButton.setBackground((Color) a.getValue("Background"));

        // TUI text field
        tuiTF = new JTextField(10);
        tuiTF.setMinimumSize(conceptIdTF.getPreferredSize());
        tuiTF.setMaximumSize(conceptIdTF.getPreferredSize());
        tuiTF.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                String string = tuiTF.getText().trim();
                if (string.equals("")) {
                    return;
                }

                boolean found = false;
                try {
                    if (!string.startsWith("T")) {
                        string = "T" + string;
                    }
                    SemanticType[] stys = JekyllKit.getAuxDataClient()
                            .getValidSemanticTypes();
                    for (int i = 0; i < stys.length; i++) {
                        String tui = stys[i].getTypeIdentifier().toString();
                        if (tui.equals(string)) {
                            tuiTF.setText(tui);
                            styTF.setText(stys[i].getValue());
                            styNameTF.setText(stys[i].getValue());
                            semantic_type = stys[i].getValue();
                            found = true;
                            break;
                        }
                    }
                } catch (Exception ex) {
                    if (ex instanceof MEMEException
                            && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                        MEMEToolkit.reportError(STYEditor.this,
                                "There was a network error."
                                        + "\nPlease try the action again.",
                                false);
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());
                }

                if (!found) {
                    tuiTF.setText(null);
                    styTF.setText(null);
                    styNameTF.setText(null);
                    semantic_type = null;
                }
            }
        });

        // "STY" button
        styButton = new JButton(add_sty_action);
        styButton.setText(resources.getString("styButton.label"));
        styButton.setFont(BUTTON_FONT);
        styButton.setBackground((Color) add_sty_action.getValue("Background"));

        // STY text field
        styTF = new JTextField();
        styTF.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                String string = styTF.getText().trim();
                if (string.equals("")) {
                    return;
                }

                boolean found = false;
                try {
                    SemanticType[] stys = JekyllKit.getAuxDataClient()
                            .getValidSemanticTypes();
                    // First check if there is a exact match otherwise go to next
                    int k = 0;
                    for (k =0; k < stys.length; k++) {
                    	if (stys[k].getValue().toLowerCase().equals(string.toLowerCase())) {
                    		tuiTF.setText(stys[k].getTypeIdentifier()
                                    .toString());
                            styTF.setText(stys[k].getValue());
                            styNameTF.setText(stys[k].getValue());
                            semantic_type = stys[k].getValue();
                            found = true;
                            break;
                    	}
                    }
                    if (!found) {
                    	for (int i = 0; i < stys.length; i++) {
                          if (stys[i].getValue().toLowerCase().startsWith(
                                string.toLowerCase())) {
                            tuiTF.setText(stys[i].getTypeIdentifier()
                                    .toString());
                            styTF.setText(stys[i].getValue());
                            styNameTF.setText(stys[i].getValue());
                            semantic_type = stys[i].getValue();
                            found = true;
                            break;
                         }
                       }
                    }
                } catch (Exception ex) {
                    if (ex instanceof MEMEException
                            && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                        MEMEToolkit.reportError(STYEditor.this,
                                "There was a network error."
                                        + "\nPlease try the action again.",
                                false);
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());
                }

                if (!found) {
                    tuiTF.setText(null);
                    styTF.setText(null);
                    styNameTF.setText(null);
                    semantic_type = null;
                }
            }
        });

        // To fill STY textfield on Enter key
        styTF.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    styTF.transferFocus();
                }
            }
        });

        // "Clear" button
        JButton clearButton = GUIToolkit.getButton(new AbstractAction() {
            {
                putValue(Action.NAME, "Clear");
                putValue(Action.SHORT_DESCRIPTION, "clear TUI and STY fields");
                putValue("Background", Color.cyan);
            }

            public void actionPerformed(ActionEvent e) {
                tuiTF.setText(null);
                styTF.setText(null);
                styNameTF.setText(null);
                semantic_type = null;
            }
        });

        // box container
        b = Box.createHorizontalBox();
        b.add(tuiButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(tuiTF);
        b.add(Box.createHorizontalStrut(12));
        b.add(styButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(styTF);
        b.add(Box.createHorizontalStrut(12));
        b.add(clearButton);

        contents
                .add(
                        "gridx=0,gridy=2,gridwidth=2,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        // STY's name text field
        styNameTF = new JTextField();
        styNameTF.setEditable(false);
        styNameTF.setBackground(Color.cyan);

        contents
                .add(
                        "gridx=0,gridy=3,gridwidth=2,fill=HORIZONTAL,anchor=WEST,insets=[12,12,12,11]",
                        styNameTF);

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
        editMenu = new JMenu();
        editMenu.setText(resources.getString("editMenu.label"));
        editMenu
                .setMnemonic(resources.getString("editMenu.mnemonic").charAt(0));

        // NOT USED
        // 	item = new JMenuItem("Untyped");
        // 	item.setEnabled(false);
        // 	editMenu.add(item);

        item = new JMenuItem(add_sty_action);
        editMenu.add(item);

        item = new JMenuItem(new DeleteSTYAction(this));
        editMenu.add(item);

        item = new JMenuItem(add_sty_action);
        item.setText(resources.getString("editMenuItem.replaceAll.label"));
        item.setActionCommand(REPLACE_ALL_CMD);
        editMenu.add(item);

        // STY Status submenu
        submenu = new JMenu();
        submenu.setText(resources.getString("statusSubMenu.label"));

        item = new JMenuItem(change_status_action);
        item.setText(resources.getString("statusSubMenuItem.reviewed.label"));
        item.setActionCommand(String.valueOf(CoreData.FV_STATUS_REVIEWED));
        submenu.add(item);

        item = new JMenuItem(change_status_action);
        item
                .setText(resources
                        .getString("statusSubMenuItem.needsReview.label"));
        item.setActionCommand(String.valueOf(CoreData.FV_STATUS_NEEDS_REVIEW));
        submenu.add(item);

        editMenu.add(submenu);

        menuBar.add(editMenu);

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

    private void clearContent() {
        conceptNameTF.setText(null);
        conceptIdTF.setText(null);
        stysTable.clearSelection();
        stysModel.getDataVector().clear();
        stysModel.fireTableRowsInserted(0, 1);
        stysModel.fireTableDataChanged();
        listOfSTYs.clear();
    }

    // -------------------------------
    // Interface implementation
    // -------------------------------

    /**
     * Implements
     * {@link StateChangeListener#stateChanged(StateChangeEvent) StateChangeListener.stateChanged(StateChangeEvent)}
     * method.
     */
    public void stateChanged(StateChangeEvent e) {
        if (e.getState().equals(StateChangeEvent.BROWSE_STATE)) {
            apprNextButton.setEnabled(false);
            tuiButton.setEnabled(false);
            styButton.setEnabled(false);
            editMenu.setEnabled(false);
        } else {
            apprNextButton.setEnabled(true);
            tuiButton.setEnabled(true);
            styButton.setEnabled(true);
            editMenu.setEnabled(true);
        }
    }

    // ---------------------------
    // Inner Classes
    // ---------------------------

    /**
     * @see AbstractAction
     */
    class ShowAllAction extends AbstractAction {

        // constructor
        public ShowAllAction(Component comp) {
            putValue(Action.NAME, "Show All");
            putValue(Action.SHORT_DESCRIPTION, "display all STYs as hieararchy");
            // 	    putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
            putValue("Background", new Color(204, 153, 051)); // light
            // brown
            // color
        }

        public void actionPerformed(ActionEvent e) {
            // code goes here
        }

        public boolean isEnabled() {
            return false;
        }

    } // ShowAllAction

    /**
     * @see AbstractAction
     */
    class STYDefAction extends AbstractAction {
        private JTextField styTF = null;

        private JTextArea defTextArea = null;

        // constructor
        public STYDefAction(Component comp) {
            putValue(Action.NAME, "DEF");
            putValue(Action.SHORT_DESCRIPTION, "display an STY's definition");
            // 	    putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
            putValue("Background", Color.green);
        }

        public void actionPerformed(ActionEvent e) {
            if (stysTable.getSelectionModel().isSelectionEmpty()) {
                return;
            }

            JDialog dialog = buildDialog();
            int index = stysTable.mapIndex(stysTable.getSelectedRow());
            styTF.setText(((ConceptSemanticType) listOfSTYs.get(index))
                    .getValue());
            defTextArea.setText(((ConceptSemanticType) listOfSTYs.get(index))
                    .getDefinition());
            dialog.pack();
            dialog.setSize(470, 335);
            dialog.setVisible(true);
        }

        private JDialog buildDialog() {
            JDialog dialog = new JDialog(STYEditor.this, false);

            dialog.setTitle("Semantic Type Definition");
            dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            // Build the contents
            Container contents = dialog.getContentPane();
            contents.setLayout(new BorderLayout());

            styTF = new JTextField();
            styTF.setEditable(false);
            styTF.setBackground(LABEL_BKG);

            defTextArea = new JTextArea();
            defTextArea.setLineWrap(true);
            defTextArea.setWrapStyleWord(true);
            defTextArea.setEditable(false);
            //JScrollPane sp = new JScrollPane(defTextArea);

            contents.add(styTF, BorderLayout.NORTH);
            contents.add(defTextArea, BorderLayout.CENTER);

            return dialog;
        } // buildDialog()

    } // STYDefAction

    /**
     * @see AbstractAction
     */
    class AddSTYAction extends AbstractAction {
        private Component target = null;

        private String action_cmd = null;

        // constructor
        public AddSTYAction(Component comp) {
            putValue(Action.NAME, "add STY");
            putValue(Action.SHORT_DESCRIPTION, "add a Semantic type");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (semantic_type == null) {
                MEMEToolkit.notifyUser(target, "Value for semantic type"
                        + "\nattribute is null.");
                return;
            }

            action_cmd = e.getActionCommand();

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(AddSTYAction.this.getClass()
                            .getName(), true);

                    try {
                        if (action_cmd.equals(REPLACE_ALL_CMD)) {
                            for (int i = 0; i < listOfSTYs.size(); i++) {
                                Attribute attr = (Attribute) listOfSTYs.get(i);

                                MolecularDeleteAttributeAction mdaa = new MolecularDeleteAttributeAction(
                                        attr);
                                JekyllKit.getDefaultActionClient()
                                        .processAction(mdaa);

                                if (i == 0) {
                                    current_concept
                                            .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
                                }
                            } // for loop
                        }

                        Attribute attr = new Attribute.Default();
                        attr.setName(Attribute.SEMANTIC_TYPE);
                        attr.setValue(semantic_type);
                        attr.setConcept(current_concept);
                        Source source = new Source.Default(JekyllKit
                                .getAuthority().toString());
                        attr.setSource(source); // SAB
                        attr.setLevel(CoreData.FV_MTH_ASSERTED); // attribute
                        // level
                        attr.setStatus(CoreData.FV_STATUS_NEEDS_REVIEW); // attribute
                        // status
                        attr.setTobereleased(CoreData.FV_RELEASABLE); // tbr
                        attr.setReleased(CoreData.FV_NOT_RELEASED); // released

                        MolecularInsertAttributeAction mcca = new MolecularInsertAttributeAction(
                                attr);
                        JekyllKit.getDefaultActionClient().processAction(mcca);

                        if (action_cmd.equals(REPLACE_ALL_CMD)) {
                            MEMEToolkit.logComment(
                                    "All semantic types have been successfully "
                                            + "replaced with the sty: "
                                            + semantic_type, true);
                        } else {
                            MEMEToolkit
                                    .logComment(
                                            "Semantic type "
                                                    + semantic_type
                                                    + " has been successfully added to the concept "
                                                    + current_concept
                                                            .getIdentifier()
                                                            .toString(), true);
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
                                            "Failed to add semantic type."
                                                    + "\nLog file/Console may contain more information.");
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
    } // AddSTYAction

    /**
     * @see AbstractAction
     */
    class DeleteSTYAction extends AbstractAction {
        private Component target = null;

        // constructor
        public DeleteSTYAction(Component comp) {
            putValue(Action.NAME, "Delete STY(s)");
            putValue(Action.SHORT_DESCRIPTION, "delete selected STY(s)");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (stysTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target, "No selected STY(s).");
                return;
            }
            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(DeleteSTYAction.this.getClass()
                            .getName(), true);

                    try {
                        int[] selected_rows = stysTable.getSelectedRows();

                        for (int i = 0; i < selected_rows.length; i++) {
                            String sty_name = (String) stysModel.getValueAt(
                                    stysTable.mapIndex(selected_rows[i]),
                                    TABLE_NAME_COLUMN);

                            // Iterate through all STYs since there can be
                            // more than one entry with the same semantic type
                            for (int k = 0; k < listOfSTYs.size(); k++) {
                                Attribute attr = (Attribute) listOfSTYs.get(k);

                                if (!sty_name.equals(attr.getValue())) {
                                    continue;
                                }

                                MolecularDeleteAttributeAction mdaa = new MolecularDeleteAttributeAction(
                                        attr);
                                JekyllKit.getDefaultActionClient()
                                        .processAction(mdaa);

                                if (k == 0) {
                                    current_concept
                                            .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
                                }
                            } // for "k" loop

                            MEMEToolkit.logComment("Semantic type " + sty_name
                                    + " has been successfully deleted", true);
                        } // for "i" loop

                        // refresh
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
                                            "Failed to delete semantic type(s) for concept "
                                                    + current_concept
                                                            .getIdentifier()
                                                            .toString()
                                                    + "\nLog file/Console may contain more information.");
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
    } // DeleteSTYAction

    /**
     * Changes status of selected STY(s).
     * 
     * @see AbstractAction
     */
    class ChangeStatusAction extends AbstractAction {
        private Component target = null;

        private String action_cmd = null;

        // constructor
        public ChangeStatusAction(Component comp) {
            putValue(Action.NAME, "STY Status");
            putValue(Action.SHORT_DESCRIPTION,
                    "change status of the selected STY(s)");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (stysTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target, "No selected STY(s).");
                return;
            }

            action_cmd = e.getActionCommand();

            JekyllKit.disableFrames();
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

                        int[] selected_rows = stysTable.getSelectedRows();

                        for (int i = 0; i < selected_rows.length; i++) {
                            String sty_name = (String) stysModel.getValueAt(
                                    stysTable.mapIndex(selected_rows[i]),
                                    TABLE_NAME_COLUMN);

                            for (int k = 0; k < listOfSTYs.size(); k++) {
                                Attribute attr = (Attribute) listOfSTYs.get(k);

                                if (!sty_name.equals(attr.getValue())) {
                                    continue;
                                }

                                attr.setStatus(status);

                                MolecularChangeAttributeAction mcaa = new MolecularChangeAttributeAction(
                                        attr);
                                JekyllKit.getDefaultActionClient()
                                        .processAction(mcaa);
                            } // k loop

                            MEMEToolkit.logComment(
                                    "Status has been changed for semantic type: "
                                            + sty_name, true);
                        } // i loop

                        // refresh
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
                                            "Failed to change status for semantic type."
                                                    + "\nLog file/Console may contain more information.");
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

    /**
     * Sets content for this screen.
     */
    public void setContent(Concept concept) {
        ActionLogger logger = new ActionLogger(this.getClass().getName(), true);
        
        try {
            current_concept = concept;

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            clearContent();

            conceptNameTF.setText(current_concept.getPreferredAtom()
                    .getString()); // concept name
            conceptIdTF.setText(current_concept.getIdentifier().toString()); // concept_id

            ConceptSemanticType[] stys = current_concept.getSemanticTypes();

            for (int i = 0; i < stys.length; i++) {
                boolean duplicate = false;

                String sty_name = stys[i].getValue();

                for (int k = 0; k < listOfSTYs.size(); k++) {
                    if (sty_name.equals(((Attribute) listOfSTYs.get(k))
                            .getValue())) {
                        duplicate = true;
                        break;
                    }
                }

                listOfSTYs.add(stys[i]);

                if (duplicate) {
                    continue;
                }

                Vector row = new Vector();

                row.add(stys[i].getValue()); // STY name
                row.add(String.valueOf(stys[i].getStatus())); // status
                row.add(stys[i].getAuthority().toString()); // authority
                row.add(stys[i].getTimestamp()); // authorized on

                stysModel.addRow(row);
            }
        } catch (Exception ex) {
            MEMEToolkit.notifyUser(this,
                    "Failed to load semantic types for the concept: "
                            + ((current_concept == null) ? " "
                                    : current_concept.getIdentifier()
                                            .toString())
                            + "Console/Log file may contain more information.");
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logger.logElapsedTime();
        }
    } // setContent()

    public Dimension getPreferredSize() {
        return new Dimension(740, 415);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            tuiTF.setText(null);
            styTF.setText(null);
            styNameTF.setText(null);
            semantic_type = null;
            stysTable.setSortState(-1, false);
        }
    }
}