/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.client
 * Object:  TestFinderFrame
 *
 *****************************************************************************/
package gov.nih.nlm.meme.client;

import gov.nih.nlm.meme.MEMEConstants;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.Checklist;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.ConceptChecklist;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.common.SemanticType;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.InitializationException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.swing.IdealWhitespaceDocument;
import gov.nih.nlm.swing.NoEditTableModel;
import gov.nih.nlm.swing.SortableJTable;
import gov.nih.nlm.swing.SuperJList;
import gov.nih.nlm.swing.SwingToolkit;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This simple GUI interface can be used to test
 * the functionality of the {@link FinderClient}
 * and {@link WorklistClient}.  To run it call,
 * <pre>
 * % memerun.pl gov.nih.nlm.meme.client.TestFinderFrame <db>
 * </pre>
 *
 * This class makes use of the full {@link EditingClient}
 * but really only accesses methods for finding concepts
 * and generating checklists.
 *
 * @author Brian Carlsen (bcarlsen@msdinc.com)
 */
public class TestFinderFrame extends JFrame implements DataSourceChangeListener,
    ServerChangeListener {

  //
  // Private constants
  //

  private static final long serialVersionUID = 1L;
// MEME4 clients & mid service name
  private EditingClient client = null;
  private String mid_service = null;
  private String host = null;
  private int port = 0;

  // Frame Components
  private SortableJTable result_table = null;
  private JTextField search_field = null;
  private boolean searching = false;

  // Glass pane component
  private GlassComponent glass = null;

  // Actions
  private ConceptReportAction concept_report_action = null;

  // Search results
  private Concept[] results = null;

  //
  // Constructor
  //

  /**
   * Instantiates a {@link TestFinderFrame} connected to the specified
   * mid service.
   * @param mid_service service name
   * @throws Exception if anything goes wrong
   */
  public TestFinderFrame(String mid_service) throws Exception {
    super();
    host = ClientToolkit.getProperty(ClientConstants.SERVER_HOST);
    port = Integer.parseInt(ClientToolkit.getProperty(ClientConstants.
        SERVER_PORT));
    client = new EditingClient(mid_service, "mth", "umls_tuttle");
    Runtime.getRuntime().addShutdownHook(
        new Thread(
        new Runnable() {
      public void run() {
        try {
          client.terminateSession();
        } catch (Exception e) {}
      }
    }
    )
        );
    concept_report_action = new ConceptReportAction(mid_service);
    this.mid_service = mid_service;

    glass = new GlassComponent(this);
    setGlassPane(glass);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    try {
      configureFrame();
      configureMenu();
    } catch (Exception e) {
      e.printStackTrace();
      MEMEToolkit.reportError(
          "Failed to initialize frame, check for server errors.", true);
    }
    pack();

    if (client.isEditingEnabled()) {
      setVisible(true);
    } else if (client.getClientEditorPreferences().getEditorLevel() >= 5) {
      MEMEToolkit.notifyUser("Editing is cutoff for everyone except you.");
      setVisible(true);
    } else {
      MEMEToolkit.reportError("Editing is cutoff.");
    }

    search_field.requestFocus();
  }

  /**
   * Configures the frame.
   * @throws Exception if failed to configure frame.
   */
  private void configureFrame() throws Exception {

    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.fill = GridBagConstraints.BOTH;
    c.gridx = GridBagConstraints.RELATIVE;
    c.insets = new Insets(5, 5, 5, 5);
    c.gridy = 0;

    setTitle("Finder: " + mid_service);

    //
    // Data Panel
    //

    JPanel data_panel = new JPanel(new GridBagLayout());

    // Add result table
    c.gridwidth = 2;
    NoEditTableModel result_model =
        new NoEditTableModel(new String[0][2],
                             new String[] {"Concept Id", "Preferred Name"});
    result_table = new SortableJTable(result_model);
    result_table.getColumnModel().getColumn(0).setPreferredWidth(100);
    result_table.getColumnModel().getColumn(1).setPreferredWidth(500);
    JScrollPane pane = new JScrollPane(result_table);
    pane.setPreferredSize(new Dimension(150, 300));
    result_table.addMouseListener(
        new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          // Double click initiates concept report
          concept_report_action.actionPerformed(null);
        }
      }
    }
    );

    data_panel.add(pane, c);
    c.gridy++;

    c.gridwidth = 1;
    data_panel.add(new JLabel("Select Max Result Count:"), c);
    final JComboBox box = new JComboBox(
        new String[] {"10", "25", "50", "100", "500", "1000", "No Limit"});
    box.setSelectedItem("100");
    client.setMaxResultCount(100);

    box.setPreferredSize(
        new Dimension(50, (int) box.getPreferredSize().getHeight()));

    box.addActionListener(
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (box.getSelectedItem().equals("No Limit")) {
          client.setMaxResultCount(10000000);
        } else {
          client.setMaxResultCount(Integer.parseInt( (String) box.
              getSelectedItem()));
        }
      }
    }
    );
    data_panel.add(box, c);
    c.gridy++;

    data_panel.add(new JLabel("Restrict By STY:"), c);
    SemanticType[] stys = client.getValidSemanticTypes();
    Arrays.sort(stys);
    final SuperJList sty_field = new SuperJList(stys);
    JScrollPane sty_pane = new JScrollPane(sty_field);
    sty_field.resizeList(4, 4);

    sty_field.addListSelectionListener(
        new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        Object[] x = sty_field.getSelectedValues();
        SemanticType[] stys = new SemanticType[x.length];
        for (int i = 0; i < x.length; i++) {
          stys[i] = (SemanticType) x[i];
        }
        client.restrictBySemanticTypes(stys.length == 0 ? null : stys);
      }
    }
    );
    data_panel.add(sty_pane, c);
    c.gridy++;

    data_panel.add(new JLabel("Restrict By Source:"), c);
    Source[] sources = client.getCurrentSources();
    Arrays.sort(sources);

    final SuperJList source_field = new SuperJList(sources);
    JScrollPane source_pane = new JScrollPane(source_field);
    source_field.resizeList(4, 4);

    source_field.addListSelectionListener(
        new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        Object[] x = source_field.getSelectedValues();
        Source[] sources = new Source[x.length];
        for (int i = 0; i < x.length; i++) {
          sources[i] = (Source) x[i];
        }
        client.restrictBySources(sources.length == 0 ? null : sources);
      }
    }
    );
    data_panel.add(source_pane, c);
    c.gridy++;

    data_panel.add(new JLabel("Enter Search String:"), c);
    search_field = new JTextField(new IdealWhitespaceDocument(), "", 50);
    data_panel.add(search_field, c);
    c.gridy++;

    //
    // Button Panel
    //

    JPanel btn_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

    // Exact String
    JButton exact_btn = new JButton(new ExactStringSearchAction());
    btn_panel.add(exact_btn);

    // Norm String
    JButton norm_btn = new JButton(new NormStringSearchAction());
    btn_panel.add(norm_btn);

    // All Words
    JButton all_btn = new JButton(new AllWordsSearchAction());
    btn_panel.add(all_btn);

    // All Norm Words
    JButton all_norm_btn = new JButton(new AllNormWordsSearchAction());
    btn_panel.add(all_norm_btn);

    // Any Words
    JButton any_btn = new JButton(new AnyWordsSearchAction());
    btn_panel.add(any_btn);

    // Any Words
    JButton any_norm_btn = new JButton(new AnyNormWordsSearchAction());
    btn_panel.add(any_norm_btn);

    c.gridwidth = 2;
    data_panel.add(btn_panel, c);
    c.gridy++;
    c.gridwidth = 1;

    data_panel.setBorder(SwingToolkit.EMPTY_BORDER);

    getRootPane().setDefaultButton(exact_btn);
    getContentPane().removeAll();
    getContentPane().add(data_panel);
    search_field.requestFocus();

  }

  /**
   * Configures the menu.
   * The Menus look like this:
   *
   *  File ->
   *    Change Database
   *    Exit
   *  Tools ->
   *    Exact
   *    Norm
   *    All words
   *    All norm words
   *    Any words
   *    Any norm words
   *    Concept Report
   *    Make Checklist
   */
  private void configureMenu() {

    JMenuBar menu_bar = new JMenuBar();

    //
    // File Menu
    //
    JMenu file_menu = new JMenu("File");
    menu_bar.add(file_menu);
    file_menu.setMnemonic('F');

    // Change Database
    ChangeDataSourceAction cdsa = new ChangeDataSourceAction(this, mid_service);
    cdsa.addDataSourceChangeListener(this);
    JMenuItem cdb_item = new JMenuItem(cdsa);
    file_menu.add(cdb_item);

    // Change Server
    ChangeServerAction csa = new ChangeServerAction(this, host, port);
    csa.addServerChangeListener(this);
    JMenuItem cs_item = new JMenuItem(csa);
    file_menu.add(cs_item);

    // Quit
    JMenuItem quit_item = new JMenuItem(new QuitApplicationAction());
    file_menu.add(quit_item);

    //
    // Tools Menu
    //
    JMenu tools_menu = new JMenu("Tools");
    menu_bar.add(tools_menu);
    tools_menu.setMnemonic('T');

    // Exact
    JMenuItem exact_item = new JMenuItem(new ExactStringSearchAction());
    tools_menu.add(exact_item);

    // Norm
    JMenuItem norm_item = new JMenuItem(new NormStringSearchAction());
    tools_menu.add(norm_item);

    // All
    JMenuItem all_item = new JMenuItem(new AllWordsSearchAction());
    tools_menu.add(all_item);

    // All Norm
    JMenuItem all_norm_item = new JMenuItem(new AllNormWordsSearchAction());
    tools_menu.add(all_norm_item);

    // Any
    JMenuItem any_item = new JMenuItem(new AnyWordsSearchAction());
    tools_menu.add(any_item);

    // Any Norm
    JMenuItem any_norm_item = new JMenuItem(new AnyNormWordsSearchAction());
    tools_menu.add(any_norm_item);

    // Concept Report
    JMenuItem concept_item = new JMenuItem(concept_report_action);
    tools_menu.add(concept_item);

    // Concept Report
    JMenuItem checklist_item = new JMenuItem(new MakeChecklistAction());
    tools_menu.add(checklist_item);

    setJMenuBar(menu_bar);

  }

  /**
   * Puts results into the results table.
   * @param results An array of object {@link Concept}.
   */
  private void setResults(Concept[] results) {
    this.results = results;
    if (results == null || results.length == 0) {
      MEMEToolkit.notifyUser("No Concepts Found");

    }
    String[][] data = new String[results.length][2];
    for (int i = 0; i < results.length; i++) {
      data[i][0] = results[i].getIdentifier().toString();
      data[i][1] = results[i].getPreferredAtom().toString();
    }
    NoEditTableModel result_model =
        new NoEditTableModel(data,
                             new String[] {"Concept Id", "Preferred Name"});
    result_table.setModel(result_model);
    result_table.getColumnModel().getColumn(0).setPreferredWidth(100);
    result_table.getColumnModel().getColumn(1).setPreferredWidth(500);
    searching = false;
    glass.setVisible(false);
  }

  //
  // DataSourceChangeListener Implementation
  //

  /**
   * Responds to a change in the data source.
   * @param dsce the event indicating the new data source
   */
  public void dataSourceChanged(DataSourceChangeEvent dsce) {
    mid_service = dsce.getService();
    client.setMidService(mid_service);
    concept_report_action.setMidService(mid_service);
    setTitle("Finder: " + mid_service + " (" + host + ":" + port + ")");
  }

  //
  // ServerChangeListener Implementation
  //

  /**
   * Responds to a change in the data source.
   * @param sce the event indicating the new data source
   */
  public void serverChanged(ServerChangeEvent sce) {
    host = sce.getHost();
    port = sce.getPort();
    try {
      client.terminateSession();
      ClientToolkit.setProperty(ClientConstants.SERVER_HOST, host);
      ClientToolkit.setProperty(ClientConstants.SERVER_PORT,
                                String.valueOf(port));
      client = new EditingClient(mid_service, "mth", "umls_tuttle");
      setTitle("Finder: " + mid_service + " (" + host + ":" + port + ")");
    } catch (MEMEException me) {
      ClientToolkit.handleError(me);
    }
  }

  //
  // GlassPaneListener implementation
  //

  /**
   * Enables glass pane.
   */
  public void enableGlassPane() {
    glass.setVisible(true);
  }

  /**
   * Disables glass pane.
   */
  public void disableGlassPane() {
    glass.setVisible(false);
  }

  //
  // Inner classes (Actions)
  //

  /**
   * Quits the application.
   */
  class QuitApplicationAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	/**
     * Constructor takes a frame that the action
     * will close when invoked.
     */
    public QuitApplicationAction() {
      super();

      // configure action
      putValue(Action.NAME, "Exit");
      putValue(Action.SHORT_DESCRIPTION, "Exit Application");
      putValue(Action.MNEMONIC_KEY,
               new Integer( (int) 'x'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke('Q', Event.CTRL_MASK));
    }

    /**
     * Exits the application.
     * @param ae An <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent ae) {
      dispose();
      System.exit(0);
    }
  }

  /**
   * Performs an exact string search.
   */
  class ExactStringSearchAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	/**
     * Constructs the action.
     */
    public ExactStringSearchAction() {
      super();

      // configure action
      putValue(Action.NAME, "Exact");
      putValue(Action.SHORT_DESCRIPTION, "Exact String Search");
      putValue(Action.MNEMONIC_KEY,
               new Integer( (int) 'e'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke('1', Event.CTRL_MASK));
    }

    /**
     * Exits the application.
     * @param ae An <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent ae) {
      if (searching) {
        return;
      }
      Thread t = new Thread(
          new Runnable() {
        public void run() {
          glass.setVisible(true);
          searching = true;
          try {
            Concept[] results =
                client.findExactStringMatches(search_field.getText().trim());
            setResults(results);
          } catch (Exception e) {
            MEMEToolkit.reportError("Error finidng any word matches.");
          }
        }
      }
      );
      t.start();
    }
  }

  /**
   * Performs an norm string search.
   */
  class NormStringSearchAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	/**
     * Constructs the action.
     */
    public NormStringSearchAction() {
      super();

      // configure action
      putValue(Action.NAME, "Normalized");
      putValue(Action.SHORT_DESCRIPTION, "Normalized String Search");
      putValue(Action.MNEMONIC_KEY,
               new Integer( (int) 'n'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke('2', Event.CTRL_MASK));
    }

    /**
     * Exits the application.
     * @param ae An <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent ae) {
      if (searching) {
        return;
      }
      Thread t = new Thread(
          new Runnable() {
        public void run() {
          glass.setVisible(true);
          searching = true;
          try {
            Concept[] results = client.findNormalizedStringMatches(
                search_field.getText().trim());
            setResults(results);
          } catch (Exception e) {
            MEMEToolkit.reportError("Error finding any word matches.");
          }
        }
      }
      );
      t.start();
    }
  }

  /**
   * Performs an "all words" search.
   */
  class AllWordsSearchAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	/**
     * Constructs the action.
     */
    public AllWordsSearchAction() {
      super();

      // configure action
      putValue(Action.NAME, "All Words");
      putValue(Action.SHORT_DESCRIPTION, "All Words Search");
      putValue(Action.MNEMONIC_KEY,
               new Integer( (int) 'a'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke('3', Event.CTRL_MASK));
    }

    /**
     * Exits the application.
     * @param ae An <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent ae) {
      if (searching) {
        return;
      }
      Thread t = new Thread(
          new Runnable() {
        public void run() {
          glass.setVisible(true);
          searching = true;
          try {
            Concept[] results = client.findAllWordMatches(
                FieldedStringTokenizer.split(search_field.getText().trim(), " "));
            setResults(results);
          } catch (Exception e) {
            MEMEToolkit.reportError("Error finidng any word matches.");
          }
        }
      }
      );
      t.start();
    }
  }

  /**
   * Performs an "all norm words" search.
   */
  class AllNormWordsSearchAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	/**
     * Constructs the action.
     */
    public AllNormWordsSearchAction() {
      super();

      // configure action
      putValue(Action.NAME, "All Norm Words");
      putValue(Action.SHORT_DESCRIPTION, "All Norm Words Search");
      putValue(Action.MNEMONIC_KEY,
               new Integer( (int) 'l'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke('4', Event.CTRL_MASK));
    }

    /**
     * Exits the application.
     * @param ae An <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent ae) {
      if (searching) {
        return;
      }
      Thread t = new Thread(
          new Runnable() {
        public void run() {
          glass.setVisible(true);
          searching = true;
          try {
            Concept[] results = client.findAllNormalizedWordMatches(
                FieldedStringTokenizer.split(search_field.getText().trim(), " "));
            setResults(results);
          } catch (Exception e) {
            MEMEToolkit.reportError("Error finidng any word matches.");
          }
        }
      }
      );
      t.start();
    }
  }

  /**
   * Performs an "any words" search.
   */
  class AnyWordsSearchAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	/**
     * Constructs the action.
     */
    public AnyWordsSearchAction() {
      super();

      // configure action
      putValue(Action.NAME, "Any Words");
      putValue(Action.SHORT_DESCRIPTION, "Any Words Search");
      putValue(Action.MNEMONIC_KEY,
               new Integer( (int) 'y'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke('5', Event.CTRL_MASK));
    }

    /**
     * Exits the application.
     * @param ae An <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent ae) {
      if (searching) {
        return;
      }
      Thread t = new Thread(
          new Runnable() {
        public void run() {
          glass.setVisible(true);
          searching = true;
          try {
            Concept[] results = client.findAnyWordMatches(
                FieldedStringTokenizer.split(search_field.getText().trim(), " "));
            setResults(results);
          } catch (Exception e) {
            MEMEToolkit.reportError("Error finidng any word matches.");
          }
        }
      }
      );
      t.start();
    }
  }

  /**
   * Performs an "any norm words" search.
   */
  class AnyNormWordsSearchAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	/**
     * Constructs the action.
     */
    public AnyNormWordsSearchAction() {
      super();

      // configure action
      putValue(Action.NAME, "Any Norm Words");
      putValue(Action.SHORT_DESCRIPTION, "Any Norm Words Search");
      putValue(Action.MNEMONIC_KEY,
               new Integer( (int) 'n'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke('6', Event.CTRL_MASK));
    }

    /**
     * Exits the application.
     * @param ae An <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent ae) {
      if (searching) {
        return;
      }
      Thread t = new Thread(
          new Runnable() {
        public void run() {
          glass.setVisible(true);
          searching = true;
          try {
            Concept[] results = client.findAnyNormalizedWordMatches(
                FieldedStringTokenizer.split(search_field.getText().trim(), " "));
            setResults(results);
          } catch (Exception e) {
            MEMEToolkit.reportError("Error finidng any word matches.");
          }
        }
      }
      );
      t.start();
    }
  }

  /**
   * Concept reports !
   */
  class ConceptReportAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	//
    // Private Fields
    //
    private TestReportFrame frame = null;

    /**
     * Constructs the action.
     * @param mid_service service name.
     */
    public ConceptReportAction(String mid_service) {
      super();

      try {
        frame = new TestReportFrame(mid_service);
      } catch (Exception e) {
        System.err.println("Failed to create concept report frame.");
      }
      // configure action
      putValue(Action.NAME, "Concept Report");
      putValue(Action.SHORT_DESCRIPTION, "Generate a Concept Report");
      putValue(Action.MNEMONIC_KEY,
               new Integer( (int) 't'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke('T', Event.CTRL_MASK));
    }

    /**
     * Change the mid service.
     * @param new_service service name.
     */
    public void setMidService(String new_service) {
      frame.dispose();
      try {
        frame = new TestReportFrame(mid_service);
      } catch (Exception e) {
        System.err.println("Failed to create concept report frame.");
      }
    }

    /**
     * Exits the application.
     * @param ae An <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent ae) {
      int i = result_table.getSelectedRow();
      int concept_id = 0;
      if (i != -1) {
        concept_id = Integer.parseInt( (String) result_table.getValueAt(i, 0));
      }
      if (concept_id != 0) {
        frame.showReport(concept_id);
      } else {
        frame.showMessage("No Concept Selected.");
      }
    }
  }

  /**
   * Action for making a checklist
   */
  class MakeChecklistAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	/**
     * Construct the action.
     */
    public MakeChecklistAction() {
      super();
      putValue(Action.NAME, "Make Checklist");
      putValue(Action.SHORT_DESCRIPTION, "Make Checklist from the results.");
      putValue(Action.MNEMONIC_KEY,
               new Integer( (int) 'm'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke('M', Event.CTRL_MASK));
    }

    /**
     * Opens a dialog that allows the user to configure the
     * checklist.
     * the list comes from mid services list.
     * @param e An <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent e) {
      if (results == null || results.length == 0) {
        MEMEToolkit.notifyUser("No results to build checklist from.");
        return;
      }
      Thread t = new Thread(
          new Runnable() {
        public void run() {
          glass.setVisible(true);

          try {
            ConceptChecklist cc = new ConceptChecklist();
            cc.add(results);
            ConfigureChecklistDialog ccd = new ConfigureChecklistDialog(cc);
            if (ccd.showConfigureDialog()) {
              cc.setBinName("");
              cc.setBinType("AH");
              cc.setCreationDate(new Date());
              if (client.checklistExists(cc.getName())) {
                boolean replace = MEMEToolkit.confirmRequest(
                    "The checklist " + cc.getName() + "\n" +
                    "already exists, would you like to\n" +
                    "replace it?");
                if (replace) {
                  client.removeChecklist(cc.getName());
                } else {
                  return;
                }
              }
              client.addConceptChecklist(cc);
              MEMEToolkit.notifyUser(
                  "Checklist " + cc.getName() + " successfully created.");
            }
          } catch (Exception ex) {
            ex.printStackTrace();
            MEMEToolkit.reportError(
                "Error creating checklist, this is most\n" +
                "likely a server error.  Please try again.");
          }
          glass.setVisible(false);
        }
      }
      );
      t.start();
    }

  }

  /**
   * Dialog for configuring checklist information
   *  checklist_name, owner, bin_name, type
   */
  class ConfigureChecklistDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	// Fields
    private Checklist checklist = null;
    private JTextField jname = null;
    private JComboBox jowner = null;
    public boolean configured = false;

    public ConfigureChecklistDialog(Checklist l_checklist) {
      super(TestFinderFrame.this);
      setTitle("Configure Checklist");
      ConfigureChecklistDialog.this.setModal(true);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      checklist = l_checklist;

      GridBagConstraints c = new GridBagConstraints();
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.fill = GridBagConstraints.BOTH;
      c.gridx = GridBagConstraints.RELATIVE;
      c.insets = new Insets(5, 5, 5, 5);
      c.gridy = 0;

      //
      // Data Panel
      //
      JPanel data_panel = new JPanel(new GridBagLayout());

      // Name
      data_panel.add(new JLabel("Name:"), c);
      jname = new JTextField("chk_finder_", 40);
      data_panel.add(jname, c);
      c.gridy++;

      // Owner
      data_panel.add(new JLabel("Owner:"), c);
      EditorPreferences[] prefs = null;
      try {
        prefs = client.getEditorPreferences();
      } catch (Exception e) {
        e.printStackTrace();
      }
      Set owners = new TreeSet();
      for (int i = 0; i < prefs.length; i++) {
        owners.add(prefs[i].getUserName());
      }
      String[] owner_list =
          (String[]) (new ArrayList(owners)).toArray(new String[0]);
      jowner = new JComboBox(owner_list);
      jowner.setEditable(true);
      data_panel.add(jowner, c);
      c.gridy++;

      //
      // Button Panel
      //
      JPanel btn_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

      // OK
      JButton ok_btn = new JButton("OK");
      btn_panel.add(ok_btn);

      JButton cancel_btn = new JButton("Cancel");
      btn_panel.add(cancel_btn);

      ok_btn.addActionListener(
          new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          configured = true;
          checklist.setName(jname.getText());
          checklist.setOwner( (String) jowner.getSelectedItem());
          setVisible(false);
        }
      }
      );

      cancel_btn.addActionListener(
          new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          configured = false;
          setVisible(false);
        }
      }
      );

      c.gridwidth = 2;
      data_panel.add(btn_panel, c);

      data_panel.setBorder(SwingToolkit.EMPTY_BORDER);

      getRootPane().setDefaultButton(ok_btn);
      getContentPane().removeAll();
      getContentPane().add(data_panel);
      pack();
    }

    /**
     * Shows the dialog, allows user to configure.
     * @return <code>true</code> if OK was pushed.
     */
    public boolean showConfigureDialog() {
      // modal so it blocks
      setVisible(true);
      return configured;
    }
  }

  /**
   * Application Entry Point.
   * @param args An array of arguments.
   */
  public static void main(String[] args) {

    if (args.length == 2) {
      try {
        ClassLoader cl = TestFinderFrame.class.getClassLoader();
        URL u = cl.getResource("meme.prop");
        Properties props = new Properties();
        props.load( (InputStream) u.getContent());
        System.setProperty("env.MEME_HOME", "X");
        System.setProperty("env.ORACLE_HOME", "X");
        ClientToolkit.initialize(props);
        new TestFinderFrame(args[0]);
        System.out.println("whazzup?");
      } catch (Exception e) {
        MEMEToolkit.handleError(e);
      }

    } else if (args.length == 1) {

      // Using view!
      System.setProperty(MEMEConstants.VIEW, "true");

      try {
        MEMEToolkit.initialize();
      } catch (InitializationException ie) {
        MEMEToolkit.handleError(ie);
      }

      try {
        new TestFinderFrame(args[0]);
      } catch (Exception e) {
        System.err.println(
            "Error starting application");
        e.printStackTrace();
        System.exit(0);
      }
    }
  }

}
