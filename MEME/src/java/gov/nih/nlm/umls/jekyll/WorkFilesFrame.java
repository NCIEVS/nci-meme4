/*
 * WorkFilesFrame.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.common.ConceptCluster;
import gov.nih.nlm.meme.common.ConceptWorklist;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Worklist;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.swing.DecreaseFontAction;
import gov.nih.nlm.swing.FontSizeManager;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.swing.IncreaseFontAction;
import gov.nih.nlm.swing.NoEditTableModel;
import gov.nih.nlm.swing.RearrangeWindowLayoutManager;
import gov.nih.nlm.umls.jekyll.relae.RelaEditor;
import gov.nih.nlm.umls.jekyll.swing.NonEditableTableModel;
import gov.nih.nlm.umls.jekyll.swing.ResizableJTable;
import gov.nih.nlm.umls.jekyll.swing.TableSorter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableColumn;

import samples.accessory.StringGridBagLayout;

/**
 * This screen displays all work files (checklists and worklists) that are
 * currently exist in the MID. User can do approve of a concept from here as
 * well.
 * 
 * @see <a href="src/WorkFilesFrame.java.html">source </a>
 * @see <a href="src/WorkFilesFrame.properties.html">properties </a>
 */
public class WorkFilesFrame extends JFrame implements Transferable,
        JekyllConstants, StateChangeListener {

    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // integer values for the table columns
    private final int TABLE_NAME_COLUMN = 0;

    private final int TABLE_CLUSTER_ID_COLUMN = 0;

    private final int TABLE_CONCEPT_ID_COLUMN = 1;

    private final int TABLE_CONCEPT_NAME_COLUMN = 2;

    private final int TABLE_CONCEPT_STATUS_COLUMN = 3;

    // Components
    private GlassComponent glass_comp = null;

    private NoEditTableModel workfilesModel = null;
    
    private NoEditTableModel mainfilesModel = null;

    private ResizableJTable workfilesTable = null;

    private TableSorter table_sorter = null;

    private NonEditableTableModel worklistModel = null;

    private ResizableJTable worklistTable = null;
    private ResizableJTable mainfilesTable = null;

    private JTextField countTF = null;

    private JButton apprNextButton = null;

    // Private variables
    private CloseAction close_action = new CloseAction(this);
    RearrangeWindowLayoutAction rearrange_window = new RearrangeWindowLayoutAction();

    private GetWorkFilesAction getWorkFiles_action = new GetWorkFilesAction(
            this, GetWorkFilesAction.CURRENT_RETRIEVAL_METHOD);

    private EditAction edit_action = new EditAction(this);

    private ConceptWorklist cw = null;

    private ConceptChecklist cc = null;

    private Vector listOfConcepts = new Vector();

    private boolean is_worklist = true;

    /**
     * Default constructor.
     */
    public WorkFilesFrame() {
        initResources();
        initComponents(false);
        FontSizeManager.addContainer(this);
        RearrangeWindowLayoutManager.addContainer(this);
        pack();
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle.getBundle("bundles.WorkFilesFrameResources");
    }
    // Soma Lanka: Added a method that refreshes the screen.
    
    public void reset() {
//    	 build the contents
        Container contents =getContentPane();
        contents.removeAll();
    	initComponents(true);
    	FontSizeManager.addContainer(this);
        RearrangeWindowLayoutManager.addContainer(this);
        pack();
    }
    // Soma Lanka Added to method for rearrange window layout
    public void rearrangeWindow() {
        JScrollPane sp = null;
        JScrollPane sp1 = null;
        String columnName = null;
        TableColumn column = null;
        Box b = null;
        Action a = null;

        // build the contents
        Container contents =getContentPane();
        contents.removeAll();
        //contents.setLayout(new StringGridBagLayout());
        contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));      
        // Add a box filled at the top
        contents.add(getBoxFiller(0,12));

        // work files table        
        Box workFilesBox = Box.createHorizontalBox();
        // Add a Box Filler
        workFilesBox.add(getBoxFiller(12,100));
        
        // Add the Work Files Scroll Pane
        Box workBox = Box.createVerticalBox();
        sp = new JScrollPane(workfilesTable);
        
        Dimension prefSize = sp.getPreferredSize();
        sp.setPreferredSize(new Dimension(prefSize.width-300,prefSize.height+150));
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        workBox.add(sp);
        // Add a work list scroll Pane
        Box workListBox = Box.createVerticalBox();
        sp1 = new JScrollPane(worklistTable);
        sp1.setBorder(new BevelBorder(BevelBorder.LOWERED));
        prefSize = sp1.getPreferredSize();
        sp1.setPreferredSize(new Dimension(prefSize.width,prefSize.height+150));
        sp1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        workListBox.add(sp1);
        
        // Add the work files and work list to the box
        workFilesBox.add(workBox);
        workFilesBox.add(getBoxFiller(11,100));
        workFilesBox.add(workListBox);
        workFilesBox.add(getBoxFiller(12,100));
        
        // Add the workFilesBox to the contents
        contents.add(workFilesBox);
        
        // count label
        JLabel countLabel = new JLabel(resources
                .getString("conceptsCount.label"));
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        b = Box.createHorizontalBox();
        b.add(countLabel);

        b.add(Box.createHorizontalStrut(5));
        
        b.add(countTF);
        b.setPreferredSize(new Dimension(200,20));
        b.setMinimumSize(new Dimension(200,20));
        b.setMaximumSize(new Dimension(200,20));

        contents.add(b);
        
        // "Approve/Next" button
        a = new ApproveNextAction(this);
        apprNextButton = new JButton(a);
        apprNextButton.setFont(BUTTON_FONT);
        apprNextButton.setBackground((Color) a.getValue("Background"));

        // "Not Aprroved/Next" button
        a = new NotApproveNextAction(this);
        JButton notApprNextButton = new JButton(a);
        notApprNextButton.setFont(BUTTON_FONT);
        notApprNextButton.setBackground((Color) a.getValue("Background"));

        // "Edit Cluster" button
        JButton editClusterButton = GUIToolkit.getButton(new EditClusterAction(
                this));

        // "Edit" button
        JButton editButton = new JButton(edit_action);
        editButton.setFont(BUTTON_FONT);
        editButton.setBackground((Color) edit_action.getValue("Background"));

        // "Close" button
        JButton closeButton = GUIToolkit.getButton(close_action);

        // box container
        b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(apprNextButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(notApprNextButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(editClusterButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(editButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(closeButton);
        b.add(getBoxFiller(12,100));

        contents.add(b);
       
    } // rearrangeWindow
    // Soma Lanka: Added a util method that returns Box.Filler
    private Box.Filler getBoxFiller(int x, int y) {
    	Dimension minSize = new Dimension(x,y);
        Dimension prefSize = new Dimension(x,y);
        Dimension maxSize = new Dimension(x,y);
        return new Box.Filler(minSize, prefSize, maxSize);
        
    }
    
    private void initComponents(boolean refresh) {
        JScrollPane sp = null;
        String columnName = null;
        TableColumn column = null;
        Box b = null;
        Action a = null;

        // set properties on this frame
        setTitle(resources.getString("window.title"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);

        // build the contents
        Container contents = getContentPane();
        if (refresh) {
        	contents.removeAll();
        }
        contents.setLayout(new StringGridBagLayout());
        if (!refresh) {
	        // work files table
	        workfilesModel = new NoEditTableModel();
	        workfilesModel.setColumnCount(1);
	        workfilesModel.setRowCount(0);
	        workfilesTable = GUIToolkit.getTable(workfilesModel);
	
	        // column headers for work files table
	        columnName = workfilesTable.getColumnName(TABLE_NAME_COLUMN);
	        column = workfilesTable.getColumn(columnName);
	        column.setHeaderValue(resources.getString("workfilesTable.name.label"));
	
	        workfilesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        workfilesTable.addMouseListener(new MouseAdapter() {
	            public void mouseClicked(MouseEvent e) {
	                if (e.getClickCount() == 2) {
	                    loadWorklist();
	                }
	            }
	        });
        }
        sp = new JScrollPane(workfilesTable);
        contents
                .add(
                        "gridx=0,gridy=0,fill=BOTH,weightx=1.0,weighty=1.0,insets=[12,12,0,11]",
                        sp);
        if (!refresh) {
	        // worklist table
	        worklistModel = new NonEditableTableModel();
	        worklistModel.setColumnCount(4);
	        worklistModel.setRowCount(0);
	        worklistTable = GUIToolkit.getTable(worklistModel);
	
	        // set column headers and preferred widths
	        columnName = worklistTable.getColumnName(TABLE_CLUSTER_ID_COLUMN);
	        column = worklistTable.getColumn(columnName);
	        column.setHeaderValue(resources
	                .getString("worklistTable.clusterId.label"));
	
	        columnName = worklistTable.getColumnName(TABLE_CONCEPT_ID_COLUMN);
	        column = worklistTable.getColumn(columnName);
	        column.setHeaderValue(resources
	                .getString("worklistTable.conceptId.label"));
	
	        columnName = worklistTable.getColumnName(TABLE_CONCEPT_NAME_COLUMN);
	        column = worklistTable.getColumn(columnName);
	        column.setHeaderValue(resources
	                .getString("worklistTable.conceptName.label"));
	
	        columnName = worklistTable.getColumnName(TABLE_CONCEPT_STATUS_COLUMN);
	        column = worklistTable.getColumn(columnName);
	        column.setHeaderValue(resources
	                .getString("worklistTable.conceptStatus.label"));
	        column.setIdentifier(StringTableCellRenderer.STATUS_IDENTIFIER);
	
	        worklistTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	
	        // TODO: ask what the editors would prefer copy+paste or
	        // edit functionality
	        // 	JTextField tf = new JTextField();
	        // 	tf.setEditable(false);
	        // 	worklistTable.setDefaultEditor(String.class, new
	        // DefaultCellEditor(tf));
	        // 	worklistTable.setDefaultEditor(Integer.class, new
	        // IntegerCellEditor(tf));
	
	        StringTableCellRenderer renderer = new StringTableCellRenderer(
	                worklistTable, worklistModel);
	        worklistTable.setDefaultRenderer(String.class, renderer);
	        worklistTable.setDefaultRenderer(Integer.class, renderer);
	        worklistTable.addMouseListener(new MouseAdapter() {
	            public void mouseClicked(MouseEvent e) {
	                if (e.getClickCount() == 2) {
	                    // 			concept_report_action.actionPerformed(new
	                    // ActionEvent(this, 0, ConceptReportAction.WIN_PAR_CHD));
	                    edit_action.actionPerformed(null);
	                }
	            }
	        });
	        table_sorter = (TableSorter) worklistTable.getModel();
        }
        sp = new JScrollPane(worklistTable);
        sp.setBorder(new BevelBorder(BevelBorder.LOWERED));

        contents
                .add(
                        "gridx=0,gridy=1,fill=BOTH,weightx=1.0,weighty=1.0,insets=[12,12,0,11]",
                        sp);

        // count label
        JLabel countLabel = new JLabel(resources
                .getString("conceptsCount.label"));
        if (!refresh) {
	        // count text field
	        countTF = new JTextField(3);
	        countTF.setForeground(LABEL_FG);
	        countTF.setEditable(false);
	        countTF.setMinimumSize(countTF.getPreferredSize());
	        countTF.setText(resources.getString("conceptsCountTextField.text"));
        }
        // box container
        b = Box.createHorizontalBox();
        b.add(countLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(countTF);

        contents.add(
                "gridx=0,gridy=2,fill=NONE,anchor=WEST,insets=[0,12,0,11]", b);

        // "Approve/Next" button
        a = new ApproveNextAction(this);
        apprNextButton = new JButton(a);
        apprNextButton.setFont(BUTTON_FONT);
        apprNextButton.setBackground((Color) a.getValue("Background"));

        // "Not Aprroved/Next" button
        a = new NotApproveNextAction(this);
        JButton notApprNextButton = new JButton(a);
        notApprNextButton.setFont(BUTTON_FONT);
        notApprNextButton.setBackground((Color) a.getValue("Background"));

        // "Edit Cluster" button
        JButton editClusterButton = GUIToolkit.getButton(new EditClusterAction(
                this));

        // "Edit" button
        JButton editButton = new JButton(edit_action);
        editButton.setFont(BUTTON_FONT);
        editButton.setBackground((Color) edit_action.getValue("Background"));

        // "Close" button
        JButton closeButton = GUIToolkit.getButton(close_action);

        // box container
        b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(apprNextButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(notApprNextButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(editClusterButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(editButton);
        b.add(Box.createHorizontalStrut(5));
        b.add(closeButton);

        contents
                .add(
                        "gridx=0,gridy=3,fill=HORIZONTAL,anchor=EAST,insets=[12,12,12,11]",
                        b);

        // adding a menu
        if (!refresh)
        setJMenuBar(buildMenuBar());

        // centers a frame onscreen
        if (!refresh)
        setLocationRelativeTo(null);

    } // initComponents()

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = null;
        JMenuItem item = null;

        // File
        menu = new JMenu();
        menu.setText(resources.getString("fileMenu.label"));
        menu.setMnemonic(resources.getString("fileMenu.mnemonic").charAt(0));

        // File->close
        item = new JMenuItem(close_action);
        menu.add(item);

        menuBar.add(menu);

        // Options
        menu = new JMenu();
        menu.setText(resources.getString("optionsMenu.label"));
        menu.setMnemonic(resources.getString("optionsMenu.mnemonic").charAt(0));

        // Options -> Open with Rela Editor
        item = new JMenuItem(new RelaEditorAction(this));
        menu.add(item);

        // separator
        menu.addSeparator();

        // Options -> Retrieve YTD workfiles
        item = new JMenuItem(new GetWorkFilesAction(this,
                GetWorkFilesAction.YTD_RETRIEVAL_METHOD));
        item.setText("Retrieve up to a year old workfiles");
        menu.add(item);

        // separator
        menu.addSeparator();

        // options->increase font
        item = new JMenuItem(new IncreaseFontAction());
        menu.add(item);

        // options->decrease font
        item = new JMenuItem(new DecreaseFontAction());
        menu.add(item);
        
        // Soma Lanka: Adding a new check box item to the menu for rearranging the window layout
        menu.addSeparator();
        item = new JCheckBoxMenuItem(rearrange_window);
        item.setSelected(false);
        menu.add(item);
        // Changes Completed by Soma Lanka
        
        menuBar.add(menu);
        
        return menuBar;
        
         
    } // buildMenuBar()

    private void clearContent() {
        worklistTable.clearSelection();
        worklistModel.getDataVector().clear();
        worklistModel.fireTableRowsInserted(0, 1);
        worklistModel.fireTableDataChanged();
        listOfConcepts.clear();
        countTF.setText("0");
        cw = null;
        cc = null;
    }

    private void loadWorklist() {
        if (workfilesTable.getSelectionModel().isSelectionEmpty()
                || workfilesTable.getSelectionModel().getValueIsAdjusting()) {
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        glass_comp.setVisible(true);

        int model_index = workfilesTable.mapIndex(workfilesTable
                .getSelectedRow());
        final String workfile_name = ((String) workfilesModel.getValueAt(
                model_index, TABLE_NAME_COLUMN)).toLowerCase();

        // clear content of the
        // current worklist or checklist
        clearContent();

        Thread t = new Thread(new Runnable() {
            public void run() {
                final ActionLogger logger = new ActionLogger(this.getClass()
                        .getName()
                        + ".loadWorklist()", true);

                try {
                    Concept[] concepts = null;

                    if (workfile_name.startsWith("wrk")) {
                        cw = JekyllKit.getWorklistClient().getConceptWorklist(
                                workfile_name);
                        concepts = cw.getConcepts();
                        is_worklist = true;
                    } else {
                        cc = JekyllKit.getWorklistClient().getConceptChecklist(
                                workfile_name);
                        concepts = cc.getConcepts();
                        is_worklist = false;
                    }

                    final Vector dataVector = new Vector();
                    for (int i = 0; i < concepts.length; i++) {
                        Vector row = new Vector();

                        row.add(Integer.valueOf(concepts[i]
                                .getClusterIdentifier().toString())); // cluster
                        // id
                        row.add(Integer.valueOf(concepts[i].getIdentifier()
                                .toString())); // concept id
                        row.add(concepts[i].getPreferredAtom().getString()); // concept
                        // name
                        row.add(String.valueOf(concepts[i].getStatus())); // concept
                        // status

                        dataVector.add(row);
                        listOfConcepts.add(concepts[i]);
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (listOfConcepts.size() == 0) {
                                MEMEToolkit
                                        .notifyUser(WorkFilesFrame.this,
                                                "No concepts exist in "
                                                        + workfile_name);
                            } else {
                                for (Iterator i = dataVector.iterator(); i
                                        .hasNext();) {
                                    worklistModel.addRow((Vector) i.next());
                                }

                                worklistTable.setSortState(
                                        TABLE_CLUSTER_ID_COLUMN, true);
                                countTF.setText(String.valueOf(listOfConcepts
                                        .size()));

                                MEMEToolkit.logComment(workfile_name
                                        + " has been loaded for editing", true);
                            }

                            glass_comp.setVisible(false);
                            WorkFilesFrame.this.setCursor(Cursor
                                    .getPredefinedCursor(Cursor.HAND_CURSOR));
                            logger.logElapsedTime();
                        }
                    });
                } catch (Exception ex) {
                    if (ex instanceof MEMEException
                            && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                        MEMEToolkit.reportError(WorkFilesFrame.this,
                                "There was a network error."
                                        + "\nPlease try the action again.",
                                false);
                    } else {
                        MEMEToolkit
                                .notifyUser(
                                        WorkFilesFrame.this,
                                        "Failed to load "
                                                + workfile_name
                                                + "\nConsole/log file may contain more information.");
                    }
                    ex.printStackTrace(JekyllKit.getLogWriter());

                    glass_comp.setVisible(false);
                    WorkFilesFrame.this.setCursor(Cursor
                            .getPredefinedCursor(Cursor.HAND_CURSOR));

                    logger.logElapsedTime();
                }
            }
        });

        t.start();
    } // loadWorklist();

    // ---------------------------------------
    // Interface implementation
    // ---------------------------------------

    /**
     * Implements {@link Editable#getConcept() Editable.getConcept()}.
     */
    public Concept getConcept() {
        if (worklistTable.getSelectionModel().isSelectionEmpty()) {
            return null;
        }

        int[] selected_rows = worklistTable.getSelectedRows();

        Concept concept = null;

        try {
            concept = (Concept) listOfConcepts.get(worklistTable
                    .mapIndex(selected_rows[0]));
            concept = JekyllKit.getCoreDataClient().getConcept(concept);
            return concept;
        } catch (Exception ex) {
            if (ex instanceof MissingDataException) {
                MEMEToolkit.notifyUser(this, "Concept "
                        + ((concept == null) ? "" : concept.getIdentifier()
                                .toString()) + " no longer exists.");
            } else if (ex instanceof MEMEException
                    && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                MEMEToolkit.reportError(WorkFilesFrame.this,
                        "There was a network error."
                                + "\nPlease try the action again.", false);
            }
            ex.printStackTrace(JekyllKit.getLogWriter());
        }

        return concept;
    } // getConcept()

    /**
     * Re-queries each occurrence of the specified concept in a
     * worklist/checklist.
     */
    // Called from:
    //   ApproveNextAction
    public void refreshConcept(Concept concept) {
        if (worklistTable.getRowCount() == 0) {
            return;
        }

        worklistModel.removeTableModelListener(table_sorter);

        int index = -1; // occurrence index

        while ((index < (listOfConcepts.size() - 1)) || (index != -1)) {

            index = listOfConcepts.indexOf(concept, (index + 1));

            if (index == -1) {
                break;
            }

            try {
                concept = JekyllKit.getCoreDataClient().getConcept(concept);
                worklistModel.setValueAt(String.valueOf(concept.getStatus()),
                        index, TABLE_CONCEPT_STATUS_COLUMN);
            } catch (Exception ex) {
                if (!(ex instanceof MissingDataException)) {
                    MEMEToolkit.logComment("Failed to refresh concept "
                            + concept.getIdentifier().toString(), true);
                    ex.printStackTrace(JekyllKit.getLogWriter());
                }
            }
        } // while loop

        worklistModel.addTableModelListener(table_sorter);
    } // refreshConcept()

    /**
     * Moves selection to the next concept from the specified concept.
     */
    // Called from:
    //   ApproveNextAction
    //   NotApproveNextAction
    public void nextConcept(Concept concept) {
        if (worklistTable.getRowCount() == 0) {
            return;
        }

        int current_row = -1;

        if (!worklistTable.getSelectionModel().isSelectionEmpty()) {
            boolean found_concept = false;

            // case when a concept exists in a worklist
            if (listOfConcepts.contains(concept)) {
                // 1st occurrence of the concept
                int index = listOfConcepts.indexOf(concept);
                int row = worklistTable.reverseMapIndex(index);

                if (worklistTable.isRowSelected(row)) {
                    current_row = row;
                    found_concept = true;
                }
            } // if

            // if a specified concept is not in a worklist or
            // not in a set of selected rows, then make the
            // last selected row as a current one.
            if (!found_concept) {
                int[] selected_rows = worklistTable.getSelectedRows();
                current_row = selected_rows[selected_rows.length - 1];
            }
        } else {
            // if selection is empty, make the last row
            // as a current one.
            current_row = worklistTable.getRowCount() - 1;
        }

        int next_row = 0;
        if (current_row != (worklistTable.getRowCount() - 1)) {
            next_row = current_row + 1;
            worklistTable.setRowSelectionInterval(next_row, next_row);
            Rectangle rect = worklistTable.getCellRect(next_row,
                    TABLE_CLUSTER_ID_COLUMN, true);
            worklistTable.scrollRectToVisible(rect);
        }
    } // nextConcept()

    /**
     * Implements {@link Transferable#getConcepts() Transferable.getConcepts()}.
     */
    public Concept[] getConcepts() {
        if (worklistTable.getSelectionModel().isSelectionEmpty()) {
            return new Concept[0];
        }

        int[] rows = worklistTable.getSelectedRows();

        Concept[] concepts = new Concept[rows.length];

        for (int i = 0; i < rows.length; i++) {
            concepts[i] = (Concept) listOfConcepts.get(worklistTable
                    .mapIndex(rows[i]));
        }

        return concepts;
    }

    /**
     * Implements
     * {@link Transferable#getConceptIds() Transferable.getConceptIds()}.
     */
    public int[] getConceptIds() {
        if (worklistTable.getSelectionModel().isSelectionEmpty()) {
            return new int[0];
        }

        int[] rows = worklistTable.getSelectedRows();

        int[] concept_ids = new int[rows.length];

        for (int i = 0; i < rows.length; i++) {
            concept_ids[i] = ((Concept) listOfConcepts.get(worklistTable
                    .mapIndex(rows[i]))).getIdentifier().intValue();
        }

        return concept_ids;
    }

    /**
     * Implements
     * {@link StateChangeListener#stateChanged(StateChangeEvent) StateChangeListener.stateChanged(StateChangeEvent)}.
     */
    public void stateChanged(StateChangeEvent e) {
        if (e.getState().equals(StateChangeEvent.BROWSE_STATE)) {
            apprNextButton.setEnabled(false);
        } else {
            apprNextButton.setEnabled(true);
        }
    }

    /**
     * Retrieves current set of worklists and checklists from the mid.
     */
    public void getWorkFiles() {
        getWorkFiles_action.actionPerformed(null);
    }

    public Dimension getPreferredSize() {
        return new Dimension(760, 730);
    }

    // -----------------------------
    // Inner Classes
    // -----------------------------
    /**
     * Rearrange the Window layout (WorkList/Data are displayed side by side)
     * New feature trying to add 
     *
     * @author Soma Lanka
     */
    public class RearrangeWindowLayoutAction extends AbstractAction {

      /**
       * Instantiates a {@link DecreaseFontAction}.
       */
      public RearrangeWindowLayoutAction() {
        super();
        // configure action
        putValue(Action.NAME, "Rearrange Window Layout");
        putValue(Action.SHORT_DESCRIPTION, "Rearrange Window Layout");
        putValue(Action.MNEMONIC_KEY,
                 new Integer( (int) 'r'));
        putValue(Action.ACCELERATOR_KEY,
                 KeyStroke.getKeyStroke('{', Event.CTRL_MASK));
      }

      /**
       * Decrease the font.
       * @param e the {@link ActionEvent}
       */
      public void actionPerformed(ActionEvent e) {
    	  if (((JCheckBoxMenuItem) e.getSource()).getState()) {
    		  RearrangeWindowLayoutManager.rearrangeWindow();
    	  } else {
    		  RearrangeWindowLayoutManager.reset();
    	  }
      }

    }
    /**
     * Retrieves work files.
     * 
     * @see AbstractAction
     */
    class GetWorkFilesAction extends AbstractAction {
        final public static int CURRENT_RETRIEVAL_METHOD = 0;

        final public static int YTD_RETRIEVAL_METHOD = 1;

        private Component target = null;

        private Thread thread = null;

        private int ret_method = CURRENT_RETRIEVAL_METHOD;

        public GetWorkFilesAction(Component comp, int retrieval_method) {
            putValue(Action.SHORT_DESCRIPTION,
                    "retrieve all checklists and worklists from the specified mid.");
            target = comp;
            ret_method = retrieval_method;
        }

        public void actionPerformed(ActionEvent e) {
            if ((thread != null) && (thread.isAlive())) {
                return;
            }

            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            // Let's clear the current
            // worklist/checklist content
            clearContent();

            // clear workfiles table
            workfilesTable.clearSelection();
            workfilesModel.getDataVector().clear();
            workfilesModel.fireTableRowsInserted(0, 1);
            workfilesModel.fireTableDataChanged();

            thread = new Thread(new Runnable() {
                public void run() {
                    final ActionLogger logger = new ActionLogger(
                            GetWorkFilesAction.this.getClass().getName(), true);

                    try {
                        final Vector dataVector = new Vector();

                        // ----------------------
                        // Checklists
                        // ----------------------

                        String[] checklist_names = JekyllKit
                                .getWorklistClient().getChecklistNames();
                        for (int i = 0; i < checklist_names.length; i++) {
                            Vector row = new Vector();
                            row.add(checklist_names[i]);
                            dataVector.add(row);
                        }

                        // ----------------------
                        // Worklists
                        // ----------------------

                        if (ret_method == CURRENT_RETRIEVAL_METHOD) {
                            // Current worklists, i.e. only those worklists
                            // whose stamp date is
                            // from this year or is null.
                            Worklist[] cur_worklists = JekyllKit
                                    .getWorklistClient().getCurrentWorklists();
                            String[] worklist_names = new String[cur_worklists.length];
                            for (int i = 0; i < worklist_names.length; i++) {
                                worklist_names[i] = cur_worklists[i].getName();
                            }
                            Arrays.sort(worklist_names);
                            for (int i = 0; i < worklist_names.length; i++) {
                                Vector row = new Vector();
                                if (worklist_names[i]
                                        .equals("chk_finder_wthmac2")) {
                                    System.out.println(worklist_names[i]);
                                }
                                row.add(worklist_names[i]);
                                dataVector.add(row);
                            }
                        } else {
                            // Selecting all worklists up to a year old.
                            Worklist[] worklists = JekyllKit
                                    .getWorklistClient().getWorklists();
                            Vector v_temp = new Vector();
                            for (int i = 0; i < worklists.length; i++) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(worklists[i].getCreationDate());
                                Calendar right_now = Calendar.getInstance();
                                if (cal.get(Calendar.YEAR) > (right_now
                                        .get(Calendar.YEAR) - 1)) {
                                    v_temp.add(worklists[i].getName());
                                }
                            }

                            String[] worklist_names = (String[]) v_temp
                                    .toArray(new String[0]);
                            Arrays.sort(worklist_names);

                            for (int i = 0; i < worklist_names.length; i++) {
                                Vector row = new Vector();
                                row.add(worklist_names[i]);
                                dataVector.add(row);
                            }
                        }

                        // --------------------------------------------------------------------
                        //          old logic
                        // --------------------------------------------------------------------
                        //            String[] workfiles_names =
                        // JekyllKit.getWorklistClient().
                        //                getWorklistAndChecklistNames();
                        //
                        //            for (int i = 0; i < workfiles_names.length; i++) {
                        //              String workfile_lc =
                        // workfiles_names[i].toLowerCase();
                        //              // per Tammy's request (01/15/2004), leave out
                        //              // worklist(s) earlier then wrk04c
                        //              if (workfile_lc.startsWith("chk") ||
                        //                  ( (workfile_lc.startsWith("wrk04") ||
                        //                     workfile_lc.startsWith("wrk05")) &&
                        //                   !workfile_lc.startsWith("wrk04a") &&
                        //                   !workfile_lc.startsWith("wrk04b"))) {
                        //                Vector row = new Vector();
                        //                row.add(workfiles_names[i]);
                        //                dataVector.add(row);
                        //              }
                        //            }

                        final Vector headers = new Vector();
                        headers.add(resources
                                .getString("workfilesTable.name.label"));

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                workfilesModel = new NoEditTableModel(
                                        dataVector, headers);
                                workfilesTable.setModel(workfilesModel);

                                glass_comp.setVisible(false);
                                target
                                        .setCursor(Cursor
                                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                                logger.logElapsedTime();
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
                                            "Failed to retrieve checklists/worklists."
                                                    + "\nConsole/log file may contain more information.");
                        }
                        ex.printStackTrace(JekyllKit.getLogWriter());

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                glass_comp.setVisible(false);
                                target
                                        .setCursor(Cursor
                                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                            }
                        });

                        logger.logElapsedTime();
                    }
                }
            });

            thread.start();
        } // actionPerformed()

    } // GetWorkFilesAction

    /**
     * Transfers cluster to the SEL screen for editing.
     * 
     * @see AbstractAction
     */
    class EditClusterAction extends AbstractAction {
        private Component target = null;

        public EditClusterAction(Component comp) {
            putValue(Action.NAME, "Edit Cluster");
            putValue(Action.SHORT_DESCRIPTION,
                    "transfer selected cluster to the SEL screen");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (worklistTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit
                        .notifyUser(target, "Please select at least one row");
                return;
            }

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            ActionLogger logger = new ActionLogger(EditClusterAction.this
                    .getClass().getName(), true);

            try {
                int[] selected_rows = worklistTable.getSelectedRows();

                Vector identifiers = new Vector();

                for (int i = 0; i < selected_rows.length; i++) {
                    int model_index = worklistTable.mapIndex(selected_rows[i]);
                    int cluster_id = ((Integer) worklistModel.getValueAt(
                            model_index, TABLE_CLUSTER_ID_COLUMN)).intValue();

                    ConceptCluster cluster = null;
                    Iterator iterator = null;

                    if (is_worklist) {
                        iterator = cw.getClusterIterator();
                    } else {
                        iterator = cc.getClusterIterator();
                    }

                    while (iterator.hasNext()) {
                        cluster = (ConceptCluster) iterator.next();

                        if (cluster_id == cluster.getIdentifier().intValue()) {
                            break;
                        }
                    }

                    Concept[] concepts = cluster.getConcepts();

                    for (int k = 0; k < concepts.length; k++) {
                        Identifier identifier = concepts[k].getIdentifier();
                        if (!identifiers.contains(identifier)) {
                            identifiers.add(identifier);
                        }
                    }
                } // for loop

                int[] concept_ids = new int[identifiers.size()];
                for (int i = 0; i < identifiers.size(); i++) {
                    concept_ids[i] = ((Identifier) identifiers.get(i))
                            .intValue();
                }

                ConceptSelector frame = JekyllKit.getConceptSelector();

                frame.replaceWith(concept_ids);

                if (frame.getExtendedState() == JFrame.ICONIFIED) {
                    frame.setExtendedState(JFrame.NORMAL);
                }
                frame.setVisible(true);
            } catch (Exception ex) {
                MEMEToolkit.notifyUser(target,
                        "Failed to move cluster to the SEL screen."
                                + "\nSee the console for more information.");
                ex.printStackTrace(JekyllKit.getLogWriter());
            } finally {
                glass_comp.setVisible(false);
                target
                        .setCursor(Cursor
                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                JekyllKit.enableFrames();
                logger.logElapsedTime();
            }
        } // actionPerformed()

    } // EditClusterAction

    private class NextClusterAction extends AbstractAction {
        private Component target = null;

        public NextClusterAction(Component comp) {
            putValue(Action.NAME, "Next Cluster");
            putValue(Action.SHORT_DESCRIPTION, "move to the next cluster");
            putValue("Background", Color.cyan);

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (worklistTable.getSelectionModel().isSelectionEmpty()) {
                MEMEToolkit.notifyUser(target, "There are no rows selected"
                        + "\non a worklist/checklist.");
                return;
            }

            JekyllKit.disableFrames();
            target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_comp.setVisible(true);

            ActionLogger logger = new ActionLogger(NextClusterAction.this
                    .getClass().getName(), true);

            try {
                int[] selected_rows = worklistTable.getSelectedRows();

                // Do nothing if the last selected row is the last row in
                // a table
                if (selected_rows[0] == (worklistTable.getRowCount() - 1)) {
                    return;
                }

                // we move from the last selected row (if multiple rows
                // are selected at one time)
                int selected_row = selected_rows[0];
                int index = worklistTable.mapIndex(selected_row);
                int cluster_id = ((Integer) worklistModel.getValueAt(index,
                        TABLE_CLUSTER_ID_COLUMN)).intValue();

                int next_cluster_id = cluster_id;

                while ((cluster_id == next_cluster_id)
                        && (selected_row < (worklistTable.getRowCount() - 1))) {
                    int next_index = worklistTable.mapIndex((++selected_row));
                    next_cluster_id = ((Integer) worklistModel.getValueAt(
                            next_index, TABLE_CLUSTER_ID_COLUMN)).intValue();
                }

                if (next_cluster_id == cluster_id) {
                    MEMEToolkit.notifyUser(target,
                            "This is the last cluster in the list.");
                    return;
                }

                ConceptCluster cluster = null;
                Iterator iterator = null;

                if (is_worklist) {
                    iterator = cw.getClusterIterator();
                } else {
                    iterator = cc.getClusterIterator();
                }

                while (iterator.hasNext()) {
                    cluster = (ConceptCluster) iterator.next();

                    if (next_cluster_id == cluster.getIdentifier().intValue()) {
                        break;
                    }
                }

                Concept[] concepts = cluster.getConcepts();

                Vector identifiers = new Vector();

                for (int k = 0; k < concepts.length; k++) {
                    Identifier identifier = concepts[k].getIdentifier();
                    if (!identifiers.contains(identifier)) {
                        identifiers.add(identifier);
                    }
                }

                int[] concept_ids = new int[identifiers.size()];
                for (int i = 0; i < identifiers.size(); i++) {
                    concept_ids[i] = ((Identifier) identifiers.get(i))
                            .intValue();
                }

                ConceptSelector frame = JekyllKit.getConceptSelector();

                frame.replaceWith(concept_ids);

                // select next cluster in this frame
                worklistTable.setRowSelectionInterval(selected_row,
                        selected_row);
                Rectangle rect = worklistTable.getCellRect(selected_row,
                        TABLE_CLUSTER_ID_COLUMN, true);
                worklistTable.scrollRectToVisible(rect);

                if (frame.getExtendedState() == JFrame.ICONIFIED) {
                    frame.setExtendedState(JFrame.NORMAL);
                }
                frame.setVisible(true);
            } catch (Exception ex) {
                MEMEToolkit
                        .notifyUser(
                                target,
                                "Failed to move to the next cluster."
                                        + "Console/Log file may contain more information.");
                ex.printStackTrace(JekyllKit.getLogWriter());
            } finally {
                glass_comp.setVisible(false);
                target
                        .setCursor(Cursor
                                .getPredefinedCursor(Cursor.HAND_CURSOR));
                JekyllKit.enableFrames();
                logger.logElapsedTime();
            }
        } // actionPerformed()

    } // NextClusterAction

    public NextClusterAction getNextClusterAction(Component c) {
        return new NextClusterAction(c);
    }

    /**
     * Displays "Concept by Concept_id" screen.
     * 
     * @see AbstractAction
     */
    class RelaEditorAction extends AbstractAction {
        private Component target = null;

        // constructor
        public RelaEditorAction(Component comp) {
            putValue(Action.NAME, "Open with Rela Editor");
            putValue(Action.SHORT_DESCRIPTION,
                    "open selected worklist with Rela Editor");

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            if (workfilesTable.getSelectionModel().isSelectionEmpty()) {
                return;
            }

            int model_index = workfilesTable.mapIndex(workfilesTable
                    .getSelectedRow());
            final String workfile_name = ((String) workfilesModel.getValueAt(
                    model_index, TABLE_NAME_COLUMN)).toLowerCase();

            RelaEditor frame = JekyllKit.getRelaEditor();

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);

            frame.setWorklist(workfile_name);
        } // actionPerformed()

    } // RelaEditorAction

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b) {
            // 	    worklistTable.setSortState(-1, false);
        }
    }
}
