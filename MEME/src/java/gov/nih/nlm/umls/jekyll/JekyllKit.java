/*
 * JekyllKit.java
 * Modified: Soma Lanka: 12/06/2005: Changed the release_date variable to 12/06/2005
 * Modified: Soma Lanka: 12/15/2005 -- Seibel Ticket Number: 1-70HJ5 : Authenticate the user 
 * 						-- Also changed the release date to 12/15/2005
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.Version;
import gov.nih.nlm.meme.action.MolecularTransaction;
import gov.nih.nlm.meme.client.ActionClient;
import gov.nih.nlm.meme.client.AdminClient;
import gov.nih.nlm.meme.client.AuxiliaryDataClient;
import gov.nih.nlm.meme.client.ClientConstants;
import gov.nih.nlm.meme.client.ClientToolkit;
import gov.nih.nlm.meme.client.CoreDataClient;
import gov.nih.nlm.meme.client.FinderClient;
import gov.nih.nlm.meme.client.ReportsClient;
import gov.nih.nlm.meme.client.TestReportFrame;
import gov.nih.nlm.meme.client.WorklistClient;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.common.Identifier;
import gov.nih.nlm.meme.common.Language;
import gov.nih.nlm.meme.integrity.EnforcableIntegrityVector;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.umls.jekyll.relae.RelaEditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.Ostermiller.util.PasswordDialog;

/**
 * Most of the initialization done here.
 * 
 * @see <a href="src/JekyllKit.java.html">source </a>
 */
public class JekyllKit {

    //
    // Private static fields
    //
    private static String data_source = System.getProperty("db.name");

    private static String release_date = System.getProperty("release.date");

    private static String host = null;

    private static int port = 0;

    private static EditorPreferences editor_prefs = null;

    private static Authority authority = null;

    private static MIDPasswordAuthenticator mpa = null;

    private static String session_id = null;

    private static Hashtable languages = null;

    private static String[] excluded_lats = null;

    private static String[] included_lats = null;

    private static String[] selected_lats = null;

    private static int action_id = 0;
    
    private static int days_left_till_expiration = 0;

    // Components
    private static MainFrame mainFrame = null;

    private static WorkFilesFrame workFilesFrame = null;

    private static ConceptSelector conceptSelector = null;

    private static Finder finder = null;

    private static ConceptFrame conceptFrame = null;

    private static ClassesFrame classesFrame = null;

    private static AttributesFrame attributes_frame = null;

    private static RelationshipsFrame relationshipsFrame = null;

    private static TestReportFrame reportFrame = null;

    private static STYEditor sty_editor = null;

    private static LexTypeEditor lex_type_editor = null;

    private static DefEditor def_editor = null;

    private static LexRelsEditor lex_rels_editor = null;

    private static AtomNotesFrame atom_notes_frame = null;

    private static ConceptNotesFrame concept_notes_frame = null;

    private static ConceptIdFrame concept_id_frame = null;

    private static InsertAtomFrame insert_atom_frame = null;

    private static ChangeTBRFrame change_tbr_frame = null;

    private static ChangeSuppFrame change_supp_frame = null;

    private static UndoRedoFrame undo_redo_frame = null;

    private static RelaEditor rela_editor = null;

    // MEME4 clients
    private static ActionClient default_action_client = null;

    private static ActionClient approval_action_client = null;

    private static ActionClient no_integrity_action_client = null;

    private static AdminClient admin_client = null;

    private static AuxiliaryDataClient aux_data_client = null;

    private static CoreDataClient core_data_client = null;

    private static FinderClient finder_client = null;

    private static ReportsClient reports_client = null;

    private static WorklistClient worklist_client = null;

    private static Vector all_frames = new Vector();

    private static Vector frames = new Vector();

    private static Vector disabled_frames = new Vector();

    private static boolean frames_enabled = true;

    private static File log_file = null;

    private static PrintWriter log_writer = null;

    /**
     * Returns reference to the Main frame.
     */
    static MainFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Returns reference to the Work Files frame.
     */
    static WorkFilesFrame getWorkFilesFrame() {
        if (workFilesFrame == null) {
            workFilesFrame = new WorkFilesFrame();
            all_frames.add(workFilesFrame);
        }

        return workFilesFrame;
    }

