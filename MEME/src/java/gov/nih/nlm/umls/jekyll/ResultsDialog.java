/*
 * ResultsDialog.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.exception.MEMEException;
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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.SocketException;
import java.util.Calendar;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import samples.accessory.StringGridBagLayout;

/**
 * The window displays Finder search results.
 * 
 * @see <a href="src/ResultsDialog.java.html">source </a>
 * @see <a href="src/ResultsDialog.properties.html">properties </a>
 */
public class ResultsDialog extends JDialog implements Reportable, Transferable,
        JekyllConstants {

    // integer values for columns
    private final int TABLE_CONCEPT_ID_COLUMN = 0;

    private final int TABLE_STATUS_COLUMN = 1;

    private final int TABLE_CONCEPT_NAME_COLUMN = 2;

    // Components
    private GlassComponent glass_comp = null;

    private ResizableJTable resultsTable = null;

    private EditableTableModel resultsModel = null;

    private Frame owner = null;

    private JPopupMenu popup = null;

    // Fields
    private ResourceBundle resources = null;

    private TransferAction transfer_action = new TransferAction(this);

    private CloseAction close_action = new CloseAction(this);

    private String[] columnNames = { "Concept Id", "Status", "Concept Name" };

    private Object[][] data = null;

    /**
     * Default constructor.
     */
    public ResultsDialog(Frame parent, boolean modal, Object[][] data) {
        super(parent, modal);
        owner = parent;
        this.data = data;
        initResources();
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle.getBundle("bundles.ResultsDialogResources");
    }

    private void initComponents() {
        String columnName = null;
        TableColumn column = null;

        setTitle("Results for " + JekyllKit.getFinder().getString());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);

        // Build the contents
        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());

        resultsModel = new EditableTableModel(data, columnNames);
        resultsTable = GUIToolkit.getTable(resultsModel);

        // table columns settings
        columnName = resultsTable.getColumnName(TABLE_CONCEPT_ID_COLUMN);
        column = resultsTable.getColumn(columnName);

        columnName = resultsTable.getColumnName(TABLE_STATUS_COLUMN);
        column = resultsTable.getColumn(columnName);
        column.setIdentifier(StringTableCellRenderer.STATUS_IDENTIFIER);

        columnName = resultsTable.getColumnName(TABLE_CONCEPT_NAME_COLUMN);
        column = resultsTable.getColumn(columnName);

        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTextField tf = new JTextField();
        tf.setEditable(false);
        resultsTable.setDefaultEditor(String.class, new DefaultCellEditor(tf));
        resultsTable.setDefaultEditor(Integer.class, new IntegerCellEditor(tf));
        StringTableCellRenderer renderer = new StringTableCellRenderer(
                resultsTable, resultsModel);
        resultsTable.setDefaultRenderer(String.class, renderer);
        resultsTable.setDefaultRenderer(Integer.class, renderer);
        resultsTable.addMouseListener(new MouseAdapter() {

            ConceptReportAction concept_report_action = new ConceptReportAction(
                    ResultsDialog.this);

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    concept_report_action.actionPerformed(new ActionEvent(this,
                            0, ConceptReportAction.WIN_PAR_CHD));
                }
            }
        });
        /* Soma Lanka: Comment out the sorting from the results table
         * No need to sort. This should fix the preferred width
         */
        resultsTable.setSortState(TABLE_CONCEPT_NAME_COLUMN, true);
        JScrollPane sp = new JScrollPane(resultsTable);
        contents
                .add(
                        "gridx=0,gridy=0,fill=BOTH,weightx=1.0,weighty=1.0,insets=[12,12,11,11]",
                        sp);

        // # of concepts label
        JLabel countLabel = new JLabel("Number of concepts returned:");

        // # of concepts text field
        JTextField countTF = new JTextField(5);
        countTF.setEditable(false);
        countTF.setForeground(JekyllConstants.LABEL_FG);
        countTF.setMinimumSize(countTF.getPreferredSize());
        countTF.setText(String.valueOf(data.length));

        Box b = Box.createHorizontalBox();
        b.add(countLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(countTF);
        contents
                .add(
                        "gridx=0,gridy=1,gridwidth=4,anchor=WEST,insets=[0,12,0,11]",
                        b);

        // panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, 0));

        // "Concept Report" button
        JButton reportButton = GUIToolkit.getButton(new ConceptReportAction(
                this));
        buttonPanel.add(reportButton);

        // space
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        // "Edit" button
        Action edit_action = new EditAction(this) {
            public void actionPerformed(ActionEvent e) {
                if (JekyllKit.getConceptSelector().getListSize() != 0) {
                    if (JOptionPane
                            .showConfirmDialog(
                                    ((Component) e.getSource()),
                                    "This will remove all other concepts"
                                            + "\nin the Concept Selector."
                                            + "\nAre you sure this is what you want to do?",
                                    "Edit action", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                        return;
                    }
                }

                super.actionPerformed(e);
            }
        };
        JButton editButton = GUIToolkit.getButton(edit_action);
        buttonPanel.add(editButton);

        // space
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        // "Transfer to SEL" button
        JButton transferButton = GUIToolkit.getButton(transfer_action);
        buttonPanel.add(transferButton);

        // space
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        // "Make Checklist" button
        JButton makeChkButton = GUIToolkit.getButton(new MakeChecklistAction(
                this));
        buttonPanel.add(makeChkButton);

        // space
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        // close button
        JButton closeButton = GUIToolkit.getButton(close_action);
        buttonPanel.add(closeButton);

        contents.add(
                "gridx=0,gridy=2,gridwidth=4,anchor=EAST,insets=[17,12,11,11]",
                buttonPanel);

        // popup menu
        popup = new JPopupMenu();
        resultsTable.addMouseListener(new PopupListener(popup));
        JMenuItem menuItem = new JMenuItem(edit_action);
        popup.add(menuItem);
        menuItem = new JMenuItem(transfer_action);
        popup.add(menuItem);

        // adding a menu
        setJMenuBar(buildMenuBar());

        setLocationRelativeTo(owner);
        
        /* Soma Lanka: Resize the column width. The following call will handle. 
         * 
         */
        resultsModel.fireTableRowsInserted(1,resultsModel.getRowCount());
    } // initComponents()

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
        // 		public void actionPerformed(ActionEvent e) {
        // 		    super.actionPerformed(e);
        // 		    resultsTable.setRowHeight(resultsTable.getRowHeight() + 6);
        // 		}
        // 			    });
        menu.add(item);

        // options->decrease font
        item = new JMenuItem(new DecreaseFontAction());
        menu.add(item);

        menuBar.add(menu);

        return menuBar;
    } // buildMenuBar()

    public Dimension getPreferredSize() {
        return new Dimension(770, 450);
    }

    // ---------------------------------
    // Interface implementation
    // ---------------------------------

    /**
     * Implements {@link Reportable#getConceptId() Reportable.getConceptId()}.
     */
    public int getConceptId() {
        if (resultsTable.getSelectionModel().isSelectionEmpty()) {
            return 0;
        }

        int row = resultsTable.getSelectedRow();
        int concept_id = ((Integer) resultsModel.getValueAt(resultsTable
                .mapIndex(row), TABLE_CONCEPT_ID_COLUMN)).intValue();
        return concept_id;
    }

    /**
     * Implements {@link Transferable#getConcepts() Transferable.getConcepts()}.
     */
    public Concept[] getConcepts() {
        return new Concept[0];
    }

    /**
     * Implements
     * {@link Transferable#getConceptIds() Transferable.getConceptIds()}.
     */
    public int[] getConceptIds() {
        if (resultsTable.getSelectionModel().isSelectionEmpty()) {
            return new int[0];
        }

        int[] rows = resultsTable.getSelectedRows();

        int[] concept_ids = new int[rows.length];

        for (int i = 0; i < rows.length; i++) {
            concept_ids[i] = ((Integer) resultsModel.getValueAt(resultsTable
                    .mapIndex(rows[i]), TABLE_CONCEPT_ID_COLUMN)).intValue();
        }

        return concept_ids;
    }

    // ------------------------------
    // Inner Classes
    // ------------------------------

    /**
     * @see AbstractAction
     */
    public class MakeChecklistAction extends AbstractAction {

        private Component target = null;

        private boolean configured = false;

        public MakeChecklistAction(Component comp) {
            putValue(Action.NAME, "Make Checklist");
            putValue(Action.SHORT_DESCRIPTION,
                    "makes a checklist out of concepts returned");
            // 	putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            putValue("Background", Color.cyan);
            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = null;

                    try {
                        final ConceptChecklist cc = new ConceptChecklist();

                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                ConfigureChecklistDialog dialog = new ConfigureChecklistDialog(
                                        ((Dialog) target), true, cc);
                                configured = dialog.showDialog();
                            }
                        });

                        if (!configured) {
                            return;
                        }

                        logger = new ActionLogger(MakeChecklistAction.this.getClass()
                                .getName(), true);

                        for (int i = 0; i < data.length; i++) {
                            int concept_id = ((Integer) resultsModel
                                    .getValueAt(i, TABLE_CONCEPT_ID_COLUMN))
                                    .intValue();
                            cc.add(JekyllKit.getCoreDataClient().getConcept(
                                    concept_id));
                        }

                        cc.setBinName("");
                        cc.setBinType("AH");
                        cc.setCreationDate(Calendar.getInstance().getTime());

                        if (JekyllKit.getWorklistClient().checklistExists(
                                cc.getName())) {
                            boolean replace = MEMEToolkit
                                    .confirmRequest("The checklist "
                                            + cc.getName()
                                            + "\nalready exists, would you like to replace it?");
                            if (replace) {
                                JekyllKit.getWorklistClient().removeChecklist(
                                        cc.getName());
                            } else {
                                return;
                            }
                        }
                        JekyllKit.getWorklistClient().addConceptChecklist(cc);

                        MEMEToolkit.logComment("Checklist " + cc.getName()
                                + " has successfully been created.", true);
                        JOptionPane.showMessageDialog(target, "Checklist "
                                + cc.getName()
                                + " has successfully been created.");

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
                                            "Failed to create a checklist\n"
                                                    + "Please see the console for more information.");
                        }
                        ex.printStackTrace(JekyllKit.getLogWriter());
                    } finally {
                        glass_comp.setVisible(false);
                        target.setCursor(Cursor
                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                        if (logger != null)
                            logger.logElapsedTime();
                    }
                }
            });

            t.start();
        }
    } // MakeChecklistAction
}