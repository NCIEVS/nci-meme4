/*
 * ConceptReportAction.java
 *  * Modfied: Soma Lanka : Changes related to Attribute printing
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.client.ReportsClient;
import gov.nih.nlm.umls.jekyll.swing.TestReportFrame;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.util.HTMLDocumentRenderer;
import gov.nih.nlm.util.SystemToolkit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.SocketException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;

/**
 * Displays a report for the current concept.
 * 
 * @see <a href="src/ConceptReportAction.java.html">source </a>
 * @see AbstractAction
 */
public class ConceptReportAction extends AbstractAction {

    // action commands for report dispaly options
    public static final String WIN_PAR_CHD = "win.par.chd";

    public static final String WIN_PAR_CHD_SIB = "win.par.chd.sib";

    public static final String WIN_ALL_CONTEXT_RELS = "win.all.context.rels";

    public static final String WIN_XR_PAR_CHD = "win.xr.par.chd";

    public static final String WIN_XR_PAR_CHD_SIB = "win.xr.par.chd.sib";

    public static final String WIN_XR_ALL_CONTEXT_RELS = "win.xr.all.context.rels";

    public static final String ALL_PAR_CHD = "all.par.chd";

    public static final String ALL_PAR_CHD_SIB = "all.par.chd.sib";

    public static final String ALL_ALL_CONTEXT_RELS = "all.all.context.rels";

    //
    // Private Fields
    //
    private Reportable target = null;

    private TestReportFrame frame = null;

    private GlassComponent glass_comp = null;

    private static int current_font_size = 12;

    private static String current_frame_txt = null;