    /**
     * Returns reference to the Concept Selector frame.
     */
    static ConceptSelector getConceptSelector() {
        if (conceptSelector == null) {
            conceptSelector = new ConceptSelector();
            all_frames.add(conceptSelector);
        }

        return conceptSelector;
    }

    /**
     * Returns reference to the Finder frame.
     */
    static Finder getFinder() {
        if (finder == null) {
            finder = new Finder();
            all_frames.add(finder);
        }

        return finder;
    }

    /**
     * Returns reference to the Concept frame.
     */
    static ConceptFrame getConceptFrame() {
        if (conceptFrame == null) {
            conceptFrame = new ConceptFrame();
            all_frames.add(conceptFrame);
            frames.add(conceptFrame);
        }

        return conceptFrame;
    }

    /**
     * Returns reference to the Classes frame.
     */
    static ClassesFrame getClassesFrame() {
        if (classesFrame == null) {
            classesFrame = new ClassesFrame();
            all_frames.add(classesFrame);
            frames.add(classesFrame);
        }

        return classesFrame;
    }

    /**
     * Returns reference to the Concept Attributes frame.
     */
    static AttributesFrame getAttributesFrame() {
        if (attributes_frame == null) {
            attributes_frame = new AttributesFrame();
            all_frames.add(attributes_frame);
            frames.add(attributes_frame);
        }

        return attributes_frame;
    }

    /**
     * Returns reference to the Concept Relationships frame.
     */
    static RelationshipsFrame getRelationshipsFrame() {
        if (relationshipsFrame == null) {
            relationshipsFrame = new RelationshipsFrame();
            all_frames.add(relationshipsFrame);
            frames.add(relationshipsFrame);
        }

        return relationshipsFrame;
    }

    /**
     * Returns reference to the STY Editor.
     */
    static STYEditor getSTYEditor() {
        if (sty_editor == null) {
            sty_editor = new STYEditor();
            all_frames.add(sty_editor);
            frames.add(sty_editor);
        }

        return sty_editor;
    }

    /**
     * Returns reference to the Lexical Rels Editor.
     */
    static LexRelsEditor getLexRelsEditor() {
        if (lex_rels_editor == null) {
            lex_rels_editor = new LexRelsEditor();
            all_frames.add(lex_rels_editor);
            frames.add(lex_rels_editor);
        }

        return lex_rels_editor;
    }

    /**
     * Returns reference to the Lexical Type Editor.
     */
    static LexTypeEditor getLexTypeEditor() {
        if (lex_type_editor == null) {
            lex_type_editor = new LexTypeEditor();
            all_frames.add(lex_type_editor);
            frames.add(lex_type_editor);
        }

        return lex_type_editor;
    }

    /**
     * Returns reference to the Definitions Editor.
     */
    static DefEditor getDefEditor() {
        if (def_editor == null) {
            def_editor = new DefEditor();
            all_frames.add(def_editor);
            frames.add(def_editor);
        }

        return def_editor;
    }

    /**
     * Returns reference to the Atom Notes Frame.
     */
    static AtomNotesFrame getAtomNotesFrame() {
        if (atom_notes_frame == null) {
            atom_notes_frame = new AtomNotesFrame();
            all_frames.add(atom_notes_frame);
            frames.add(atom_notes_frame);
        }

        return atom_notes_frame;
    }

    /**
     * Returns reference to the Concept Notes Frame.
     */
    static ConceptNotesFrame getConceptNotesFrame() {
        if (concept_notes_frame == null) {
            concept_notes_frame = new ConceptNotesFrame();
            all_frames.add(concept_notes_frame);
            frames.add(concept_notes_frame);
        }

        return concept_notes_frame;
    }

    /**
     * Returns reference to the "Concept Id" Frame.
     */
    static ConceptIdFrame getConceptIdFrame() {
        if (concept_id_frame == null) {
            concept_id_frame = new ConceptIdFrame();
            all_frames.add(concept_id_frame);
        }

        return concept_id_frame;
    }

    /**
     * Returns reference to the "Insert Atom" Frame.
     */
    static InsertAtomFrame getInsertAtomFrame() {
        if (insert_atom_frame == null) {
            insert_atom_frame = new InsertAtomFrame();
            all_frames.add(insert_atom_frame);
            frames.add(insert_atom_frame);
        }

        return insert_atom_frame;
    }

