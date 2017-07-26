/*
 * ChangeTBRFrame.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularChangeAtomAction;
import gov.nih.nlm.meme.common.Atom;
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.SocketException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
 * This window is a part of Helper Frames. Here, user can change releasability
 * of atom(s) of the current concept.
 * 
 * @see <a href="src/ChangeTBRFrame.java.html">source </a>
 * @see <a href="src/ChangeTBRFrame.properties.html">properties </a>
 */
public class ChangeTBRFrame extends JFrame implements JekyllConstants,
        Refreshable {

    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // integer values for columns
    private final int TABLE_ATOM_ID_COLUMN = 0;

    private final int TABLE_ATOM_NAME_COLUMN = 1;

    private final int TABLE_STATUS_COLUMN = 2;

    private final int TABLE_TBR_COLUMN = 3;

    private final int TABLE_TERMGROUP_COLUMN = 4;

    private final int TABLE_CODE_COLUMN = 5;

    // Various components
    private GlassComponent glass_comp = null;

    private EditableTableModel atomsModel = null;

    private ResizableJTable atomsTable = null;

    private JComboBox tbrComboBox = null;

    private DefaultComboBoxModel tbrComboBoxModel = null;

    private Concept current_concept = null;

    private char[] tbr_values = null;

    private Vector listOfAtoms = new Vector();

    // Actions
    CloseAction close_action = new CloseAction(this);

    /**
     * Default constructor.
     */
    public ChangeTBRFrame() {
        this.setName("ChangeTBRFrame");
        initResources();
        initComponents();
        initValues();
        FontSizeManager.addContainer(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle.getBundle("bundles.ChangeTBRFrameResources");
    }

    private void initComponents() {
        String columnName = null;
        TableColumn column = null;

        setTitle(resources.getString("window.title"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);

        JTextArea instrArea = new JTextArea();
        instrArea.setEditable(false);
        instrArea.setBackground(contents.getBackground());
        instrArea.setText(resources.getString("instructions.label"));
        contents.add(
                "gridx=0,gridy=0,fill=NONE,anchor=CENTER,insets=[12,12,0,11]",
                instrArea);

        atomsModel = new EditableTableModel();
        atomsModel.setColumnCount(6);
        atomsModel.setRowCount(0);
        atomsTable = GUIToolkit.getTable(atomsModel);

        columnName = atomsTable.getColumnName(TABLE_ATOM_ID_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.atomId.label"));

        columnName = atomsTable.getColumnName(TABLE_ATOM_NAME_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.atomName.label"));

        columnName = atomsTable.getColumnName(TABLE_STATUS_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.status.label"));
        column.setIdentifier(StringTableCellRenderer.STATUS_IDENTIFIER);

        columnName = atomsTable.getColumnName(TABLE_TBR_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.tbr.label"));
        column.setIdentifier(StringTableCellRenderer.TBR_IDENTIFIER);

        columnName = atomsTable.getColumnName(TABLE_TERMGROUP_COLUMN);
        column = atomsTable.getColumn(columnName);
        column
                .setHeaderValue(resources
                        .getString("atomsTable.termgroup.label"));

        columnName = atomsTable.getColumnName(TABLE_CODE_COLUMN);
        column = atomsTable.getColumn(columnName);
        column.setHeaderValue(resources.getString("atomsTable.code.label"));

        atomsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTextField tf = new JTextField();
        tf.setEditable(false);
        atomsTable.setDefaultEditor(String.class, new DefaultCellEditor(tf));
        atomsTable.setDefaultEditor(Integer.class, new IntegerCellEditor(tf));
        StringTableCellRenderer renderer = new StringTableCellRenderer(
                atomsTable, atomsModel);
        atomsTable.setDefaultRenderer(String.class, renderer);
        atomsTable.setDefaultRenderer(Integer.class, renderer);
        JScrollPane sp = new JScrollPane(atomsTable);
        contents
                .add(
                        "gridx=0,gridy=1,fill=BOTH,anchor=CENTER,weightx=1.0,weighty=1.0,insets=[12,12,0,11]",
                        sp);

        JLabel tbrLabel = new JLabel();
        tbrLabel.setText(resources.getString("tbr.label"));

        tbrComboBoxModel = new DefaultComboBoxModel();
        tbrComboBox = new JComboBox(tbrComboBoxModel);

        Action a = new ChangeTBRAction(this);
        JButton tbrButton = new JButton(a);
        tbrButton.setFont(BUTTON_FONT);
        tbrButton.setBackground((Color) a.getValue("Background"));

        JButton closeButton = new JButton(close_action);
        closeButton.setFont(BUTTON_FONT);
        closeButton.setBackground((Color) close_action.getValue("Background"));

        Box b = Box.createHorizontalBox();
        b.add(tbrLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(tbrComboBox);
        b.add(Box.createHorizontalStrut(12));
        b.add(tbrButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(closeButton);
        contents.add(
                "gridx=0,gridy=3,fill=NONE,anchor=CENTER,insets=[12,12,12,11]",
                b);

        setJMenuBar(buildMenuBar());

    } // initComponents

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = null;
        JMenuItem item = null;

        // File
        menu = new JMenu();
        menu.setText(resources.getString("fileMenu.label"));
        menu.setMnemonic(resources.getString("fileMenu.mnemonic").charAt(0));

        // file->close
        item = new JMenuItem(close_action);
        menu.add(item);

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
    } // buildMenuBar

    private void initValues() {
        try {
            // TBR values
            tbr_values = JekyllKit.getAuxDataClient()
                    .getValidTobereleasedValues();
            for (int i = 0; i < tbr_values.length; i++) {
                tbrComboBoxModel.addElement(String.valueOf(tbr_values[i]));
            }
        } catch (Exception ex) {
            MEMEToolkit.logComment("[" + this.getName()
                    + "]: failed to initialize TBR values.", true);
            ex.printStackTrace(JekyllKit.getLogWriter());
        }
    } // initValues()

    private void clearContent() {
        atomsTable.clearSelection();
        atomsModel.getDataVector().clear();
        atomsModel.fireTableRowsInserted(0, 1);
        atomsModel.fireTableDataChanged();
        listOfAtoms.clear();
        tbrComboBox.setSelectedItem(String.valueOf(CoreData.FV_RELEASABLE));
    }

    // ------------------------
    // Inner Classes
    // ------------------------

    /**
     * Changes releasability of selected atom(s).
     * 
     * @see AbstractAction
     */
    class ChangeTBRAction extends AbstractAction {
        private Component target = null;

        public ChangeTBRAction(Component comp) {
            putValue(Action.NAME, "Change TBR");
            putValue(Action.SHORT_DESCRIPTION, "");
            putValue("Background", Color.lightGray);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (atomsTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target, "No selected atom(s).");
                return;
            }

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(ChangeTBRAction.this.getClass()
                            .getName(), true);

                    try {
                        int[] selected_rows = atomsTable.getSelectedRows();

                        for (int i = 0; i < selected_rows.length; i++) {
                            char tbr_value = tbr_values[tbrComboBox
                                    .getSelectedIndex()];
                            Atom atom = (Atom) listOfAtoms.get(atomsTable
                                    .mapIndex(selected_rows[i]));
                            atom.setTobereleased(tbr_value);

                            MolecularChangeAtomAction mcaa = new MolecularChangeAtomAction(
                                    atom);
                            JekyllKit.getDefaultActionClient().processAction(
                                    mcaa);

                            if (i == 0) {
                                current_concept
                                        .setStatus(CoreData.FV_STATUS_NEEDS_REVIEW);
                            }

                            MEMEToolkit
                                    .logComment("Tobereleased for the atom "
                                            + atom.getIdentifier().toString()
                                            + " has been changed to "
                                            + tbr_value, true);
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
                                            "Failed to change releasability for the atom(s)."
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
    } // ChangeTBRAction

    public Dimension getPreferredSize() {
        return new Dimension(700, 360);
    }

    /**
     * Sets content for this screen.
     */
    public void setContent(Concept concept) {
        ActionLogger logger = new ActionLogger(this.getClass().getName() + ".setContent()",
                true);

        try {
            current_concept = concept;

            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            clearContent();

            Atom[] atoms = current_concept.getAtoms();
            for (int i = 0; i < atoms.length; i++) {
                final Vector row = new Vector();

                row.add(Integer.valueOf(atoms[i].getIdentifier().toString())); // atom
                // id
                row.add(atoms[i].getString()); // atom name
                row.add(String.valueOf(atoms[i].getStatus())); // status
                row.add(String.valueOf(atoms[i].getTobereleased())); // tbr
                row.add(atoms[i].getTermgroup().toString()); // termgroup
                row.add(atoms[i].getCode().toString()); // code

                atomsModel.addRow(row);

                listOfAtoms.add(atoms[i]);
            }
        } catch (Exception ex) {
            MEMEToolkit
                    .notifyUser(
                            ChangeTBRFrame.this,
                            "There was an error in loading"
                                    + "\natom(s) information for this window."
                                    + "\nMessage: "
                                    + ex.getMessage()
                                    + "\nConsole/Log file may contain more information.");
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            ChangeTBRFrame.this.setCursor(Cursor
                    .getPredefinedCursor(Cursor.HAND_CURSOR));
            logger.logElapsedTime();
        }

    } // setContent()

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            atomsTable.setSortState(-1, false);
        }
    }
}