    // Constructor
    public ConceptReportAction(Reportable comp) {
        putValue(Action.NAME, "Concept Report");
        putValue(Action.SHORT_DESCRIPTION, "get Concept report");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R,
                Event.CTRL_MASK));
        putValue(Action.ACTION_COMMAND_KEY, WIN_PAR_CHD);
        putValue("Background", Color.green);

        target = comp;
    }

    public void actionPerformed(ActionEvent e) {

        ((Component) target).setCursor(Cursor
                .getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (target instanceof RootPaneContainer) {
            glass_comp = (GlassComponent) ((RootPaneContainer) target)
                    .getGlassPane();
            glass_comp.setVisible(true);
        }

        int concept_id = 0;

        try {
            concept_id = target.getConceptId();

            frame = JekyllKit.getReportFrame();

            if (concept_id != 0) {

                String action_cmd = e.getActionCommand();

                ReportsClient client = JekyllKit.getReportsClient();

                if (action_cmd.equals(WIN_PAR_CHD)) {
                    client.setRelationshipViewMode(ReportsClient.DEFAULT);
                    client
                            .setContextRelationshipViewMode(ReportsClient.DEFAULT);
                } else if (action_cmd.equals(WIN_PAR_CHD_SIB)) {
                    client.setRelationshipViewMode(ReportsClient.DEFAULT);
                    client
                            .setContextRelationshipViewMode(ReportsClient.INCLUDE_SIB);
                } else if (action_cmd.equals(WIN_ALL_CONTEXT_RELS)) {
                    client.setRelationshipViewMode(ReportsClient.DEFAULT);
                    client.setContextRelationshipViewMode(ReportsClient.ALL);
                } else if (action_cmd.equals(WIN_XR_PAR_CHD)) {
                    client.setRelationshipViewMode(ReportsClient.XR);
                    client
                            .setContextRelationshipViewMode(ReportsClient.DEFAULT);
                } else if (action_cmd.equals(WIN_XR_PAR_CHD_SIB)) {
                    client.setRelationshipViewMode(ReportsClient.XR);
                    client
                            .setContextRelationshipViewMode(ReportsClient.INCLUDE_SIB);
                } else if (action_cmd.equals(WIN_XR_ALL_CONTEXT_RELS)) {
                    client.setRelationshipViewMode(ReportsClient.XR);
                    client.setContextRelationshipViewMode(ReportsClient.ALL);
                } else if (action_cmd.equals(ALL_PAR_CHD)) {
                    client.setRelationshipViewMode(ReportsClient.ALL);
                    client
                            .setContextRelationshipViewMode(ReportsClient.DEFAULT);
                } else if (action_cmd.equals(ALL_PAR_CHD_SIB)) {
                    client.setRelationshipViewMode(ReportsClient.ALL);
                    client
                            .setContextRelationshipViewMode(ReportsClient.INCLUDE_SIB);
                } else if (action_cmd.equals(ALL_ALL_CONTEXT_RELS)) {
                    client.setRelationshipViewMode(ReportsClient.ALL);
                    client.setContextRelationshipViewMode(ReportsClient.ALL);
                }

                current_frame_txt = client.getReport(concept_id);
                frame.showMessage("<html><head><style> body {font-size: "
                        + current_font_size + "pt; }</style></head><body>"
                        + current_frame_txt + "</body></html>");
                frame.setTitle("Concept Report for: " + concept_id);
            } else {
                frame.showMessage("No Concept Selected.");
            }
        } catch (Exception ex) {
            current_frame_txt = null;

            if (ex instanceof MEMEException
                    && ((MEMEException) ex).getEnclosedException() instanceof SocketException) {
                MEMEToolkit.reportError((Component) target,
                        "There was a network error."
                                + "\nPlease try the action again.", false);
            } else {
                MEMEToolkit
                        .notifyUser(
                                (Component) target,
                                "Failed to get report for "
                                        + concept_id
                                        + "\nConsole/Log file may contain more information.");
            }
            ex.printStackTrace(JekyllKit.getLogWriter());
        } finally {
            glass_comp.setVisible(false);
            ((Component) target).setCursor(Cursor
                    .getPredefinedCursor(Cursor.HAND_CURSOR));

            if (frame.getExtendedState() == JFrame.ICONIFIED) {
                frame.setExtendedState(JFrame.NORMAL);
            }
            frame.setVisible(true);
        }
    } // actionPerformed();

    /**
     * Returns reference to current report frame.
     */
    public TestReportFrame getFrame() {
        return frame;
    }

    public static void increaseFont() {
        current_font_size += 2;
    }

    public static void decreaseFont() {
        current_font_size -= 2;
    }

    public static void increaseFont5xDefault() {
        current_font_size = 22;
    }

    public static void refreshScreen() {
        if (current_frame_txt == null) {
            return;
        }

        try {
            TestReportFrame frame = JekyllKit.getReportFrame();
            frame.showMessage("<html><head><style> body {font-size: "
                    + current_font_size + "pt; }</style></head><body>"
                    + current_frame_txt + "</body></html>");
        } catch (Exception ex) {
            ex.printStackTrace(JekyllKit.getLogWriter());
        }
    }

    /**
     * Alternative printing method, which allows to print reports in a font size
     * set by the user.
     */
    public static void printReport(final int pages_per_sheet) {
        if (current_frame_txt == null) {
            return;
        }

        Thread t = new Thread(new Runnable() {
            public void run() {
                TestReportFrame tr_frame = null;

                try {
                    tr_frame = JekyllKit.getReportFrame();
                    tr_frame.enableGlassPane();
                    HTMLDocumentRenderer hdr = new HTMLDocumentRenderer();
                    if (pages_per_sheet == 2) {
                        hdr.setTwoPagesPerSheet();
                    } else if (pages_per_sheet == 1) {
                        hdr.setOnePagePerSheet();
                    } else {
                        throw new Exception(
                                "Wrong number of pages per sheet specified. "
                                        + "It must be 1 or 2.");
                    }
                    hdr.setTitle(null);
                    JEditorPane editor = new JEditorPane();
                    editor.setContentType("text/html");
                    editor
                            .setText("<html><head><style> a {text-decoration: none; } body {font-size: "
                                    + current_font_size
                                    + "pt; }</style></head><body>"
                                    + SystemToolkit
                                            .removeLinks(current_frame_txt)
                                    + "</body></html>");
                    //HTMLDocument doc = (HTMLDocument)editor.getDocument();
                    hdr.print(editor);
                    tr_frame.disableGlassPane();
                } catch (Exception e) {
                    if (tr_frame != null) {
                        tr_frame.disableGlassPane();
                    }
                    MEMEToolkit.handleError(e);
                }
                ;
            }
        });
        t.start();

    } // printReport()

} // ConceptReportAction
