/*
 * RelationshipsFrame.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularChangeRelationshipAction;
import gov.nih.nlm.meme.action.MolecularDeleteRelationshipAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.CoreData;
import gov.nih.nlm.meme.common.Relationship;
import gov.nih.nlm.meme.common.ReportsRelationshipRestrictor;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.IntegrityViolationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.exception.StaleDataException;
import gov.nih.nlm.swing.DecreaseFontAction;
import gov.nih.nlm.swing.FontSizeManager;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.swing.IncreaseFontAction;
import gov.nih.nlm.umls.jekyll.swing.ResizableJTable;
import gov.nih.nlm.umls.jekyll.util.JavaToolkit;

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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import samples.accessory.StringGridBagLayout;

/**
 * Screen for displaying and editing relationships of the current concept.
 * 
 * @see <a href="src/RelationshipsFrame.java.html">source </a>
 * @see <a href="src/RelationshipsFrameResources.properties.html">properties
 *          </a>
 */
public class RelationshipsFrame extends JFrame implements Transferable,
        Switchable, Mergeable, JekyllConstants, Refreshable,
        StateChangeListener {

    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // integer values for columns
    private final int TABLE_SWITCH_COLUMN = 0;

    private final int TABLE_CONCEPT_NAME_COLUMN = 1;

    private final int TABLE_REL_COLUMN = 2;

    private final int TABLE_RELA_COLUMN = 3;

    private final int TABLE_SOURCE_COLUMN = 4;

    private final int TABLE_CONCEPT_ID_COLUMN = 5;

    private final int TABLE_LEVEL_COLUMN = 6;

    private final int TABLE_STATUS_COLUMN = 7;

    private final int TABLE_AUTHORITY_COLUMN = 8;

    private final int TABLE_TERMGROUP_COLUMN = 9;

    private final int TABLE_TBR_COLUMN = 10;

    private final int TABLE_SORT_RANK_COLUMN = 11;

    // integer values for sort column
    static final String JUST_EDITED = "7";

    static final String DEMOTED = "5";

    static final String NEEDS_REVIEW = "3";

    static final String OTHER = "0";

    // Various components
    private GlassComponent glass_comp = null;

    private JTextField conceptNameTF = null;

    private JTextField conceptIdTF = null;

    private JTextField countTF = null;

    private RelationshipsTableModel relsModel = null;

    protected ResizableJTable relsTable = null;

    private JButton mergeButton, linkButton = null;

    private JTextField mergedConceptIdTF = null;

    private JTextField linkedConceptIdTF = null;

    private JMenu editMenu = null;

    // Actions
    CloseAction close_action = new CloseAction(this);

    ShowWinRelsAction show_win_rels_action = new ShowWinRelsAction(this);

    MergeAction merge_action = new MergeAction(this);

    NewRelAction newrel_action = new NewRelAction(this);

    OverrideRelsAction override_rels_action = new OverrideRelsAction(this);

    ChangeStatusAction change_status_action = new ChangeStatusAction(this);

    BequeathAction bequeath_action = new BequeathAction(this);

    // Private Fields
    private Concept current_concept = null;

    private Concept linked_concept = null;

    private Concept source_concept = null;

    private Vector listOfRels = new Vector();

    private Vector listOfEditedRels = new Vector();

    /**
     * Default constructor.
     */
    public RelationshipsFrame() {
        initResources();
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle
                .getBundle("bundles.RelationshipsFrameResources");
    }

    private void initComponents() {
        Box b = null;
        String columnName = null;
        TableColumn column = null;
        JScrollPane sp = null;

        // set properties on this frame
        setTitle(resources.getString("window.title"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);

        // build the contents
        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());

        // concept name textfield
        conceptNameTF = GUIToolkit.getNonEditField();

        // current concept_id textfield
        conceptIdTF = new JTextField(10);
        conceptIdTF.setEditable(false);
        conceptIdTF.setBackground(LABEL_BKG);
        conceptIdTF.setMinimumSize(conceptIdTF.getPreferredSize());
        conceptIdTF.setMaximumSize(conceptIdTF.getPreferredSize());

        // "Transfer to SEL" button
        JButton transferButton = GUIToolkit.getButton(new TransferAction(this));

        // "Close" button
        JButton closeButton = GUIToolkit.getButton(close_action);

        // box container
        b = Box.createHorizontalBox();
        b.add(conceptNameTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(conceptIdTF);
        b.add(Box.createHorizontalStrut(12));
        b.add(transferButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(closeButton);

        contents
                .add(
                        "gridx=0,gridy=0,gridwidth=2,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        // label for count textfield
        JLabel countLabel = new JLabel();
        countLabel.setText(resources.getString("count.label"));

        // count textfield
        countTF = new JTextField(5);
        countTF.setEditable(false);
        countTF.setForeground(LABEL_FG);
        countTF.setMinimumSize(countTF.getPreferredSize());
        countTF.setText(resources.getString("countTextField.text"));

        // box container
        b = Box.createHorizontalBox();
        b.add(countLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(countTF);

        contents.add(
                "gridx=0,gridy=1,fill=NONE,anchor=WEST,insets=[12,12,0,0]", b);

        // "Show WINs" button
        JButton showWinButton = GUIToolkit.getButton(show_win_rels_action);

        // "Show ALL" button
        JButton showAllButton = GUIToolkit
                .getButton(new ShowAllRelsAction(this));

        // box container
        b = Box.createHorizontalBox();
        b.add(showWinButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(showAllButton);

        contents.add(
                "gridx=1,gridy=1,fill=NONE,anchor=EAST,insets=[12,12,0,11]", b);

        relsModel = new RelationshipsTableModel() {
            // Overriding this method to prevent Switch column
            // from sorting when it is being edited.
            public void fireTableCellUpdated(int row, int column) {
                if (column != TABLE_SWITCH_COLUMN) {
                    super.fireTableCellUpdated(row, column);
                }
            }
        };

        relsModel.setColumnCount(12);
        relsModel.setRowCount(0);
        relsTable = GUIToolkit.getBoldTable(relsModel);

        // table columns settings
        columnName = relsTable.getColumnName(TABLE_SWITCH_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("relsTable.switch.label"));

        columnName = relsTable.getColumnName(TABLE_CONCEPT_NAME_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("relsTable.conceptName.label"));

        columnName = relsTable.getColumnName(TABLE_REL_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("relsTable.rel.label"));

        columnName = relsTable.getColumnName(TABLE_RELA_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("relsTable.rela.label"));

        columnName = relsTable.getColumnName(TABLE_SOURCE_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("relsTable.source.label"));

        columnName = relsTable.getColumnName(TABLE_CONCEPT_ID_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("relsTable.conceptId.label"));

        columnName = relsTable.getColumnName(TABLE_LEVEL_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("relsTable.level.label"));

        columnName = relsTable.getColumnName(TABLE_STATUS_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("relsTable.status.label"));
        // 	column.setIdentifier(StringTableCellRenderer.STATUS_IDENTIFIER);

        columnName = relsTable.getColumnName(TABLE_AUTHORITY_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("relsTable.authority.label"));

        columnName = relsTable.getColumnName(TABLE_TERMGROUP_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("relsTable.termgroup.label"));

        // tbr column, invisible
        columnName = relsTable.getColumnName(TABLE_TBR_COLUMN);
        TableColumn tbr_column = relsTable.getColumn(columnName);

        // making SORT RANK column invisible
        columnName = relsTable.getColumnName(TABLE_SORT_RANK_COLUMN);
        column = relsTable.getColumn(columnName);

        // hiding columns
        relsTable.removeColumn(tbr_column);
        relsTable.removeColumn(column);

        relsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTextField tf = new JTextField();
        tf.setEditable(false);
        relsTable.setDefaultEditor(String.class, new DefaultCellEditor(tf));
        relsTable.setDefaultEditor(Integer.class, new IntegerCellEditor(tf));
        StringTableCellRenderer renderer = new StringTableCellRenderer(
                relsTable, relsModel);
        renderer.setColumnIndex(StringTableCellRenderer.SORT_RANK_IDENTIFIER,
                TABLE_SORT_RANK_COLUMN);
        renderer.setColumnIndex(StringTableCellRenderer.TBR_IDENTIFIER,
                TABLE_TBR_COLUMN);
        relsTable.setDefaultRenderer(String.class, renderer);
        relsTable.setDefaultRenderer(Integer.class, renderer);
        sp = new JScrollPane(relsTable);

        contents
                .add(
                        "gridx=0,gridy=2,gridwidth=2,fill=BOTH,anchor=CENTER,weightx=1.0,weighty=1.0,insets=[12,12,12,11]",
                        sp);

        // "Concept_id to be merged" button
        mergeButton = GUIToolkit.getButton(new ConceptIdToBeMergedAction(this));

        // concept_id textfield
        mergedConceptIdTF = new JTextField(10);
        mergedConceptIdTF.setMinimumSize(mergedConceptIdTF.getPreferredSize());
        mergedConceptIdTF.setText(resources
                .getString("mergedConceptIdTextField.text"));

        // "into CC" label
        JLabel intoCCLabel = new JLabel();
        intoCCLabel.setText(resources.getString("intoCC.label"));

        // "Concept_id to be linked" button
        linkButton = GUIToolkit.getButton(new ConceptIdToBeLinkedAction(this));

        // concept_id textfield
        linkedConceptIdTF = new JTextField(10);
        linkedConceptIdTF.setMinimumSize(linkedConceptIdTF.getPreferredSize());
        linkedConceptIdTF.setText(resources
                .getString("linkedConceptIdTextField.text"));

        // "to CC" label
        JLabel toCCLabel = new JLabel();
        toCCLabel.setText(resources.getString("toCC.label"));

        // box container
        b = Box.createHorizontalBox();
        b.add(mergeButton);
        b.add(mergedConceptIdTF);
        b.add(Box.createHorizontalStrut(3));
        b.add(intoCCLabel);
        b.add(Box.createHorizontalStrut(20));
        b.add(linkButton);
        b.add(linkedConceptIdTF);
        b.add(Box.createHorizontalStrut(3));
        b.add(toCCLabel);

        contents
                .add(
                        "gridx=0,gridy=3,gridwidth=2,fill=NONE,anchor=WEST,insets=[12,12,12,11]",
                        b);

        // adding a menu
        setJMenuBar(buildMenuBar());

    } // initComponents()

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = null;
        JMenu submenu = null;
        JMenuItem item = null;

        menu = new JMenu();
        menu.setText(resources.getString("fileMenu.label"));
        menu.setMnemonic(resources.getString("fileMenu.mnemonic").charAt(0));

        // File -> Close
        item = new JMenuItem(close_action);
        menu.add(item);

        menuBar.add(menu);

        // Edit
        editMenu = new JMenu();
        editMenu.setText(resources.getString("editMenu.label"));
        editMenu
                .setMnemonic(resources.getString("editMenu.mnemonic").charAt(0));

        item = new JMenuItem(new DeleteRelsAction(this));
        editMenu.add(item);

        item = new JMenuItem(merge_action);
        editMenu.add(item);

        // New Rel submenu
        submenu = new JMenu(resources.getString("newRelSubMenu.label"));

        item = new JMenuItem(newrel_action);
        item.setText(resources.getString("SubMenuItem.broader.label"));
        item.setActionCommand(BROADER);
        submenu.add(item);

        item = new JMenuItem(newrel_action);
        item.setText(resources.getString("SubMenuItem.narrower.label"));
        item.setActionCommand(NARROWER);
        submenu.add(item);

        item = new JMenuItem(newrel_action);
        item.setText(resources.getString("SubMenuItem.otherRelated.label"));
        item.setActionCommand(OTHER_RELATED);
        submenu.add(item);

        item = new JMenuItem(newrel_action);
        item.setText(resources.getString("SubMenuItem.notRelated.label"));
        item.setActionCommand(NOT_RELATED);
        submenu.add(item);

        editMenu.add(submenu);

        // Override Rels submenu
        submenu = new JMenu(resources.getString("overrideRelsSubMenu.label"));

        item = new JMenuItem(override_rels_action);
        item.setText(resources.getString("SubMenuItem.broader.label"));
        item.setActionCommand(BROADER);
        submenu.add(item);

        item = new JMenuItem(override_rels_action);
        item.setText(resources.getString("SubMenuItem.narrower.label"));
        item.setActionCommand(NARROWER);
        submenu.add(item);

        item = new JMenuItem(override_rels_action);
        item.setText(resources.getString("SubMenuItem.otherRelated.label"));
        item.setActionCommand(OTHER_RELATED);
        submenu.add(item);

        item = new JMenuItem(override_rels_action);
        item.setText(resources.getString("SubMenuItem.notRelated.label"));
        item.setActionCommand(NOT_RELATED);
        submenu.add(item);

        editMenu.add(submenu);

        // Status submenu
        submenu = new JMenu(resources.getString("statusSubMenu.label"));

        item = new JMenuItem(change_status_action);
        item.setText(resources.getString("statusSubMenuItem.reviewed.label"));
        item.setActionCommand(String.valueOf(APPROVED));
        submenu.add(item);

        item = new JMenuItem(change_status_action);
        item.setText(resources.getString("statusSubMenuItem.unreviewed.label"));
        item.setActionCommand(String.valueOf(UNREVIEWED));
        submenu.add(item);

        item = new JMenuItem(change_status_action);
        item
                .setText(resources
                        .getString("statusSubMenuItem.needsReview.label"));
        item.setActionCommand(String.valueOf(UNAPPROVED));
        submenu.add(item);

        item = new JMenuItem(change_status_action);
        item.setText(resources.getString("statusSubMenuItem.suggested.label"));
        item.setActionCommand(String.valueOf(SUGGESTED));
        submenu.add(item);

        editMenu.add(submenu);

        // Bequeath submenu
        submenu = new JMenu(resources.getString("bequeathSubMenu.label"));

        item = new JMenuItem(bequeath_action);
        item.setText(resources.getString("bequeathSubMenuItem.broader.label"));
        item.setActionCommand(BEQUEATHED_BROADER);
        submenu.add(item);

        item = new JMenuItem(bequeath_action);
        item.setText(resources.getString("bequeathSubMenuItem.narrower.label"));
        item.setActionCommand(BEQUEATHED_NARROWER);
        submenu.add(item);

        item = new JMenuItem(bequeath_action);
        item.setText(resources.getString("bequeathSubMenuItem.related.label"));
        item.setActionCommand(BEQUEATHED_RELATED);
        submenu.add(item);

        editMenu.add(submenu);

        menuBar.add(editMenu);

        // Options
        menu = new JMenu();
        menu.setText(resources.getString("optionsMenu.label"));
        menu.setMnemonic(resources.getString("optionsMenu.mnemonic").charAt(0));

        // Options -> Increase font
        item = new JMenuItem(new IncreaseFontAction() {
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                int new_row_height = relsTable.getRowHeight() + 6;
                relsTable.setRowHeight(new_row_height);
                JekyllKit.getConceptSelector().editListTable
                        .setRowHeight(new_row_height);
                JekyllKit.getClassesFrame().atomsTable
                        .setRowHeight(new_row_height);
            }
        });
        menu.add(item);

        // Options -> Decrease font
        item = new JMenuItem(new DecreaseFontAction() {
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                int new_row_height = relsTable.getRowHeight() + 6;
                relsTable.setRowHeight(new_row_height);
                JekyllKit.getConceptSelector().editListTable
                        .setRowHeight(new_row_height);
                JekyllKit.getClassesFrame().atomsTable
                        .setRowHeight(new_row_height);
            }
        });
        menu.add(item);

        menuBar.add(menu);

        return menuBar;
    } // buildMenuBar()

    // clears content
    private void clearContent() {
        conceptNameTF.setText(null);
        conceptIdTF.setText(null);
        countTF.setText("0");
        mergedConceptIdTF.setText("0");
        linkedConceptIdTF.setText("0");
        relsTable.clearSelection();
        relsModel.getDataVector().clear();
        relsModel.fireTableRowsInserted(0, 1);
        relsModel.fireTableDataChanged();
        listOfRels.clear();
    }

    /**
     * Returns a <code>Concept</code> if a concept_id is a valid concept in
     * the mid, null otherwise.
     */
    private Concept verifyConcept(String str) throws Exception {
        Concept concept = null;

        try {
            int concept_id = Integer.parseInt(str);
            concept = JekyllKit.getCoreDataClient().getConcept(concept_id);
        } catch (NumberFormatException ex) {
            MEMEToolkit.notifyUser(this, "Invalid Concept Id: " + str);
        } catch (Exception ex) {
            if (ex instanceof MissingDataException) {
                MEMEToolkit.notifyUser(this, "Concept was not found: " + str);
            } else if (ex instanceof MEMEException
                    && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                throw ex;
            } else {
                throw new Exception("Failed to verify concept: " + str);
            }
        }

        return concept;
    } // verifyConcept()

    // -------------------------------------
    // Intefrace implementations
    // -------------------------------------

    /**
     * Implements {@link Transferable#getConcepts() Transferable.getConcepts()}.
     */
    public Concept[] getConcepts() {
        if (relsTable.getSelectionModel().isSelectionEmpty()) {
            return new Concept[0];
        }

        int[] rows = relsTable.getSelectedRows();

        Concept[] concepts = new Concept[rows.length];

        for (int i = 0; i < rows.length; i++) {
            concepts[i] = ((Relationship) listOfRels.get(relsTable
                    .mapIndex(rows[i]))).getRelatedConcept();
        }

        return concepts;
    }

    /**
     * Implements
     * {@link Transferable#getConceptIds() Transferable.getConceptIds()}.
     */
    public int[] getConceptIds() {
        if (relsTable.getSelectionModel().isSelectionEmpty()) {
            return new int[0];
        }

        int[] rows = relsTable.getSelectedRows();

        int[] concept_ids = new int[rows.length];

        for (int i = 0; i < rows.length; i++) {
            concept_ids[i] = ((Relationship) listOfRels.get(relsTable
                    .mapIndex(rows[i]))).getRelatedConcept().getIdentifier()
                    .intValue();
        }

        return concept_ids;
    }

    /**
     * Implements
     * {@link Switchable#getSwitchedValues() Switchable.getSwitchedValue()}.
     */
    public Object[] getSwitchedValues() {
        Vector v = new Vector();

        for (int i = 0; i < relsModel.getRowCount(); i++) {
            Boolean b = (Boolean) relsModel.getValueAt(relsTable.mapIndex(i),
                    TABLE_SWITCH_COLUMN);
            if (b.booleanValue()) {
                v.add(listOfRels.get(relsTable.mapIndex(i)));
            }
        }

        return v.toArray();
    }

    /**
     * Implements
     * {@link Mergeable#getSourceConcept() Mergeable.getSourceConcept()}.
     */
    public Concept getSourceConcept() {
        source_concept = null;

        if (getSwitchedValues().length == 0) {
            MEMEToolkit.notifyUser(this, "No relationship switched for merge.");
            return null;
        }

        if (getSwitchedValues().length > 1) {
            MEMEToolkit.notifyUser(this,
                    "Multiple concept merges are not supported."
                            + "\nPlease switch only one.");

            // Reseting checkboxes
            for (int i = 0; i < relsModel.getRowCount(); i++) {
                relsModel
                        .setValueAt(new Boolean(false), i, TABLE_SWITCH_COLUMN);
            }

            return null;
        }

        Relationship rel = (Relationship) getSwitchedValues()[0];
        source_concept = rel.getRelatedConcept();

        if ((source_concept != null)
                && (source_concept.equals(current_concept))) {
            MEMEToolkit.notifyUser(this,
                    "Source concept is the same as the current concept");

            // Reseting checkboxes
            for (int i = 0; i < relsModel.getRowCount(); i++) {
                relsModel
                        .setValueAt(new Boolean(false), i, TABLE_SWITCH_COLUMN);
            }

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
     * {@link StateChangeListener#stateChanged(StateChangeEvent) StateChangeListener.stateChanged(StateChangeEvent)}.
     */
    public void stateChanged(StateChangeEvent e) {
        if (e.getState().equals(StateChangeEvent.BROWSE_STATE)) {
            linkButton.setEnabled(false);
            mergeButton.setEnabled(false);
            editMenu.setEnabled(false);
        } else {
            linkButton.setEnabled(true);
            mergeButton.setEnabled(true);
            editMenu.setEnabled(true);
        }
    }

    // ----------------------------
    // Inner Classes
    // ----------------------------

    /**
     * Table model for the Relationships table.
     */
    class RelationshipsTableModel extends DefaultTableModel {

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public void setValueAt(Object aValue, int row, int column) {
            if (column == TABLE_SWITCH_COLUMN) {
                if (((Boolean) aValue).equals(Boolean.TRUE)) {
                    mergedConceptIdTF.setText(((Integer) getValueAt(row,
                            TABLE_CONCEPT_ID_COLUMN)).toString());
                } else {
                    mergedConceptIdTF.setText("0");
                }
            }
            super.setValueAt(aValue, row, column);
        }
    } // RelationshipsTableModel

    /**
     * Shows only winning relationships.
     * 
     * @see AbstractAction
     */
    class ShowWinRelsAction extends AbstractAction {
        private Component target = null;

        // constructor
        public ShowWinRelsAction(Component comp) {
            putValue(Action.NAME, "Show WINs");
            putValue(Action.SHORT_DESCRIPTION,
                    "show only winning relationships");
            putValue("Background", new Color(204, 153, 051)); // light
            // brown
            // color

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            try {
                loadWinningRels();
            } catch (Exception ex) {
                MEMEToolkit
                        .notifyUser("Failed to display winning relationships."
                                + "\nConsole/Log file may contain more information.");
                ex.printStackTrace(JekyllKit.getLogWriter());
            } finally {
                glass_comp.setVisible(false);
                target
                        .setCursor(Cursor
                                .getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
    } // ShowWinRelsAction

    /**
     * Shows all relationships.
     * 
     * @see AbstractAction
     */
    class ShowAllRelsAction extends AbstractAction {
        private Component target = null;

        // constructor
        public ShowAllRelsAction(Component comp) {
            putValue(Action.NAME, "Show ALL");
            putValue(Action.SHORT_DESCRIPTION, "show all relationships");
            // light brown color
            putValue("Background", new Color(204, 153, 051));

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            try {
                Relationship[] rels = current_concept.getRelationships();
                addRelationships(rels);
            } catch (Exception ex) {
                MEMEToolkit.notifyUser("Failed to display all relationships."
                        + "\nConsole/Log file may contain more information.");
                ex.printStackTrace(JekyllKit.getLogWriter());
            } finally {
                glass_comp.setVisible(false);
                target
                        .setCursor(Cursor
                                .getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
    } // ShowAllRelsAction

    /**
     * Verifies if an entered concept_id to be merged is a valid concept_id in
     * the mid.
     * 
     * @see AbstractAction
     */
    class ConceptIdToBeMergedAction extends AbstractAction {
        private Component target = null;

        public ConceptIdToBeMergedAction(Component comp) {
            putValue(Action.NAME, "Concept_id to be merged:");
            putValue(Action.SHORT_DESCRIPTION,
                    "check if a valid concept_id has been entered");
            putValue("Background", Color.lightGray);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            ActionLogger logger = new ActionLogger(ConceptIdToBeMergedAction.this
                    .getClass().getName(), true);
            try {
                String str = mergedConceptIdTF.getText().trim();
                if (str.equals("") || str.equals("0")) {
                    MEMEToolkit.notifyUser(target,
                            "Please enter concept_id to be "
                                    + "merged to the current concept.");
                    return;
                }

                Concept concept = verifyConcept(str);
                if (concept != null) {
                    MEMEToolkit.notifyUser(target,
                            "The concept_id to be merged is valid.");
                }
            } catch (Exception ex) {
                if (ex instanceof MEMEException
                        && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                    MEMEToolkit.reportError(target,
                            "There was a network error."
                                    + "\nPlease try the action again.", false);
                } else {
                    MEMEToolkit
                            .notifyUser(
                                    target,
                                    "Failed to verify concept "
                                            + ((linked_concept == null) ? " "
                                                    : linked_concept
                                                            .getIdentifier()
                                                            .toString())
                                            + "\nThe console may contain additional information.");
                }
                ex.printStackTrace(JekyllKit.getLogWriter());
            } finally {
                glass_comp.setVisible(false);
                target
                        .setCursor(Cursor
                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                logger.logElapsedTime();
            }
        } // actionPerformed()

    } // ConceptIdToBeMergedAction

    /**
     * Verifies if an entered concept_id to be linked is a valid concept_id in
     * the mid.
     * 
     * @see AbstractAction
     */
    class ConceptIdToBeLinkedAction extends AbstractAction {
        private Component target = null;

        public ConceptIdToBeLinkedAction(Component comp) {
            putValue(Action.NAME, "Concept_id to be linked:");
            putValue(Action.SHORT_DESCRIPTION,
                    "check if a valid concept_id has been entered");
            putValue("Background", Color.lightGray);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            ActionLogger logger = new ActionLogger(ConceptIdToBeLinkedAction.this
                    .getClass().getName(), true);

            try {
                String str = linkedConceptIdTF.getText().trim();
                if (str.equals("") || str.equals("0")) {
                    MEMEToolkit.notifyUser(target,
                            "Please enter concept_id to be "
                                    + "linked to the current concept.");
                    return;
                }

                linked_concept = verifyConcept(str);
                if (linked_concept != null) {
                    MEMEToolkit.notifyUser(target,
                            "The concept_id to be linked is valid.");
                }
            } catch (Exception ex) {
                if (ex instanceof MEMEException
                        && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                    MEMEToolkit.reportError(target,
                            "There was a network error."
                                    + "\nPlease try the action again.", false);
                } else {
                    MEMEToolkit
                            .notifyUser(
                                    target,
                                    "Failed to verify concept "
                                            + ((linked_concept == null) ? "null"
                                                    : linked_concept
                                                            .getIdentifier()
                                                            .toString())
                                            + "\nThe console may contain additional information.");
                }
                ex.printStackTrace(JekyllKit.getLogWriter());
            } finally {
                glass_comp.setVisible(false);
                target
                        .setCursor(Cursor
                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                logger.logElapsedTime();
            }
        } // actionPerformed()

    } // ConceptIdToBeLinkedAction

    /**
     * Deletes switched relationship(s).
     * 
     * @see AbstractAction
     */
    class DeleteRelsAction extends AbstractAction {
        private Component target = null;

        public DeleteRelsAction(Component comp) {
            putValue(Action.NAME, "Delete switched rels");
            putValue(Action.SHORT_DESCRIPTION,
                    "delete switched relationship(s)");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            final Relationship[] rels = (Relationship[]) JavaToolkit.castArray(
                    getSwitchedValues(), Relationship.class);

            if (rels.length == 0) {
                MEMEToolkit.notifyUser(target, "There are no rows switched"
                        + "\nto perform this operation.");
                return;
            }

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(DeleteRelsAction.this.getClass()
                            .getName(), true);

                    try {
                        boolean source_level = false;
                        boolean need_refresh = false;

                        for (int i = 0; i < rels.length; i++) {

                            if (rels[i].getLevel() == CoreData.FV_SOURCE_ASSERTED) {
                                source_level = true;
                                continue;
                            }

                            MolecularDeleteRelationshipAction mdra = new MolecularDeleteRelationshipAction(
                                    rels[i]);
                            JekyllKit.getDefaultActionClient().processAction(
                                    mdra);

                            MEMEToolkit.logComment("Relationship from concept "
                                    + rels[i].getRelatedConcept()
                                            .getIdentifier().toString()
                                    + " to the current concept ("
                                    + current_concept.getIdentifier()
                                            .toString()
                                    + ") has successfully been deleted.", true);
                            need_refresh = true;

                            listOfEditedRels.remove(mdra
                                    .getRelationshipToDelete());

                            current_concept
                                    .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
                        } // for loop

                        if (source_level) {
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "You have attempted to delete one\n"
                                                    + "or more source level relationships.\n"
                                                    + "This is not allowed. The status for these\n"
                                                    + "relationships will remain unchanged.");
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
                                            "Failed to delete relationship(s)."
                                                    + "\nConsole/Log file may contain additional information");
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

    } // DeleteRelsAction

    /**
     * Creates a new relationship.
     * 
     * @see AbstractAction
     */
    class NewRelAction extends AbstractAction {
        private Component target = null;

        // constructor
        public NewRelAction(Component comp) {
            putValue(Action.NAME, "New Rel");
            putValue(Action.SHORT_DESCRIPTION, "create a new relationship");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            final String action_cmd = e.getActionCommand();

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(NewRelAction.this.getClass()
                            .getName(), true);

                    try {
                        String str = linkedConceptIdTF.getText().trim();

                        if (str.equals("") || str.equals("0")) {
                            MEMEToolkit.notifyUser(target,
                                    "Please enter concept_id to be "
                                            + "linked to the current concept.");
                            return;
                        }

                        linked_concept = verifyConcept(str);

                        if (linked_concept == null) {
                            return;
                        }

                        if (linked_concept.equals(current_concept)) {
                            MEMEToolkit.notifyUser(target,
                                    "Concept to be linked is the same as "
                                            + "\nthe current concept.");
                            return;
                        }

                        boolean stale_data = false;
                        do {
                            try {
                                Relationship relationship = new Relationship.Default();
                                relationship.setConcept(current_concept);
                                relationship.setRelatedConcept(linked_concept);

                                // setting relationship name
                                if (action_cmd.equals(BROADER)) {
                                    relationship.setName(BROADER);
                                } else if (action_cmd.equals(NARROWER)) {
                                    relationship.setName(NARROWER);
                                } else if (action_cmd.equals(OTHER_RELATED)) {
                                    relationship.setName(OTHER_RELATED);
                                } else {
                                    relationship.setName(NOT_RELATED);
                                }

                                Source source = new Source.Default(JekyllKit
                                        .getAuthority().toString());
                                relationship.setSource(source); // SAB
                                relationship.setSourceOfLabel(source); // SL
                                relationship.setLevel(CoreData.FV_MTH_ASSERTED); // relationship
                                // level
                                relationship
                                        .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW); // relationship
                                // status
                                relationship
                                        .setTobereleased(CoreData.FV_RELEASABLE); // tbr
                                relationship
                                        .setReleased(CoreData.FV_NOT_RELEASED); // released

                                MolecularInsertRelationshipAction mira = new MolecularInsertRelationshipAction(
                                        relationship);
                                JekyllKit.getDefaultActionClient()
                                        .processAction(mira);

                                MEMEToolkit
                                        .logComment(
                                                "The concept "
                                                        + linked_concept
                                                                .getIdentifier()
                                                                .toString()
                                                        + " now has "
                                                        + relationship
                                                                .getName()
                                                        + " relationship to the current concept ("
                                                        + current_concept
                                                                .getIdentifier()
                                                                .toString()
                                                        + ").", true);

                                listOfEditedRels.add(mira
                                        .getRelationshipToInsert());

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

                        JekyllKit.getConceptSelector().refreshConcept();
                    } catch (Exception ex) {
                        if (ex instanceof IntegrityViolationException) {
                            GUIToolkit.showIntegrityViolationDialog(target,
                                    (IntegrityViolationException) ex);
                        } else {
                            if (ex instanceof MissingDataException) {
                                MEMEToolkit.notifyUser(target, "Concept "
                                        + current_concept.getIdentifier()
                                                .toString() + " is no longer"
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
                                                "Failed to create a relationship from "
                                                        + ((linked_concept == null) ? "null"
                                                                : linked_concept
                                                                        .getIdentifier()
                                                                        .toString())
                                                        + "\nto the current concept ("
                                                        + current_concept
                                                                .getIdentifier()
                                                                .toString()
                                                        + ")."
                                                        + "\nConsole/Log file may contain additional information.");
                            }
                            ex.printStackTrace(JekyllKit.getLogWriter());
                        }
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
    } // NewRelAction

    /**
     * Overrides relationship(s).
     * 
     * @see AbstractAction
     */
    class OverrideRelsAction extends AbstractAction {
        private Component target = null;

        public OverrideRelsAction(Component comp) {
            putValue(Action.NAME, "Override Rels");
            putValue(Action.SHORT_DESCRIPTION,
                    "override switched relationship(s)");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            final Relationship[] rels = (Relationship[]) JavaToolkit.castArray(
                    getSwitchedValues(), Relationship.class);

            if (rels.length == 0) {
                MEMEToolkit.notifyUser(target,
                        "There are no switched relationship(s)"
                                + "\nto perform this operation.");
                return;
            }

            final String action_cmd = e.getActionCommand();

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(OverrideRelsAction.this
                            .getClass().getName(), true);

                    try {
                        for (int i = 0; i < rels.length; i++) {
                            Relationship relationship = new Relationship.Default();
                            relationship.setConcept(current_concept);
                            linked_concept = rels[i].getRelatedConcept();
                            relationship.setRelatedConcept(linked_concept);

                            if (action_cmd.equals(BROADER)) {
                                relationship.setName(BROADER);
                            } else if (action_cmd.equals(NARROWER)) {
                                relationship.setName(NARROWER);
                            } else if (action_cmd.equals(OTHER_RELATED)) {
                                relationship.setName(OTHER_RELATED);
                            } else {
                                relationship.setName(NOT_RELATED);
                            }

                            Source source = new Source.Default(JekyllKit
                                    .getAuthority().toString());
                            relationship.setSource(source); // SAB
                            relationship.setSourceOfLabel(source); // SL
                            relationship.setLevel(CoreData.FV_MTH_ASSERTED); // relationship
                            // level
                            relationship
                                    .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW); // relationship
                            // status
                            relationship
                                    .setTobereleased(CoreData.FV_RELEASABLE); // tbr
                            relationship.setReleased(CoreData.FV_NOT_RELEASED); // released

                            MolecularInsertRelationshipAction mira = new MolecularInsertRelationshipAction(
                                    relationship);
                            JekyllKit.getDefaultActionClient().processAction(
                                    mira);

                            if (i == 0) {
                                current_concept
                                        .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
                            }

                            MEMEToolkit
                                    .logComment(
                                            "The override was successful.\n"
                                                    + "The concept "
                                                    + linked_concept
                                                            .getIdentifier()
                                                            .toString()
                                                    + " now has "
                                                    + relationship.getName()
                                                    + " relationship to the current concept ("
                                                    + current_concept
                                                            .getIdentifier()
                                                            .toString() + ").",
                                            true);

                            listOfEditedRels
                                    .add(mira.getRelationshipToInsert());
                        }

                        if (rels.length != 0) {
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
                                            "Failed to override a relationship from "
                                                    + ((linked_concept == null) ? "null"
                                                            : linked_concept
                                                                    .getIdentifier()
                                                                    .toString())
                                                    + "\nto the current concept ("
                                                    + current_concept
                                                            .getIdentifier()
                                                            .toString()
                                                    + ")."
                                                    + "\nThe console may contain additional information.");
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

    } // OverrideRelsAction

    /**
     * Changes status of relationship(s). NOTE: Changing status of
     * relationship(s) don't make row(s) appear in purple color.
     * 
     * @see AbstractAction
     */
    class ChangeStatusAction extends AbstractAction {
        private Component target = null;

        private String action_cmd = null;

        public ChangeStatusAction(Component comp) {
            putValue(Action.NAME, "Status");
            putValue(Action.SHORT_DESCRIPTION,
                    "change the status of relationship(s)");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            final Relationship[] rels = (Relationship[]) JavaToolkit.castArray(
                    getSwitchedValues(), Relationship.class);

            if (rels.length == 0) {
                MEMEToolkit.notifyUser(target,
                        "There are no switched relationship(s)"
                                + "\nto perform this operation.");
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
                        boolean source_level = false;
                        boolean demoted = false;
                        boolean need_refresh = false;

                        for (int i = 0; i < rels.length; i++) {
                            if (rels[i].getLevel() == CoreData.FV_SOURCE_ASSERTED) {
                                source_level = true;
                                continue;
                            }

                            if (rels[i].isDemoted()) {
                                demoted = true;
                                continue;
                            }

                            if (action_cmd.equals(String.valueOf(APPROVED))) {
                                rels[i].setStatus(APPROVED);
                            } else if (action_cmd.equals(String
                                    .valueOf(UNREVIEWED))) {
                                rels[i].setStatus(UNREVIEWED);
                            } else if (action_cmd.equals(String
                                    .valueOf(UNAPPROVED))) {
                                rels[i].setStatus(UNAPPROVED);
                            } else { // Suggested
                                rels[i].setStatus(SUGGESTED);
                            }

                            MolecularChangeRelationshipAction mcra = new MolecularChangeRelationshipAction(
                                    rels[i]);
                            JekyllKit.getDefaultActionClient().processAction(
                                    mcra);

                            MEMEToolkit
                                    .logComment(
                                            "Status of the relationship from concept "
                                                    + rels[i]
                                                            .getRelatedConcept()
                                                            .getIdentifier()
                                                            .toString()
                                                    + " to the current concept ("
                                                    + current_concept
                                                            .getIdentifier()
                                                            .toString()
                                                    + ") has successfully been changed.",
                                            true);
                            need_refresh = true;

                            listOfEditedRels
                                    .add(mcra.getRelationshipToChange());
                        }

                        if (source_level) {
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "You have attempted to change the status\n"
                                                    + "of one or more source level relationships.\n"
                                                    + "This is not allowed. The status for these\n"
                                                    + "relationships will remain unchanged.");
                        }

                        if (demoted) {
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "You have attempted to change the status\n"
                                                    + "of one or more demoted relationships.\n"
                                                    + "This is not allowed. The status for these\n"
                                                    + "relationships will remain unchanged.");
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
                                            "Failed to change status for relationship(s)."
                                                    + "\nConsole/Log file may contain additional information");
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
        } // actionPerfomred()

    } // ChangeStatusAction

    /**
     * Bequeaths current concept to other concept.
     * 
     * @see AbstractAction
     */
    class BequeathAction extends AbstractAction {
        private Component target = null;

        public BequeathAction(Component comp) {
            putValue(Action.NAME, "Bequeath");
            putValue(Action.SHORT_DESCRIPTION, "");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            final String action_cmd = e.getActionCommand();

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(BequeathAction.this.getClass()
                            .getName(), true);

                    try {
                        String str = linkedConceptIdTF.getText().trim();

                        if (str.equals("") || str.equals("0")) {
                            MEMEToolkit.notifyUser(target,
                                    "Please enter concept_id to be "
                                            + "linked to the current concept.");
                            return;
                        }

                        linked_concept = verifyConcept(str);

                        if (linked_concept == null) {
                            return;
                        }

                        if (linked_concept.equals(current_concept)) {
                            MEMEToolkit.notifyUser(target,
                                    "Concept to be linked is the same as "
                                            + "\nthe current concept.");
                            return;
                        }

                        boolean stale_data = false;
                        do {
                            try {
                                Relationship relationship = new Relationship.Default();
                                relationship.setConcept(current_concept);
                                relationship.setRelatedConcept(linked_concept);

                                // setting relationship name
                                if (action_cmd.equals(BEQUEATHED_BROADER)) {
                                    relationship.setName(BEQUEATHED_BROADER);
                                } else if (action_cmd
                                        .equals(BEQUEATHED_NARROWER)) {
                                    relationship.setName(BEQUEATHED_NARROWER);
                                } else {
                                    relationship.setName(BEQUEATHED_RELATED);
                                }

                                Source source = new Source.Default(JekyllKit
                                        .getAuthority().toString());
                                relationship.setSource(source); // SAB
                                relationship.setSourceOfLabel(source); // SL
                                relationship.setLevel(CoreData.FV_MTH_ASSERTED); // relationship
                                // level
                                relationship
                                        .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW); // relationship
                                // status
                                relationship
                                        .setTobereleased(CoreData.FV_RELEASABLE); // tbr
                                relationship
                                        .setReleased(CoreData.FV_NOT_RELEASED); // released

                                MolecularInsertRelationshipAction mira = new MolecularInsertRelationshipAction(
                                        relationship);
                                JekyllKit.getDefaultActionClient()
                                        .processAction(mira);

                                MEMEToolkit
                                        .logComment(
                                                "The concept "
                                                        + linked_concept
                                                                .getIdentifier()
                                                                .toString()
                                                        + "now has "
                                                        + relationship
                                                                .getName()
                                                        + " relationship to the current concept ("
                                                        + current_concept
                                                                .getIdentifier()
                                                                .toString()
                                                        + ").", true);

                                listOfEditedRels.add(mira
                                        .getRelationshipToInsert());

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

                        JekyllKit.getConceptSelector().refreshConcept();
                    } catch (Exception ex) {
                        if (ex instanceof IntegrityViolationException) {
                            GUIToolkit.showIntegrityViolationDialog(target,
                                    (IntegrityViolationException) ex);
                        } else {
                            if (ex instanceof MissingDataException) {
                                MEMEToolkit.notifyUser(target, "Concept "
                                        + current_concept.getIdentifier()
                                                .toString() + " is no longer"
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
                                                "Failed to create a bequeathed relationship from "
                                                        + ((linked_concept == null) ? "null"
                                                                : linked_concept
                                                                        .getIdentifier()
                                                                        .toString())
                                                        + "\nto the current concept ("
                                                        + current_concept
                                                                .getIdentifier()
                                                                .toString()
                                                        + ")."
                                                        + "\nConsole/Log file may contain additional information.");
                            }
                            ex.printStackTrace(JekyllKit.getLogWriter());
                        }
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
    } // BequeathAction

    /**
     * Sets content for this screen.
     */
    public void setContent(Concept concept) {
        ActionLogger logger = new ActionLogger(this.getClass().getName() + ".setContent()",
                true);

        try {
            if (!concept.equals(current_concept)) {
                listOfEditedRels.clear();

            }
            current_concept = concept;

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            clearContent();

            if (JekyllKit.getCoreDataClient().getRelationshipCount(
                    current_concept) > 1000) {
                if (JOptionPane.showConfirmDialog(RelationshipsFrame.this,
                        "There are more than 1000 relationships"
                                + "\nfor this concept. Continue?",
                        "Load relationships?", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            conceptNameTF.setText(current_concept.getPreferredAtom()
                    .getString()); // concept name
            conceptIdTF.setText(current_concept.getIdentifier().toString()); // concept_id

            JekyllKit.getCoreDataClient()
                    .populateRelationships(current_concept);
            loadWinningRels();

        } catch (Exception ex) {
            if (ex instanceof MEMEException
                    && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                MEMEToolkit.reportError(RelationshipsFrame.this,
                        "There was a network error."
                                + "\nPlease try the action again.", false);
            } else {
                MEMEToolkit
                        .notifyUser(
                                this,
                                "Failed to load relationship(s) for the concept: "
                                        + ((current_concept == null) ? " "
                                                : source_concept
                                                        .getIdentifier()
                                                        .toString())
                                        + "\nConsole/Log file may contain additional information.");
            }
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logger.logElapsedTime();
        }
    } // setContent()

    private void loadWinningRels() throws Exception {
        Relationship[] rels = current_concept
                .getRestrictedRelationships(new ReportsRelationshipRestrictor(
                        current_concept));
        addRelationships(rels);
    }

    // adds relationship(s) data to the table
    private void addRelationships(Relationship[] rels) throws Exception {

        // clear the table first, if
        // it hasn't been cleared already
        if (listOfRels.size() != 0) {
            relsTable.clearSelection();
            relsModel.getDataVector().clear();
            relsModel.fireTableRowsInserted(0, 1);
            relsModel.fireTableDataChanged();
            listOfRels.clear();
        }

        if (rels.length == 0) {
            return;
        }

        Vector dataVector = new Vector();
        for (int i = 0; i < rels.length; i++) {
            if (rels[i].getName().equals(SFO_LFO_NAME)) {
                continue;
            }

            Vector row = new Vector();

            row.add(new Boolean(false)); // switch
            Concept relatedConcept = rels[i].getRelatedConcept();
            Atom atom = relatedConcept.getPreferredAtom();
            row.add(atom.getString()); // concept name
            row.add(rels[i].getName()); // rel
            String rela = rels[i].getAttribute();
            row.add((rela == null) ? "" : rela); // rela
            row.add(rels[i].getSource().toString()); // source
            row.add(Integer.valueOf(relatedConcept.getIdentifier().toString())); // concept
            // id
            row.add(String.valueOf(rels[i].getLevel())); // level
            row.add(String.valueOf(rels[i].getStatus())); // status
            row.add(rels[i].getAuthority().toString()); // authority
            row.add(atom.getTermgroup().toString()); // termgroup
            row.add(String.valueOf(rels[i].getTobereleased())); // tbr

            // Setting SORT_RANK column
            if (listOfEditedRels.contains(rels[i])) {
                row.add(JUST_EDITED + rels[i].getIdentifier().toString());
            } else if (rels[i].isDemoted()) {
                row.add(DEMOTED + rels[i].getIdentifier().toString());
            } else if (rels[i].getStatus() == CoreData.FV_STATUS_NEEDS_REVIEW) {
                row.add(NEEDS_REVIEW + rels[i].getIdentifier().toString());
            } else {
                row.add(OTHER + rels[i].getIdentifier().toString());
            }

            dataVector.add(row);
            listOfRels.add(rels[i]);
        } // for loop

        for (Iterator i = dataVector.iterator(); i.hasNext();) {
            relsModel.addRow((Vector) i.next());
        }

        // Sorting
        relsTable.setSortState(TABLE_SORT_RANK_COLUMN, false);

        countTF.setText(String.valueOf(listOfRels.size()));

    } // addRelationships()

    void addEditedRelationship(Relationship rel) {
        listOfEditedRels.add(rel);
    }

    void clearEditedRelationships() {
        listOfEditedRels.clear();
    }

    public Dimension getPreferredSize() {
        return new Dimension(1025, 425);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            listOfEditedRels.clear();
            relsTable.setSortState(-1, false);
        }
    }
}