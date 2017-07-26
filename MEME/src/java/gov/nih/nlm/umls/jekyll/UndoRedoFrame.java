/*
 * UndoRedoFrame.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.client.FinderClient;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.swing.DecreaseFontAction;
import gov.nih.nlm.swing.FontSizeManager;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.swing.IncreaseFontAction;
import gov.nih.nlm.umls.jekyll.swing.EditableTableModel;
import gov.nih.nlm.umls.jekyll.swing.ResizableJTable;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.SocketException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import samples.accessory.StringGridBagLayout;

/**
 * This window is a part of Helper Frames. It allows user undo or redo prior
 * performed molecular actions.
 * 
 * @see <a href="src/UndoRedoFrame.java.html">source </a>
 * @see <a href="src/UndoRedoFrameResources.properties.html">properties </a>
 */
public class UndoRedoFrame extends JFrame implements JekyllConstants,
        Refreshable, ListSelectionListener {

    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // integer values for columns
    private final int TABLE_AUTHORIZED_BY_COLUMN = 0;

    private final int TABLE_AUTHORIZED_ON_COLUMN = 1;

    private final int TABLE_MOLECULAR_ACTION_COLUMN = 2;

    private final int TABLE_SOURCE_ID_COLUMN = 3;

    private final int TABLE_TARGET_ID_COLUMN = 4;

    private final int TABLE_UNDONE_COLUMN = 5;

    // Various components
    private GlassComponent glass_comp = null;

    private JTextField conceptIdTF = null;

    private JTextField conceptNameTF = null;

    private JTextField startDateTF = null;

    private EditableTableModel actionsModel = null;

    private ResizableJTable actionsTable = null;

    private JEditorPane action_details_area = null;

    // Actions
    CloseAction close_action = new CloseAction(this);

    FindAction find_action = new FindAction(this);

    // Private fields
    private Concept current_concept = null;

    private Vector listOfActions = new Vector();

    private ThreadGroup tg = new ThreadGroup("FIND_ACTIONS");

    /**
     * Default constructor.
     */
    public UndoRedoFrame() {
        initResources();
        initComponents();
        FontSizeManager.addContainer(this);
        pack();
    }

    // ----------------------------
    // Private Methods
    // ----------------------------

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle.getBundle("bundles.UndoRedoFrameResources");
    }

    private void initComponents() {
        Action a = null;
        Box b = null;
        String columnName = null;
        TableColumn column = null;
        JScrollPane sp = null;

        setTitle(resources.getString("window.title"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);

        conceptIdTF = new JTextField(10);
        conceptIdTF.setEditable(false);
        conceptIdTF.setBackground(LABEL_BKG);
        conceptIdTF.setMinimumSize(conceptIdTF.getPreferredSize());
        conceptIdTF.setMaximumSize(conceptIdTF.getPreferredSize());

        conceptNameTF = GUIToolkit.getNonEditField();

        b = Box.createHorizontalBox();
        b.add(conceptIdTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(conceptNameTF);
        contents
                .add(
                        "gridx=0,gridy=0,fill=HORIZONTAL,anchor=WEST,insets=[12,12,0,11]",
                        b);

        JLabel startDateLabel = new JLabel();
        startDateLabel.setText(resources.getString("startDate.label"));

        startDateTF = new JTextField(15);
        startDateTF.setMinimumSize(startDateTF.getPreferredSize());

        JButton findButton = new JButton(find_action);
        findButton.setFont(BUTTON_FONT);
        findButton.setBackground((Color) find_action.getValue("Background"));

        b = Box.createHorizontalBox();
        b.add(startDateLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(startDateTF);
        b.add(Box.createHorizontalStrut(12));
        b.add(findButton);
        contents.add(
                "gridx=0,gridy=1,fill=NONE,anchor=WEST,insets=[12,12,0,11]", b);

        actionsModel = new EditableTableModel();
        actionsModel.setColumnCount(6);
        actionsModel.setRowCount(0);
        actionsTable = GUIToolkit.getTable(actionsModel);

        columnName = actionsTable.getColumnName(TABLE_AUTHORIZED_BY_COLUMN);
        column = actionsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("actionsTable.authorizedBy.label"));

        columnName = actionsTable.getColumnName(TABLE_AUTHORIZED_ON_COLUMN);
        column = actionsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("actionsTable.authorizedOn.label"));

        columnName = actionsTable.getColumnName(TABLE_MOLECULAR_ACTION_COLUMN);
        column = actionsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("actionsTable.molecularAction.label"));

        columnName = actionsTable.getColumnName(TABLE_SOURCE_ID_COLUMN);
        column = actionsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("actionsTable.sourceId.label"));

        columnName = actionsTable.getColumnName(TABLE_TARGET_ID_COLUMN);
        column = actionsTable.getColumn(columnName);
        column.setHeaderValue(resources
                .getString("actionsTable.targetId.label"));

        // making UNDO column invisible
        columnName = actionsTable.getColumnName(TABLE_UNDONE_COLUMN);
        column = actionsTable.getColumn(columnName);
        actionsTable.removeColumn(column);

        actionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        actionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTextField tf = new JTextField();
        tf.setEditable(false);
        actionsTable.setDefaultEditor(String.class, new DefaultCellEditor(tf));
        actionsTable.setDefaultEditor(Integer.class, new IntegerCellEditor(tf));
        actionsTable.setDefaultEditor(Date.class, new DateCellEditor(tf));
        StringTableCellRenderer renderer = new StringTableCellRenderer(
                actionsTable, actionsModel);
        renderer.setColumnIndex(StringTableCellRenderer.UNDO_IDENTIFIER,
                TABLE_UNDONE_COLUMN);
        actionsTable.setDefaultRenderer(String.class, renderer);
        actionsTable.setDefaultRenderer(Date.class, renderer);
        actionsTable.setDefaultRenderer(Integer.class, renderer);
        actionsTable.getSelectionModel().addListSelectionListener(this);
        sp = new JScrollPane(actionsTable);
        Dimension d = new Dimension(sp.getPreferredSize().width, 150);
        sp.setPreferredSize(d);
        sp.setMinimumSize(d);

        contents.add(
                "gridx=0,gridy=2,fill=BOTH,anchor=CENTER,insets=[12,12,0,11]",
                sp);

        action_details_area = new JEditorPane();
        action_details_area.setContentType("text/html");
        action_details_area.setEditable(false);
        sp = new JScrollPane(action_details_area);

        contents
                .add(
                        "gridx=0,gridy=3,fill=BOTH,anchor=CENTER,weightx=1.0,weighty=1.0,insets=[12,12,0,11]",
                        sp);

        a = new UndoAction(this);
        JButton undoButton = new JButton(a);
        undoButton.setFont(BUTTON_FONT);
        undoButton.setBackground((Color) a.getValue("Background"));

        a = new RedoAction(this);
        JButton redoButton = new JButton(a);
        redoButton.setFont(BUTTON_FONT);
        redoButton.setBackground((Color) a.getValue("Background"));

        JButton closeButton = new JButton(close_action);
        closeButton.setFont(BUTTON_FONT);
        closeButton.setBackground((Color) close_action.getValue("Background"));

        b = Box.createHorizontalBox();
        b.add(undoButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(redoButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(closeButton);
        contents
                .add(
                        "gridx=0,gridy=4,fill=NONE,anchor=EAST,insets=[12,12,12,11]",
                        b);

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

        // Options
        menu = new JMenu();
        menu.setText(resources.getString("optionsMenu.label"));
        menu.setMnemonic(resources.getString("optionsMenu.mnemonic").charAt(0));

        // Options -> Increase Font
        item = new JMenuItem(new IncreaseFontAction());
        menu.add(item);

        // Options -> Decrease Font
        item = new JMenuItem(new DecreaseFontAction());
        menu.add(item);

        menuBar.add(menu);

        return menuBar;
    } // buildMenuBar

    private void clearContent() {
        conceptIdTF.setText(null);
        conceptNameTF.setText(null);
        startDateTF.setText("01-jan-"
                + Calendar.getInstance().get(Calendar.YEAR));
        actionsTable.clearSelection();
        actionsModel.getDataVector().clear();
        actionsModel.fireTableRowsInserted(0, 1);
        actionsModel.fireTableDataChanged();
        action_details_area.setText(null);
        listOfActions.clear();
    }

    // ----------------------------
    // Interface Implementation
    // ----------------------------

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()
                || actionsTable.getSelectionModel().isSelectionEmpty()) {
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        glass_comp.setVisible(true);

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {

                    // Clear text area
                    action_details_area.setText(null);

                    int index = actionsTable.mapIndex(actionsTable
                            .getSelectedRow());
                    MolecularAction molecular_action = (MolecularAction) listOfActions
                            .get(index);

                    int molecular_id = molecular_action.getIdentifier()
                            .intValue();
                    String action_report = JekyllKit.getReportsClient()
                            .getActionReportDocument(molecular_id);
                    action_details_area.setText(action_report);
                    action_details_area.setCaretPosition(0);

                } catch (Exception ex) {
                    if (ex instanceof MEMEException
                            && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                        MEMEToolkit.reportError(UndoRedoFrame.this,
                                "There was a network error."
                                        + "\nPlease try the action again.",
                                false);
                    } else {
                        MEMEToolkit
                                .notifyUser(
                                        UndoRedoFrame.this,
                                        "Failed to retrieve report for the selected action."
                                                + "\nConsole/Log file may contain more information.");
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());
                } finally {
                    glass_comp.setVisible(false);
                    UndoRedoFrame.this.setCursor(Cursor
                            .getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        });

        t.start();
    } // valueChanged()

    // ----------------------------
    // Inner Classes
    // ----------------------------

    /**
     * Finds all molecular actions for the current concept starting from the
     * specified date.
     * 
     * @see AbstractAction
     */
    class FindAction extends AbstractAction {
        private Component target = null;

        private String date_string = null;

        public FindAction(Component comp) {
            putValue(Action.NAME, "Find");
            putValue(Action.SHORT_DESCRIPTION,
                    "find molecular actions for the concept");
            putValue("Background", Color.lightGray);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (current_concept == null) {
                return;
            }

            date_string = startDateTF.getText().trim();
            if (date_string.equals("")) {
                MEMEToolkit.notifyUser(target, "Please enter a start date");
                return;
            }

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            Thread t = new Thread(tg, new Runnable() {
                public void run() {
                    final ActionLogger logger = new ActionLogger(
                            FindAction.this.getClass().getName(), true);

                    try {
                        if (tg.activeCount() != 0) {
                            Thread[] threads = new Thread[tg.activeCount()];
                            tg.enumerate(threads);
                            // joining the last active thread
                            // 				System.out.println("# of threads: " +
                            // threads.length);
                            for (int i = (threads.length - 1); i >= 0; i--) {
                                // 				    System.out.println("thread " + i);
                                if (!threads[i].equals(Thread.currentThread())) {
                                    threads[i].join();
                                    // 					System.out.println("joining " +
                                    // threads[i] + " (" + i + ")");
                                    break;
                                }
                            }
                        }

                        // clear previous search results
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                actionsTable.clearSelection();
                                actionsModel.getDataVector().clear();
                                actionsModel.fireTableRowsInserted(0, 1);
                                actionsModel.fireTableDataChanged();
                                action_details_area.setText(null);
                                // 					System.out.println("1. finished with clear");
                            }
                        });

                        // clear cached actions
                        listOfActions.clear();

                        FinderClient finder_client = JekyllKit
                                .getFinderClient();
                        finder_client.clearRestrictions();
                        finder_client.setMaxResultCount(1000000); // default
                        // is
                        // 100
                        finder_client.restrictByConcept(current_concept);
                        String[] tokens = FieldedStringTokenizer.split(
                                date_string, "-");
                        String date = tokens[1] + " " + tokens[0] + ", "
                                + tokens[2];
                        Date start_date = DateFormat.getDateInstance(
                                DateFormat.MEDIUM).parse(date);
                        //  			    Date end_date = Calendar.getInstance().getTime();
                        finder_client.restrictByDateRange(start_date, null);
                        // 			    finder_client.setRecursive(true);

                        MolecularAction[] actions = finder_client
                                .findMolecularActions();

                        final Vector dataVector = new Vector();
                        for (int i = 0; i < actions.length; i++) {
                            Vector row = new Vector();

                            row.add(actions[i].getAuthority().toString()); // authorized
                            // by
                            row.add(actions[i].getTimestamp()); // authorized
                            // on
                            row.add(actions[i].getActionName()); // molecular
                            // action
                            row.add(Integer.valueOf(actions[i]
                                    .getSourceIdentifier().toString())); // source
                            // id
                            row.add(Integer.valueOf(actions[i]
                                    .getTargetIdentifier().toString())); // target
                            // id
                            row.add((actions[i].isUndone()) ? new Boolean(true)
                                    : new Boolean(false)); // is
                            // undone?

                            dataVector.add(row);
                            listOfActions.add(actions[i]);
                        }

                        if (dataVector.size() != 0) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    try {
                                        for (Iterator i = dataVector.iterator(); i
                                                .hasNext();) {
                                            actionsModel.addRow((Vector) i
                                                    .next());
                                        }

                                        actionsTable.setSortState(
                                                TABLE_AUTHORIZED_ON_COLUMN,
                                                false);
                                    } catch (Exception ex) {
                                        ex.printStackTrace(JekyllKit
                                                .getLogWriter());
                                        glass_comp.setVisible(false);
                                        target
                                                .setCursor(Cursor
                                                        .getPredefinedCursor(Cursor.HAND_CURSOR));
                                        if (!JekyllKit.isFramesEnabled()) {
                                            JekyllKit.enableFrames();
                                        }
                                        if (!logger.isLogged())
                                            logger.logElapsedTime();
                                    }
                                }
                            });
                        }
                    } catch (Exception ex) {
                        if (ex instanceof MEMEException
                                && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                            MEMEToolkit.reportError(target,
                                    "There was a network error."
                                            + "\nPlease try the action again.",
                                    false);
                        } else if (!(ex instanceof java.io.InterruptedIOException)
                                || !(ex instanceof InterruptedException)) {
                            MEMEToolkit
                                    .notifyUser(
                                            target,
                                            "Failed to find molecular actions for concept "
                                                    + ((current_concept == null) ? " "
                                                            : current_concept
                                                                    .getIdentifier()
                                                                    .toString())
                                                    + ".\nPlease check whether correct start date was entered."
                                                    + "\nConsole/Log File may contain more information.");
                        }
                        ex.printStackTrace(JekyllKit.getLogWriter());
                    } finally {
                        if (tg.activeCount() <= 1) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    glass_comp.setVisible(false);
                                    target
                                            .setCursor(Cursor
                                                    .getPredefinedCursor(Cursor.HAND_CURSOR));
                                    if (!JekyllKit.isFramesEnabled()) {
                                        JekyllKit.enableFrames();
                                    }
                                }
                            });
                        }
                        if (!logger.isLogged())
                            logger.logElapsedTime();
                    }
                }
            });

            t.start();
        } // actionPerformed()

    } // FindAction

    /**
     * @see AbstractAction
     */
    class UndoAction extends AbstractAction {
        private Component target = null;

        private boolean set_cursor_back = true;

        public UndoAction(Component comp) {
            putValue(Action.NAME, "Undo");
            putValue(Action.SHORT_DESCRIPTION, "undo selected molecular action");
            putValue("Background", Color.lightGray);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (actionsTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target,
                        "Please select an action to undo");
                return;
            }

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            final int row = actionsTable.getSelectedRow();

            set_cursor_back = true;

            Thread t = new Thread(new Runnable() {
                public void run() {
                    final ActionLogger logger = new ActionLogger(
                            UndoAction.this.getClass().getName(), true);

                    try {
                        MolecularAction action = (MolecularAction) listOfActions
                                .get(actionsTable.mapIndex(row));

                        if (action.isUndone()) {
                            MEMEToolkit
                                    .notifyUser(target,
                                            "The selected action has already been undone.");
                            return;
                        }

                        JekyllKit.getDefaultActionClient().processUndo(action);

                        set_cursor_back = false;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    actionsModel.setValueAt(new Boolean(true),
                                            row, TABLE_UNDONE_COLUMN);
                                    JekyllKit.getConceptSelector()
                                            .updateEditList();
                                } catch (Exception ex) {
                                    ex
                                            .printStackTrace(JekyllKit
                                                    .getLogWriter());
                                    glass_comp.setVisible(false);
                                    target
                                            .setCursor(Cursor
                                                    .getPredefinedCursor(Cursor.HAND_CURSOR));
                                    JekyllKit.enableFrames();
                                    if (!logger.isLogged())
                                        logger.logElapsedTime();
                                }
                            }
                        });
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
                                            "Failed to undo selected molecular action."
                                                    + "\nConsole/log file may contain more information.");
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
                                    // 					    System.out.println("undo 2; finished
                                    // setting cursor back");
                                }
                            });
                        }
                        if (!logger.isLogged())
                            logger.logElapsedTime();
                    }
                }
            });

            t.start();
        } // actionPerformed()

    } // UndoAction

    /**
     * @see AbstractAction
     */
    class RedoAction extends AbstractAction {
        private Component target = null;

        private boolean set_cursor_back = true;

        public RedoAction(Component comp) {
            putValue(Action.NAME, "Redo");
            putValue(Action.SHORT_DESCRIPTION, "redo selected molecular action");
            putValue("Background", Color.lightGray);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {

            if (actionsTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target,
                        "Please select an action to redo");
                return;
            }

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            final int row = actionsTable.getSelectedRow();

            set_cursor_back = true;

            Thread t = new Thread(new Runnable() {
                public void run() {
                    ActionLogger logger = new ActionLogger(RedoAction.this
                            .getClass().getName(), true);

                    try {
                        MolecularAction action = (MolecularAction) listOfActions
                                .get(actionsTable.mapIndex(row));

                        if (action.isUndone()) {
                            JekyllKit.getDefaultActionClient().processRedo(
                                    action);

                            set_cursor_back = false;
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    JekyllKit.getConceptSelector()
                                            .refreshConcept();
                                }
                            });

                        } else {
                            MEMEToolkit.notifyUser(target,
                                    "Selected action cannot be redone."
                                            + "\nOnly undone actions in the"
                                            + "\nlist can be redone.");
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
                                            "Failed to redo selected molecular action."
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
                        }
                        logger.logElapsedTime();
                    }
                }
            });

            t.start();
        } // actionPerformed()

    } // RedoAction

    public Dimension getPreferredSize() {
        return new Dimension(740, 550);
    }

    /**
     * Sets content of this screen.
     */
    public void setContent(Concept concept) {
        current_concept = concept;

        if (current_concept == null) {
            clearContent();
            return;
        }

        startDateTF.setText("01-jan-"
                + Calendar.getInstance().get(Calendar.YEAR));
        conceptIdTF.setText(current_concept.getIdentifier().toString());
        conceptNameTF.setText(current_concept.getPreferredAtom().getString());
        find_action.actionPerformed(null);
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            actionsTable.setSortState(-1, false);
            if (tg.activeCount() > 1) {
                Thread[] threads = new Thread[tg.activeCount()];
                tg.enumerate(threads);
                for (int i = 0; i < threads.length; i++) {
                    threads[i].interrupt();
                    MEMEToolkit.logComment(threads[i] + " was interrupted",
                            true);
                }
            } // if _tg_
        }
    }
}