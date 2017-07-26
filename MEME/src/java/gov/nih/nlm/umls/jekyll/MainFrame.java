/*
 * MainFrame.java
 * Modified: Soma Lanka: 12/06/2005 : Added new checkbox option in help menu 
 * for debugging. 
 * Modified: Soma Lanka: 12/15/2005 -- Seibel Ticket Number: 1-70HJ5 : Authenticate the user 
 * Modified: BAC: 12/21/2005 -- Seibel Ticket Number: 1-719RR : URL for changing password 
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.Version;
import gov.nih.nlm.meme.client.ChangeDataSourceAction;
import gov.nih.nlm.meme.client.ChangeServerAction;
import gov.nih.nlm.meme.client.DataSourceChangeEvent;
import gov.nih.nlm.meme.client.DataSourceChangeListener;
import gov.nih.nlm.meme.client.ServerChangeEvent;
import gov.nih.nlm.meme.client.ServerChangeListener;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.swing.DecreaseFontAction;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.swing.RearrangeWindowLayoutManager;
import gov.nih.nlm.umls.jekyll.WorkFilesFrame.RearrangeWindowLayoutAction;
import gov.nih.nlm.umls.jekyll.swing.ListDialog;
import gov.nih.nlm.util.BrowserLauncher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import com.Ostermiller.util.PasswordDialog;

import samples.accessory.StringGridBagLayout;

/**
 * Main window of the editing interface.
 */
public class MainFrame extends JFrame implements DataSourceChangeListener,
        ServerChangeListener {

    /**
     * Resource bundle with default locale
     */
    private ResourceBundle resources = null;

    // Components
    private GlassComponent glass_comp = null;

    private MainFrameTF versionTF = null;

    private MainFrameTF dbTF = null;

    private MainFrameTF stateTF = null;

    private JTextArea serverTextArea = null;

    private JCheckBoxMenuItem checkItem = null;

    private LogFileDialog log_file_dialog = null;
    
    XMLLogAction xmlLog = new XMLLogAction();
    // Actions
    ExitAction exitAction = new ExitAction();

    SetStateAction state_action = new SetStateAction();
    MainFrameTF editorTF = null; // editor initials
    MainFrameTF editorLevelTF = null ;// editor Level
    MainFrameTF expiresInTF = null; // editor password expires;
    /**
     * Default constructor.
     */
    public MainFrame() {
        initResources();
        initComponents();
        pack();
        /*
         * Soma Lanka: As per Tammy Powell- Changing the tool tip color. background to black
         * and foreground to yellow.
         */
        //Override the ToolTip.foreground color in Swing's defaults
        // table.

        UIManager.put ("ToolTip.foreground", Color.BLACK);

        // Override the ToolTip.background color in Swing's defaults
        // table.

        UIManager.put ("ToolTip.background", Color.YELLOW);
        // Override the TootlTip.background color in 
    }

    // Loads resources using the default locale
    private void initResources() {
        resources = ResourceBundle.getBundle("bundles.MainFrameResources");
    }

    private void initComponents() {
        Box b = null;

        // Set properties on this frame
        setTitle(resources.getString("window.title"));
        glass_comp = new GlassComponent(this);
        setGlassPane(glass_comp);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitAction.actionPerformed(null);
            }
        });

        // build the contents
        Container contents = getContentPane();
        contents.setLayout(new StringGridBagLayout());

        // splash image
        JLabel splashLabel = new JLabel();
        URL imageURL = MainFrame.class.getResource(resources
                .getString("jekyll.image"));
        if (imageURL != null) {
            splashLabel.setIcon(new ImageIcon(imageURL));
        } else {
            MEMEToolkit.logComment("Jekyll's image file was not found.", true);
        }
        contents.add(
                "gridx=0,gridy=0,fill=NONE,anchor=CENTER,insets=[12,12,0,11]",
                splashLabel);

        // label for version field
        JLabel versionLabel = new JLabel();
        versionLabel.setText(resources.getString("version.label"));

        // version field
//        versionTF = new MainFrameTF();
//        versionTF.setText(JekyllKit.getReleaseDate());
        JLabel version_lbl = new JLabel();
