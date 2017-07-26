/**
 * ClassesFrame.java
 * Modified: 12/06/2005: Soma Lanka: Added the logic to display 
 * 	the RXCUI for the atoms that have attribute as RXCUI
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularChangeAtomAction;
import gov.nih.nlm.meme.action.MolecularMoveAction;
import gov.nih.nlm.meme.action.MolecularSplitAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.ReportsAtomComparator;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.IntegrityViolationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.exception.StaleDataException;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.meme.integrity.IntegrityCheck;
import gov.nih.nlm.meme.integrity.IntegrityVector;
import gov.nih.nlm.swing.DecreaseFontAction;
import gov.nih.nlm.swing.FontSizeManager;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.swing.IncreaseFontAction;
import gov.nih.nlm.umls.jekyll.swing.EditableTableModel;
import gov.nih.nlm.umls.jekyll.swing.ResizableJTable;
import gov.nih.nlm.umls.jekyll.util.JavaToolkit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import samples.accessory.StringGridBagLayout;

/**
 * Window for displaying and editing atom(s) for the current concept.
 * 
 * @see <a href="src/ClassesFrame.java.html">source </a>
 * @see <a href="src/ClassesFrame.properties.html">properties </a>
 */
public class ClassesFrame extends JFrame implements JekyllConstants,
        Refreshable, Switchable, Mergeable, StateChangeListener {

    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // integer values for columns
    private final int TABLE_SWITCH_COLUMN = 0;

    private final int TABLE_ATOM_NAME_COLUMN = 1;

    private final int TABLE_TERMGROUP_COLUMN = 2;

    private final int TABLE_CODE_COLUMN = 3;

    private final int TABLE_MUI_COLUMN = 4;

    private final int TABLE_AUTHORITY_COLUMN = 5;

    private final int TABLE_ATOM_ID_COLUMN = 6;

    private final int TABLE_STATUS_COLUMN = 7;

    private final int TABLE_RANK_COLUMN = 8;

    private final int TABLE_TBR_COLUMN = 9;
    
    private final int TABLE_SUPPRESSIBILITY_COLUMN = 10;
    
    private final int TABLE_ROOT_SOURCE_COLUMN = 11;
    
    private final int TABLE_BASE_AMBIGUITY_FLAG_COLUMN = 12;
    
   // private final int TABLE_RXCUI_COLUMN = 5;

    // Components
    private GlassComponent glass_comp = null;

    private JTextField conceptNameTF = null;

    private JTextField conceptIdTF = null;

    private JTextField statusTF = null;

    private JRadioButton copyRadioButton = null;

    private JRadioButton nocopyRadioButton = null;

    private JTextField countTF = null;

    private JButton atomNoteButton = null;

    private EditableTableModel atomsModel = null;

    protected ResizableJTable atomsTable = null;

    private JTextField targetConceptIdTF = null;

    private JButton apprNextButton = null;

    private JButton apprButton = null;

    private JButton moveButton = null;

    private JButton mergeButton = null;

    private JTextField mergedConceptIdTF = null;

    private JMenu moveMenu = null;

    private JMenu mergeMenu = null;

    private JMenu splitMenu = null;

    private JMenu statusMenu = null;

    // Actions
    CloseAction close_action = new CloseAction(this);

    AtomNotesEditorAction atom_notes_action = new AtomNotesEditorAction(this);

    MoveAction move_action = new MoveAction(this);

    MergeAction merge_action = new MergeAction(this);

    SplitAction split_action = new SplitAction(this);

    ChangeStatusAction change_status_action = new ChangeStatusAction(this);

    // Core data
    private Concept current_concept = null;

    private Concept source_concept = null;

    private Vector listOfAtoms = new Vector();

    /**
     * Default constructor.
     */
    public ClassesFrame() {
        initResources();
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle.getBundle("bundles.ClassesFrameResources");
    }

    private void initComponents() {
        Action a = null;
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

        // concept name text field
        conceptNameTF = GUIToolkit.getNonEditField();

        // concept_id text field
        conceptIdTF = new JTextField(10);
        conceptIdTF.setEditable(false);
        conceptIdTF.setBackground(LABEL_BKG);
        conceptIdTF.setMinimumSize(conceptIdTF.getPreferredSize());
        conceptIdTF.setMaximumSize(conceptIdTF.getPreferredSize());

        // status text field
        statusTF = new JTextField(2);
        statusTF.setEditable(false);
        statusTF.setBackground(LABEL_BKG);
        statusTF.setMinimumSize(statusTF.getPreferredSize());
        statusTF.setMaximumSize(statusTF.getPreferredSize());

        // "Close" button
        JButton closeButton = GUIToolkit.getButton(close_action);

        // box container
        b = Box.createHorizontalBox();
        b.add(conceptNameTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(conceptIdTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(statusTF);
        b.add(Box.createHorizontalStrut(12));
        b.add(closeButton);

        contents
                .add(
                        "gridx=0,gridy=0,gridwidth=2,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        // copy label
        JLabel copyLabel = new JLabel();
        copyLabel.setText(resources.getString("copy.label"));

        // copy radio button
        copyRadioButton = new JRadioButton();
        copyRadioButton.setText(resources.getString("copyRadioButton.label"));
        // 	copyRadioButton.setMnemonic(resources.getString("").charAt(0));

        // no copy radio button
        nocopyRadioButton = new JRadioButton();
        nocopyRadioButton.setText(resources
                .getString("nocopyRadioButton.label"));
        // 	nocopyRadioButton.setMnemonic(resources.getString("").charAt(0));
        nocopyRadioButton.setSelected(true);

        ButtonGroup buttonsGroup = new ButtonGroup();
        buttonsGroup.add(copyRadioButton);
        buttonsGroup.add(nocopyRadioButton);

        // label for count text field
        JLabel countLabel = new JLabel();
        countLabel.setText(resources.getString("count.label"));

        // count textfield
        countTF = new JTextField(5);
        countTF.setEditable(false);
        countTF.setForeground(LABEL_FG);
        countTF.setMinimumSize(countTF.getPreferredSize());
        countTF.setText("0");

        // box container
        b = Box.createHorizontalBox();
        b.add(copyLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(copyRadioButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(nocopyRadioButton);
        b.add(Box.createHorizontalStrut(12));
        b.add(countLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(countTF);

        contents.add(
                "gridx=0,gridy=1,fill=NONE,anchor=WEST,insets=[12,12,0,0]", b);

        // "Lex Rel" button
        a = new LexRelsEditorAction(this);
        JButton lexRelButton = new JButton(a);
        lexRelButton.setFont(BUTTON_FONT);
        lexRelButton.setBackground((Color) a.getValue("Background"));

        // "Lex Tag" button
        a = new LexTypeEditorAction(this);
        JButton lexTagButton = new JButton(a);
        lexTagButton.setFont(BUTTON_FONT);
        lexTagButton.setBackground((Color) a.getValue("Background"));

        // "Atom Note" button
        atomNoteButton = new JButton(atom_notes_action);
        atomNoteButton.setFont(BUTTON_FONT);
        atomNoteButton.setBackground((Color) atom_notes_action
                .getValue("Background"));

        // "DEF" button
        a = new DefEditorAction(this);
        JButton defButton = new JButton(a);
        defButton.setFont(BUTTON_FONT);
        defButton.setBackground((Color) a.getValue("Background"));

        // box container
        b = Box.createHorizontalBox();
        b.add(lexRelButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(lexTagButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(atomNoteButton);
        b.add(Box.createHorizontalStrut(12));
        b.add(defButton);

        contents.add(
                "gridx=1,gridy=1,fill=NONE,anchor=WEST,insets=[12,12,0,11]", b);

        atomsModel = new EditableTableModel() {
            // Overriding this method to prevent Switch column
            // from sorting when it is being edited.
            public void fireTableCellUpdated(int row, int column) {
                if (column != TABLE_SWITCH_COLUMN) {
                    super.fireTableCellUpdated(row, column);
                }
            }
        };
        atomsModel.setColumnCount(13);
        atomsModel.setRowCount(0);
        /*
         * Soma Lanka: Changed the method to call getBoldTableWithToolTipEnabled from getBoldTable
         * Users like to see the value of the cell on reducing the column width.
         */
        atomsTable = GUIToolkit.getBoldTable(atomsModel);
        

        // table columns settings
        columnName = atomsTable.getColumnName(TABLE_SWITCH_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.switch.label"));

        columnName = atomsTable.getColumnName(TABLE_ATOM_NAME_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.atomName.label"));
  
        columnName = atomsTable.getColumnName(TABLE_TERMGROUP_COLUMN);
        column = atomsTable.getColumn(columnName);
        column
                .setHeaderValue(resources
                        .getString("atomsTable.termgroup.label"));

        columnName = atomsTable.getColumnName(TABLE_CODE_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.code.label"));
              
        columnName = atomsTable.getColumnName(TABLE_MUI_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.mui.label"));

        columnName = atomsTable.getColumnName(TABLE_AUTHORITY_COLUMN);
        column = atomsTable.getColumn(columnName);
        column
                .setHeaderValue(resources
                        .getString("atomsTable.authority.label"));

        columnName = atomsTable.getColumnName(TABLE_ATOM_ID_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.atomId.label"));

        columnName = atomsTable.getColumnName(TABLE_STATUS_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.status.label"));
        column.setIdentifier(StringTableCellRenderer.STATUS_IDENTIFIER);

        // Rank column, hidden.
        columnName = atomsTable.getColumnName(TABLE_RANK_COLUMN);
        column = atomsTable.getColumn(columnName);
        
        
        // TBR column, hidden.
        columnName = atomsTable.getColumnName(TABLE_TBR_COLUMN);
        TableColumn tbr_column = atomsTable.getColumn(columnName);
        
        //      Suppressible Column, hidden
        columnName = atomsTable.getColumnName(TABLE_SUPPRESSIBILITY_COLUMN);
        TableColumn supp_column = atomsTable.getColumn(columnName);
//      RootSource Column, hidden
        columnName = atomsTable.getColumnName(TABLE_ROOT_SOURCE_COLUMN);
        TableColumn root_source_column = atomsTable.getColumn(columnName);
        
        // Base Ambiguity Flag column, hidden
        columnName = atomsTable.getColumnName(TABLE_BASE_AMBIGUITY_FLAG_COLUMN);
        TableColumn base_ambiguity_flag_column = atomsTable.getColumn(columnName);

        // Hiding columns
        atomsTable.removeColumn(column);
        atomsTable.removeColumn(tbr_column);
        atomsTable.removeColumn(supp_column);
        atomsTable.removeColumn(root_source_column);
        atomsTable.removeColumn(base_ambiguity_flag_column);

        atomsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTextField tf = new JTextField();
        tf.setEditable(false);
        atomsTable.setDefaultEditor(String.class, new DefaultCellEditor(tf));
        atomsTable.setDefaultEditor(Integer.class, new IntegerCellEditor(tf));
        StringTableCellRenderer renderer = new StringTableCellRenderer(atomsTable, atomsModel);
        renderer.setColumnIndex(StringTableCellRenderer.TBR_IDENTIFIER,
                TABLE_TBR_COLUMN);
        renderer.setColumnIndex(StringTableCellRenderer.SUPP_IDENTIFIER,
        		TABLE_SUPPRESSIBILITY_COLUMN);
        renderer.setColumnIndex(StringTableCellRenderer.SOURCE_IDENTIFIER,
        		TABLE_ROOT_SOURCE_COLUMN);
        renderer.setColumnIndex(StringTableCellRenderer.BASE_AMBIGUITY_FLAG,
        		TABLE_BASE_AMBIGUITY_FLAG_COLUMN);        
        atomsTable.setDefaultRenderer(String.class, renderer);
        atomsTable.setDefaultRenderer(Integer.class, renderer);

        atomsTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (e.getValueIsAdjusting()
                                || atomsTable.getSelectionModel()
                                        .isSelectionEmpty()) {
                            return;
                        }

                        LexTypeEditor lex_type_editor = JekyllKit
                                .getLexTypeEditor();
                        AtomNotesFrame atom_notes_frame = JekyllKit
                                .getAtomNotesFrame();
                        DefEditor def_editor = JekyllKit.getDefEditor();

                        if (!lex_type_editor.isShowing()
                                && !def_editor.isShowing()
                                && !atom_notes_frame.isShowing()) {
                            return;
                        }

                        Atom atom = (Atom) listOfAtoms.get(atomsTable
                                .mapIndex(atomsTable.getSelectedRow()));

                        if (lex_type_editor.isShowing()) {
                            lex_type_editor.setAtom(atom);
                        } else if (atom_notes_frame.isShowing()) {
                            atom_notes_frame.setAtom(atom);
                        } else {
                            def_editor.setAtom(atom);
                        }
                    }
                });
        sp = new JScrollPane(atomsTable);

        contents
                .add(
                        "gridx=0,gridy=2,gridwidth=2,fill=BOTH,anchor=CENTER,weightx=1.0,weighty=1.0,insets=[12,12,12,11]",
                        sp);

        // "Move" button
        moveButton = GUIToolkit.getButton(move_action);

        // label for target concept_id text field
        JLabel targetCIdLabel = new JLabel();
        targetCIdLabel.setText(resources.getString("targetConceptId.label"));

        // text field for target concept_id
        targetConceptIdTF = new JTextField(10);
        targetConceptIdTF.setMinimumSize(targetConceptIdTF.getPreferredSize());

        // box container
        b = Box.createHorizontalBox();
        b.add(moveButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(targetCIdLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(targetConceptIdTF);

        contents.add(
                "gridx=0,gridy=3,fill=NONE,anchor=WEST,insets=[12,12,0,0]", b);

        // "Approve/Next" button
        apprNextButton = GUIToolkit.getButton(new ApproveNextAction(this));

        // "Approve" button
        apprButton = GUIToolkit.getButton(new ApproveAction(this));

        // "Not Approve/Next" button
        JButton notApprButton = GUIToolkit.getButton(new NotApproveNextAction(
                this));

        // box container
        b = Box.createHorizontalBox();
        b.add(apprNextButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(apprButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(notApprButton);

        contents.add(
                "gridx=1,gridy=3,fill=NONE,anchor=EAST,insets=[12,12,0,11]", b);

        // "Merge" button
        mergeButton = GUIToolkit.getButton(merge_action);

        // text field for concept_id to be merged
        mergedConceptIdTF = new JTextField(10);
        mergedConceptIdTF.setMinimumSize(targetConceptIdTF.getPreferredSize());

        // label for adjacent text field
        JLabel intoCCLabel = new JLabel();
        intoCCLabel.setText(resources.getString("intoCC.label"));

        // box container
        b = Box.createHorizontalBox();
        b.add(mergeButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(mergedConceptIdTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(intoCCLabel);

        contents
                .add(
                        "gridx=0,gridy=4,gridwidth=2,fill=NONE,anchor=WEST,insets=[12,12,12,11]",
                        b);

        // adding a menu
        setJMenuBar(buildMenuBar());

        // 	TableModelListener[] listeners = atomsModel.getTableModelListeners();
        // 	for (int i = 0; i < listeners.length; i++) {
        // 	    System.out.println(i + ": " + listeners[i]);
        // 	}
    } // initComponents()

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = null;
        JMenuItem item = null;

        // File
        menu = new JMenu();
        menu.setText(resources.getString("fileMenu.label"));
        menu.setMnemonic(resources.getString("fileMenu.mnemonic").charAt(0));

        // File->Close
        item = new JMenuItem(close_action);
        menu.add(item);

        menuBar.add(menu);

        // Move
        moveMenu = new JMenu();
        moveMenu.setText("Move");
        // 	menu.setMnemonic(resources.getString("editMenu.mnemonic").charAt(0));;

        item = new JMenuItem(move_action);
        item.setText(resources.getString("moveMenuItem.label"));
        moveMenu.add(item);

        menuBar.add(moveMenu);

        // Merge
        mergeMenu = new JMenu();
        mergeMenu.setText(resources.getString("mergeMenu.label"));
        mergeMenu.setMnemonic(resources.getString("mergeMenu.mnemonic").charAt(
                0));

        item = new JMenuItem(merge_action);
        item.setText(resources.getString("mergeMenuItem.label"));
        mergeMenu.add(item);

        menuBar.add(mergeMenu);

        // Split
        splitMenu = new JMenu();
        splitMenu.setText(resources.getString("splitMenu.label"));
        splitMenu.setMnemonic(resources.getString("splitMenu.mnemonic").charAt(
                0));

        item = new JMenuItem(split_action);
        item.setText(resources.getString("splitMenuItem.broader.label"));
        item.setActionCommand(BROADER);
        splitMenu.add(item);

        item = new JMenuItem(split_action);
        item.setText(resources.getString("splitMenuItem.narrower.label"));
        item.setActionCommand(NARROWER);
        splitMenu.add(item);

        item = new JMenuItem(split_action);
        item.setText(resources.getString("splitMenuItem.otherRelated.label"));
        item.setActionCommand(OTHER_RELATED);
        splitMenu.add(item);

        item = new JMenuItem(split_action);
        item.setText(resources.getString("splitMenuItem.notRelated.label"));
        item.setActionCommand(NOT_RELATED);
        splitMenu.add(item);

        menuBar.add(splitMenu);

        // Class status
        statusMenu = new JMenu();
        statusMenu.setText(resources.getString("statusMenu.label"));

        item = new JMenuItem(change_status_action);
        item.setText(resources.getString("statusMenuItem.reviewed.label"));
        item.setActionCommand(String.valueOf(CoreData.FV_STATUS_REVIEWED));
        statusMenu.add(item);

        item = new JMenuItem(change_status_action);
        item.setText(resources.getString("statusMenuItem.unreviewed.label"));
        item.setActionCommand(String.valueOf(CoreData.FV_STATUS_UNREVIEWED));
        statusMenu.add(item);

        item = new JMenuItem(change_status_action);
        item.setText(resources.getString("statusMenuItem.needsReview.label"));
        item.setActionCommand(String.valueOf(CoreData.FV_STATUS_NEEDS_REVIEW));
        statusMenu.add(item);

        menuBar.add(statusMenu);

        // Sort By...
        menu = new JMenu();
        menu.setText(resources.getString("sortMenu.label"));
        // 	menu.setMnemonic(resources.getString("sortMenu.mnemonic").charAt(0));;

        // sort by precedence
        item = new JMenuItem(new AbstractAction() {
            {
                putValue(Action.NAME, "Precedence");
            }

            public void actionPerformed(ActionEvent e) {
                if (atomsModel.getRowCount() != 0) {
                    atomsTable.setSortState(TABLE_RANK_COLUMN, false);
                }
            }
        });
        menu.add(item);

        // sort by the worklist order
        item = new JMenuItem(new AbstractAction() {
            {
                putValue(Action.NAME, "Worklist order");
            }

            public void actionPerformed(ActionEvent e) {
                if (atomsModel.getRowCount() == 0) {
                    return;
                }

                glass_comp.setVisible(true);
                ClassesFrame.this.setCursor(Cursor
                        .getPredefinedCursor(Cursor.WAIT_CURSOR));

                ActionLogger logger = new ActionLogger(this.getClass()
                        .getName(), true);

                try {
                    // clear prior content
                    countTF.setText("0");
                    atomsTable.clearSelection();
                    atomsModel.getDataVector().clear();
                    atomsModel.fireTableRowsInserted(0, 1);
                    atomsModel.fireTableDataChanged();
                    listOfAtoms.clear();
                    targetConceptIdTF.setText(null);
                    mergedConceptIdTF.setText(null);

                    Atom[] atoms = current_concept.getAtoms();
                    if (atoms.length == 0) {
                        return;
                    }

                    ReportsAtomComparator rac = new ReportsAtomComparator(
                            current_concept);
                    Arrays.sort(atoms, rac);
                    HashMap atom_rxcui_map = new HashMap();
                    Attribute[] rxcuis = current_concept.getAttributesByName("RXCUI");
                    // Add releasable ones first
                    for (int i = 0; i < rxcuis.length; i++) {
                      if (rxcuis[i].isReleasable())
                        atom_rxcui_map.put(rxcuis[i].getAtom().getIdentifier(), rxcuis[i].getValue());
                    }
                    // Add in unreleasable ones
                    for (int i = 0; i < rxcuis.length; i++) {
                      if (!atom_rxcui_map.containsKey(rxcuis[i].getAtom().getIdentifier()))
                        atom_rxcui_map.put(rxcuis[i].getAtom().getIdentifier(), rxcuis[i].getValue());
                    }
                    for (int i = 0; i < atoms.length; i++) {

                        // do not display atoms with tbr = 'N'
                        if (atoms[i].getTobereleased() == CoreData.FV_UNRELEASABLE) {
                            continue;
                        }

                        Vector row = new Vector();

                        row.add(new Boolean(false)); // switch
                        // (default:
                        // off)
                        row.add(atoms[i].getString()); // atom name
                        row.add(atoms[i].getTermgroup().toString()); // termgroup
                        row.add(atoms[i].getCode().toString()); // code
                        if (atoms[i].getTermgroup().toString().startsWith("MSH") ||
                        		atoms[i].getTermgroup().toString().startsWith("RXNORM") ||
                        		atoms[i].getTermgroup().toString().startsWith("NCI")) {
                            row
                                    .add((atoms[i].getSourceConceptIdentifier() == null) ? ""
                                            : atoms[i].getSourceConceptIdentifier()
                                                    .toString()); // MUI/RXCUI/NCI
                        } else if ((atom_rxcui_map != null && atom_rxcui_map.containsKey(atoms[i].getIdentifier()))){
                        	row
                            .add((atom_rxcui_map.get(atoms[i].getIdentifier())== null) ? ""
                                    : atom_rxcui_map.get(atoms[i].getIdentifier()));// MUI/RXCUI                                  
                        } else {
                        	row.add("");
                        }
                        row.add(atoms[i].getAuthority().toString()); // authority
                        row.add(Integer.valueOf(atoms[i].getIdentifier()
                                .toString())); // atom id
                        row.add(String.valueOf(atoms[i].getStatus())); // status
                        row.add(Integer.valueOf(atoms[i].getTermgroup()
                                .getRank().toString())); // rank
                        row.add(String.valueOf(atoms[i].getTobereleased())); // tbr
                        row.add(String.valueOf(atoms[i].getSuppressible())); // suppressible
                        row.add(String.valueOf(atoms[i].getSource().getRootSourceAbbreviation()));

                        if (isRxNormBaseAmbiguous(atoms[i])) {
                            row.add("Y"); //The atom is RxNorm with "AMBIGUITY_FLAG" ATN set to "Base" value
                        } /*else {
                        	row.add("");
                        }*/
                        atomsModel.addRow(row);

                        listOfAtoms.add(atoms[i]);
                    } // for loop

                    countTF.setText(String.valueOf(listOfAtoms.size()));
                } catch (Exception ex) {
                    MEMEToolkit
                            .notifyUser(
                                    ClassesFrame.this,
                                    "Failed to sort atoms by the worklist order."
                                            + "\nConsole/Log file may contain more information.");
                    ex.printStackTrace(JekyllKit.getLogWriter());
                } finally {
                    glass_comp.setVisible(false);
                    ClassesFrame.this.setCursor(Cursor
                            .getPredefinedCursor(Cursor.HAND_CURSOR));
                    logger.logElapsedTime();
                }
            } // actionPerformed()
        });
        menu.add(item);

        menuBar.add(menu);

        // Options
        menu = new JMenu();
        menu.setText(resources.getString("optionsMenu.label"));
        menu.setMnemonic(resources.getString("optionsMenu.mnemonic").charAt(0));

        // Options -> Increase font
        item = new JMenuItem(new IncreaseFontAction() {
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                int new_row_height = atomsTable.getRowHeight() + 6;
                atomsTable.setRowHeight(new_row_height);
                JekyllKit.getConceptSelector().editListTable
                        .setRowHeight(new_row_height);
                JekyllKit.getRelationshipsFrame().relsTable
                        .setRowHeight(new_row_height);
            }
        });
        menu.add(item);

        // Options -> Decrease font
        item = new JMenuItem(new DecreaseFontAction() {
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                int new_row_height = atomsTable.getRowHeight() + 6;
                atomsTable.setRowHeight(new_row_height);
                JekyllKit.getConceptSelector().editListTable
                        .setRowHeight(new_row_height);
                JekyllKit.getRelationshipsFrame().relsTable
                        .setRowHeight(new_row_height);
            }
        });
        menu.add(item);

        menuBar.add(menu);

        return menuBar;
    } // buildMenuBar()

    private synchronized void clearContent() {
        conceptNameTF.setText(null);
        conceptIdTF.setText(null);
        nocopyRadioButton.setSelected(true);
        statusTF.setText(null);
        countTF.setText("0");
        atomsTable.clearSelection();
        atomsModel.getDataVector().clear();
        atomsModel.fireTableRowsInserted(0, 1);
        atomsModel.fireTableDataChanged();
        listOfAtoms.clear();
        targetConceptIdTF.setText(null);
        mergedConceptIdTF.setText(null);
    }

    // --------------------------------------
    // Interface implementation
    // --------------------------------------

    /**
     * Implements
     * {@link Mergeable#getSourceConcept() Mergeable.getSourceConcept()}.
     */
    public Concept getSourceConcept() {
        source_concept = null;
        String string = mergedConceptIdTF.getText().trim();
        if (string.equals("")) {
            return null;
        }

        // checking whether entered concept_id contains only numbers
        if (!JavaToolkit.isInteger(string)) {
            MEMEToolkit.notifyUser(this, "Invalid Concept Id: " + string);
            return null;
        }

        int concept_id = Integer.parseInt(string);

        try {
            source_concept = JekyllKit.getCoreDataClient().getConcept(
                    concept_id);
        } catch (Exception ex) {
            if (ex instanceof MissingDataException) {
                MEMEToolkit.notifyUser(this, "Concept was not found: "
                        + concept_id);
            } else {
                ex.printStackTrace(JekyllKit.getLogWriter());
                source_concept = null;
            }
        }

        if ((source_concept != null)
                && (source_concept.equals(current_concept))) {
            MEMEToolkit.notifyUser(this,
                    "Source concept is the same as the current concept");

            return null;
        }

        return source_concept;
    } // getSourceConcept()

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
        JekyllKit.getConceptSelector().removeSourceConceptFromTheList(
                source_concept);
    }

    /**
     * Implements
     * {@link Switchable#getSwitchedValues() Switchable.getSwitchedValue()}.
     */
    public Object[] getSwitchedValues() {
        Vector v = new Vector();

        for (int i = 0; i < atomsModel.getRowCount(); i++) {
            Boolean b = (Boolean) atomsModel.getValueAt(atomsTable.mapIndex(i),
                    TABLE_SWITCH_COLUMN);
            if (b.booleanValue()) {
                v.add(listOfAtoms.get(atomsTable.mapIndex(i)));
            }
        }

        return v.toArray();
    }

    /**
     * Implements
     * {@link StateChangeListener#stateChanged(StateChangeEvent) StateChangeListener.stateChanged(StateChangeEvent)}.
     */
    public void stateChanged(StateChangeEvent e) {
        if (e.getState().equals(StateChangeEvent.BROWSE_STATE)) {
            apprButton.setEnabled(false);
            apprNextButton.setEnabled(false);
            moveButton.setEnabled(false);
            mergeButton.setEnabled(false);
            moveMenu.setEnabled(false);
            mergeMenu.setEnabled(false);
            splitMenu.setEnabled(false);
            statusMenu.setEnabled(false);
        } else {
            apprButton.setEnabled(true);
            apprNextButton.setEnabled(true);
            moveButton.setEnabled(true);
            mergeButton.setEnabled(true);
            moveMenu.setEnabled(true);
            mergeMenu.setEnabled(false);
            splitMenu.setEnabled(false);
            statusMenu.setEnabled(false);
        }
    }

    // --------------------------------
    // Inner classes
    // --------------------------------

    /**
     * Invokes Lexical Rels Editor for a concept.
     * 
     * @see AbstractAction
     */
    class LexRelsEditorAction extends AbstractAction {
        private Component target = null;

        // constructor
        public LexRelsEditorAction(Component comp) {
            putValue(Action.NAME, "Lex Rel");
            putValue(Action.SHORT_DESCRIPTION, "open Lexical Rels Editor");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            LexRelsEditor frame = JekyllKit.getLexRelsEditor();

            frame.setContent(current_concept);

            glass_comp.setVisible(false);
            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);
        }
    } // LexRelsEditorAction

    /**
     * Invokes Lexical Type editor for the current concept.
     * 
     * @see AbstractAction
     */
    class LexTypeEditorAction extends AbstractAction {
        private Component target = null;

        // constructor
        public LexTypeEditorAction(Component comp) {
            putValue(Action.NAME, "Lex Tag");
            putValue(Action.SHORT_DESCRIPTION, "open Lexical Type editor");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            LexTypeEditor frame = JekyllKit.getLexTypeEditor();

            frame.setContent(current_concept);

            glass_comp.setVisible(false);
            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        }
    } // LexTypeEditorAction

    /**
     * Invokes Atom Notes Editor.
     * 
     * @see AbstractAction
     */
    class AtomNotesEditorAction extends AbstractAction {
        private Component target = null;

        // constructor
        public AtomNotesEditorAction(Component comp) {
            putValue(Action.NAME, "Atom Note");
            putValue(Action.SHORT_DESCRIPTION, "open Atom Notes Editor");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            AtomNotesFrame frame = JekyllKit.getAtomNotesFrame();

            frame.setContent(current_concept);

            glass_comp.setVisible(false);
            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        }
    } // AtomNotesEditorAction

    /**
     * Brings up Definitions Editor.
     * 
     * @see AbstractAction
     */
    class DefEditorAction extends AbstractAction {
        private Component target = null;

        // constructor
        public DefEditorAction(Component comp) {
            putValue(Action.NAME, "DEF");
            putValue(Action.SHORT_DESCRIPTION, "open Definitions Editor");
            putValue("Background", new Color(204, 153, 051)); // light
            // brown
            // color

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            DefEditor frame = JekyllKit.getDefEditor();

            frame.setContent(current_concept);

            glass_comp.setVisible(false);
            target.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

        }
    } // DefEditorAction

    /**
     * Moves selected atom(s) into another concept. Relationship(s) and STY(s)
     * are not copied over.
     * 
     * @see AbstractAction
     */
    class MoveAction extends AbstractAction {
        private Component target = null;


        // constructor
        public MoveAction(Component comp) {
            putValue(Action.NAME, "Move");
            putValue(Action.SHORT_DESCRIPTION,
                    "move switched atom(s) into another concept");
            putValue("Background", Color.cyan); // light brown
            // color

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            final Atom[] atoms = (Atom[]) JavaToolkit.castArray(
                    getSwitchedValues(), Atom.class);

            if (atoms.length == 0) {
                MEMEToolkit.notifyUser(target, "No atom(s) switched");
                return;
            }

            if (atoms.length == atomsModel.getRowCount()) {
                MEMEToolkit
                        .notifyUser(
                                target,
                                "You're trying to move remaining number"
                                        + "\n of atoms in the current concept."
                                        + "\nYou may want to consider to do merge instead.");
                return;
            }

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(MoveAction.this
                            .getClass().getName(), true);

                    String target_str = null;

                    try {
                        target_str = targetConceptIdTF.getText().trim();

                        if (target_str.equals("")) {
                            MEMEToolkit.notifyUser(target,
                                    "No target concept specified");
                            return;
                        }

                        int concept_id = Integer.parseInt(target_str);

                        if (concept_id == current_concept.getIdentifier()
                                .intValue()) {
                            MEMEToolkit
                                    .notifyUser(target,
                                            "Target concept is the same as source concept.");
                            return;
                        }

                        Concept target_concept = JekyllKit.getCoreDataClient()
                                .getConcept(concept_id);
                        MolecularMoveAction mma = new MolecularMoveAction(
                                current_concept, target_concept);

                        MEMEToolkit.logComment("checked atom(s): ");
                        for (int i = 0; i < atoms.length; i++) {
                            mma.addAtomToMove(atoms[i]);
                            MEMEToolkit.logComment(atoms[i].getIdentifier()
                                    .toString());
                        }

                        //            if (JekyllKit.getEditorLevel() != 3) {
                        // Acquire the default integrity vector
                        EnforcableIntegrityVector action_vector = JekyllKit
                                .getAuxDataClient().getApplicationVector(
                                        "DEFAULT");

                        // Acquire the override vector for this editor
                        int editor_level = JekyllKit.getEditorLevel();
                        IntegrityVector override_vector = JekyllKit
                                .getAuxDataClient().getOverrideVector(
                                        editor_level);

                        // Apply the override vector to the action vector
                        action_vector.applyOverrideVector(override_vector);

                        mma.setIntegrityVector(action_vector);
                        //            }

                        JekyllKit.getDefaultActionClient().processAction(mma);

                        MEMEToolkit.logComment(
                                "Checked atom(s) have been successfully moved into "
                                        + concept_id, true);

                        target_concept = JekyllKit.getCoreDataClient()
                                .getConcept(target_concept);
                        JekyllKit.getConceptSelector().addConcept(
                                target_concept);
                        JekyllKit.getConceptSelector().refreshConcept();

                        // Check for warnings
                        if (mma.getViolationsVector() != null) {
                            IntegrityCheck[] warnings = mma
                                    .getViolationsVector().getWarnings()
                                    .getChecks();

                            if (warnings.length == 0) {
                                return;
                            }

                            StringBuffer sb = new StringBuffer(500);
                            sb
                                    .append("\nAttempt to perform a merge on the current concept\n");
                            sb
                                    .append(target_concept.getIdentifier()
                                            .toString());
                            sb.append("\ncaused the following warning(s):\n");
                            for (int i = 0; i < warnings.length; i++) {
                                sb.append("Check name: "
                                        + warnings[i].getName());
                                sb.append("\n\t"
                                        + warnings[i].getShortDescription());
                                sb.append("\n\n");
                            }

                            JOptionPane.showMessageDialog(target,
                                    sb.toString(), "Integrity Failure",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        MEMEToolkit.notifyUser(target, "Invalid Concept Id: "
                                + target_str);
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
                        if (ex instanceof MissingDataException) {
                            MEMEToolkit.notifyUser(target,
                                    "Concept was not found: " + target_str);
                        } else if (ex instanceof IntegrityViolationException) {
                            GUIToolkit.showIntegrityViolationDialog(target,
                                    (IntegrityViolationException) ex);
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
                                            "Failed to move checked atom(s) into "
                                                    + target_str
                                                    + ".\nConsole/Log file may contain more information.");
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

    } // MoveAction

    /**
     * Splits selected atoms into another concept. Relationship(s) and STY(s)
     * can be copied over into new concept.
     * 
     * @see AbstractAction
     */
    class SplitAction extends AbstractAction {
        private Component target = null;

        private boolean set_cursor_back = true;

        // constructor
        public SplitAction(Component comp) {
            putValue(Action.NAME, "Split");
            putValue(Action.SHORT_DESCRIPTION,
                    "split switched atom(s) into other concept");
            putValue("Background", Color.cyan); // light brown
            // color

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (listOfAtoms.size() == 1) {
                MEMEToolkit.notifyUser(target,
                        "There is only one atom in a concept."
                                + "\nSplit is not allowed.");
                return;
            }

            if (getSwitchedValues().length == 0) {
                MEMEToolkit
                        .notifyUser(target,
                                "Please switch atom(s) to be split from the current concept");
                return;
            }

            if (listOfAtoms.size() == getSwitchedValues().length) {
                MEMEToolkit.notifyUser(target, "You are trying to split"
                        + "\nremaining number of atoms.");
                return;
            }

            set_cursor_back = true;

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            final String action_cmd = e.getActionCommand();

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(SplitAction.this
                            .getClass().getName(), true);

                    try {
                        MolecularSplitAction msa = new MolecularSplitAction(
                                current_concept);

                        Atom[] atoms = (Atom[]) JavaToolkit.castArray(
                                getSwitchedValues(), Atom.class);

                        MEMEToolkit.logComment("selected atom(s): ");
                        for (int i = 0; i < atoms.length; i++) {
                            msa.addAtomToSplit(atoms[i]);
                            MEMEToolkit.logComment(atoms[i].getIdentifier()
                                    .toString());
                        }

                        Relationship relationship = new Relationship.Default();
                        Source source = new Source.Default(JekyllKit
                                .getAuthority().toString());
                        relationship.setSource(source); // SAB
                        relationship.setSourceOfLabel(source); // SL
                        relationship.setLevel(CoreData.FV_MTH_ASSERTED); // relationship
                        // level
                        relationship.setStatus(CoreData.FV_STATUS_NEEDS_REVIEW); // relationship
                        // status
                        relationship.setTobereleased(CoreData.FV_RELEASABLE); // tbr
                        relationship.setReleased(CoreData.FV_NOT_RELEASED); // released

                        if (action_cmd.equals(BROADER)) {
                            relationship.setName(BROADER);
                        } else if (action_cmd.equals(NARROWER)) {
                            relationship.setName(NARROWER);
                        } else if (action_cmd.equals(OTHER_RELATED)) {
                            relationship.setName(OTHER_RELATED);
                        } else {
                            relationship.setName(NOT_RELATED);
                        }

                        msa.setSplitRelationship(relationship);

                        if (copyRadioButton.isSelected()) {
                            msa.setCloneRelationships(true);
                            msa.setCloneSemanticTypes(true);
                        } else {
                            msa.setCloneRelationships(false);
                            msa.setCloneSemanticTypes(false);
                        }

                        JekyllKit.getDefaultActionClient().processAction(msa);

                        Concept target_concept = JekyllKit.getCoreDataClient()
                                .getConcept(msa.getTarget());

                        MEMEToolkit.logComment(
                                "Selected atom(s) have been successfully split "
                                        + "into the new concept: "
                                        + target_concept.getIdentifier()
                                                .toString(), true);

                        // 			    JekyllKit.getConceptSelector().addConcepts(new int[]
                        // {target_concept.getIdentifier().intValue()});
                        JekyllKit.getConceptSelector().addConcept(
                                target_concept);
                        JekyllKit.getConceptSelector().refreshConcept();

                        // 			    set_cursor_back = false;
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
                        if (ex instanceof IntegrityViolationException) {
                            GUIToolkit.showIntegrityViolationDialog(target,
                                    (IntegrityViolationException) ex);
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
                                            "Failed to split atom(s) from the current concept."
                                                    + "\nConsole/Log file may contain more information.");
                        }
                        ex.printStackTrace(JekyllKit.getLogWriter());
                    } finally {
                        if (set_cursor_back) {
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
                }
            });

            t.start();
        }
    } // SplitAction

    class ChangeStatusAction extends AbstractAction {
        private Component target = null;

        // constructor
        public ChangeStatusAction(Component comp) {
            putValue(Action.NAME, "Change status");
            putValue(Action.SHORT_DESCRIPTION,
                    "change status of the selected atom(s)");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            final Atom[] atoms = (Atom[]) JavaToolkit.castArray(
                    getSwitchedValues(), Atom.class);

            if (atoms.length == 0) {
                MEMEToolkit.notifyUser(target, "No switched atom(s).");
                return;
            }

            JekyllKit.enableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            final String action_cmd = e.getActionCommand();

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(
                            ChangeStatusAction.this.getClass().getName(), true);

                    try {
                        char status;

                        if (action_cmd.equals(String
                                .valueOf(CoreData.FV_STATUS_REVIEWED))) {
                            status = CoreData.FV_STATUS_REVIEWED;
                        } else if (action_cmd.equals(String
                                .valueOf(CoreData.FV_STATUS_UNREVIEWED))) {
                            status = CoreData.FV_STATUS_UNREVIEWED;
                        } else {
                            status = CoreData.FV_STATUS_NEEDS_REVIEW;
                        }

                        for (int i = 0; i < atoms.length; i++) {
                            atoms[i].setStatus(status);

                            MolecularChangeAtomAction mcaa = new MolecularChangeAtomAction(
                                    atoms[i]);
                            JekyllKit.getDefaultActionClient().processAction(
                                    mcaa);

                            MEMEToolkit.logComment(
                                    "Status has been changed for atom: "
                                            + atoms[i].getIdentifier()
                                                    .toString() + " ("
                                            + atoms[i].getString() + ")", true);
                        }

                        if (atoms.length != 0) {
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
                                            "Failed to change status of the selected atom(s)."
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

            conceptNameTF.setText(current_concept.getPreferredAtom()
                    .getString()); // concept name
            conceptIdTF.setText(current_concept.getIdentifier().toString()); // concept_id
            statusTF.setText(String.valueOf(current_concept.getStatus())); // status

            Atom[] atoms = current_concept.getAtoms();
            
            /*
             * Soma Changed to add a RXCUI field to be displayed in MUI column. This is for
             * SNOMEDCT atoms that have RXCUI represented as attributes.  
             */
            //

            HashMap atom_rxcui_map = new HashMap();
            Attribute[] rxcuis = current_concept.getAttributesByName("RXCUI");
            // Add releasable ones first
            for (int i = 0; i < rxcuis.length; i++) {
              if (rxcuis[i].isReleasable())
                atom_rxcui_map.put(rxcuis[i].getAtom().getIdentifier(), rxcuis[i].getValue());
            }
            // Add in unreleasable ones
            for (int i = 0; i < rxcuis.length; i++) {
              if (!atom_rxcui_map.containsKey(rxcuis[i].getAtom().getIdentifier()))
                atom_rxcui_map.put(rxcuis[i].getAtom().getIdentifier(), rxcuis[i].getValue());
            }
            
            Vector dataVector = new Vector();
            for (int i = 0; i < atoms.length; i++) {
                // do not display atoms with tbr = 'N'
                if (atoms[i].getTobereleased() == CoreData.FV_UNRELEASABLE) {
                    continue;
                }
                Vector row = new Vector();
                row.add(new Boolean(false)); // switch (default:
                // off)
                row.add(atoms[i].getString()); // atom name
                row.add(atoms[i].getTermgroup().toString()); // termgroup
                row.add(atoms[i].getCode().toString()); // code
                /*
                 * Soma Lanka: Added a or statement to include the RXNORM CUI 
                 * This is because users requested to view the RXCUI in the MUI column 
                 * for atoms that are coming from RXNORM
                 */
                if (atoms[i].getTermgroup().toString().startsWith("MSH") ||
                		atoms[i].getTermgroup().toString().startsWith("RXNORM")) {
                    row
                            .add((atoms[i].getSourceConceptIdentifier() == null) ? ""
                                    : atoms[i].getSourceConceptIdentifier()
                                            .toString()); // MUI/RXCUI
                } else if ((atom_rxcui_map != null && atom_rxcui_map.containsKey(atoms[i].getIdentifier()))){
                	row
                    .add((atom_rxcui_map.get(atoms[i].getIdentifier())== null) ? ""
                            : atom_rxcui_map.get(atoms[i].getIdentifier()));// MUI/RXCUI                                  
                } else {
                	row.add("");
                }
                row.add(atoms[i].getAuthority().toString()); // authority
                row.add(Integer.valueOf(atoms[i].getIdentifier().toString())); // atom
                // id
                row.add(String.valueOf(atoms[i].getStatus())); // status
                row.add(Integer.valueOf(atoms[i].getTermgroup().getRank()
                        .toString())); // rank
                row.add(String.valueOf(atoms[i].getTobereleased())); // tbr
                row.add(String.valueOf(atoms[i].getSuppressible())); // suppressible
                row.add(String.valueOf(atoms[i].getSource().getRootSourceAbbreviation()));
                if (isRxNormBaseAmbiguous(atoms[i])) {
                    row.add("Y"); //The atom is RxNorm with "AMBIGUITY_FLAG" ATN set to "Base" value
                } /*else {
                	row.add("");
                }*/


                dataVector.add(row);

                listOfAtoms.add(atoms[i]);
            } // for loop

            for (Iterator i = dataVector.iterator(); i.hasNext();) {
                atomsModel.addRow((Vector) i.next());
            }

            countTF.setText(String.valueOf(listOfAtoms.size()));

            if (atoms.length != 0) {
                atomsTable.setSortState(TABLE_RANK_COLUMN, false);
            }

            // If atom notes exist for the current concept,
            // set background for the "Atom Note" button
            // to be red
            Attribute[] attrs = current_concept
                    .getAttributesByName(Attribute.ATOM_NOTE);
            if (attrs.length != 0) {
                atomNoteButton.setBackground(Color.red);
            } else {
                atomNoteButton.setBackground((Color) atom_notes_action
                        .getValue("Background"));
            }
        } catch (Exception ex) {
            MEMEToolkit.notifyUser(this, "Failed to load atoms for concept "
                    + ((current_concept == null) ? " " : current_concept
                            .getIdentifier().toString())
                    + "\nConsole/Log file may contain more information.");
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logger.logElapsedTime();
        }
    } // setContent()

    Atom getSelectedAtom() {
        Atom atom = null;

        if (!atomsTable.getSelectionModel().isSelectionEmpty()) {
            atom = (Atom) listOfAtoms.get(atomsTable.mapIndex(atomsTable
                    .getSelectedRow()));
        }

        return atom;
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            atomsTable.setSortState(-1, false);
        }
    }
    
    private boolean isRxNormBaseAmbiguous(Atom atom)
    {
  	  boolean retVal = false;
// Only check for the existence of AMBIGUITY_FLAG=Base condition, irrespective of source.  	 
//  	  if (!"RXNORM".equals(atom.getSource().getRootSourceAbbreviation()))
//  		  return retVal;
  	  final Attribute[] flags = atom.getAttributesByName("AMBIGUITY_FLAG");
  	  for (Attribute attribute : flags)
  	  if ("Base".equals(attribute.getValue()) && attribute.isReleasable())
//  			  && "RXNORM".equals(attribute.getSource().getRootSourceAbbreviation())  
  	  {
  		  retVal = true; 
  		  break; 
  	  }
  	  return retVal;
    }

}