    /**
     * Returns reference to the "Change Releasability" Frame.
     */
    static ChangeTBRFrame getChangeTBRFrame() {
        if (change_tbr_frame == null) {
            change_tbr_frame = new ChangeTBRFrame();
            all_frames.add(change_tbr_frame);
            frames.add(change_tbr_frame);
        }

        return change_tbr_frame;
    }

    /**
     * Returns reference to the "Change Suppressibility" Frame.
     */
    static ChangeSuppFrame getChangeSuppFrame() {
        if (change_supp_frame == null) {
            change_supp_frame = new ChangeSuppFrame();
            all_frames.add(change_supp_frame);
            frames.add(change_supp_frame);
        }

        return change_supp_frame;
    }

    /**
     * Returns reference to the "Undo/Redo Actions" Frame.
     */
    static UndoRedoFrame getUndoRedoFrame() {
        if (undo_redo_frame == null) {
            undo_redo_frame = new UndoRedoFrame();
            all_frames.add(undo_redo_frame);
            frames.add(undo_redo_frame);
        }

        return undo_redo_frame;
    }

    /**
     * Returns reference to the Report frame.
     */
    static synchronized TestReportFrame getReportFrame() throws Exception {
        if (reportFrame == null) {
            reportFrame = new TestReportFrame(getDataSource());
            all_frames.add(reportFrame);

            JMenuBar menuBar = new JMenuBar();

            // Options
            JMenu menu = new JMenu();
            menu.setText("Options");
            menu.setMnemonic('O');

            JMenuItem item = null;
            // Options -> Increase Report Font
            item = new JMenuItem("Increase Report Font");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ConceptReportAction.increaseFont();
                    ConceptReportAction.refreshScreen();
                }
            });
            menu.add(item);

            // Options -> Decrease Report Font
            item = new JMenuItem("Decrease Report Font");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ConceptReportAction.decreaseFont();
                    ConceptReportAction.refreshScreen();
                }
            });
            menu.add(item);

            // Options -> Make text size 5x bigger
            if (getEditorLevel() == 5) {
                // separator
                menu.addSeparator();

                item = new JMenuItem("Make text size 5x bigger");
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ConceptReportAction.increaseFont5xDefault();
                        ConceptReportAction.refreshScreen();
                    }
                });
                menu.add(item);
            }

            // separator
            menu.addSeparator();

            // Options -> Print Report
            item = new JMenuItem("Print Scaled Report");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ConceptReportAction.printReport(3);
                }
            });
            menu.add(item);

            // Options -> Print Report Portrait Style
            item = new JMenuItem("Print Report Portrait Style");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ConceptReportAction.printReport(1);
                }
            });
            menu.add(item);

            menuBar.add(menu);
            reportFrame.setJMenuBar(menuBar);
        }

        return reportFrame;
    }

    /**
     * Returns reference to RelaEditor frame.
     */
    static RelaEditor getRelaEditor() {
        if (rela_editor == null) {
            rela_editor = new RelaEditor();
            all_frames.add(rela_editor);
        }

        return rela_editor;
    }

    /**
     * Returns a list of frames eligible for refresh.
     */
    static JFrame[] getFrames() {
        return (JFrame[]) frames.toArray(new JFrame[0]);
    }

    // ----------------------------------
    // MEME4 clients
    // ----------------------------------

    /**
     * Returns reference to the action client, which is used to perform
     * molecular actions. It's configured to use default set of integrity
     * checks.
     */
    public synchronized static ActionClient getDefaultActionClient()
            throws Exception {
        if (default_action_client == null) {
            default_action_client = initActionClient();
            EnforcableIntegrityVector default_vector = getAuxDataClient()
                    .getApplicationVector("DEFAULT");
            default_action_client.setIntegrityVector(default_vector);
        }

        return default_action_client;
    }

    /**
     * Returns reference to the action client configured with a set of approval
     * integrity checks.
     */
    public synchronized static ActionClient getApprovalActionClient()
            throws Exception {
        if (approval_action_client == null) {
            approval_action_client = initActionClient();
            EnforcableIntegrityVector approval_vector = getAuxDataClient()
                    .getApplicationVector("APPROVAL");
            approval_action_client.setIntegrityVector(approval_vector);
        }

        return approval_action_client;
    }

    /**
     * Returns reference to the action client with no integrity checks.
     */
    public synchronized static ActionClient getNoIntegrityActionClient()
            throws Exception {
        if (no_integrity_action_client == null) {
            no_integrity_action_client = initActionClient();
            no_integrity_action_client.setIntegrityVector(null);
        }

        return no_integrity_action_client;
    }

    /**
     * Returns reference to the admin client.
     */
    public synchronized static AdminClient getAdminClient() throws Exception {
        if (admin_client == null) {
            admin_client = new AdminClient(getDataSource());
            admin_client.setSessionId(getSessionId());
            admin_client.setTimeout(100000000);
        }

        return admin_client;
    }

    /**
     * Returns reference to the auxiliary data client.
     */
    public synchronized static AuxiliaryDataClient getAuxDataClient()
            throws Exception {
        if (aux_data_client == null) {
            aux_data_client = new AuxiliaryDataClient(getDataSource());
            aux_data_client.setAuthentication(mpa.getAuthentication());
            aux_data_client.setSessionId(getSessionId());
            aux_data_client.setTimeout(100000000);
        }

        return aux_data_client;
    }

    /**
     * Returns reference to the core data client.
     */
    public synchronized static CoreDataClient getCoreDataClient()
            throws Exception {
        if (core_data_client == null) {
            core_data_client = new CoreDataClient(getDataSource());
            core_data_client.setAuthentication(mpa.getAuthentication());
            core_data_client.setSessionId(getSessionId());
            core_data_client.setTimeout(100000000);
        }

        return core_data_client;
    }

    /**
     * Returns reference to the finder client.
     */
    public synchronized static FinderClient getFinderClient() throws Exception {
        if (finder_client == null) {
            finder_client = new FinderClient(getDataSource());
            finder_client.setAuthentication(mpa.getAuthentication());
            finder_client.setSessionId(getSessionId());
            finder_client.setTimeout(100000000);
        }

        return finder_client;
    }

    /**
     * Returns reference to the reports client.
     */
    public synchronized static ReportsClient getReportsClient()
            throws Exception {
        if (reports_client == null) {
            reports_client = new ReportsClient(getDataSource());
            reports_client.setContentType("text/html");
            reports_client.setTimeout(100000000);
        }

        return reports_client;
    }

    /**
     * Returns reference to the worklist client.
     */
    public synchronized static WorklistClient getWorklistClient()
            throws Exception {
        if (worklist_client == null) {
            worklist_client = new WorklistClient(getDataSource());
            worklist_client.setAuthentication(mpa.getAuthentication());
            worklist_client.setSessionId(getSessionId());
            worklist_client.setTimeout(100000000);
        }

        return worklist_client;
    }

    // ----------------------------------
    // Various Accessor Methods
    // ----------------------------------

    /**
     * Returns current authority.
     */
    public synchronized static Authority getAuthority() {
        if (authority == null) {
            authority = new Authority.Default("E-" + editor_prefs.getInitials());
        }

        return authority;
    }

    /**
     * Returns editor's initials.
     */
    public synchronized static String getEditorInitials() {
        return editor_prefs.getInitials();
    }

    /**
     * Returns editor's level.
     */
    public synchronized static int getEditorLevel() {
        return editor_prefs.getEditorLevel();
    }

    /**
     * Returns editor's status.
     * 
     * @return editor's status, true if an editor is current, false otherwise.
     */
    public synchronized static boolean isEditorCurrent() {
        return editor_prefs.isCurrent();
    }
    
    /**
     * Soma Lanka: Adding this method to set the isEditingEnabled.
     */
    public synchronized static boolean isEditingEnabled() {
    	boolean enabled;
    	try {
    		enabled =  getAdminClient().isEditingEnabled();
    	} catch (Exception ex) {
    		return false;
    	}
    	return enabled;
    }
    /**
     * Returns current data source.
     */
    public synchronized static String getDataSource() {
        if (data_source.endsWith("-db")) {
            try {
                return MIDServices.getService(data_source);
            } catch (Exception ex) {
                ex.printStackTrace(JekyllKit.getLogWriter());
                return "";
            }
        } else {
            return data_source;
        }
    }

    /**
     * Returns version information of the current MEME server.
     */
    static String getServerInfo() {
        try {
            String version = getAdminClient().getServerVersion();
            int beginIndex = version.indexOf('4');
            int endIndex = version.indexOf('(');
            String server_info = getHost() + " [port: " + getPort() + "]\n"
                    + version.substring(beginIndex, endIndex);
            return server_info;
        } catch (Exception ex) {
            MEMEToolkit.logComment(
                    "Failed to retrieve information about the server", true);
            ex.printStackTrace(JekyllKit.getLogWriter());
            return null;
        }
    }

    /**
     * Returns <code>true</code> if at lease one of the application's windows
     * is visible (except Main Frame screen), <code>false</code> otherwise.
     */
    static boolean anyWindowsOpen() {
        boolean visible = false;

        for (int i = 0; i < all_frames.size(); i++) {
            Component comp = (Component) all_frames.get(i);
            if (comp.isVisible()) {
                visible = true;
                break;
            }
        }

        return visible;
    }

    /**
     * Changes current data source and re-initializes all MEME4 clients with
     * this new setting.
     * 
     * @param service
     *                  a mid-service or data source
     */
    static void setDataSource(String service) {
        data_source = service;

        if (default_action_client != null) {
            default_action_client.setMidService(getDataSource());
        }
        if (approval_action_client != null) {
            approval_action_client.setMidService(getDataSource());
        }
        if (no_integrity_action_client != null) {
            no_integrity_action_client.setMidService(getDataSource());
        }
        if (admin_client != null) {
            admin_client.setMidService(getDataSource());
        }
        if (aux_data_client != null) {
            aux_data_client.setMidService(getDataSource());
        }
        if (core_data_client != null) {
            core_data_client.setMidService(getDataSource());
        }
        if (finder_client != null) {
            finder_client.setMidService(getDataSource());
        }
        if (reports_client != null) {
            reports_client.setMidService(getDataSource());
        }
        if (worklist_client != null) {
            worklist_client.setMidService(getDataSource());
        }
        if (reportFrame != null) {
            reportFrame.setMidService(getDataSource());
        }

        if (rela_editor != null) {
            rela_editor.updateReportsClient(getDataSource());
        }
    } // setDataSource()

    /**
     * Returns current host for MEME server.
     */
    static String getHost() {
        return host;
    }

    /**
     * Returns current port for MEME server.
     */
    static int getPort() {
        return port;
    }

    /**
     * Returns current release date.
     */
    static String getReleaseDate() {
        return release_date;
    }

    /**
     * Returns current log file.
     * 
     * @return File
     */
    static File getLogFile() {
        return log_file;
    }

    /**
     * Re-initializes MEME4 client the new <code>server_host</code> and the
     * new <code>server_port</code>.
     */
    static void setServer(String server_host, int server_port) throws Exception {

        endSession();

        host = server_host;
        port = server_port;

        if (default_action_client != null) {
            default_action_client = null;
        }
        if (approval_action_client != null) {
            approval_action_client = null;
        }

        if (no_integrity_action_client != null) {
            no_integrity_action_client = null;
        }

        if (admin_client != null) {
            admin_client = null;
        }

        if (aux_data_client != null) {
            aux_data_client = null;
        }

        if (core_data_client != null) {
            core_data_client = null;
        }

        if (finder_client != null) {
            finder_client = null;
        }

        if (reports_client != null) {
            reports_client = null;
        }

        if (worklist_client != null) {
            worklist_client = null;
        }

        if (reportFrame != null) {
            reportFrame = null;
        }

        if (rela_editor != null) {
            rela_editor.resetReportsClient();
        }

        selected_lats = null;

        ClientToolkit.setProperty(ClientConstants.SERVER_HOST, host);
        ClientToolkit.setProperty(ClientConstants.SERVER_PORT, String
                .valueOf(port));

        startSession();

        MEMEToolkit.logComment("You are now using MEME server on host " + host
                + ", port " + port, true);
    } // setServer()

    /**
     * Initializes a new instance of an action client.
     */
    private static ActionClient initActionClient() throws Exception {
        ActionClient action_client = null;

        try {
            action_client = new ActionClient(getDataSource());
            action_client.setAuthority(getAuthority());
            action_client.setWorkIdentifier(new Identifier.Default(0));
            action_client.setTransactionIdentifier(getAuxDataClient()
                    .getNextIdentifierForType(MolecularTransaction.class));
            action_client.setChangeStatus(true);
            action_client.setAuthentication(mpa.getAuthentication());
            action_client.setSessionId(getSessionId());
            action_client.setTimeout(100000000);
        } catch (Exception ex) {
            throw new Exception("Failed to create an action client");
        }

        return action_client;
    } // initActionClient()

    private static void startSession() {
        try {
            getAdminClient().initiateSession();
            session_id = admin_client.getSessionId();
        } catch (Exception ex) {
            MEMEToolkit.logComment("Failed to initialize a session", true);
            ex.printStackTrace(JekyllKit.getLogWriter());
        }

        admin_client = null;
    }

    private static String getSessionId() {
        return session_id;
    }

    static void endSession() {
        try {
            getAdminClient().terminateSession();
        } catch (Exception ex) {
            MEMEToolkit.logComment("Failed to terminate current session", true);
            ex.printStackTrace(JekyllKit.getLogWriter());
        }
    }

    static void closeAllWindows() {
        for (int i = 0; i < all_frames.size(); i++) {
            Component comp = (Component) all_frames.get(i);
            if (comp.isVisible()) {
                comp.setVisible(false);
            }
        }
    }

    static void closeSomeWindows() {
        for (int i = 0; i < all_frames.size(); i++) {
            Component comp = (Component) all_frames.get(i);
            if ((comp instanceof WorkFilesFrame)
                    || (comp instanceof ConceptSelector)
                    || (comp instanceof Finder)
                    || (comp instanceof ConceptIdFrame)
                    || (comp instanceof RelaEditor)) {
                continue;
            }
            if (comp.isVisible()) {
                comp.setVisible(false);
            }
        }
    }

    static void disableFrames() {
        for (int i = 0; i < all_frames.size(); i++) {
            JFrame frame = (JFrame) all_frames.get(i);
            if (frame instanceof Finder) {
                continue;
            }
            if (frame.isVisible()) {
                GlassComponent glass_comp = (GlassComponent) frame
                        .getGlassPane();
                glass_comp.setVisible(true);
                // 		frame.setEnabled(false);
                disabled_frames.add(frame);
            }
        }

        frames_enabled = false;
    }

    static boolean isFramesEnabled() {
        return frames_enabled;
    }

    static void enableFrames() {
        for (int i = 0; i < disabled_frames.size(); i++) {
            JFrame frame = (JFrame) disabled_frames.get(i);
            GlassComponent glass_comp = (GlassComponent) frame.getGlassPane();
            glass_comp.setVisible(false);
            // 	    frame.setEnabled(true);
        }

        disabled_frames.clear();
        frames_enabled = true;
    }

    /**
     * All entries will be written to current directory.
     */
    public static PrintWriter getLogWriter() {
        try {
            if (log_writer == null) {
                String pattern = "MMddyy.HHmmss";
                SimpleDateFormat formatter = new SimpleDateFormat(pattern);
                Calendar calendar = Calendar.getInstance();
                String log_filename = "jekyll."
                        + formatter.format(calendar.getTime())
                        // 		    + calendar.get(Calendar.DAY_OF_MONTH)
                        // 		    + (calendar.get(Calendar.MONTH) + 1)
                        // 		    + calendar.get(Calendar.YEAR)
                        // 		    + "."
                        // 		    + calendar.get(Calendar.HOUR_OF_DAY)
                        // 		    + calendar.get(Calendar.MINUTE)
                        // 		    + calendar.get(Calendar.SECOND)
                        + ".log";

                File log_dir = new File(System.getProperty("user.home")
                        + "/meme/logs");
                if (!log_dir.exists()) {
                    log_dir.mkdirs();
                } else {
                    File[] files = log_dir.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].getName().startsWith("jekyll")) {
                            // removing all files older than a week
                            if ((calendar.getTimeInMillis() - files[i]
                                    .lastModified()) > 604800000) {
                                files[i].delete();
                            }
                        }
                    }
                }

                log_file = new File(log_dir, log_filename);
                log_writer = new PrintWriter(new FileWriter(log_file), true);
            }
        } catch (IOException ex) {
            MEMEToolkit
                    .reportError("Application failed to initialize a log file."
                            + "\nEditing will proceed without writing to the log.");
            ex.printStackTrace();
        }

        return log_writer;
    } // getLogWriter()

    
    /**
     * All entries will be written to current directory.
     */
    public static PrintWriter getXMLLogWriter() {
        try {
                String pattern = "MMddyy.HHmmss";
                SimpleDateFormat formatter = new SimpleDateFormat(pattern);
                Calendar calendar = Calendar.getInstance();
                String log_filename = "jekyll.xml."
                        + formatter.format(calendar.getTime())
                        // 		    + calendar.get(Calendar.DAY_OF_MONTH)
                        // 		    + (calendar.get(Calendar.MONTH) + 1)
                        // 		    + calendar.get(Calendar.YEAR)
                        // 		    + "."
                        // 		    + calendar.get(Calendar.HOUR_OF_DAY)
                        // 		    + calendar.get(Calendar.MINUTE)
                        // 		    + calendar.get(Calendar.SECOND)
                        + ".log";

                File log_dir = new File(System.getProperty("user.home")
                        + "/meme/logs");
                if (!log_dir.exists()) {
                    log_dir.mkdirs();
                } else {
                    File[] files = log_dir.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].getName().startsWith("jekyll")) {
                            // removing all files older than a week
                            if ((calendar.getTimeInMillis() - files[i]
                                    .lastModified()) > 604800000) {
                                files[i].delete();
                            }
                        }
                    }

                log_file = new File(log_dir, log_filename);
                log_writer = new PrintWriter(new FileWriter(log_file), true);
            }
        } catch (IOException ex) {
            MEMEToolkit
                    .reportError("Application failed to initialize a log file."
                            + "\nEditing will proceed without writing to the log.");
            ex.printStackTrace();
        }

        return log_writer;
    } // getLogWriter()
    public static Hashtable getLanguages() throws Exception {
        if (languages == null) {
            languages = new Hashtable();

            Language[] langs = JekyllKit.getAuxDataClient().getLanguages();

            for (int i = 0; i < langs.length; i++) {
                languages.put(langs[i].toString(), langs[i].getAbbreviation());
            }
        }

        return languages;
    } // getLanguages()

    static void setLanguages(String[] lats) throws Exception {
        getCoreDataClient().setReadLanguagesToInclude(lats);
        getReportsClient().setReadLanguagesToInclude(lats);

        Vector v = new Vector(languages.values());
        for (int i = 0; i < lats.length; i++) {
            if (v.contains(lats[i])) {
                v.remove(lats[i]);
            }
        }

        String[] ex_lats = (String[]) v.toArray(new String[0]);
        getCoreDataClient().setReadLanguagesToExclude(ex_lats);
        getReportsClient().setReadLanguagesToExclude(ex_lats);

        selected_lats = lats;
    } // setLanguages()

    static String[] getSelectedLanguages() {
        if (selected_lats == null) {
            return null;
        }

        java.util.List l = Arrays.asList(selected_lats);

        Vector v = new Vector();
        for (Enumeration e = languages.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            if (l.contains(languages.get(key))) {
                v.add(key);
            }
        }

        return (String[]) v.toArray(new String[0]);
    } // getSelectedLanguages()

    static int getNextActionId() {
        return action_id++;
    }

    static int getDaysLeftTillExpiration() {
        return days_left_till_expiration;
    }
    
    static void setDaysLeftTillExpiration(int days_left) {
        days_left_till_expiration = days_left;
    }
    
    /**
     * Start point of the application.
     */
    public static void main(String args[]) {
        MEMEToolkit.initializeLog();
        MEMEToolkit.setLog(getLogWriter());
/*
 * Soma Lanka: Adding a new log and setting the lgowriter in MEMEToolKit
 */
        MEMEToolkit.setXMLLog(getXMLLogWriter());
        MEMEToolkit
                .logComment("----------------------------------------------");
        MEMEToolkit.logComment("Starting Jekyll... "
                + Calendar.getInstance().getTime().toString());
        MEMEToolkit.logComment("Jekyll version: " + release_date);
        MEMEToolkit.logComment("Client MEME library version: "
                + Version.getVersion() + " " + Version.getDate());
        MEMEToolkit
                .logComment("----------------------------------------------");

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                PasswordDialog p = new PasswordDialog(null, "Jekyll");
                // put it in the center of the user's screen
                Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                p.setLocation(d.width / 2, d.height / 2);
                p.setVisible(true);

                while (p != null) {
                    if (p.okPressed()) {
                        mpa = new MIDPasswordAuthenticator();
                        mpa.setUsernameAndPassword(p.getName().toLowerCase(), p
                                .getPass().toCharArray());

                        if (!mpa.failed()) {
                            p.dispose();
                            p = null;
                            break;
                        } else {
                            MEMEToolkit.notifyUser(p, mpa.getReasonFailed());
                            p.setVisible(true);
                        }
                    } else {
                        System.out.println("User selected cancel");
                        System.exit(0);
                    }
                }

                try {
                    startSession();

                    host = MEMEToolkit.getProperty(ClientConstants.SERVER_HOST);
                    port = Integer.parseInt(MEMEToolkit
                            .getProperty(ClientConstants.SERVER_PORT));
                    editor_prefs = getAuxDataClient()
                            .getEditorPreferencesByUsername(mpa.getUsername());
//                  Soma: Setting the languages to include
                    String[]include_languages = MEMEToolkit.getProperty(ClientConstants.INCLUDE_LANGUAGES).split(",");
                    if (include_languages != null && include_languages.length > 0 && !include_languages[0].equals("")) {
                    	 JekyllKit.getLanguages();
                    	JekyllKit.setLanguages(include_languages);
                    }
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                            endSession();
                        }
                    });
                    
                    
                    MainFrame main_frame = new MainFrame();
                    if (!isEditorCurrent()) {
                        MEMEToolkit
                                .notifyUser("System indicates that you do not have a status of a current user."
                                        + "\nYour session will be read-only.");
                        main_frame.setBrowseMode();
                    } else if (!getAdminClient().isEditingEnabled()) {
                        if (getEditorLevel() == 5) {
                            JOptionPane.showMessageDialog(null,
                                    "Welcome superuser. For your information:\n"
                                            + "editing is currently cut off.",
                                    "Jekyll", JOptionPane.WARNING_MESSAGE);
                        } else {
                            MEMEToolkit
                                    .notifyUser(
                                            main_frame,
                                            "Editing is currently cut off."
                                                    + "\nJekyll will open in browse mode only.");
                            main_frame.setBrowseMode();
                        }
                    }
                    
                    main_frame.setVisible(true);
                } catch (Exception ex) {
                    MEMEToolkit
                            .reportError("Jekyll failed to initialize."
                                    + "\nConsole/log file may contain more information.");
                    ex.printStackTrace(getLogWriter());
                    System.exit(1);
                }
            }
        });
    } // main()
    
    /*
     * Soma Lanka: Adding a method to authenticate the user.
     */
    public static String authenticateUser(String userName, char[] password) {
    	try {
    	EditorPreferences ep = JekyllKit.getAdminClient().authenticate(
                userName, new String(password));

        if (ep != null) {
            // Set the mpa user name and password
        	mpa.setUsernameAndPassword(userName.toLowerCase(), password);
        	editor_prefs = ep;
        	return null;
        } else {
            return "Invalid User ID/Password";
        }    
    	} catch(Exception ex) {
    		return "Invalid User ID/Password";
    	}
    }
    public static boolean authenticateUser(){
    	 //mpa.setUsernameAndPassword(mpa.getUsername().toLowerCase(), mpa.getPassword());
        try {
	    	EditorPreferences ep = JekyllKit.getAdminClient().authenticate(
	                mpa.getUsername(), new String(mpa.getPassword()));
	
	        if (ep == null) {
	            return false;
	        } else {
	        	editor_prefs = ep;
	        }
        } catch (Exception ex) {
        	return false;
        }
        return true; 
    }
    
}