//        version_lbl.setBorder(LineBorder.createGrayLineBorder());
        version_lbl.setFont(GUIToolkit.BUTTON_FONT);
        version_lbl.setText(JekyllKit.getReleaseDate());

        // box container
        b = Box.createHorizontalBox();
        b.add(versionLabel);
        b.add(Box.createHorizontalStrut(5));
//        b.add(versionTF);
        b.add(version_lbl);
        contents
                .add(
                        "gridx=0,gridy=1,fill=HORIZONTAL,anchor=WEST,insets=[12,3,0,3]",
                        b);

        // label for database field
        JLabel dbLabel = new JLabel();
        dbLabel.setText(resources.getString("db.label"));

        // database field
        dbTF = new MainFrameTF();
        dbTF.setText(JekyllKit.getDataSource());

        // box container
        b = Box.createHorizontalBox();
        b.add(dbLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(dbTF);
        contents.add(
                "gridx=0,gridy=2,fill=HORIZONTAL,anchor=WEST,insets=[3,3,0,3]",
                b);

        // label for server field
        JLabel server_label = new JLabel();
        server_label.setText(resources.getString("server.label"));

        // server field
        serverTextArea = new JTextArea();
        serverTextArea.setEditable(false);
        serverTextArea.setFont(GUIToolkit.BUTTON_FONT);
        serverTextArea.setBackground(contents.getBackground());
        serverTextArea.setBorder(LineBorder.createGrayLineBorder());
        serverTextArea.setMargin(new Insets(0, 1, 1, 0));
        serverTextArea.setText(JekyllKit.getServerInfo());

        b = Box.createHorizontalBox();
        b.add(server_label);
        b.add(Box.createHorizontalStrut(5));
        b.add(serverTextArea);
        contents.add(
                "gridx=0,gridy=3,fill=HORIZONTAL,anchor=WEST,insets=[3,3,0,3]",
                b);

        // label for state field
        JLabel stateLabel = new JLabel();
        stateLabel.setText(resources.getString("state.label"));

        // state field
        stateTF = new MainFrameTF();
        stateTF.setText(StateChangeEvent.EDIT_STATE);

        // box container
        b = Box.createHorizontalBox();
        b.add(stateLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(stateTF);

        contents.add(
                "gridx=0,gridy=4,fill=HORIZONTAL,anchor=WEST,insets=[3,3,0,3]",
                b);

        // label for editor field
        JLabel editorLabel = new JLabel();
        editorLabel.setText(resources.getString("editor.label"));

        // editor field
        editorTF = new MainFrameTF();
        editorTF.setText(JekyllKit.getEditorInitials());

        // label for editor's level field
        JLabel editorLevelLabel = new JLabel();
        editorLevelLabel.setText(resources.getString("editorLevel.label"));

        // editor label field
        editorLevelTF = new MainFrameTF();
        editorLevelTF.setText(String.valueOf(JekyllKit.getEditorLevel()));
        
        // label for Expires In
        JLabel expiresInLabel = new JLabel();
        expiresInLabel.setText(resources.getString("expiresIn.label"));
        
        // Expires In value
        expiresInTF = new MainFrameTF();
        int days_left = JekyllKit.getDaysLeftTillExpiration();
        expiresInTF.setText((days_left < 0) ? "N/A" : String.valueOf(days_left));

        // box container
        b = Box.createHorizontalBox();
        b.add(editorLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(editorTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(editorLevelLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(editorLevelTF);
        b.add(Box.createHorizontalStrut(5));
        b.add(expiresInLabel);
        b.add(Box.createHorizontalStrut(5));
        b.add(expiresInTF);
        
        contents.add(
                "gridx=0,gridy=5,fill=HORIZONTAL,anchor=WEST,insets=[3,3,5,3]",
                b);

        // setting a menu
        setJMenuBar(buildMenuBar());

        setResizable(false);
    } // initComponents()

    // menu bar
    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setLayout(new BoxLayout(menuBar, BoxLayout.X_AXIS));
        menuBar.add(buildFileMenu());
        menuBar.add(buildEditMenu());
        menuBar.add(buildSetMenu());
        menuBar.add(buildToolsMenu());
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(buildHelpMenu());
        return menuBar;
    }

    // File menu
    private JMenu buildFileMenu() {
        JMenu menu = null;
        JMenuItem item = null;

        menu = new JMenu();
        menu.setText(resources.getString("fileMenu.label"));
        menu.setMnemonic(resources.getString("fileMenu.mnemonic").charAt(0));

        // Change Data Source
        ChangeDataSourceAction cdsa = new ChangeDataSourceAction(this,
                JekyllKit.getDataSource()) {
            public void actionPerformed(ActionEvent e) {
                if ((!JekyllKit.anyWindowsOpen())
                        || (JOptionPane.showConfirmDialog(null,
                                "All current windows will be closed."
                                        + "\nWould you like to continue?",
                                "Change Data Source?",
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)) {
                    JekyllKit.closeAllWindows();
                    super.actionPerformed(e);
                }
            }
        };
        cdsa.addDataSourceChangeListener(this);
        item = new JMenuItem(cdsa);
        menu.add(item);

        // Change Server
        ChangeServerAction csa = new ChangeServerAction(this, JekyllKit
                .getHost(), JekyllKit.getPort()) {
            public void actionPerformed(ActionEvent e) {
                if ((!JekyllKit.anyWindowsOpen())
                        || (JOptionPane.showConfirmDialog(null,
                                "All current windows will be closed."
                                        + "\nWould you like to continue?",
                                "Change Server?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)) {
                    JekyllKit.closeAllWindows();
                    super.actionPerformed(e);
                }
            }
        };
        csa.addServerChangeListener(this);
        item = new JMenuItem(csa);
        menu.add(item);

        // separator
        menu.addSeparator();

        // file->exit
        item = new JMenuItem(exitAction);
        menu.add(item);

        return menu;
    } // buildFileMenu()

    // Edit menu
    private JMenu buildEditMenu() {
        JMenu menu = null;
        JMenuItem item = null;
        Action a = null;

        menu = new JMenu();
        menu.setText(resources.getString("editMenu.label"));
        menu.setMnemonic(resources.getString("editMenu.mnemonic").charAt(0));

        // edit->work files
        item = new JMenuItem(new WorkFilesFrameAction(this));
        menu.add(item);

        // edit->concept-id
        item = new JMenuItem(new ConceptIdFrameAction(this));
        menu.add(item);

        // edit->finder
        item = new JMenuItem(new FinderAction(this));
        menu.add(item);

        return menu;
    } // buildEditMenu()

    // Set menu
    private JMenu buildSetMenu() {
        JMenu menu = null;
        JMenuItem item = null;

        menu = new JMenu();
        menu.setText(resources.getString("setMenu.label"));
        menu.setMnemonic(resources.getString("setMenu.mnemonic").charAt(0));

        // set->editor initials
        item = new JMenuItem();
        item.setText("Editor Initials");
        item.setMnemonic('I');
        item.setEnabled(false);
        menu.add(item);

        // set->editor preferences
        item = new JMenuItem();
        item.setText("Editor Preferences");
        item.setMnemonic('P');
        item.setEnabled(false);
        menu.add(item);

        // separator
        menu.addSeparator();

        // set->Languages to Exclude
        item = new JMenuItem(new SetLanguagesAction(this));
        item.setText("Languages to Include/Exclude");
        menu.add(item);

        // separator
        menu.addSeparator();

        // set->browse
        checkItem = new JCheckBoxMenuItem(state_action);
        checkItem.setSelected(false);
        menu.add(checkItem);
        
        return menu;
    } // buildSetMenu()

    // Tools menu
    private JMenu buildToolsMenu() {
        JMenu menu = null;
        JMenuItem item = null;

        menu = new JMenu();
        menu.setText("Tools");
        menu.setMnemonic('T');

        // Rela Editor
        item = new JMenuItem();
        item.setText("Rela Editor");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JekyllKit.getRelaEditor().setVisible(true);
            }
        });
        menu.add(item);

        item = new JMenuItem();
        /*
         * Soma Lanka: Per Vlad the following url is changed. May have to move this to config in future.
         * BAC: data driven from the jnlp file (12/21/2005)
         */
        item
                .setText("Change Password form http://" + System.getProperty("meow.url") + "/webapps-meme-editors/changePasswordform.do");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                BrowserLauncher.openURL("http://" + System.getProperty("meow.url") + "/webapps-meme-editors/changePasswordform.do");
                } catch (IOException ex) {
                    MEMEToolkit.notifyUser("Failed to open URL.\n"
                            + "Log file may contain more information.");
                    ex.printStackTrace(JekyllKit.getLogWriter());
                }
            }
        });
        menu.add(item);

        return menu;
    } // buildToolsMenu()

    // Help menu
    private JMenu buildHelpMenu() {
        JMenu menu = null;
        JMenuItem item = null;

        menu = new JMenu();
        menu.setText(resources.getString("helpMenu.label"));
        menu.setMnemonic(resources.getString("helpMenu.mnemonic").charAt(0));
        menu.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // Help -> View log file
        item = new JMenuItem();
        item.setText("View log file");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (log_file_dialog == null) {
                    log_file_dialog = new LogFileDialog();
                }

                log_file_dialog.showDialog();
            }
        });
        menu.add(item);

        // TODO add this when time allows
        //        item = new JMenuItem();
        //        item.setText("Toggle Look and Feel");
        //        item.addActionListener(new ActionListener() {
        //            public void actionPerformed(ActionEvent e) {
        //                try {
        //                    UIManager.setLookAndFeel(UIManager
        //                            .getSystemLookAndFeelClassName());
        //                    System.out.println(UIManager
        //                            .getSystemLookAndFeelClassName());
        //                } catch (Exception ex) {
        //                    MEMEToolkit.notifyUser("Failed to change Look and Feel."
        //                            + "\nLog file may contain more information.");
        //                    ex.printStackTrace(JekyllKit.getLogWriter());
        //                }
        //            }
        //        });
        //        menu.add(item);

        // separator
        menu.addSeparator();

        // Help -> STY Groups
        item = new JMenuItem();
        item.setText("STY Groups Help");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                STYGroupsHelp frame = new STYGroupsHelp(MainFrame.this);
                frame.setVisible(true);
            }
        });
        menu.add(item);
