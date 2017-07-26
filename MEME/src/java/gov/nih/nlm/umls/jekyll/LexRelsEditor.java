/*
 * LexRelsEditor.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularDeleteRelationshipAction;
import gov.nih.nlm.meme.action.MolecularInsertRelationshipAction;
import gov.nih.nlm.meme.common.Atom;
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
import java.net.SocketException;
import java.util.Date;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import samples.accessory.StringGridBagLayout;

/**
 * Window for displaying and editing SFO/LFO relationships of the current
 * concept.
 * 
 * @see <a href="src/LexRelsEditor.java.html">source </a>
 * @see <a href="src/LexRelsEditorResources.properties.html">properties </a>
 */
public class LexRelsEditor extends JFrame implements JekyllConstants,
        Refreshable, StateChangeListener {

    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // integer values for columns
    private final int TABLE_ATOM_NAME_SFO_COLUMN = 0;

    private final int TABLE_ATOM_NAME_LFO_COLUMN = 1;

    private final int TABLE_STATUS_COLUMN = 2;

    private final int TABLE_AUTHORIZED_BY_COLUMN = 3;

    private final int TABLE_AUTHORIZED_ON_COLUMN = 4;

    private final String SFO_BUTTON = "sfo.button";

    private final String LFO_BUTTON = "lfo.button";

    // Various components
    private GlassComponent glass_comp = null;

    private JTextField conceptNameTF = null;

    private JTextField conceptIdTF = null;

    private EditableTableModel relsModel = null;

    private ResizableJTable relsTable = null;

    private JTextField sfoAtomIdTF = null;

    private JTextField lfoAtomIdTF = null;

    private JTextField sfoAtomNameTF = null;

    private JTextField lfoAtomNameTF = null;

    private JMenu editMenu = null;

    // Actions
    CloseAction close_action = new CloseAction(this);

    ChangeStatusAction change_status_action = new ChangeStatusAction(this);

    TransferAtomAction transfer_atom_action = new TransferAtomAction(this);

    // Core data
    private Concept current_concept = null;

    private Atom sfo_atom = null;

    private Atom lfo_atom = null;

    private Vector listOfRels = new Vector();

    /**
     * Default constructor
     */
    public LexRelsEditor() {
        initResources();
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle.getBundle("bundles.LexRelsEditorResources");
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

        // concept name text field
        conceptNameTF = GUIToolkit.getNonEditField();

        // concept_id text field
        conceptIdTF = new JTextField(10);
        conceptIdTF.setEditable(false);
        conceptIdTF.setBackground(LABEL_BKG);
        conceptIdTF.setMinimumSize(conceptIdTF.getPreferredSize());
        conceptIdTF.setMaximumSize(conceptIdTF.getPreferredSize());

        // "Close" button
        JButton closeButton = new JButton(close_action);
        closeButton.setFont(BUTTON_FONT);
        closeButton.setBackground((Color) close_action.getValue("Background"));

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

        JLabel lexRelsLabel = new JLabel();
        lexRelsLabel.setText(resources.getString("lexRels.label"));

        contents
                .add(
                        "gridx=0,gridy=1,gridwidth=2,fill=NONE,anchor=CENTER,insets=[12,12,0,11]",
                        lexRelsLabel);

        relsModel = new EditableTableModel();
        relsModel.setColumnCount(5);
        relsModel.setRowCount(0);
        relsTable = GUIToolkit.getTable(relsModel);

        // table columns settings
        columnName = relsTable.getColumnName(TABLE_ATOM_NAME_SFO_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("relsTable.atomNameSFO.label"));

        columnName = relsTable.getColumnName(TABLE_ATOM_NAME_LFO_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("relsTable.atomNameLFO.label"));

        columnName = relsTable.getColumnName(TABLE_STATUS_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("relsTable.status.label"));
        column.setIdentifier(StringTableCellRenderer.STATUS_IDENTIFIER);

        columnName = relsTable.getColumnName(TABLE_AUTHORIZED_BY_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("relsTable.authorizedBy.label"));

        columnName = relsTable.getColumnName(TABLE_AUTHORIZED_ON_COLUMN);
        column = relsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("relsTable.authorizedOn.label"));

        relsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTextField tf = new JTextField();
        tf.setEditable(false);
        relsTable.setDefaultEditor(String.class, new DefaultCellEditor(tf));
        relsTable.setDefaultEditor(Date.class, new DateCellEditor(tf));
        StringTableCellRenderer renderer = new StringTableCellRenderer(
                relsTable, relsModel);
        relsTable.setDefaultRenderer(String.class, renderer);
        relsTable.setDefaultRenderer(Date.class, renderer);
        sp = new JScrollPane(relsTable);
        contents
                .add(
                        "gridx=0,gridy=2,gridwidth=2,fill=BOTH,anchor=CENTER,weightx=1.0,weighty=1.0,insets=[12,12,0,11]",
                        sp);

        // "SFO" button
        JButton sfoButton = new JButton(transfer_atom_action);
        sfoButton.setText(resources.getString("sfoButton.label"));
        sfoButton.setActionCommand(SFO_BUTTON);
        sfoButton.setFont(BUTTON_FONT);
        sfoButton.setBackground((Color) transfer_atom_action
                .getValue("Background"));

        // sfo_atom_id text field
        sfoAtomIdTF = new JTextField(10);
        sfoAtomIdTF.setEditable(false);
        sfoAtomIdTF.setBackground(LABEL_BKG);
        sfoAtomIdTF.setMinimumSize(sfoAtomIdTF.getPreferredSize());

        // box container
        b = Box.createHorizontalBox();
        b.add(sfoButton);
        // 	b.add(Box.createHorizontalStrut(5));
        b.add(sfoAtomIdTF);

        contents.add(
                "gridx=0,gridy=3,fill=NONE,anchor=WEST,insets=[12,12,0,11]", b);

        // "LFO" button
        JButton lfoButton = new JButton(transfer_atom_action);
        lfoButton.setText(resources.getString("lfoButton.label"));
        lfoButton.setActionCommand(LFO_BUTTON);
        lfoButton.setFont(BUTTON_FONT);
        lfoButton.setBackground((Color) transfer_atom_action
                .getValue("Background"));

        // lfo_atom_id text field
        lfoAtomIdTF = new JTextField(10);
        lfoAtomIdTF.setEditable(false);
        lfoAtomIdTF.setBackground(LABEL_BKG);
        lfoAtomIdTF.setMinimumSize(lfoAtomIdTF.getPreferredSize());

        // box container
        b = Box.createHorizontalBox();
        b.add(lfoButton);
        // 	b.add(Box.createHorizontalStrut(5));
        b.add(lfoAtomIdTF);

        contents.add(
                "gridx=1,gridy=3,fill=NONE,anchor=WEST,insets=[12,12,0,11]", b);

        // sfo atom name text field
        sfoAtomNameTF = new JTextField();
        sfoAtomNameTF.setEditable(false);
        sfoAtomNameTF.setBackground(LABEL_BKG);

        // lfo atom name text field
        lfoAtomNameTF = new JTextField();
        lfoAtomNameTF.setEditable(false);
        lfoAtomNameTF.setBackground(LABEL_BKG);

        // box container
        b = Box.createHorizontalBox();
        b.add(sfoAtomNameTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(lfoAtomNameTF);

        contents
                .add(
                        "gridx=0,gridy=4,gridwidth=2,fill=HORIZONTAL,anchor=WEST,insets=[12,12,12,11]",
                        b);

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

        item = new JMenuItem(new AddRelAction(this));
        editMenu.add(item);

        item = new JMenuItem(new DeleteRelsAction(this));
        editMenu.add(item);

        // Status submenu
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
        relsTable.clearSelection();
        relsModel.getDataVector().clear();
        relsModel.fireTableRowsInserted(0, 1);
        relsModel.fireTableDataChanged();
        listOfRels.clear();
        sfoAtomIdTF.setText(null);
        lfoAtomIdTF.setText(null);
        sfoAtomNameTF.setText(null);
        lfoAtomNameTF.setText(null);
        sfo_atom = null;
        lfo_atom = null;
    }

    // ---------------------------------
    // Interface implementation
    // ---------------------------------

    /**
     * Implements
     * {@link StateChangeListener#stateChanged(StateChangeEvent) StateChangeListener.stateChanged(StateChangeEvent)}
     * method.
     */
    public void stateChanged(StateChangeEvent e) {
        if (e.getState().equals(StateChangeEvent.BROWSE_STATE)) {
            editMenu.setEnabled(false);
        } else {
            editMenu.setEnabled(true);
        }
    }

    // ---------------------------------
    // Inner Classes
    // ---------------------------------

    /**
     * Changes status for selected relationship(s).
     * 
     * @see AbstractAction
     */
    class ChangeStatusAction extends AbstractAction {

        // constructor
        public ChangeStatusAction(Component comp) {
            putValue(Action.NAME, "Status");
            putValue(Action.SHORT_DESCRIPTION,
                    "change status of the selected relationship(s)");
            // 	    putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
            // 	    putValue("Background", Color.cyan);

        }

        public void actionPerformed(ActionEvent e) {
        }

        public boolean isEnabled() {
            return false;
        }

    } // ChangeStatusAction

    /**
     * Transfer Atom information to the screen for inserting relationship.
     * 
     * @see AbstractAction
     */
    class TransferAtomAction extends AbstractAction {
        private Component target = null;

        private String action_cmd = null;

        // constructor
        public TransferAtomAction(Component comp) {
            // 	    putValue(Action.NAME, "");
            // 	    putValue(Action.SHORT_DESCRIPTION,"");
            // 	    putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            Atom atom = JekyllKit.getClassesFrame().getSelectedAtom();

            if (atom == null) {
                MEMEToolkit.notifyUser(target,
                        "Please select an atom in the Classes screen.");
                return;
            }

            action_cmd = e.getActionCommand();

            if (action_cmd.equals(SFO_BUTTON)) {
                sfo_atom = atom;
                sfoAtomIdTF.setText(atom.getIdentifier().toString());
                sfoAtomNameTF.setText(atom.getString());
            } else {
                lfo_atom = atom;
                lfoAtomIdTF.setText(atom.getIdentifier().toString());
                lfoAtomNameTF.setText(atom.getString());
            }
        }
    } // TransferAtomAction

    /**
     * Adds SFO/LFO relationship to the current concept.
     * 
     * @see AbstractAction
     */
    class AddRelAction extends AbstractAction {
        private Component target = null;

        // constructor
        public AddRelAction(Component comp) {
            putValue(Action.NAME, "Add");
            putValue(Action.SHORT_DESCRIPTION,
                    "add an SFO/LFO relationship to the concept");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (sfo_atom == null || lfo_atom == null) {
                return;
            }

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(AddRelAction.this.getClass()
                            .getName(), true);

                    try {
                        boolean stale_data = false;
                        do {
                            try {
                                Relationship relationship = new Relationship.Default();
                                relationship.setConcept(current_concept);
                                relationship.setRelatedConcept(current_concept);
                                relationship.setName("SFO/LFO");
                                relationship.setAtom(sfo_atom);
                                relationship.setRelatedAtom(lfo_atom);
                                // 			    relationship.setAttribute("");
                                Source source = new Source.Default(JekyllKit
                                        .getAuthority().toString());
                                relationship.setSource(source); // SAB
                                relationship.setSourceOfLabel(source); // SL
                                relationship
                                        .setLevel(CoreData.FV_SOURCE_ASSERTED); // relationship
                                // level
                                relationship
                                        .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW); // relationship
                                // status
                                relationship
                                        .setTobereleased(CoreData.FV_WEAKLY_UNRELEASABLE); // tbr
                                relationship
                                        .setReleased(CoreData.FV_NOT_RELEASED); // released

                                MolecularInsertRelationshipAction mira = new MolecularInsertRelationshipAction(
                                        relationship);
                                JekyllKit.getDefaultActionClient()
                                        .processAction(mira);

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

                        MEMEToolkit.logComment(
                                "SFO/LFO relationship has successfully "
                                        + "been created for the concept "
                                        + current_concept.getIdentifier()
                                                .toString(), true);

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
                                            "Failed to create SFO/LFO relationship."
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
    } // AddRelAction

    /**
     * Deletes selected relationship(s).
     * 
     * @see AbstractAction
     */
    class DeleteRelsAction extends AbstractAction {

        private Component target = null;

        public DeleteRelsAction(Component comp) {
            putValue(Action.NAME, "Delete");
            putValue(Action.SHORT_DESCRIPTION,
                    "delete selected SFO/LFO relationship(s)");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (relsTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target, "There are no selected row(s).");
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
                        int[] selected_rows = relsTable.getSelectedRows();

                        for (int i = 0; i < selected_rows.length; i++) {
                            Relationship rel = (Relationship) listOfRels
                                    .get(relsTable.mapIndex(selected_rows[i]));
                            MolecularDeleteRelationshipAction mcra = new MolecularDeleteRelationshipAction(
                                    rel);
                            JekyllKit.getDefaultActionClient().processAction(
                                    mcra);

                            MEMEToolkit
                                    .logComment(
                                            "The SFO/LFO relationships for the concept "
                                                    + current_concept
                                                            .getIdentifier()
                                                            .toString()
                                                    + " has successfully been deleted.",
                                            true);

                            if (i == 0) {
                                current_concept
                                        .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
                            }
                        } // i loop

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
                                            "Failed to delete SFO/LFO relationship(s)."
                                                    + "\nConsole/Log file may contain more information");
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
    } // DeleteRelsAction

    /**
     * Sets content of this screen.
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

            Relationship[] rels = current_concept.getRelationships();

            if (rels.length == 0) {
                JekyllKit.getCoreDataClient().populateRelationships(
                        current_concept);
                rels = current_concept.getRelationships();
            }

            for (int i = 0; i < rels.length; i++) {
                if (rels[i].getName().equals("SFO/LFO")) {
                    final Vector row = new Vector();

                    row.add(rels[i].getAtom().getString()); // sfo
                    // atom
                    // name
                    row.add(rels[i].getRelatedAtom().getString()); // lfo
                    // atom
                    // name
                    row.add(String.valueOf(rels[i].getStatus())); // status
                    row.add(rels[i].getAuthority().toString()); // authority
                    row.add(rels[i].getTimestamp()); // authorized
                    // on

                    relsModel.addRow(row);

                    listOfRels.add(rels[i]);
                }
            }

        } catch (Exception ex) {
            if (ex instanceof MEMEException
                    && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                MEMEToolkit.reportError(LexRelsEditor.this,
                        "There was a network error."
                                + "\nPlease try the action again.", false);
            } else {
                MEMEToolkit
                        .notifyUser(
                                this,
                                "There was problem in setting content"
                                        + "\nof this window for concept: "
                                        + ((current_concept == null) ? ""
                                                : current_concept
                                                        .getIdentifier()
                                                        .toString())
                                        + "\nPlease check console/log file for more information.");
            }
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logger.logElapsedTime();
        }
    } // setContent()

    // window's size
    public Dimension getPreferredSize() {
        return new Dimension(870, 530);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            relsTable.setSortState(-1, false);
        }
    }
}