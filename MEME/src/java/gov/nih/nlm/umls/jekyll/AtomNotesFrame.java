/*
 * AtomNotesFrame.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularChangeAttributeAction;
import gov.nih.nlm.meme.action.MolecularDeleteAttributeAction;
import gov.nih.nlm.meme.action.MolecularInsertAttributeAction;
import gov.nih.nlm.meme.common.Atom;
import gov.nih.nlm.meme.common.Attribute;
import gov.nih.nlm.meme.common.Concept;
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import samples.accessory.StringGridBagLayout;

/**
 * Window for displaying and editing of atom notes.
 * 
 * @see <a href="src/AtomNotesFrame.java.html">source </a>
 * @see <a href="src/AtomNotesFrame.properties.html">properties </a>
 */
public class AtomNotesFrame extends JFrame implements JekyllConstants,
        Refreshable, StateChangeListener {

    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // integer values for table columns
    private final int TABLE_ATOM_NAME_COLUMN = 0;

    private final int TABLE_STATUS_COLUMN = 1;

    private final int TABLE_AUTHORIZED_BY_COLUMN = 2;

    private final int TABLE_AUTHORIZED_ON_COLUMN = 3;

    // Various components
    private GlassComponent glass_comp = null;

    private JTextField conceptNameTF = null;

    private JTextField conceptIdTF = null;

    private EditableTableModel attrsModel = null;

    private ResizableJTable attrsTable = null;

    private JTextArea textArea = null;

    private JTextField atomIdTF = null;

    private JTextField atomNameTF = null;

    private JMenu editMenu = null;

    // Actions
    CloseAction close_action = new CloseAction(this);

    ChangeStatusAction change_status_action = new ChangeStatusAction(this);

    private Concept current_concept = null;

    private Atom current_atom = null;

    private Vector listOfAttrs = new Vector();

    /**
     * Default constructor
     */
    public AtomNotesFrame() {
        this.setName("AtomNotesFrame");
        initResources();
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle.getBundle("bundles.AtomNotesFrameResources");
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
                        "gridx=0,gridy=0,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        attrsModel = new EditableTableModel();
        attrsModel.setColumnCount(4);
        attrsModel.setRowCount(0);
        attrsTable = GUIToolkit.getTable(attrsModel);

        // table columns settings
        columnName = attrsTable.getColumnName(TABLE_ATOM_NAME_COLUMN);
        column = attrsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("attrsTable.atomName.label"));

        columnName = attrsTable.getColumnName(TABLE_STATUS_COLUMN);
        column = attrsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("attrsTable.status.label"));
        column.setIdentifier(StringTableCellRenderer.STATUS_IDENTIFIER);

        columnName = attrsTable.getColumnName(TABLE_AUTHORIZED_BY_COLUMN);
        column = attrsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("attrsTable.authorizedBy.label"));

        columnName = attrsTable.getColumnName(TABLE_AUTHORIZED_ON_COLUMN);
        column = attrsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("attrsTable.authorizedOn.label"));

        attrsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTextField tf = new JTextField();
        tf.setEditable(false);
        new DefaultCellEditor(tf);
        attrsTable.setDefaultEditor(String.class, new DefaultCellEditor(tf));
        attrsTable.setDefaultEditor(Date.class, new DateCellEditor(tf));
        StringTableCellRenderer renderer = new StringTableCellRenderer(
                attrsTable, attrsModel);
        attrsTable.setDefaultRenderer(String.class, renderer);
        attrsTable.setDefaultRenderer(Date.class, renderer);
        attrsTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (e.getValueIsAdjusting()) {
                            return;
                        }
                        if (attrsTable.getSelectionModel().isSelectionEmpty()) {
                            return;
                        }

                        Attribute attr = (Attribute) listOfAttrs.get(attrsTable
                                .mapIndex(attrsTable.getSelectedRow()));
                        textArea.setText(attr.getValue());
                        atomIdTF.setText(attr.getAtom().getIdentifier()
                                .toString());
                        atomNameTF.setText(attr.getAtom().getString());
                    }
                });
        sp = new JScrollPane(attrsTable);
        Dimension d = new Dimension(sp.getPreferredSize().width, 150);
        sp.setPreferredSize(d);
        sp.setMinimumSize(d);

        contents.add(
                "gridx=0,gridy=1,fill=BOTH,anchor=CENTER,insets=[12,12,0,11]",
                sp);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        sp = new JScrollPane(textArea);

        contents
                .add(
                        "gridx=0,gridy=2,fill=BOTH,anchor=CENTER,weightx=1.0,weighty=1.0,insets=[12,12,0,11]",
                        sp);

        // atom_id text field
        atomIdTF = new JTextField(10);
        atomIdTF.setEditable(false);
        atomIdTF.setBackground(LABEL_BKG);
        atomIdTF.setMinimumSize(conceptIdTF.getPreferredSize());
        atomIdTF.setMaximumSize(conceptIdTF.getPreferredSize());

        // atom name text field
        atomNameTF = new JTextField();
        atomNameTF.setEditable(false);
        atomNameTF.setBackground(LABEL_BKG);

        // box container
        b = Box.createHorizontalBox();
        b.add(atomIdTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(atomNameTF);

        contents
                .add(
                        "gridx=0,gridy=3,fill=HORIZONTAL,anchor=WEST,insets=[12,12,12,11]",
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

        // File->Close
        item = new JMenuItem(close_action);
        menu.add(item);

        menuBar.add(menu);

        // Edit
        editMenu = new JMenu();
        editMenu.setText(resources.getString("editMenu.label"));
        editMenu
                .setMnemonic(resources.getString("editMenu.mnemonic").charAt(0));

        item = new JMenuItem(new AddNoteAction(this));
        editMenu.add(item);

        item = new JMenuItem(new DeleteNoteAction(this));
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
        attrsTable.clearSelection();
        attrsModel.getDataVector().clear();
        attrsModel.fireTableRowsInserted(0, 1);
        attrsModel.fireTableDataChanged();
        listOfAttrs.clear();
        current_atom = null;
        textArea.setText(null);
        atomIdTF.setText(null);
        atomNameTF.setText(null);
    }

    // -------------------------------------
    // Interface implementation
    // -------------------------------------

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

    // -------------------------------------
    // Inner Classes
    // -------------------------------------

    /**
     * Adds a note to the specified atom.
     * 
     * @see AbstractAction
     */
    class AddNoteAction extends AbstractAction {
        private Component target = null;

        private String text = null;

        // constructor
        public AddNoteAction(Component comp) {
            putValue(Action.NAME, "Add");
            putValue(Action.SHORT_DESCRIPTION, "add note to an atom");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (current_atom == null) {
                MEMEToolkit.notifyUser(target,
                        "No atom selected in the Classes screen");
                return;
            }

            text = textArea.getText().trim();
            if (text.equals("")) {
                MEMEToolkit.notifyUser(target, "The note is empty.");
                return;
            }

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(AddNoteAction.this.getClass()
                            .getName(), true);

                    try {

                        boolean stale_data = false;
                        do {
                            try {
                                Attribute attr = new Attribute.Default();
                                attr.setName(Attribute.ATOM_NOTE);
                                attr.setValue(text);
                                attr.setConcept(current_concept);
                                attr.setAtom(current_atom);
                                Source source = new Source.Default(JekyllKit
                                        .getAuthority().toString());
                                attr.setSource(source); // SAB
                                attr.setLevel(CoreData.FV_SOURCE_ASSERTED); // attribute
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

                        MEMEToolkit.logComment(
                                "Note has been successfully added to the atom "
                                        + current_atom.getIdentifier()
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
                                            "Failed to add a note to the atom (AUI: "
                                                    + ((current_atom == null) ? ""
                                                            : current_atom
                                                                    .getAUI()
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
        }
    } // AddNoteAction

    /**
     * Deletes atom note(s).
     * 
     * @see AbstractAction
     */
    class DeleteNoteAction extends AbstractAction {
        private Component target = null;

        // constructor
        public DeleteNoteAction(Component comp) {
            putValue(Action.NAME, "Delete");
            putValue(Action.SHORT_DESCRIPTION, "delete selected atom note(s)");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (attrsTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target, "There are no selected row(s).");
                return;
            }

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(DeleteNoteAction.this.getClass()
                            .getName(), true);

                    try {
                        int[] selected_rows = attrsTable.getSelectedRows();

                        for (int i = 0; i < selected_rows.length; i++) {
                            Attribute attr = (Attribute) listOfAttrs
                                    .get(attrsTable.mapIndex(selected_rows[i]));

                            MolecularDeleteAttributeAction mdaa = new MolecularDeleteAttributeAction(
                                    attr);
                            JekyllKit.getDefaultActionClient().processAction(
                                    mdaa);

                            MEMEToolkit.logComment("Note (" + attr.getValue()
                                    + ") for atom "
                                    + attr.getAtom().getIdentifier().toString()
                                    + " has been successfully deleted", true);

                            if (i == 0) {
                                current_concept
                                        .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
                            }
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
                                            "Failed to delete atom note(s)."
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
    } // DeleteNoteAction

    /**
     * Changes status for selected atom note(s).
     * 
     * @see AbstractAction
     */
    class ChangeStatusAction extends AbstractAction {
        private Component target = null;

        private String action_cmd = null;

        // constructor
        public ChangeStatusAction(Component comp) {
            putValue(Action.NAME, "Status");
            putValue(Action.SHORT_DESCRIPTION,
                    "change status of selected atom note(s)");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (attrsTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target, "There are no selected row(s).");
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
                                    "Status has been successfully changed "
                                            + "for note of the atom: "
                                            + attr.getAtom().getIdentifier()
                                                    .toString(), true);
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
                                            "Failed to change status of atom note(s)."
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

            // Concept Name
            conceptNameTF.setText(current_concept.getPreferredAtom()
                    .getString());
            // Concept Id
            conceptIdTF.setText(current_concept.getIdentifier().toString());

            Attribute[] attrs = current_concept
                    .getAttributesByName(Attribute.ATOM_NOTE);

            for (int i = 0; i < attrs.length; i++) {
                final Vector row = new Vector();

                Atom atom = attrs[i].getAtom();
                row.add(atom.getString()); // atom name
                row.add(String.valueOf(attrs[i].getStatus())); // status
                row.add(attrs[i].getAuthority().toString()); // authorized
                // by
                row.add(attrs[i].getTimestamp()); // authorized on

                attrsModel.addRow(row);

                listOfAttrs.add(attrs[i]);
            }

            if (attrs.length != 0) {
                textArea.setText(attrs[0].getValue());
            }

        } catch (Exception ex) {
            MEMEToolkit.logComment("[" + this.getName()
                    + "]: failed to set content.", true);
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logger.logElapsedTime();
        }

    } // setContent()

    public void setAtom(Atom atom) {
        current_atom = atom;

        atomIdTF.setText(current_atom.getIdentifier().toString()); // atom_id
        atomNameTF.setText(current_atom.getString()); // atom
        // name
    } // setAtom

    // window's size
    public Dimension getPreferredSize() {
        return new Dimension(790, 530);
    }

    public void setVisible(boolean b) {
        if (!b) {
            attrsTable.setSortState(-1, false);
        }
        super.setVisible(b);
    }
}