//      Soma Lanka: Adding a new check box item to the menu for rearranging the window layout
        menu.addSeparator();
        item = new JCheckBoxMenuItem(xmlLog);
        item.setSelected(false);
        menu.add(item);
        // Changes Completed by Soma Lanka
        // Help -> About
        item = new JMenuItem();
        item.setText("About Jekyll");
        item.setMnemonic('A');
        item.setEnabled(false);
        menu.add(item);

        return menu;
    } // buildHelpMenu()

    // ----------------------------
    // Interface implementation
    // ----------------------------

    /**
     * Responds to a change in the data source.
     * 
     * @param dsce
     *                  the event indicating the new data source
     */
    public void dataSourceChanged(DataSourceChangeEvent dsce) {
        String service = dsce.getService();
        /* Soma Lanka: Authenticate the inputs.
         *             1) Get the current datasource and save. ( Used when authentication 
         *                fails and user clicks on Cancel
         *             2) Authenticate the user using current credentials
         *             3) on success, follow existing logic.
         *             4) on Failure, Display the Password Dialog box.
         *             5) if user presses OK, Authenticate with new credentials.
         *             6) if new credentials fail, do not change the datasource and go back to main screen.
         *             7) on success, call JekyllKit.setDataSource(service) and reset the JekyllKit.mda variable (???)
         */
        
        String oldService = JekyllKit.getDataSource();
        JekyllKit.setDataSource(service);
        if (!JekyllKit.authenticateUser()) {
        	try { 
	        	AuthenticatorRunnable authRunnable = new AuthenticatorRunnable(oldService);
	        	SwingUtilities.invokeAndWait(authRunnable);
	        	service = authRunnable.getService();
        	} catch ( Exception ex) {
        		// do nothing. most probably the interrupt exception. in such cases just come out the function
        		return;
        	}
        } 
        JekyllKit.setDataSource(service);
        if (!JekyllKit.isEditorCurrent()) {
            setBrowseMode();
        } else if (!JekyllKit.isEditingEnabled()) {
            if (!(JekyllKit.getEditorLevel() == 5)) {
                  setBrowseMode();
            }
        }
        // editor field
        dbTF.setText(service);
        editorTF.setText(JekyllKit.getEditorInitials());
        editorLevelTF.setText(String.valueOf(JekyllKit.getEditorLevel()));
        int days_left = JekyllKit.getDaysLeftTillExpiration();
        expiresInTF.setText((days_left < 0) ? "N/A" : String.valueOf(days_left));
    }

    /**
     * Responds to a change in the data source.
     * 
     * @param sce
     *                  the event indicating the new data source
     */
    public void serverChanged(ServerChangeEvent sce) {
        try {
            String host = sce.getHost();
            int port = sce.getPort();
            JekyllKit.setServer(host, port);
            serverTextArea.setText(JekyllKit.getServerInfo());
            String server_version = JekyllKit.getAdminClient()
                    .getServerVersion();
            if (!server_version.equals(Version.getVersionInformation())) {
                JOptionPane.showMessageDialog(null,
                        "The version of MEME library on your machine"
                                + "\nis different from the version that MEME"
                                + "\nserver is running on. The unexpected"
                                + "\nbehavior may occur.", "Server changed!",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            if (ex instanceof MEMEException
                    && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                MEMEToolkit.reportError(MainFrame.this,
                        "There was a network error."
                                + "\nPlease try the action again.", false);
            } else {
                MEMEToolkit
                        .notifyUser(
                                MainFrame.this,
                                "Failed to change server to host "
                                        + sce.getHost()
                                        + ", port "
                                        + sce.getPort()
                                        + "\nConsole/Log file may contain additional information.");
            }
            ex.printStackTrace(JekyllKit.getLogWriter());
        }
    } // serverChanged()

    /**
     * Enables glass pane.
     */
    public void enableGlassPane() {
        glass_comp.setVisible(true);
    }

    /**
     * Disables glass pane.
     */
    public void disableGlassPane() {
        glass_comp.setVisible(false);
    }

    // --------------------------------
    // Inner classes
    // --------------------------------

    /**
     * Terminates application.
     * 
     * @see AbstractAction
     */
    class ExitAction extends AbstractAction {

        // constructor
        public ExitAction() {
            putValue(Action.NAME, "Exit");
            putValue(Action.SHORT_DESCRIPTION, "exit Jekyll");
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    } // ExitAction

    /**
     * Makes the "Work Files" frame visible and loads current set of work files.
     * 
     * @see AbstractAction
     */
    class WorkFilesFrameAction extends AbstractAction {

        // Private Fields
        private Component target = null;

        // constructor
        public WorkFilesFrameAction(Component comp) {
            putValue(Action.NAME, "Work Files");
            putValue(Action.SHORT_DESCRIPTION, "open \"Work Files\" screen");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_W));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_W, InputEvent.CTRL_MASK));

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            WorkFilesFrame frame = JekyllKit.getWorkFilesFrame();

            // frame is either behind other windows or iconified
            if (frame.isShowing()) {
                frame.setExtendedState(JFrame.NORMAL);
                frame.toFront();
            } else { // otherwise it must be hidden
                frame.setVisible(true);
                frame.getWorkFiles();
            }
        }
    } // WorkFilesFrameAction

    /**
     * Displays "Concept by Concept_id" screen.
     * 
     * @see AbstractAction
     */
    class ConceptIdFrameAction extends AbstractAction {
        private Component target = null;

        // constructor
        public ConceptIdFrameAction(Component comp) {
            putValue(Action.NAME, "Concept by concept_id");
            putValue(Action.SHORT_DESCRIPTION,
                    "search for concept by concept_id");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            // 	    putValue(Action.ACCELERATOR_KEY,
            // KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));

            target = comp;
        }

        public void actionPerformed(ActionEvent e) {
            ConceptIdFrame frame = JekyllKit.getConceptIdFrame();

            if (frame.isShowing()) { // frame is either behind other
                // windows or iconified
                frame.setExtendedState(JFrame.NORMAL);
                frame.toFront();
            } else { // then it must be hidden
                frame.setVisible(true);
            }
        }
    } // ConceptIdFrameAction

    /**
     * Toggles application's state between "edit" and "browse".
     * 
     * TODO better implementation needed, too much initialization occurs
     * 
     * @see AbstractAction
     */
    class SetStateAction extends AbstractAction {

        private ChangeStateAction change_state_action = new ChangeStateAction(
                MainFrame.this, StateChangeEvent.EDIT_STATE);

        public SetStateAction() {
            putValue(Action.NAME, "Browse");
            putValue(Action.SHORT_DESCRIPTION,
                    "toggles state between Edit and Browse");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_B, InputEvent.CTRL_MASK));

            change_state_action.addStateChangeListener(JekyllKit
                    .getConceptSelector());
            change_state_action.addStateChangeListener(JekyllKit
                    .getWorkFilesFrame());
            change_state_action.addStateChangeListener(JekyllKit
                    .getClassesFrame());
            change_state_action.addStateChangeListener(JekyllKit
                    .getRelationshipsFrame());
            change_state_action
                    .addStateChangeListener(JekyllKit.getSTYEditor());
            change_state_action.addStateChangeListener(JekyllKit
                    .getLexTypeEditor());
            change_state_action
                    .addStateChangeListener(JekyllKit.getDefEditor());
            change_state_action.addStateChangeListener(JekyllKit
                    .getLexRelsEditor());
            change_state_action.addStateChangeListener(JekyllKit
                    .getAtomNotesFrame());
            change_state_action.addStateChangeListener(JekyllKit
                    .getConceptNotesFrame());
        }

        public void actionPerformed(ActionEvent e) {
            if (((JCheckBoxMenuItem) e.getSource()).getState()) {
                stateTF.setText(StateChangeEvent.BROWSE_STATE);
                change_state_action
                        .fireStateChanged(StateChangeEvent.BROWSE_STATE);
            } else {
                stateTF.setText(StateChangeEvent.EDIT_STATE);
                change_state_action
                        .fireStateChanged(StateChangeEvent.EDIT_STATE);
            }
        }
    } // SetStateAction

    class SetLanguagesAction extends AbstractAction {
        private Component parent = null;

        public SetLanguagesAction(Component comp) {
            putValue(Action.SHORT_DESCRIPTION,
                    "set languages to include/exclude");

            parent = comp;
        }

        public void actionPerformed(ActionEvent e) {
            try {

                String[] lang_names = (String[]) JekyllKit.getLanguages()
                        .keySet().toArray(new String[0]);
                Arrays.sort(lang_names);

                Object[] langs = ListDialog
                        .showListMultipleMode(
                                parent,
                                "Select language(s) to include (all others will be excluded):",
                                "Include/Exclude language(s)", lang_names,
                                JekyllKit.getSelectedLanguages());

                // if dialog is cancelled
                if (langs.length == 0) {
                    return;
                }

                String[] lats = new String[langs.length];
                for (int i = 0; i < langs.length; i++) {
                    lats[i] = (String) JekyllKit.getLanguages().get(langs[i]);
                }

                JekyllKit.setLanguages(lats);
            } catch (Exception ex) {
                MEMEToolkit.notifyUser(null, "Failed to set languages."
                        + "\nLog file may contain more information.");
                ex.printStackTrace(JekyllKit.getLogWriter());
            }
        }
    } // SetLanguagesAction

    private class MainFrameTF extends JTextField {

        // Constructor
        public MainFrameTF() {
            setEditable(false);
            setFont(GUIToolkit.BUTTON_FONT);
        }
    } // MainFrameTF
    /**
    * Toggle the XML request/response log indicator.
    * New feature trying to add 
    *
    * @author Soma Lanka
    */
   public class XMLLogAction extends AbstractAction {

     /**
      * Instantiates a {@link DecreaseFontAction}.
      */
     public XMLLogAction() {
       super();
       // configure action
       putValue(Action.NAME, "Trace ON/OFF");
       putValue(Action.SHORT_DESCRIPTION, "Trace the Request/Response");
       putValue(Action.MNEMONIC_KEY,
                new Integer( (int) 'l'));
       }

     /**
      * Decrease the font.
      * @param e the {@link ActionEvent}
      */
     public void actionPerformed(ActionEvent e) {
   	  if (((JCheckBoxMenuItem) e.getSource()).getState()) {
   		  MEMEToolkit.setProperty("meme.xml.log.enabled","true");
   	  } else {
   		MEMEToolkit.setProperty("meme.xml.log.enabled","false");
   	  }
     }

   }

    /**
     * Returns the preferred size of this frame.
     * 
     * @return a dimension object indicating this component's preferred size.
     */
    public Dimension getPreferredSize() {
        return new Dimension(366, 357);
    }

    void setBrowseMode() {
        checkItem.setSelected(true);
        state_action.actionPerformed(new ActionEvent(checkItem, 1, "browse"));
        checkItem.setEnabled(false);
    }
}
