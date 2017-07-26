/*
 * ConceptSelector.java
 * Modified: Soma Lanka : 12/06/2005: Modified the method refresh concepts to fire the rows inserted. 
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Relationship;
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import samples.accessory.StringGridBagLayout;

/**
 * Concept Selector (SEL) window.
 */
public class ConceptSelector extends JFrame implements Reportable,
        JekyllConstants, Mergeable, StateChangeListener {

    //-----------------------------------
    // Private Fields
    //-----------------------------------

    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // integer values for columns
    private final int TABLE_ACTION_SELECTOR_COLUMN = 0;

    private final int TABLE_CONCEPT_NAME_COLUMN = 1;

    private final int TABLE_CONCEPT_ID_COLUMN = 2;

    private final int TABLE_CODE_COLUMN = 3;

    private final int TABLE_TERMGROUP_COLUMN = 4;

    private final int TABLE_STATUS_COLUMN = 5;

    // integer values for actions
    private final int MAKE_CC_ACTION = 0;

    private final int LINK_AS_BT_TO_CC_ACTION = 1;

    private final int LINK_AS_NT_TO_CC_ACTION = 2;

    private final int LINK_AS_RT_TO_CC_ACTION = 3;

    private final int LINK_AS_XR_TO_CC_ACTION = 4;

    private final int MERGE_INTO_CC_ACTION = 5;

    private Hashtable actions = new Hashtable();

    // action commands for Add buttons
    private final String ADD_BY_CONCEPT_ID = "add.by.concept_id";

    private final String ADD_BY_FILE = "add.by.file";

    private final String ADD_BY_CODE = "add.by.code";

    private final String ADD_BY_CUI = "add.by.cui";

    // Components
    private GlassComponent glass_comp = null;

    private JComboBox actionComboBox = null;

    private EditableTableModel editListModel = null;

    protected ResizableJTable editListTable = null;

    private JTextField currentConceptTF = null;

    private JTextField editorTF = null;

    private JTextField countTF = null;

    private JTextField conceptIdTF = null;

    private JTextField fileTF = null;

    private JTextField codeTF = null;

    private JTextField cuiTF = null;

    private JPopupMenu popup = null;

    private JButton apprNextButton = null;

    private JButton apprButton = null;

    private JMenu status_menu = null;

    // Actions
    CloseAction close_action = new CloseAction(this);

    ConceptReportAction concept_report_action = new ConceptReportAction(this);

    MergeAction merge_action = new MergeAction(this);

    // Fields
    private Vector listOfConcepts = new Vector();

    private Concept current_concept = null;

    private boolean browse_mode = false;

    /**
     * Default constructor.
     */
    public ConceptSelector() {
        initResources();
        initValues();
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    //-----------------------------------
    // Private Methods
    //-----------------------------------

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle
                .getBundle("bundles.ConceptSelectorResources");
    }

    private void initValues() {
        actions.put(resources.getString("actionComboBox.makeCC.label"),
                new Integer(0));
        actions.put(resources.getString("actionComboBox.linkAsBT.label"),
                new Integer(1));
        actions.put(resources.getString("actionComboBox.linkAsNT.label"),
                new Integer(2));
        actions.put(resources.getString("actionComboBox.linkAsRT.label"),
                new Integer(3));
        actions.put(resources.getString("actionComboBox.linkAsXR.label"),
                new Integer(4));
        actions.put(resources.getString("actionComboBox.mergeIntoCC.label"),
                new Integer(5));
    }

    private void initComponents() {
        AddAction addAction = new AddAction(this);
        Box b = null;
        String columnName = null;
        TableColumn column = null;
        JButton addButton = null;

        // set properties on this frame
        setTitle(resources.getString("window.title"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);

        // build the contents
        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());

        //-----------------------------------------
        // 1st row
        //-----------------------------------------

        // "Concept Report" button
        JButton reportBtn = GUIToolkit.getButton(concept_report_action);

        // "Finder" button
        JButton finderBtn = GUIToolkit.getButton(new FinderAction(this));

        // "Attribute" button
        JButton attributeBtn = GUIToolkit.getButton(new AttributesFrameAction(
                this));

        // "Close" button
        JButton closeBtn = GUIToolkit.getButton(close_action);

        // box container
        b = Box.createHorizontalBox();
        b.add(reportBtn);
        b.add(Box.createHorizontalStrut(5));
        b.add(finderBtn);
        b.add(Box.createHorizontalGlue());
        b.add(attributeBtn);
        b.add(Box.createHorizontalStrut(5));
        b.add(closeBtn);

        contents
                .add(
                        "gridx=0,gridy=0,gridwidth=4,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        //-----------------------------------------
        // 2nd row
        //-----------------------------------------

        // label for current concept text field
        JLabel current_concept_lbl = new JLabel();
        current_concept_lbl
                .setText(resources.getString("currentConcept.label"));

        // text field for current concept
        currentConceptTF = GUIToolkit.getNonEditField();

        // box container
        b = Box.createHorizontalBox();
        b.add(current_concept_lbl);
        b.add(Box.createHorizontalStrut(12));
        b.add(currentConceptTF);

        contents
                .add(
                        "gridx=0,gridy=1,gridwidth=4,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        //-----------------------------------------
        // 3rd row
        //-----------------------------------------

        // action button
        JButton actionBtn = GUIToolkit.getButton(new ActionAction(this));

        // next cluster button
        JButton nextClusterBtn = GUIToolkit.getButton(JekyllKit
                .getWorkFilesFrame().getNextClusterAction(this));

        // label for editor text field
        JLabel editor_lbl = new JLabel();
        editor_lbl.setText(resources.getString("editor.label"));

        // text field for editor's initials
        editorTF = new JTextField(5);
        editorTF.setEditable(false);
        editorTF.setForeground(LABEL_FG);
        editorTF.setMinimumSize(editorTF.getPreferredSize());
        editorTF.setMaximumSize(editorTF.getPreferredSize());
        editorTF.setText(JekyllKit.getEditorInitials());

        // label for count text field
        JLabel count_lbl = new JLabel();
        count_lbl.setText(resources.getString("conceptsCount.label"));

        // a count of concepts in the Edit List
        countTF = new JTextField(5);
        countTF.setEditable(false);
        countTF.setForeground(LABEL_FG);
        countTF.setMinimumSize(countTF.getPreferredSize());
        countTF.setMaximumSize(countTF.getPreferredSize());
        countTF.setText(resources.getString("conceptsCountTextField.text"));

        // box container
        b = Box.createHorizontalBox();
        b.add(actionBtn);
        b.add(Box.createHorizontalGlue());
        b.add(nextClusterBtn);
        b.add(Box.createHorizontalStrut(5));
        b.add(editor_lbl);
        b.add(Box.createHorizontalStrut(5));
        b.add(editorTF);
        b.add(Box.createHorizontalStrut(50));
        b.add(count_lbl);
        b.add(Box.createHorizontalStrut(5));
        b.add(countTF);
        contents
                .add(
                        "gridx=0,gridy=2,gridwidth=4,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        //-----------------------------------------
        // 4rd row
        //-----------------------------------------

        // edit list table
        editListModel = new EditableTableModel();
        editListModel.setColumnCount(6);
        editListModel.setRowCount(0);
        editListTable = GUIToolkit.getBoldTable(editListModel);

        // a combo box for the action selector column
        actionComboBox = new JComboBox();
        actionComboBox.insertItemAt(resources
                .getString("actionComboBox.makeCC.label"), MAKE_CC_ACTION);
        actionComboBox.insertItemAt(resources
                .getString("actionComboBox.linkAsBT.label"),
                LINK_AS_BT_TO_CC_ACTION);
        actionComboBox.insertItemAt(resources
                .getString("actionComboBox.linkAsNT.label"),
                LINK_AS_NT_TO_CC_ACTION);
        actionComboBox.insertItemAt(resources
                .getString("actionComboBox.linkAsRT.label"),
                LINK_AS_RT_TO_CC_ACTION);
        actionComboBox.insertItemAt(resources
                .getString("actionComboBox.linkAsXR.label"),
                LINK_AS_XR_TO_CC_ACTION);
        actionComboBox.insertItemAt(resources
                .getString("actionComboBox.mergeIntoCC.label"),
                MERGE_INTO_CC_ACTION);
        actionComboBox.setSelectedIndex(MAKE_CC_ACTION);
        // 	actionComboBox.setRenderer(new ActionListCellRenderer());

        columnName = editListTable.getColumnName(TABLE_ACTION_SELECTOR_COLUMN);
        column = editListTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("editListTable.actionSelector.label"));
        column.setCellEditor(new DefaultCellEditor(actionComboBox));

        columnName = editListTable.getColumnName(TABLE_CONCEPT_NAME_COLUMN);
        column = editListTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("editListTable.conceptName.label"));

        columnName = editListTable.getColumnName(TABLE_CONCEPT_ID_COLUMN);
        column = editListTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("editListTable.conceptId.label"));

        columnName = editListTable.getColumnName(TABLE_CODE_COLUMN);
        column = editListTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("editListTable.code.label"));

        columnName = editListTable.getColumnName(TABLE_TERMGROUP_COLUMN);
        column = editListTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("editListTable.termgroup.label"));

        // table columns settings
        columnName = editListTable.getColumnName(TABLE_STATUS_COLUMN);
        column = editListTable.getColumn(columnName);
        column
                .setHeaderValue(resources
                        .getString("editListTable.status.label"));
        column.setIdentifier(StringTableCellRenderer.STATUS_IDENTIFIER);

        editListTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        editListTable.getTableHeader().setReorderingAllowed(false);
        editListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTextField tf = new JTextField();
        tf.setEditable(false);
        editListTable.setDefaultEditor(String.class, new DefaultCellEditor(tf));
        editListTable
                .setDefaultEditor(Integer.class, new IntegerCellEditor(tf));
        StringTableCellRenderer renderer = new StringTableCellRenderer(
                editListTable, editListModel);
        editListTable.setDefaultRenderer(String.class, renderer);
        editListTable.setDefaultRenderer(Integer.class, renderer);
        editListTable
                .setPreferredScrollableViewportSize(new Dimension(700, 200));
        JScrollPane sp = new JScrollPane(editListTable);

        contents
                .add(
                        "gridx=0,gridy=3,gridwidth=4,gridheight=3,fill=BOTH,weighty=1.0,insets=[0,12,11,11]",
                        sp);

        // -------------------------------
        // 1st row of buttons, textfields
        // -------------------------------

        // label for concept_id text field
        JLabel conceptIdLabel = new JLabel();
        conceptIdLabel.setText(resources.getString("conceptId.label"));
        conceptIdLabel.setDisplayedMnemonic(resources.getString(
                "conceptId.mnemonic").charAt(0));

        // concept_id text field
        conceptIdTF = new JTextField(10);
        conceptIdLabel.setLabelFor(conceptIdTF);
        conceptIdTF.setMinimumSize(conceptIdTF.getPreferredSize());

        // add button
        addButton = GUIToolkit.getButton(addAction);
        addButton.setActionCommand(ADD_BY_CONCEPT_ID);

        // Approve/Next button
        apprNextButton = GUIToolkit.getButton(new ApproveNextAction(this));

        // Concept button
        JButton conceptButton = GUIToolkit.getButton(new ConceptFrameAction(
                this));

        // box container
        b = Box.createHorizontalBox();
        b.add(conceptIdLabel);
        b.add(Box.createHorizontalStrut(12));
        b.add(conceptIdTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(addButton);

        contents
                .add(
                        "gridx=0,gridy=6,gridwidth=2,fill=NONE,anchor=WEST,insets=[12,12,0,0]",
                        b);

        b = Box.createHorizontalBox();
        b.add(apprNextButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(conceptButton);

        contents
                .add(
                        "gridx=2,gridy=6,gridwidth=2,fill=NONE,anchor=EAST,insets=[12,12,0,11]",
                        b);

        // -------------------------------
        // 2nd row of buttons, textfields
        // -------------------------------

        // label for file text field
        JLabel fileLabel = new JLabel();
        fileLabel.setText(resources.getString("file.label"));
        fileLabel.setDisplayedMnemonic(resources.getString("file.mnemonic")
                .charAt(0));

        // file text field
        fileTF = new JTextField(10);
        fileLabel.setLabelFor(fileTF);
        fileTF.setMinimumSize(fileTF.getPreferredSize());

        // add button
        addButton = GUIToolkit.getButton(addAction);
        addButton.setActionCommand(ADD_BY_FILE);

        // Approve button
        apprButton = GUIToolkit.getButton(new ApproveAction(this));

        // Class button
        JButton classButton = GUIToolkit
                .getButton(new ClassesFrameAction(this));

        // box container
        b = Box.createHorizontalBox();
        b.add(fileLabel);
        b.add(Box.createHorizontalStrut(12));
        b.add(fileTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(addButton);

        contents
                .add(
                        "gridx=0,gridy=7,gridwidth=2,fill=NONE,anchor=WEST,insets=[5,12,0,0]",
                        b);

        b = Box.createHorizontalBox();
        b.add(apprButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(classButton);

        contents
                .add(
                        "gridx=2,gridy=7,gridwidth=2,fill=NONE,anchor=EAST,insets=[5,12,0,11]",
                        b);

        // -------------------------------
        // 3rd row of buttons, textfields
        // -------------------------------

        // label for code text field
        JLabel codeLabel = new JLabel();
        codeLabel.setText(resources.getString("code.label"));
        codeLabel.setDisplayedMnemonic(resources.getString("code.mnemonic")
                .charAt(0));

        // code text field
        codeTF = new JTextField(10);
        codeLabel.setLabelFor(codeTF);
        codeTF.setMinimumSize(codeTF.getPreferredSize());

        // an add button
        addButton = GUIToolkit.getButton(addAction);
        addButton.setActionCommand(ADD_BY_CODE);

        // "Helper" button
        JButton helperButton = GUIToolkit.getButton(new HelperAction(this));
        if ((JekyllKit.getEditorLevel() != 5)
                && (JekyllKit.getEditorLevel() != 3)) {
            helperButton.setVisible(false);
        }

        // Not Approved/Next button
        JButton notApprNextButton = GUIToolkit
                .getButton(new NotApproveNextAction(this));

        // Relationship button
        JButton relButton = GUIToolkit.getButton(new RelationshipsFrameAction(
                this));

        // box container
        b = Box.createHorizontalBox();
        b.add(codeLabel);
        b.add(Box.createHorizontalStrut(12));
        b.add(codeTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(addButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(helperButton);

        contents
                .add(
                        "gridx=0,gridy=8,gridwidth=2,fill=NONE,anchor=WEST,insets=[5,12,0,0]",
                        b);

        b = Box.createHorizontalBox();
        b.add(notApprNextButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(relButton);

        contents
                .add(
                        "gridx=2,gridy=8,gridwidth=2,fill=NONE,anchor=EAST,insets=[5,12,0,11]",
                        b);

        // -------------------------------
        // 4rd row of buttons, textfields
        // -------------------------------

        // label for cui textfield
        JLabel cuiLabel = new JLabel();
        cuiLabel.setText(resources.getString("cui.label"));
        cuiLabel.setDisplayedMnemonic(resources.getString("cui.mnemonic")
                .charAt(0));

        // cui textfield
        cuiTF = new JTextField(10);
        cuiLabel.setLabelFor(cuiTF);
        cuiTF.setMinimumSize(cuiTF.getPreferredSize());

        // an add button
        addButton = GUIToolkit.getButton(addAction);
        addButton.setActionCommand(ADD_BY_CUI);

        // box container
        b = Box.createHorizontalBox();
        b.add(cuiLabel);
        b.add(Box.createHorizontalStrut(12));
        b.add(cuiTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(addButton);

        contents
                .add(
                        "gridx=0,gridy=9,gridwidth=2,fill=NONE,anchor=WEST,insets=[5,12,12,0]",
                        b);

        // popup menu
        popup = new JPopupMenu();
        editListTable.addMouseListener(new PopupListener(popup));
        JMenuItem menuItem = new JMenuItem(new RemoveConceptAction(this));
        popup.add(menuItem);

        // adding a menu
        setJMenuBar(buildMenuBar());

    } // initComponents()

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = null;
        JMenuItem item = null;

        // File
        menu = new JMenu();
        menu.setText(resources.getString("fileMenu.label"));
        menu.setMnemonic(resources.getString("fileMenu.mnemonic").charAt(0));

        // File -> Close
        item = new JMenuItem(close_action);
        menu.add(item);

        menuBar.add(menu);

        // Report
        menu = new JMenu();
        menu.setText(resources.getString("reportMenu.label"));
        menu.setMnemonic(resources.getString("reportMenu.mnemonic").charAt(0));

        // Report -> 1
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.1.label"));
        menu.add(item);

        // Report -> 2
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.2.label"));
        item.setActionCommand(ConceptReportAction.WIN_PAR_CHD_SIB);
        menu.add(item);

        // Report -> 3
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.3.label"));
        item.setActionCommand(ConceptReportAction.WIN_ALL_CONTEXT_RELS);
        menu.add(item);

        // Report -> 4
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.4.label"));
        item.setActionCommand(ConceptReportAction.WIN_XR_PAR_CHD);
        menu.add(item);

        // Report -> 5
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.5.label"));
        item.setActionCommand(ConceptReportAction.WIN_XR_PAR_CHD_SIB);
        menu.add(item);

        // Report -> 6
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.6.label"));
        item.setActionCommand(ConceptReportAction.WIN_XR_ALL_CONTEXT_RELS);
        menu.add(item);

        // Report -> 7
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.7.label"));
        item.setActionCommand(ConceptReportAction.ALL_PAR_CHD);
        menu.add(item);

        // Report -> 8
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.8.label"));
        item.setActionCommand(ConceptReportAction.ALL_PAR_CHD_SIB);
        menu.add(item);

        // Report -> 9
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.9.label"));
        item.setActionCommand(ConceptReportAction.ALL_ALL_CONTEXT_RELS);
        menu.add(item);

        menuBar.add(menu);

        // Edit
        menu = new JMenu();
        menu.setText(resources.getString("editMenu.label"));
        menu.setMnemonic(resources.getString("editMenu.mnemonic").charAt(0));

        // Edit -> Save List to a file
        item = new JMenuItem(new SaveListToFileAction(this));
        menu.add(item);

        menuBar.add(menu);

        // Status
        status_menu = new JMenu();
        status_menu.setText(resources.getString("statusMenu.label"));
        status_menu.setMnemonic(resources.getString("statusMenu.mnemonic")
                .charAt(0));

        item = new JMenuItem(new ChangeStatusAction(this));
        status_menu.add(item);

        menuBar.add(status_menu);

        // Options
        menu = new JMenu();
        menu.setText(resources.getString("optionsMenu.label"));
        menu.setMnemonic(resources.getString("optionsMenu.mnemonic").charAt(0));

        // Options -> Increase Font
        item = new JMenuItem(new IncreaseFontAction() {
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                int new_row_height = editListTable.getRowHeight() + 6;
                editListTable.setRowHeight(new_row_height);
                JekyllKit.getClassesFrame().atomsTable
                        .setRowHeight(new_row_height);
                JekyllKit.getRelationshipsFrame().relsTable
                        .setRowHeight(new_row_height);
            }
        });
        menu.add(item);

        // Options -> Decrease Font
        item = new JMenuItem(new DecreaseFontAction() {
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                int new_row_height = editListTable.getRowHeight() + 6;
                editListTable.setRowHeight(new_row_height);
                JekyllKit.getClassesFrame().atomsTable
                        .setRowHeight(new_row_height);
                JekyllKit.getRelationshipsFrame().relsTable
                        .setRowHeight(new_row_height);
            }
        });
        menu.add(item);

        menuBar.add(menu);

        return menuBar;
    } // buildMenuBar()

    /**
     * Clears content of this screen.
     */
    private void clearContent() {
        current_concept = null;
        currentConceptTF.setText(null);
        countTF.setText("0");
        editListTable.clearSelection();
        editListModel.getDataVector().clear();
        editListModel.fireTableRowsInserted(0, 1);
        editListModel.fireTableDataChanged();
        listOfConcepts.clear();
        conceptIdTF.setText(null);
        codeTF.setText(null);
        cuiTF.setText(null);
    } // clearContent()

    /**
     * Returns the index of the row of the current concept.
     */
    private int getCCRow() {
        int model_index = listOfConcepts.indexOf(current_concept);
        return editListTable.reverseMapIndex(model_index);
    } // getCCRow()

    /**
     * Sets the concept at the specified index of the current list of the
     * concepts to be the current concept.
     */
    private void setCurrentConcept(int index) {
        current_concept = (Concept) listOfConcepts.get(index);

        MEMEToolkit.logComment("Current Concept has been set to "
                + current_concept.getIdentifier().toString() + " ("
                + current_concept.getPreferredAtom().getString() + ")", true);

        refreshConcept();
    } // setCurrentConcept

    public void updateEditList() {
        Concept cc = current_concept;

        Vector temp_concepts = (Vector) listOfConcepts.clone();

        // clear current list
        clearContent();

        for (Iterator i = temp_concepts.iterator(); i.hasNext();) {
            try {
                Concept concept = JekyllKit.getCoreDataClient().getConcept(
                        (Concept) i.next());
                addConcept(concept);
            } catch (Exception ex) {
                if (!(ex instanceof MissingDataException)) {
                    ex.printStackTrace(JekyllKit.getLogWriter());
                }
            }
        }

        int cc_model_index = listOfConcepts.indexOf(cc);
        if (cc_model_index != -1) {
            setCurrentConcept(cc_model_index);
        } else if ((cc_model_index == -1) && (listOfConcepts.size() != 0)) {
            setCurrentConcept(0);
        }
    } // updateEditList()

    /**
     * Re-queries the current concept and refreshes open screens with its
     * updated content.
     */
    public void refreshConcept() {

        try {
            current_concept = JekyllKit.getCoreDataClient().getConcept(
                    current_concept.getIdentifier());
            setConceptAt(current_concept);
            currentConceptTF.setText(current_concept.getIdentifier().toString()
                    + "   " + current_concept.getPreferredAtom().getString());
            if (SwingUtilities.isEventDispatchThread()) {
                updateFrames();
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            updateFrames();
                        } catch (Exception ex) {
                            ex.printStackTrace(JekyllKit.getLogWriter());
                        }
                    }
                });
            } // if

        } catch (Exception ex) {
            MEMEToolkit.notifyUser(this, "Failed to refresh concept: "
                    + current_concept.getIdentifier().toString()
                    + "\nThe intended action most likely finished correctly,"
                    + "\nbut the data on the screens might be stale."
                    + "\nConsole/Log File may contain more information.");
            ex.printStackTrace(JekyllKit.getLogWriter());
        }
    } // refreshConcept()

    private void updateFrames() {
        JFrame[] frames = JekyllKit.getFrames();
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].isVisible()) {
                ((Refreshable) frames[i]).setContent(current_concept);
            }
        }

        if ((concept_report_action.getFrame() != null)
                && (concept_report_action.getFrame().isShowing())) {
            concept_report_action.actionPerformed(new ActionEvent(this,
                    ActionEvent.ACTION_FIRST, ConceptReportAction.WIN_PAR_CHD));
        }
    } // updateFrames();

    // ---------------------------------
    // Interface implementation
    // ---------------------------------

    /**
     * Implements
     * {@link Mergeable#getSourceConcept() Mergeable.getSourceConcept()}.
     */
    public Concept getSourceConcept() {
        int selected_row = editListTable.getSelectedRow();
        Concept source_concept = (Concept) listOfConcepts.get(editListTable
                .mapIndex(selected_row));
        return source_concept;
    }

    /**
     * Implements
     * {@link Mergeable#getTargetConcept() Mergeable.getTargetConcept()}.
     */
    public Concept getTargetConcept() {
        return current_concept;
    }

    /**
     * Implements
     * {@link Mergeable#removeSourceConcept() Mergeable.removeSourceConcept()}.
     */
    public void removeSourceConcept() throws Exception {
        int selected_row = editListTable.getSelectedRow();
        removeConcept(selected_row);
    }

    /**
     * Implements {@link Editable#getConcept() Editable.getConcept()}.
     */
    public Concept getConcept() {
        if (current_concept == null) {
            MEMEToolkit.notifyUser(this, "Current Concept is not set");
            return null;
        }

        return current_concept;
    }

    /**
     * Implements {@link Reportable#getConceptId() Reportable.getConceptId()}.
     */
    public int getConceptId() {
        if (current_concept != null) {
            return current_concept.getIdentifier().intValue();
        } else {
            return 0;
        }
    }

    /**
     * Implements {@link StateChangeListener#stateChanged(StateChangeEvent)
     * StateChangeListener.stateChanged(StateChangeEvent)}.
     */
    public void stateChanged(StateChangeEvent e) {
        if (e.getState().equals(StateChangeEvent.BROWSE_STATE)) {
            apprButton.setEnabled(false);
            apprNextButton.setEnabled(false);
            status_menu.setEnabled(false);
            for (int i = 0; i < editListModel.getRowCount(); i++) {
                editListModel.setValueAt(resources
                        .getString("actionComboBox.makeCC.label"), i,
                        TABLE_ACTION_SELECTOR_COLUMN);
            }
            actionComboBox.setEnabled(false);
        } else {
            apprButton.setEnabled(true);
            apprNextButton.setEnabled(true);
            status_menu.setEnabled(false);
            browse_mode = false;
            actionComboBox.setEnabled(true);
        }
    }

    // -------------------------------
    // Inner classes
    // -------------------------------

    class ActionListCellRenderer extends JLabel implements ListCellRenderer {
        // Constructor
        public ActionListCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());
            setBackground(isSelected ? list.getSelectionBackground() : list
                    .getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list
                    .getForeground());

            if (browse_mode && index != MAKE_CC_ACTION) {
                setEnabled(false);
            }

            return this;
        }

    }

    /**
     * Performs an action selected from the "Action Selector" picklist.
     * 
     * @see AbstractAction
     */
    class ActionAction extends AbstractAction {
        private Component target = null;

        private int selected_row = 0;

        private int selected_model_index = 0;

        private int selected_action_index = 0;

        private boolean set_cursor_back = true;

        // constructor
        public ActionAction(Component comp) {
            putValue(Action.NAME, "Do the action");
            putValue(Action.SHORT_DESCRIPTION, "perform selected action");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (editListTable.getSelectionModel().isSelectionEmpty()) {
                return;
            }

            ActionLogger logger = null;

            try {
                set_cursor_back = true;

                JekyllKit.disableFrames();
                target
                        .setCursor(Cursor
                                .getPredefinedCursor(Cursor.WAIT_CURSOR));
                glass_comp.setVisible(true);

                selected_row = editListTable.getSelectedRow();
                selected_model_index = editListTable.mapIndex(selected_row);
                String value = (String) editListTable.getValueAt(selected_row,
                        TABLE_ACTION_SELECTOR_COLUMN);
                selected_action_index = ((Integer) actions.get(value))
                        .intValue();

                actions: if (selected_action_index == MAKE_CC_ACTION) {
                    logger = new ActionLogger(this.getClass().getName()
                            + ".setCurrentConcept", true);

                    if (selected_row != getCCRow()) {
                        setCurrentConcept(editListTable.mapIndex(selected_row));
                    }

                } else if ((selected_action_index == LINK_AS_BT_TO_CC_ACTION)
                        || (selected_action_index == LINK_AS_NT_TO_CC_ACTION)
                        || (selected_action_index == LINK_AS_RT_TO_CC_ACTION)
                        || (selected_action_index == LINK_AS_XR_TO_CC_ACTION)) {

                    if (!JekyllKit.getRelationshipsFrame().isShowing()) {
                        MEMEToolkit.notifyUser(target,
                                "Relationships must be displayed to link.");
                        break actions;
                    }

                    if (current_concept == null) {
                        MEMEToolkit.notifyUser(target,
                                "Current Concept is not set");
                        break actions;
                    }

                    if (selected_row == getCCRow()) {
                        MEMEToolkit
                                .notifyUser(target,
                                        "Related concept is the same as the current concept");
                        break actions;
                    }

                    set_cursor_back = false;

                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            ActionLogger logger = new ActionLogger(ActionAction.this
                                    .getClass().getName()
                                    + ".insertRelationship", true);

                            try {

                                Relationship relationship = null;
                                Concept related_concept = null;
                                MolecularInsertRelationshipAction mira = null;

                                boolean stale_data = false;
                                do {
                                    try {
                                        relationship = new Relationship.Default();
                                        relationship
                                                .setConcept(current_concept);
                                        related_concept = (Concept) listOfConcepts
                                                .get(selected_model_index);
                                        relationship
                                                .setRelatedConcept(related_concept);

                                        // setting relationship name
                                        if (selected_action_index == LINK_AS_BT_TO_CC_ACTION) {
                                            relationship.setName(BROADER);
                                        } else if (selected_action_index == LINK_AS_NT_TO_CC_ACTION) {
                                            relationship.setName(NARROWER);
                                        } else if (selected_action_index == LINK_AS_RT_TO_CC_ACTION) {
                                            relationship.setName(OTHER_RELATED);
                                        } else {
                                            relationship.setName(NOT_RELATED);
                                        }

                                        Source source = new Source.Default(
                                                JekyllKit.getAuthority()
                                                        .toString());
                                        relationship.setSource(source); // SAB
                                        relationship.setSourceOfLabel(source); // SL
                                        relationship
                                                .setLevel(CoreData.FV_MTH_ASSERTED); // relationship
                                        // level
                                        relationship
                                                .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW); // relationship
                                        // status
                                        relationship
                                                .setTobereleased(CoreData.FV_RELEASABLE); // tbr
                                        relationship
                                                .setReleased(CoreData.FV_NOT_RELEASED); // released

                                        mira = new MolecularInsertRelationshipAction(
                                                relationship);
                                        JekyllKit.getDefaultActionClient()
                                                .processAction(mira);

                                        if (stale_data) {
                                            stale_data = false;
                                        }
                                    } catch (StaleDataException sde) {
                                        // re-reading concepts
                                        current_concept = JekyllKit
                                                .getCoreDataClient()
                                                .getConcept(current_concept);
                                        related_concept = JekyllKit
                                                .getCoreDataClient()
                                                .getConcept(related_concept);
                                        stale_data = true;
                                    }
                                } while (stale_data); // do loop

                                JekyllKit.getRelationshipsFrame()
                                        .addEditedRelationship(
                                                mira.getRelationshipToInsert());

                                MEMEToolkit.logComment("Concept "
                                        + related_concept.getIdentifier()
                                                .toString()
                                        + " has been linked as "
                                        + relationship.getName()
                                        + " to the current concept ("
                                        + current_concept.getIdentifier()
                                                .toString() + ")", true);

                                refreshConcept();
                            } catch (Exception ex) {
                                if (ex instanceof MissingDataException) {
                                    MEMEToolkit
                                            .notifyUser(
                                                    target,
                                                    "One of the concepts, involved"
                                                            + "\nin the action, is no longer"
                                                            + "\na valid concept in the database.");
                                } else if (ex instanceof MEMEException
                                        && ((MEMEException) ex)
                                                .getEnclosedException() instanceof SocketException) {
                                    MEMEToolkit
                                            .reportError(
                                                    target,
                                                    "There was a network error."
                                                            + "\nPlease try the action again.",
                                                    false);
                                } else {
                                    MEMEToolkit
                                            .notifyUser(
                                                    target,
                                                    "Failed to create a relationship."
                                                            + "\nConsole/Log file may contain additional information.");
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
                } else if (selected_action_index == MERGE_INTO_CC_ACTION) {

                    if (current_concept == null) {
                        MEMEToolkit.notifyUser(target,
                                "Current Concept is not set");
                        break actions;
                    }

                    if (selected_row == getCCRow()) {
                        MEMEToolkit
                                .notifyUser(target,
                                        "Source concept is the same as the current concept");
                        break actions;
                    }

                    set_cursor_back = false;
                    merge_action.actionPerformed(null);
                } // actions if
            } catch (Exception ex) {
                MEMEToolkit
                        .notifyUser(
                                target,
                                "Failed to perform selected action"
                                        + "Console/Log file may contain more information.");
                ex.printStackTrace(JekyllKit.getLogWriter());
            } finally {
                if (set_cursor_back) {
                    glass_comp.setVisible(false);
                    target.setCursor(Cursor
                            .getPredefinedCursor(Cursor.HAND_CURSOR));
                    JekyllKit.enableFrames();
                }
                if (logger != null)
                    logger.logElapsedTime();
            }
        } // actionPerformed()
    } // ActionAction

    /**
     * Add concept_id action.
     * 
     * @see AbstractAction
     */
    class AddAction extends AbstractAction {
        private Component target = null;

        private String action_cmd = null;

        // consturctor
        public AddAction(Component comp) {
            putValue(Action.NAME, "Add");
            putValue(Action.SHORT_DESCRIPTION,
                    "add concept(s) to the Edit List");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            ActionLogger logger = null;
            action_cmd = e.getActionCommand();

            String string = "";

            add_actions: if (action_cmd.equals(ADD_BY_CONCEPT_ID)) {

                string = conceptIdTF.getText().trim();
                if (string.equals("")) {
                    break add_actions;
                }

                logger = new ActionLogger(this.getClass().getName()
                        + ".addByConceptId", true);

                try {
                    for (int i = 0; i < listOfConcepts.size(); i++) {
                        if (string.equals(((Concept) listOfConcepts.get(i))
                                .getIdentifier().toString())) {
                            MEMEToolkit.notifyUser(target,
                                    "Concept is already in list");
                            break add_actions;
                        }
                    }

                    int concept_id = Integer.parseInt(string);

                    Concept concept = JekyllKit.getCoreDataClient().getConcept(
                            concept_id);
                    addConcept(concept);

                } catch (NumberFormatException ex) {
                    MEMEToolkit.notifyUser(target,
                            "This is not a valid concept id: " + string);
                } catch (MissingDataException ex) {
                    MEMEToolkit.notifyUser(target, "Concept was not found: "
                            + string);
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
                                        "Failed to retrieve a concept: "
                                                + string
                                                + "\nConsole/Log file may contain more information.");
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());
                }

            } else if (action_cmd.equals(ADD_BY_FILE)) {

                string = fileTF.getText().trim();
                if (string.equals("")) {
                    break add_actions;
                }

                logger = new ActionLogger(this.getClass().getName() + ".addByFile",
                        true);

                try {
                    BufferedReader in = new BufferedReader(new FileReader(
                            string));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        Concept concept = JekyllKit.getCoreDataClient()
                                .getConcept(Integer.parseInt(line));
                        addConcept(concept);
                    }
                    in.close();
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
                                        "Failed to load concept(s) from the file."
                                                + "\nPlease check if filename is correct."
                                                + "\nConsole/Log file may contain more information.");
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());
                }

            } else if (action_cmd.equals(ADD_BY_CODE)) {

                string = codeTF.getText().trim();
                if (string.equals("")) {
                    break add_actions;
                }

                logger = new ActionLogger(this.getClass().getName() + ".addByCode",
                        true);

                Concept[] concepts = null;

                try {
                    JekyllKit.getFinderClient().clearRestrictions();
                    JekyllKit.getFinderClient().setMaxResultCount(1000000);
                    concepts = JekyllKit.getFinderClient().findConceptsByCode(
                            Code.newCode(string));
                    for (int i = 0; i < concepts.length; i++) {
                        addConcept(concepts[i]);
                    }

                    if (concepts.length == 0) {
                        MEMEToolkit.notifyUser(target,
                                "Either invalid code was entered or"
                                        + "\nno concepts exist by this code.");
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
                                        "Failed to retrieve concept(s) for code: "
                                                + string
                                                + "\nConsole/Log file may contain more information.");
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());
                }

            } else if (action_cmd.equals(ADD_BY_CUI)) {

                string = cuiTF.getText().trim();
                if (string.equals("")) {
                    break add_actions;
                }

                logger = new ActionLogger(this.getClass().getName() + ".addByCUI",
                        true);

                try {
                    Concept concept = JekyllKit.getCoreDataClient().getConcept(
                            new CUI(string));
                    addConcept(concept);
                } catch (MissingDataException ex) {
                    MEMEToolkit.notifyUser(target, "Concept was not found: "
                            + string);
                } catch (Exception ex) {
                    String msg = ex.getMessage();

                    if (msg.startsWith("Illegal CUI value")) {
                        MEMEToolkit.notifyUser(target, msg);
                    } else if (ex instanceof MEMEException
                            && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                        MEMEToolkit.reportError(target,
                                "There was a network error."
                                        + "\nPlease try the action again.",
                                false);
                    } else {
                        MEMEToolkit
                                .notifyUser(
                                        target,
                                        "Failed to retrieve a concept for cui: "
                                                + string
                                                + "\nConsole/Log file may contain more information.");
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());
                }
            }

            glass_comp.setVisible(false);
            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JekyllKit.enableFrames();

            if (logger != null)
                logger.logElapsedTime();
        } // actionPerformed()

    } // AddAction

    /**
     * Removes a concept from the Edit List.
     * 
     * @see AbstractAction
     */
    class RemoveConceptAction extends AbstractAction {
        private Component target = null;

        // Constructor
        public RemoveConceptAction(Component comp) {
            putValue(Action.NAME, "Remove Concept");
            putValue(Action.SHORT_DESCRIPTION,
                    "remove selected concept from the Edit List");
            putValue("Background", Color.lightGray);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (editListTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target, "Concept must be selected.");
                return;
            }

            Concept concept = null;

            try {
                int selected_row = editListTable.getSelectedRow();
                concept = (Concept) listOfConcepts.get(editListTable
                        .mapIndex(selected_row));

                if (concept.equals(current_concept)) {
                    MEMEToolkit.notifyUser(target,
                            "You cannot remove the current concept.");
                    return;
                }

                removeConcept(selected_row);
            } catch (Exception ex) {
                MEMEToolkit.notifyUser(target, "Failed to remove concept "
                        + ((concept == null) ? " " : concept.getIdentifier()
                                .toString())
                        + "Console/Log file may contain more information.");
                ex.printStackTrace(JekyllKit.getLogWriter());
            }
        } // actionPerformed()

    } // RemoveConceptAction

    /**
     * Displays "Concept Attributes" window for current concept.
     * 
     * @see AbstractAction
     */
    class AttributesFrameAction extends AbstractAction {
        private Component target = null;

        // Constructor
        public AttributesFrameAction(Component comp) {
            putValue(Action.NAME, "Attributes");
            putValue(Action.SHORT_DESCRIPTION,
                    "open Attributes screen for current concept");
            putValue("Background", new Color(204, 153, 051)); // light
            // brown
            // color

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (current_concept == null) {
                return;
            }

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            AttributesFrame frame = JekyllKit.getAttributesFrame();

            frame.setContent(current_concept);

            glass_comp.setVisible(false);
            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        } // actionPerformed()

    } // AttributesFrameAction

    /**
     * @see AbstractAction
     */
    class HelperAction extends AbstractAction {
        private Component target = null;

        // Constructor
        public HelperAction(Component comp) {
            putValue(Action.NAME, "Helper");
            putValue(Action.SHORT_DESCRIPTION,
                    "open Helper Frames access dialog");
            putValue("Background", new Color(204, 153, 051)); // light
            // brown
            // color

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            HelperFramesDialog dialog = new HelperFramesDialog((JFrame) target);
            dialog.setVisible(true);
        }
    } // HelperAction

    /**
     * Displays "Concept" screen for the current concept.
     * 
     * @see AbstractAction
     */
    class ConceptFrameAction extends AbstractAction {
        private Component target = null;

        // Constructor
        public ConceptFrameAction(Component comp) {
            putValue(Action.NAME, "Concept");
            putValue(Action.SHORT_DESCRIPTION,
                    "open Concept screen for current concept");
            // 	    putValue(Action.MNEMONIC_KEY, );
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (current_concept == null) {
                return;
            }

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            ConceptFrame frame = JekyllKit.getConceptFrame();

            frame.setContent(current_concept);

            glass_comp.setVisible(false);
            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        } // actionPerformed()

    } // ConceptFrameAction

    /**
     * Opens Classes screen populated with the data for the current concept.
     * 
     * @see AbstractAction
     */
    class ClassesFrameAction extends AbstractAction {
        private Component target = null;

        // Constructor
        public ClassesFrameAction(Component comp) {
            putValue(Action.NAME, "Class");
            putValue(Action.SHORT_DESCRIPTION,
                    "open Classes screen for current concept");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (current_concept == null) {
                return;
            }

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            ClassesFrame frame = JekyllKit.getClassesFrame();

            frame.setContent(current_concept);

            glass_comp.setVisible(false);
            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        } // actionPerformed()

    } // ClassesFrameAction

    /**
     * Displays "Concept Relationships" window for the current concept.
     * 
     * @see AbstractAction
     */
    class RelationshipsFrameAction extends AbstractAction {
        private Component target = null;

        // Constructor
        public RelationshipsFrameAction(Component comp) {
            putValue(Action.NAME, "Relationship");
            putValue(Action.SHORT_DESCRIPTION,
                    "open Relationships screen for current concept");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (current_concept == null) {
                return;
            }

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            RelationshipsFrame frame = JekyllKit.getRelationshipsFrame();

            frame.setContent(current_concept);

            glass_comp.setVisible(false);
            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        } // actionPerformed()

    } // RelationshipsFrameAction

    /**
     * Allows concepts on the Edit List to be saved to a file.
     */
    class SaveListToFileAction extends AbstractAction {
        private Component target = null;

        private JTextField filenameTF = null;

        private boolean okPressed = false;

        // Constructor
        public SaveListToFileAction(Component comp) {
            putValue(Action.NAME, "Save Edit List to file");
            putValue(Action.SHORT_DESCRIPTION, "save Edit List to a file");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_F4, InputEvent.CTRL_MASK));

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(SaveListToFileAction.this
                            .getClass().getName(), true);

                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                JDialog dialog = buildDialog();
                                dialog.pack();
                                dialog.setVisible(true);
                            }
                        });

                        String file_name;

                        if (okPressed) {
                            file_name = filenameTF.getText().trim();
                        } else {
                            return;
                        }

                        File f = new File(".", file_name);
                        PrintWriter out = new PrintWriter(new FileWriter(f));
                        for (int i = 0; i < listOfConcepts.size(); i++) {
                            Concept concept = (Concept) listOfConcepts.get(i);
                            out.println(concept.getIdentifier().toString());
                        }
                        out.close();
                    } catch (Exception ex) {
                        MEMEToolkit
                                .notifyUser(
                                        target,
                                        "Failed to save Edit List to a file."
                                                + "\nMake sure you have write permissions to the"
                                                + "\ndirectory where you are running Jekyll from."
                                                + "\nConsole/Log file may contain more information.");
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
        } // actionPerformed()

        private JDialog buildDialog() {
            final JDialog dialog = new JDialog(((JFrame) target), true); // modal

            dialog.setTitle("Filename needed");
            dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            // Build the contents
            Container contents = dialog.getContentPane();
            contents.setLayout(new StringGridBagLayout());

            JLabel label = new JLabel();
            label
                    .setText("Enter filename and press OK to save current Edit List:");

            contents
                    .add(
                            "gridx=0,gridy=0,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                            label);

            filenameTF = new JTextField();

            contents
                    .add(
                            "gridx=0,gridy=1,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                            filenameTF);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (filenameTF.getText().equals("")) {
                        return;
                    }
                    okPressed = true;
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed = false;
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });

            Box b = Box.createHorizontalBox();
            b.add(okButton);
            b.add(Box.createHorizontalStrut(5));
            b.add(cancelButton);

            contents
                    .add(
                            "gridx=0,gridy=2,fill=NONE,anchor=CENTER,insets=[12,12,12,11]",
                            b);

            dialog.setLocationRelativeTo(ConceptSelector.this);

            return dialog;

        } // buildDialog()

    } // SaveListToFileAction

    /**
     * Sets current concept status to "Needs Review" by adding a concept note to
     * the concept.
     * 
     * @see AbstractAction
     */
    class ChangeStatusAction extends AbstractAction {
        private Component target = null;

        // constructor
        public ChangeStatusAction(Component comp) {
            putValue(Action.NAME, "Make Needs Review");
            putValue(Action.SHORT_DESCRIPTION,
                    "make current concept as Needs Review");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(ChangeStatusAction.this
                            .getClass().getName(), true);

                    try {
                        boolean stale_data = false;

                        do {
                            try {
                                Attribute attr = new Attribute.Default();
                                attr.setName(Attribute.CONCEPT_NOTE);
                                attr.setValue("Concept set to N by "
                                        + JekyllKit.getAuthority().toString()
                                        + " on "
                                        + Calendar.getInstance().getTime());
                                attr.setConcept(current_concept);
                                Source source = new Source.Default(JekyllKit
                                        .getAuthority().toString());
                                attr.setSource(source); // SAB
                                attr.setLevel(CoreData.FV_MTH_ASSERTED); // attribute
                                // level
                                attr.setStatus(CoreData.FV_STATUS_NEEDS_REVIEW); // attribute
                                // status
                                attr
                                        .setTobereleased(CoreData.FV_WEAKLY_UNRELEASABLE); // tbr
                                attr.setReleased(CoreData.FV_NOT_RELEASED); // released

                                MolecularInsertAttributeAction mcca = new MolecularInsertAttributeAction(
                                        attr);
                                JekyllKit.getDefaultActionClient()
                                        .processAction(mcca);

                                if (stale_data) {
                                    stale_data = false;

                                }
                            } catch (StaleDataException sde) {
                                // re-reading concept
                                current_concept = JekyllKit.getCoreDataClient()
                                        .getConcept(current_concept);
                                stale_data = true;
                            }
                        } while (stale_data); // do loop

                        MEMEToolkit.logComment("Status of the concept "
                                + current_concept.getIdentifier().toString()
                                + "("
                                + current_concept.getPreferredAtom()
                                        .getString()
                                + ") has been successfully changed to N", true);

                        JekyllKit.getConceptSelector().refreshConcept();

                    } catch (Exception ex) {
                        if (ex instanceof MissingDataException) {
                            MEMEToolkit.notifyUser(target, "Concept "
                                    + current_concept.getIdentifier()
                                            .toString() + " is no longer"
                                    + "\na valid concept in the database.");
                        } else if (ex instanceof MEMEException
                                && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                            MEMEToolkit.reportError(target,
                                    "There was a network error."
                                            + "\nPlease try the action again.",
                                    false);
                        } else {
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "Failed to change status of the concept: "
                                                    + ((current_concept == null) ? " "
                                                            : current_concept
                                                                    .getIdentifier()
                                                                    .toString())
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
        } // actionPerformed()

    } // ChangeStatusAction

    /**
     * Replaces the Edit List with the specified concept(s).
     */
    // Accessed from these threads:
    //  EditAction - AWT thread
    //  WorkFilesFrame.EditClusterAction - AWT thread
    //  ApproveNextAction - thread
    void replaceWith(Concept[] concepts) {

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            // since we're replacing the edit list with
            // the new set, clear old one first.
            clearContent();

            for (int i = 0; i < concepts.length; i++) {
                if (concepts[i] != null) {
                    addConcept(concepts[i]);
                }
            }

            // making the first valid concept in the list
            // as the current concept.
            if (current_concept == null && listOfConcepts.size() != 0) {
                setCurrentConcept(0);
            }

        } catch (Exception ex) {
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

    } // replaceWith(Concept[])

    /**
     * Replaces edit list with the concepts specified by concept_ids.
     */
    // Accessed from these threads:
    //  EditAction - AWT thread
    //  WorkFilesFrame.EditClusterAction - AWT thread
    //  ApproveNextAction - thread
    void replaceWith(int[] concept_ids) {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            // since we're replacing the edit list with
            // the new set, clear old one first.
            clearContent();

            for (int i = 0; i < concept_ids.length; i++) {
                try {
                    Concept concept = JekyllKit.getCoreDataClient().getConcept(
                            concept_ids[i]);
                    addConcept(concept);
                } catch (Exception ex) {
                    if (ex instanceof MissingDataException) {
                        MEMEToolkit.notifyUser(ConceptSelector.this,
                                "Concept was not found: " + concept_ids[i]);
                    } else {
                        MEMEToolkit
                                .notifyUser(
                                        ConceptSelector.this,
                                        "There was an error adding"
                                                + "\nconcept: "
                                                + concept_ids[i]
                                                + "\nConsole/Log file may contain more information.");
                        ex.printStackTrace(JekyllKit.getLogWriter());
                    }
                }
            } // for loop

            // making the first valid concept in the list
            // as the current concept.
            if (current_concept == null && listOfConcepts.size() != 0) {
                setCurrentConcept(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    } // replaceWith(int[])

    /**
     * Adds concept(s) to the Edit List.
     */
    // Accessed from these threads:
    //  - TransferAction
    void addConcepts(Concept[] concepts) {

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            for (int i = 0; i < concepts.length; i++) {
                if (concepts[i] != null) {
                    addConcept(concepts[i]);
                }
            }

            // making the first valid concept in the list
            // as the current concept.
            if (current_concept == null && listOfConcepts.size() != 0) {
                setCurrentConcept(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    } // addConcepts(Concept[])

    /**
     * Adds concept(s) specified by concept_id(s) to the Edit List.
     */
    // Accessed from:
    //  TransferAction
    void addConcepts(int[] concept_ids) {

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            for (int i = 0; i < concept_ids.length; i++) {

                try {
                    Concept concept = JekyllKit.getCoreDataClient().getConcept(
                            concept_ids[i]);
                    addConcept(concept);
                } catch (Exception ex) {
                    if (ex instanceof MissingDataException) {
                        MEMEToolkit.notifyUser(ConceptSelector.this,
                                "Concept was not found: " + concept_ids[i]);
                    } else {
                        MEMEToolkit
                        .notifyUser(
                                ConceptSelector.this,
                                "There was an error adding"
                                        + "\nconcept: "
                                        + concept_ids[i]
                                        + "\nConsole/Log file may contain more information.");
                        ex.printStackTrace(JekyllKit.getLogWriter());
                    }
                }
            } // for

            // making the first valid concept in the list
            // as the current concept.
            if (current_concept == null && listOfConcepts.size() != 0) {
                setCurrentConcept(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    } // addConcepts(int[])

    /**
     * Adds a specified concept to the Edit List.
     * 
     * @param concept
     *                  <code>Concept</code> object to be added.
     */
    void addConcept(final Concept concept) throws Exception {

        if (listOfConcepts.contains(concept)) {
            MEMEToolkit.notifyUser(this, "Concept "
                    + concept.getIdentifier().toString()
                    + " is already in the list");
            return;
        }

        final Vector row = new Vector();

        row.add(resources.getString("actionComboBox.makeCC.label")); // default
        // action
        Atom atom = concept.getPreferredAtom();
        row.add(atom.getString()); // concept name
        row.add(Integer.valueOf(concept.getIdentifier().toString())); // concept_id
        row.add(atom.getCode().toString()); // code
        row.add(atom.getTermgroup().toString()); // termgroup
        row.add(String.valueOf(concept.getStatus())); // status

        if (SwingUtilities.isEventDispatchThread()) {
            editListModel.insertRow(0, row);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    editListModel.insertRow(0, row);
                }
            });
        }

        listOfConcepts.add(0, concept);

        countTF.setText(String.valueOf(listOfConcepts.size()));

        MEMEToolkit.logComment("Concept " + concept.getIdentifier().toString()
                + " (" + atom.getString() + ")"
                + "\nhas been added to the Edit List", true);

    } // addConcept()

    /**
     * This method is only called from <code>refreshConcept()</code> method.
     * 
     * @param concept - a Concept object that should be updated in the Edit List
     * 								of the SEL screen. The object should already be re-queried
     * 								from database.
     * @throws Exception - if failed to update.
     */
    private void setConceptAt(Concept concept) throws Exception {

        final int model_index = listOfConcepts.indexOf(concept);

        if (model_index == -1) {
            throw new Exception("Concept was not found in the cache");
        }

        final Vector row = new Vector();

        row.add(resources.getString("actionComboBox.makeCC.label")); // default
        // action
        Atom atom = concept.getPreferredAtom();
        row.add(atom.getString()); // concept name
        row.add(Integer.valueOf(concept.getIdentifier().toString())); // concept_id
        row.add(atom.getCode().toString()); // code
        row.add(atom.getTermgroup().toString()); // termgroup
        row.add(String.valueOf(concept.getStatus())); // status

        if (SwingUtilities.isEventDispatchThread()) {
            editListModel.getDataVector().setElementAt(row, model_index);
            /*
             * Soma:  Changed from fireTableRowsChanged to fireTableRowsInserted
             */
            editListModel.fireTableRowsInserted(model_index, model_index);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    editListModel.getDataVector()
                            .setElementAt(row, model_index);
                    /*
                     * Soma:  Changed from fireTableRowsChanged to fireTableRowsInserted
                     */
                    editListModel
                            .fireTableRowsInserted(model_index, model_index);
                }
            });
        }

        listOfConcepts.setElementAt(concept, model_index);
    } // setConceptAt()

    /**
     * Removes specified concept from the Edit List.
     */
    void removeSourceConceptFromTheList(Concept concept) throws Exception {
        if (!listOfConcepts.contains(concept)) {
            return;
        }

        int model_index = listOfConcepts.indexOf(concept);
        int table_row = editListTable.reverseMapIndex(model_index);
        removeConcept(table_row);
    }

    /**
     * Removes a concept, specified by the row, from the Edit List.
     */
    private void removeConcept(int row) throws Exception {
        final int model_index = editListTable.mapIndex(row);

        if (SwingUtilities.isEventDispatchThread()) {
            editListModel.removeRow(model_index);
            listOfConcepts.removeElementAt(model_index);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    editListModel.removeRow(model_index);
                    listOfConcepts.removeElementAt(model_index);
                }
            });
        }

        countTF.setText(String.valueOf(listOfConcepts.size()));
    } // removeConcept()

    /**
     * Returns a number of concepts that are currently in the Edit List.
     */
    int getListSize() {
        return listOfConcepts.size();
    }

    /**
     * Shows or hides this component and: - clears all content; - resets table
     * sorting; - closes windows.
     * 
     * @param b
     *                  if <code>true</code>, shows this component; otherwise,
     *                  hides it.
     */
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            clearContent();
            editListTable.setSortState(-1, false);
            JekyllKit.closeSomeWindows();
        }
    }
}