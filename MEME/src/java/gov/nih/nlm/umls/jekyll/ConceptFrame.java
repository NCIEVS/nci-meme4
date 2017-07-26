/*
 * ConceptFrame.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.CUI;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptSemanticType;
import gov.nih.nlm.meme.common.CoreData;
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
import java.net.SocketException;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import samples.accessory.StringGridBagLayout;

/**
 * Concept screen.
 * 
 * @see <a href="src/ConceptFrame.java.html">source </a>
 * @see <a href="src/ConceptFrame.properties.html">properties </a>
 */
public class ConceptFrame extends JFrame implements Reportable, Refreshable,
        JekyllConstants {

    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // integer values for columns
    private final int TABLE_TERMGROUP_COLUMN = 0;

    private final int TABLE_SOURCE_CODE_COLUMN = 1;

    private final int TABLE_ATOM_NAME_COLUMN = 2;

    private final int TABLE_STATUS_COLUMN = 3;

    // Various components
    private GlassComponent glass_comp = null;

    private JTextField cuiTF = null;

    private JTextField conceptIdTF = null;

    private JTextField conceptNameTF = null;

    private JTextField nhTF = null;

    private JButton conceptNoteButton = null;

    private DefaultListModel styModel = null;

    private EditableTableModel atomsModel = null;

    private ResizableJTable atomsTable = null;

    private JTextField termgroupTF = null;

    private JTextField currentContextTF = null;

    private JTextField totalContextTF = null;

    private JTextArea defTextArea = null;

    private JTextArea contextTextArea = null;

    private JList lexTagsList = null;

    private DefaultListModel lexTagsModel = null;

    // Actions
    CloseAction close_action = new CloseAction(this);

    ConceptReportAction concept_report_action = new ConceptReportAction(this);

    ConceptNoteAction concept_note_action = new ConceptNoteAction(this);

    OfAction of_action = new OfAction(this);

    // core data
    private Concept current_concept = null;

    private Vector atomsWithContext = new Vector();

    private Vector listOfSTYs = new Vector();

    private boolean nh_exist = false;

    // Constructors
    public ConceptFrame() {
        initResources();
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle.getBundle("bundles.ConceptFrameResources");
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

        // "Report" button
        JButton reportButton = GUIToolkit.getButton(concept_report_action);

        // label for CUI textfield
        JLabel cuiLabel = GUIToolkit.getLabel();
        cuiLabel.setText(resources.getString("cui.label"));

        // CUI textfield
        cuiTF = new JTextField(10);
        cuiTF.setEditable(false);
        cuiTF.setBackground(LABEL_BKG);
        cuiTF.setMinimumSize(cuiTF.getPreferredSize());

        // label for current concept_id textfield
        JLabel conceptIdLabel = GUIToolkit.getLabel();
        conceptIdLabel.setText(resources.getString("conceptId.label"));

        // current concept_id textfield
        conceptIdTF = new JTextField(10);
        conceptIdTF.setEditable(false);
        conceptIdTF.setBackground(LABEL_BKG);
        conceptIdTF.setMinimumSize(conceptIdTF.getPreferredSize());

        // "Close" button
        JButton closeButton = GUIToolkit.getButton(close_action);

        // box container
        b = Box.createHorizontalBox();
        b.add(reportButton);
        b.add(Box.createHorizontalStrut(12));
        b.add(cuiLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(cuiTF);
        b.add(Box.createHorizontalStrut(12));
        b.add(conceptIdLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(conceptIdTF);
        b.add(Box.createHorizontalStrut(12));
        b.add(closeButton);
        contents
                .add(
                        "gridx=0,gridy=0,gridwidth=5,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        // Concept name field
        conceptNameTF = GUIToolkit.getNonEditField();
        contents
                .add(
                        "gridx=0,gridy=1,gridwidth=5,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        conceptNameTF);

        // "NH" button
        JButton nhButton = GUIToolkit.getButton(new NHAction(this));

        // "NH" field
        nhTF = new JTextField(2);
        nhTF.setEditable(false);
        int width = nhTF.getPreferredSize().width;
        int height = nhButton.getPreferredSize().height;
        Dimension d = new Dimension(width, height);
        nhTF.setMinimumSize(d);
        nhTF.setMaximumSize(d);

        // "Concept Note" button
        conceptNoteButton = GUIToolkit.getButton(concept_note_action);

        // box container
        b = Box.createHorizontalBox();
        b.add(nhButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(nhTF);
        b.add(Box.createHorizontalGlue());
        b.add(conceptNoteButton);
        contents
                .add(
                        "gridx=0,gridy=2,gridwidth=2,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,0]",
                        b);

        // "Concept Source & Termgroup" label
        JLabel atomsLabel = GUIToolkit.getHeaderLabel();
        atomsLabel.setText(resources.getString("atoms.label"));
        contents
                .add(
                        "gridx=2,gridy=2,gridwidth=3,fill=NONE,anchor=CENTER,insets=[12,12,0,11]",
                        atomsLabel);

        // label for STY(s) list
        JLabel styLabel = GUIToolkit.getHeaderLabel();
        styLabel.setText(resources.getString("stys.label"));

        // button for STY editor
        JButton styEditorButton = GUIToolkit
                .getButton(new STYEditorAction(this));

        // box container
        b = Box.createHorizontalBox();
        b.add(styLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(styEditorButton);
        contents
                .add(
                        "gridx=0,gridy=3,gridwidth=2,fill=NONE,anchor=CENTER,insets=[12,12,0,0]",
                        b);

        // atoms table
        atomsModel = new EditableTableModel();
        atomsModel.setColumnCount(4);
        atomsModel.setRowCount(0);
        atomsTable = GUIToolkit.getTable(atomsModel);

        // table columns settings
        columnName = atomsTable.getColumnName(TABLE_TERMGROUP_COLUMN);
        column = atomsTable.getColumn(columnName);
        column
                .setHeaderValue(resources
                        .getString("atomsTable.termgroup.label"));

        columnName = atomsTable.getColumnName(TABLE_SOURCE_CODE_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("atomsTable.sourceCode.label"));

        columnName = atomsTable.getColumnName(TABLE_ATOM_NAME_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.atomName.label"));

        // status column, hidden.
        columnName = atomsTable.getColumnName(TABLE_STATUS_COLUMN);
        column = atomsTable.getColumn(columnName);
        atomsTable.removeColumn(column);

        atomsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTextField tf = new JTextField();
        tf.setEditable(false);
        atomsTable.setDefaultEditor(String.class, new DefaultCellEditor(tf));
        StringTableCellRenderer renderer = new StringTableCellRenderer(
                atomsTable, atomsModel);
        renderer.setColumnIndex(StringTableCellRenderer.STATUS_IDENTIFIER,
                TABLE_STATUS_COLUMN);
        atomsTable.setDefaultRenderer(String.class, renderer);
        atomsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        atomsTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
        atomsTable.setRowHeight(atomsTable.getRowHeight() + 5);
        sp = new JScrollPane(atomsTable);
        sp.setMinimumSize(sp.getPreferredSize());
        contents
                .add(
                        "gridx=2,gridy=3,gridwidth=3,gridheight=2,fill=HORIZONTAL,anchor=CENTER,weightx=0.0,weighty=0.0,insets=[12,12,0,11]",
                        sp);

        // sty(s) list
        styModel = new DefaultListModel();
        JList styJList = new JList(styModel);
        styJList.setFont(GUIToolkit.DIALOG_FONT_BOLD_14);
        sp = new JScrollPane(styJList);
        sp.setMinimumSize(sp.getPreferredSize());
        sp.setMaximumSize(sp.getPreferredSize());
        contents
                .add(
                        "gridx=0,gridy=4,gridwidth=2,gridheight=1,fill=HORIZONTAL,anchor=CENTER,weightx=0.0,weighty=0.0,insets=[6,12,0,0]",
                        sp);
        // weighty=1.0

        // "Definition(s)" label
        JLabel defLabel = GUIToolkit.getHeaderLabel();
        defLabel.setText(resources.getString("definitions.label"));

        // "DEF" button
        JButton defEditorButton = GUIToolkit
                .getButton(new DefEditorAction(this));

        // box container
        b = Box.createHorizontalBox();
        b.add(defLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(defEditorButton);
        contents
                .add(
                        "gridx=0,gridy=6,gridwidth=3,fill=NONE,anchor=CENTER,insets=[12,12,0,0]",
                        b);

        // Source context label
        JLabel contextLabel = new JLabel();
        contextLabel.setText(resources.getString("context.label"));

        // termgroup textfield
        termgroupTF = new JTextField();
        termgroupTF.setEditable(false);
        termgroupTF.setMinimumSize(termgroupTF.getPreferredSize());

        // box container
        // 	b = Box.createHorizontalBox();
        // 	b.add(contextLabel);
        // 	b.add(Box.createHorizontalStrut(5));
        // 	b.add(termgroupTF);

        // 	contents.add("gridx=1,gridy=6,gridwidth=1,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,0]",
        // b);

        // current context textfield
        currentContextTF = new JTextField(3);
        currentContextTF.setEditable(false);
        currentContextTF.setMinimumSize(currentContextTF.getPreferredSize());
        currentContextTF.setMaximumSize(currentContextTF.getPreferredSize());

        // "OF" button
        JButton ofButton = GUIToolkit.getButton(of_action);

        // total contexts textfield
        totalContextTF = new JTextField(3);
        totalContextTF.setEditable(false);
        totalContextTF.setMinimumSize(totalContextTF.getPreferredSize());
        totalContextTF.setMaximumSize(totalContextTF.getPreferredSize());

        // box container
        b = Box.createHorizontalBox();
        b.add(contextLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(termgroupTF);
        b.add(Box.createHorizontalStrut(12));
        b.add(currentContextTF);
        b.add(ofButton);
        b.add(totalContextTF);
        contents
                .add(
                        "gridx=3,gridy=5,gridwidth=2,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        // Definitions list
        defTextArea = new GUIToolkit.NonEditTextArea(true);
        sp = new JScrollPane(defTextArea);
        // 	height = sp.getPreferredSize().height;
        // 	sp.setPreferredSize(new Dimension(400, height));
        contents
                .add(
                        "gridx=0,gridy=7,gridwidth=3,gridheight=4,fill=BOTH,anchor=CENTER,weightx=1.0,weighty=1.0,insets=[12,12,0,0]",
                        sp);

        // Context Hierarchy
        contextTextArea = new GUIToolkit.NonEditTextArea(false);
        sp = new JScrollPane(contextTextArea);
        contents
                .add(
                        "gridx=3,gridy=6,gridwidth=2,gridheight=5,fill=BOTH,anchor=CENTER,weightx=0.0,weighty=1.0,insets=[12,12,0,11]",
                        sp);

        // "Lex Tag" button
        JButton lexTagButton = GUIToolkit.getButton(new LexTypeEditorAction(
                this));

        // lexical tag textfield
        lexTagsList = new JList();
        lexTagsModel = new DefaultListModel();
        lexTagsList.setModel(lexTagsModel);
        lexTagsList.setEnabled(false);
        lexTagsList.setVisibleRowCount(1);
        sp = new JScrollPane(lexTagsList);

        // box container
        // 	b = Box.createHorizontalBox();
        // 	b.add(lexTagButton);
        // 	b.add(Box.createHorizontalStrut(5));
        // 	b.add(sp);
        // 	contents.add("gridx=0,gridy=11,gridwidth=3,fill=HORIZONTAL,anchor=WEST,insets=[12,12,12,0]",
        // b);

        // "MeSH Attr." button
        JButton meshAttrButton = new JButton();
        meshAttrButton.setText("MeSH Attr.");
        meshAttrButton.setEnabled(false);

        // NOTE: No longer used
        // "Imn" button
        // 	JButton imnButton = new JButton();
        // 	imnButton.setFont(BUTTON_FONT);
        // 	imnButton.setText("Imn");
        // 	imnButton.setEnabled(false);

        // "Not Approved/Next" button
        JButton notApprNextButton = GUIToolkit
                .getButton(new NotApproveNextAction(this));

        // box container
        b = Box.createHorizontalBox();
        b.add(lexTagButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(sp);
        b.add(Box.createHorizontalStrut(12));
        b.add(meshAttrButton);
        b.add(Box.createHorizontalGlue());
        b.add(notApprNextButton);
        contents
                .add(
                        "gridx=0,gridy=11,gridwidth=5,fill=HORIZONTAL,anchor=WEST,insets=[12,12,12,11]",
                        b);

        // adding a menu
        setJMenuBar(buildMenuBar());

    } // initComponents()

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = null;
        JMenuItem item = null;

        // file
        menu = new JMenu();
        menu.setText(resources.getString("fileMenu.label"));
        menu.setMnemonic(resources.getString("fileMenu.mnemonic").charAt(0));

        // file->close
        item = new JMenuItem(close_action);
        menu.add(item);

        menuBar.add(menu);

        // report
        menu = new JMenu();
        menu.setText(resources.getString("reportMenu.label"));
        menu.setMnemonic(resources.getString("reportMenu.mnemonic").charAt(0));

        // report->1
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.1.label"));
        menu.add(item);

        // report->2
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.2.label"));
        item.setActionCommand(ConceptReportAction.WIN_PAR_CHD_SIB);
        menu.add(item);

        // report->3
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.3.label"));
        item.setActionCommand(ConceptReportAction.WIN_ALL_CONTEXT_RELS);
        menu.add(item);

        // report->4
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.4.label"));
        item.setActionCommand(ConceptReportAction.WIN_XR_PAR_CHD);
        menu.add(item);

        // report->5
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.5.label"));
        item.setActionCommand(ConceptReportAction.WIN_XR_PAR_CHD_SIB);
        menu.add(item);

        // report->6
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.6.label"));
        item.setActionCommand(ConceptReportAction.WIN_XR_ALL_CONTEXT_RELS);
        menu.add(item);

        // report->7
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.7.label"));
        item.setActionCommand(ConceptReportAction.ALL_PAR_CHD);
        menu.add(item);

        // report->8
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.8.label"));
        item.setActionCommand(ConceptReportAction.ALL_PAR_CHD_SIB);
        menu.add(item);

        // report->9
        item = new JMenuItem(concept_report_action);
        item.setText(resources.getString("reportMenuItem.9.label"));
        item.setActionCommand(ConceptReportAction.ALL_ALL_CONTEXT_RELS);
        menu.add(item);

        menuBar.add(menu);

        // options
        menu = new JMenu();
        menu.setText(resources.getString("optionsMenu.label"));
        menu.setMnemonic(resources.getString("optionsMenu.mnemonic").charAt(0));
        ;

        // options->increase font
        item = new JMenuItem(new IncreaseFontAction());
        menu.add(item);

        // options->decrease font
        item = new JMenuItem(new DecreaseFontAction());
        menu.add(item);

        menuBar.add(menu);

        return menuBar;
    } // buildMenuBar()

    // --------------------------------
    // Interface implementation
    // --------------------------------

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
     * Sets content of the screen.
     */
    public void setContent(Concept concept) {
        ActionLogger logger = new ActionLogger(this.getClass().getName()
                + ".setContent()", true);

        try {
            current_concept = concept;

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            clearContent();

            Attribute[] attrs = new Attribute[0];

            CUI cui = current_concept.getCUI();
            cuiTF.setText((cui == null) ? "" : cui.toString()); // cui
            conceptIdTF.setText(current_concept.getIdentifier().toString()); // concept_id
            conceptNameTF.setText(current_concept.getPreferredAtom()
                    .getString()); // concept name

            // NH
            attrs = current_concept.getAttributesByName(Attribute.NON_HUMAN);
            if (attrs.length != 0) {
                nhTF.setText("Y");
                nh_exist = true;
            }

            // Concept Note
            attrs = current_concept.getAttributesByName(Attribute.CONCEPT_NOTE);
            if (attrs.length != 0) {
                conceptNoteButton.setBackground(Color.red);
            } else {
                conceptNoteButton.setBackground((Color) concept_note_action
                        .getValue("Background"));
            }

            // STY(s)
            ConceptSemanticType[] stys = current_concept.getSemanticTypes();
            for (int i = 0; i < stys.length; i++) {
                boolean duplicate = false;

                for (int k = 0; k < styModel.size(); k++) {
                    if (stys[i].getValue().equals((String) styModel.get(k))) {
                        duplicate = true;
                        break;
                    }
                }

                if (duplicate) {
                    continue;
                }

                styModel.addElement(stys[i].getValue());
                listOfSTYs.add(stys[i]);
            }

            // adding atoms
            Atom[] atoms = current_concept.getAtoms();

            final Vector dataVector = new Vector();
            for (int i = 0; i < atoms.length; i++) {
                Vector row = new Vector();

                row.add(atoms[i].getTermgroup().toString()); // termgroup
                row.add(atoms[i].getCode().toString()); // code
                row.add(atoms[i].getString()); // atom name
                row.add(String.valueOf(atoms[i].getStatus())); // atom
                // status

                dataVector.add(row);

                if (atoms[i].getFormattedContexts().length != 0) {
                    // 			    atomsWithContext.add(atoms[i].getIdentifier());
                    atomsWithContext.add(atoms[i]);
                }
            }

            for (Iterator i = dataVector.iterator(); i.hasNext();) {
                atomsModel.addRow((Vector) i.next());
            }
            // setting total number of contexts text field
            totalContextTF.setText(String.valueOf(atomsWithContext.size()));

            // loading the first context
            of_action.setCurrentContext(0);
            of_action.actionPerformed(null);

            // Definition(s)
            attrs = current_concept.getAttributesByName(Attribute.DEFINITION);
            for (int i = 0; i < attrs.length; i++) {
                defTextArea.append("(");
                defTextArea.append(attrs[i].getSource().toString());
                defTextArea.append(") ");
                defTextArea.append(attrs[i].getValue());
                defTextArea.append("\n");
                defTextArea.append("-----------------------");
                defTextArea.append("\n");
            }

            defTextArea.setCaretPosition(0);

            // Lexical tag
            attrs = current_concept.getAttributesByName(Attribute.LEXICAL_TAG);
            for (int i = 0; i < attrs.length; i++) {
                lexTagsModel.addElement(attrs[i].getValue());
            }

        } catch (Exception ex) {
            MEMEToolkit
                    .notifyUser(
                            ConceptFrame.this,
                            "Failed to load data for concept: "
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

    // clears all content
    private void clearContent() {
        nhTF.setText(null);
        styModel.clear();
        listOfSTYs.clear();
        atomsTable.clearSelection();
        atomsModel.getDataVector().clear(); // atoms table
        atomsModel.fireTableRowsInserted(0, 1);
        atomsModel.fireTableDataChanged();
        atomsWithContext.clear();
        defTextArea.setText(null); // definitions Text Area
        termgroupTF.setText(null);
        currentContextTF.setText("0");
        totalContextTF.setText("0");
        contextTextArea.setText(null);
        lexTagsModel.clear();
    }

    // --------------------------------------
    // Inner classes
    // --------------------------------------

    class NHAction extends AbstractAction {
        private Component target = null;

        public NHAction(Component comp) {
            putValue(Action.NAME, "NH");
            putValue(Action.SHORT_DESCRIPTION,
                    "toggle Non-human attribute for the concept");
            putValue("Background", JekyllConstants.LIGHT_BROWN_BKG);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(NHAction.this
                            .getClass().getName(), true);

                    try {
                        if (nh_exist) {

                            boolean stale_data = false;
                            do {
                                try {
                                    Attribute[] attrs = current_concept
                                            .getAttributesByName(Attribute.NON_HUMAN);

                                    for (int i = 0; i < attrs.length; i++) {
                                        MolecularDeleteAttributeAction mdaa = new MolecularDeleteAttributeAction(
                                                attrs[i]);
                                        JekyllKit.getDefaultActionClient()
                                                .processAction(mdaa);

                                        if (i == 0) {
                                            current_concept
                                                    .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
                                        }
                                    } // i loop

                                    if (stale_data) {
                                        stale_data = false;
                                    }
                                } catch (StaleDataException sde) {
                                    // re-reading concept
                                    current_concept = JekyllKit
                                            .getCoreDataClient().getConcept(
                                                    current_concept);
                                    stale_data = true;
                                }
                            } while (stale_data); // do loop

                            MEMEToolkit.logComment("NH attribute for concept "
                                    + current_concept.getIdentifier()
                                            .toString()
                                    + " has been successfully deleted", true);

                            nhTF.setText(null);
                            nh_exist = false;
                        } else {
                            boolean valid_stys = false;

                            for (int i = 0; i < listOfSTYs.size(); i++) {
                                String tree_pos = ((ConceptSemanticType) listOfSTYs
                                        .get(i)).getTreePosition();

                                if (!tree_pos.startsWith("A1.2") || // Anatomical
                                        // Structure
                                        !tree_pos.equals("B2.2.1.2.1") || // Disease
                                        // or
                                        // Syndrome
                                        !tree_pos.equals("B2.2.1.2.1.2")) { // Neoplastic
                                    // Process
                                    valid_stys = false;
                                    break;
                                }

                                valid_stys = true;
                            }

                            if (!valid_stys) {
                                MEMEToolkit
                                        .notifyUser(
                                                target,
                                                "This concept does not have a Semantic Type"
                                                        + "\ncompatible with an NH (non-human) designation.");
                                return;
                            }

                            boolean stale_data = false;
                            do {
                                try {
                                    Attribute attr = new Attribute.Default();
                                    attr.setName(Attribute.NON_HUMAN);
                                    attr.setValue("");
                                    attr.setConcept(current_concept);
                                    Source source = new Source.Default(
                                            JekyllKit.getAuthority().toString());
                                    attr.setSource(source); // SAB
                                    attr.setLevel(CoreData.FV_MTH_ASSERTED); // attribute
                                    // level
                                    attr
                                            .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW); // attribute
                                    // status
                                    attr
                                            .setTobereleased(CoreData.FV_RELEASABLE); // tbr
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
                                    current_concept = JekyllKit
                                            .getCoreDataClient().getConcept(
                                                    current_concept);
                                    stale_data = true;
                                }
                            } while (stale_data); // do loop

                            MEMEToolkit.logComment(
                                    "NH attribute has been successfully added to the concept "
                                            + current_concept.getIdentifier()
                                                    .toString(), true);
                            nhTF.setText("Y");
                            nh_exist = true;
                        } // if
                    } catch (Exception ex) {
                        if (ex instanceof MissingDataException) {
                            MEMEToolkit.notifyUser(target, "Concept "
                                    + current_concept.getIdentifier()
                                            .toString() + " is not"
                                    + "\na valid concept in the database."
                                    + "\nPlease re-read a concept.");
                        } else if (ex instanceof MEMEException
                                && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                            MEMEToolkit.reportError(target,
                                    "There was a network error."
                                            + "\nPlease try the action again.",
                                    false);
                        } else if (nh_exist) {
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "Failed to remove NH attribute for the concept."
                                                    + "\nConsole/Log file may contain more information.");
                        } else {
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "Failed to add NH attribute to the concept."
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
    } // NHAction

    class STYEditorAction extends AbstractAction {
        // constructor
        public STYEditorAction(Component comp) {
            putValue(Action.NAME, "STY");
            putValue(Action.SHORT_DESCRIPTION, "open STY editor");
            putValue("Background", Color.cyan);

            //target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            STYEditor frame = JekyllKit.getSTYEditor();

            frame.setContent(current_concept);

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        } // actionPerformed()

    } // STYEditorAction

    class ConceptNoteAction extends AbstractAction {
        //private Component target = null;

        // constructor
        public ConceptNoteAction(Component comp) {
            putValue(Action.NAME, "Concept Note");
            putValue(Action.SHORT_DESCRIPTION, "open Concept Notes editor");
            putValue("Background", Color.cyan);

            //target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            ConceptNotesFrame frame = JekyllKit.getConceptNotesFrame();

            frame.setContent(current_concept);

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        } // actionPerformed()

    } // ConceptNoteAction

    /**
     * Invokes Definitions Editor.
     * 
     * @see AbstractAction
     */
    class DefEditorAction extends AbstractAction {
        //private Component target = null;

        // constructor
        public DefEditorAction(Component comp) {
            putValue(Action.NAME, "DEF");
            putValue(Action.SHORT_DESCRIPTION, "open Definitions Editor");
            putValue("Background", Color.cyan);

            //target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            DefEditor frame = JekyllKit.getDefEditor();

            frame.setContent(current_concept);

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        } // actionPerformed()

    } // DefEditorAction

    /**
     * Invokes Lexical Type editor for the current concept.
     * 
     * @see AbstractAction
     */
    class LexTypeEditorAction extends AbstractAction {
        //private Component target = null;

        // constructor
        public LexTypeEditorAction(Component comp) {
            putValue(Action.NAME, "Lex Tag");
            putValue(Action.SHORT_DESCRIPTION, "open Lexical Type editor");
            putValue("Background", new Color(204, 153, 051)); // light
            // brown
            // color

            //target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            LexTypeEditor frame = JekyllKit.getLexTypeEditor();

            frame.setContent(current_concept);

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        } // actionPerformed()

    } // LexTypeEditorAction

    /**
     * Allows to navigate through the list of available contexts.
     * 
     * @see AbstractAction
     */
    class OfAction extends AbstractAction {
        //private Component target = null;

        private int current_context = 0;

        // constructor
        public OfAction(Component comp) {
            putValue(Action.NAME, "OF");
            putValue(Action.SHORT_DESCRIPTION,
                    "navigate through available contexts");
            putValue("Background", Color.cyan);

            //target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            // 	    target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Thread t = new Thread(new Runnable() {
                public void run() {

                    try {
                        int total_number_of_contexts = atomsWithContext.size();

                        if (total_number_of_contexts == 0) {
                            return;
                        }

                        if (current_context == total_number_of_contexts) {
                            current_context = 0;
                        }

                        Atom atom = (Atom) atomsWithContext
                                .get(current_context);
                        Attribute[] contexts = atom.getFormattedContexts();

                        termgroupTF.setText(atom.getTermgroup().toString()); // termgroup
                        contextTextArea.setText(null); // clear the
                        // area

                        for (int i = 0; i < contexts.length; i++) {
                            contextTextArea.append(contexts[i].getValue());
                        }

                        contextTextArea.setCaretPosition(0);

                        current_context++;
                        currentContextTF.setText(String
                                .valueOf(current_context));
                    } catch (Exception ex) {
                        ex.printStackTrace(JekyllKit.getLogWriter());
                    } finally {
                        // 			    target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                }
            });

            t.start();
        } // actionPerformed()

        public void setCurrentContext(int context) {
            if (context >= atomsWithContext.size()) {
                current_context = 0;
            } else {
                current_context = context;
            }
        }

    } // OfAction

    public Dimension getPreferredSize() {
        return new Dimension(1035, 840);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            atomsTable.setSortState(-1, false);
        }
    }
}