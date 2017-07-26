/*****************************************************************************
 * Package: gov.nih.nlm.umls.jekyll.swing
 * Object:  ReportFrame
 *
 * Author:  BAC
 *
 * History:
 *   11/13/2002: Javadoc Update
 *   01/07/2002: First Version
 *
 *****************************************************************************/
package gov.nih.nlm.umls.jekyll.swing;

import gov.nih.nlm.swing.SwingToolkit;
import gov.nih.nlm.meme.common.Concept;
import gov.nih.nlm.meme.common.Code;
import gov.nih.nlm.util.FieldedStringTokenizer;
import gov.nih.nlm.swing.GlassComponent;
import gov.nih.nlm.swing.GlassPaneListener;
import gov.nih.nlm.swing.HtmlToStringTransferHandler;
import gov.nih.nlm.meme.client.PrintConceptReportAction;
import gov.nih.nlm.meme.client.FinderClient;
import gov.nih.nlm.meme.client.ReportsClient;
import gov.nih.nlm.meme.client.TestReportFrame;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.text.html.*;
import javax.swing.text.Style;
import java.util.Enumeration;

/**
 * This tester application is used to display concept
 * reports in an editor pane.  It is currently
 * employed by the {@link TestFinderFrame} and by the
 * RxNorm editor code.
 *
 * @author Brian Carlsen (bcarlsen@apelon.com)
 */
public class ReportFrame
    extends JFrame
    implements GlassPaneListener {

  //
  // private fields
  //
  private JEditorPane report;
  //private boolean visible = false;
  private ReportsClient r_client = null;
  private FinderClient f_client = null;
  //private String mid_service = null;
  private String id = "", text = "";
  private PrintConceptReportAction pcra = null;

  // Glass pane component
  private GlassComponent glass = null;
  private HtmlToStringTransferHandler transfer_handler = null;

  //
  // Constructors
  //

  /**
   * Instantiates a {@link TestReportFrame} connected to the
   * default mid service.
   * @throws Exception if clients cannot be initialized
   */
  public ReportFrame() throws Exception {
    this("editing-db");
  }

  /**
   * Instantiates a {@link TestReportFrame} connected to the
   * specified mid service.
   * @param mid_service a valid MID service name
   * @throws Exception if clients cannot be initialized
   */
  public ReportFrame(String mid_service) throws Exception {
    super("Concept Reports");

    r_client = new ReportsClient();
    r_client.setMaxReviewedRelationshipCount(10);
    f_client = new FinderClient();
    setMidService(mid_service);
    r_client.setContentType("text/html");
    System.out.println("TestReportFrame: setting read languages ...");
    //r_client.setReadLanguagesToInclude(new String[0]);
    //r_client.setReadLanguagesToInclude(new String[] {"ENG", "SPA"});
    //r_client.setReadLanguagesToInclude(new String[] {"ENG"});
    //r_client.setReadLanguagesToInclude(new String[] {"SPA"});

    //r_client.setReadLanguagesToExclude(new String[] {"SPA"});
    //r_client.setReadLanguagesToExclude(new String[] {"ENG"});
    //r_client.setReadLanguagesToExclude(new String[] {"ENG", "SPA"});
    r_client.setReadLanguagesToExclude(new String[0]);

    // Configure MEME4 Client (this could also be done at the
    // level of the script calling this application)

    setDefaultCloseOperation(HIDE_ON_CLOSE);

    glass = new GlassComponent(this);
    setGlassPane(glass);

    // Create a panel
    JPanel panel = new JPanel(new BorderLayout());

    report = new JEditorPane();
    transfer_handler = new HtmlToStringTransferHandler();
    report.setTransferHandler(transfer_handler);
    report.setContentType("text/html");
    report.setText("No Report");
    report.addHyperlinkListener(
        new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent he) {
        if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          String dsc = he.getDescription();
          if (dsc.indexOf("searchbyconceptid") != -1) {
            String[] args = FieldedStringTokenizer.split(dsc, "&");
            String[] pair = FieldedStringTokenizer.split(args[4], "=");
            id = FieldedStringTokenizer.split(pair[1], "#")[0];
            final int concept_id = Integer.parseInt(id);
            showReport(concept_id);
          }
          else if (dsc.indexOf("searchbycode") != -1) {
            String[] args = FieldedStringTokenizer.split(dsc, "&");
            String[] pair = FieldedStringTokenizer.split(args[4], "=");
            final String id = FieldedStringTokenizer.split(pair[1], "#")[0];
            Concept[] results = null;
            try {
              results = f_client.findConceptsByCode(new Code(id));
            }
            catch (Exception e) {}
            if (results.length == 0) {
              showMessage("No matches found.");
            }
            else if (results.length == 1) {
              showReport(results[0].getIdentifier().intValue());
            }
            else {
              StringBuffer html = new StringBuffer();
              html.append("<HTML><HEAD><TITLE>Code Search:").append(id)
                  .append("</TITLE></HEAD><BODY bgcolor=\"white\">")
                  .append("The following concepts contain atoms with code = ")
                  .append(id).append(".")
                  .append("<UL>");
              for (int i = 0; i < results.length; i++) {
                html.append("<LI><A HREF=\"0&1&2&action=searchbyconceptid&arg=")
                    .append(results[i].getIdentifier().intValue())
                    .append("#report\">")
                    .append(results[i].getIdentifier().intValue())
                    .append("</A>")
                    .append(": ")
                    .append(results[i].getPreferredAtom().toString());
              }
              html.append("</UL></BODY></HTML>");
              showMessage(html.toString());
            }
          }
        }
      }
    }
    );

    report.setEditable(false);

    System.out.println("document: " + report.getDocument().getClass().getName());
    HTMLDocument doc = (HTMLDocument) report.getDocument();

    StyleSheet styles = doc.getStyleSheet();

    Enumeration rules = styles.getStyleNames();
    while (rules.hasMoreElements()) {
      String name = (String) rules.nextElement();
      Style rule = styles.getStyle(name);
      System.out.println("font: " + doc.getFont(rule).getSize());
      Enumeration e = rule.getAttributeNames();
      while (e.hasMoreElements()) {
        System.out.println("attr: " + e.nextElement());
      }
      System.out.println(rule.toString());
    }

    JScrollPane pane = new JScrollPane();
    pane.setPreferredSize(new Dimension(800, 600));
    pane.setViewportView(report);

    panel.add(pane, BorderLayout.NORTH);

    // Button Panel
    JPanel btn_panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

    JButton close = new JButton("Close");
    close.addActionListener(
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    }
    );
    close.setMnemonic('C');
    btn_panel.add(close);

    JButton find = new JButton("Find");
    find.addActionListener(
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String str = SwingToolkit.getUserInput("Enter search string:");
        showNormalizedStringLookup(str);
      }
    }
    );
    close.setMnemonic('F');
    btn_panel.add(find);

    // Add print button.
//      pcra = new PrintConceptReportAction(this);
//      JButton print = new JButton(pcra);
    JButton print = new JButton("print");
    print.setMnemonic('P');
    btn_panel.add(print);

    panel.add(btn_panel, BorderLayout.SOUTH);
    setContentPane(panel);
    pack();
  }

  /**
   * Sets the mid service.
   * @param mid_service A MID service name
   */
  public void setMidService(String mid_service) {
    //this.mid_service = mid_service;
    r_client.setMidService(mid_service);
    f_client.setMidService(mid_service);
  }

  /**
   * Show the page of norm string matches.
   * @param lookup a string to look up
   */
  public void showNormalizedStringLookup(String lookup) {
    prepare();
    final String str = lookup;
    Thread thread = new Thread(
        new Runnable() {
      public void run() {
        glass.setVisible(true);
        pcra.setTitle("Lookup by norm string: " + str);
        setTitle("Lookup by norm string: " + str);
        showMessage("Looking up by norm string  '" + str + "' . . .");
        try {
          Concept[] results = null;
          try {
            results = f_client.findNormalizedStringMatches(str);
          }
          catch (Exception e) {
            e.printStackTrace();
          }
          if (results.length == 0) {
            showMessage("No matches found.");
          }
          else if (results.length == 1) {
            showReport(results[0].getIdentifier().intValue());
          }
          else {
            StringBuffer html = new StringBuffer();
            html.append("<HTML><HEAD><TITLE>Normalized String Search:").append(
                id)
                .append("</TITLE></HEAD><BODY bgcolor=\"white\">")
                .append(
                "The following concepts contain atoms with the norm string = '")
                .append(str).append("'.")
                .append("<UL>");
            for (int i = 0; i < results.length; i++) {
              html.append("<LI><A HREF=\"0&1&2&action=searchbyconceptid&arg=")
                  .append(results[i].getIdentifier().intValue())
                  .append("#report\">")
                  .append(results[i].getIdentifier().intValue())
                  .append("</A>")
                  .append(": ")
                  .append(results[i].getPreferredAtom().toString());
            }
            html.append("</UL></BODY></HTML>");
            showMessage(html.toString());
          }
          glass.setVisible(false);
        }
        catch (Exception e) {
          report.setText("Results Unavailable.");
          e.printStackTrace();
          glass.setVisible(false);
        }
        report.scrollRectToVisible(new Rectangle());
      }
    }
    );
    thread.start();
  }

  /**
   * Show a concept report for a concept id.
   * This code uses a separate thread so as to
   * not slow down the GUI.
   * @param concept_id the id to generate a report for
   */
  public void showReport(int concept_id) {
    prepare();
    final int id = concept_id;
    Thread thread = new Thread(
        new Runnable() {
      public void run() {
        glass.setVisible(true);
        setTitle("Fetching Concept Report: " + id);
        showMessage("Fetching Report For " + id + " . . .");
        try {
          String rpt = r_client.getReport(id);
          report.setText(rpt);
          text = rpt;
          report.setCaretPosition(0);
          pcra.setTitle("Concept Report: " + id);
          setTitle("Concept Report: " + id);
          glass.setVisible(false);
        }
        catch (Exception e) {
          report.setText("Concept Report Unavailable.");
          e.printStackTrace();
          glass.setVisible(false);
        }
      }
    }
    );
    thread.start();
  }

  String showReport(String atom_name) throws Exception {
    pcra.setTitle(null);
    Concept[] concepts = f_client.findExactStringMatches(atom_name);
    if (concepts.length > 0) {
      showReport(concepts[0].getIdentifier().intValue());
      return "";
    }
    else {
      return "No Report.";
    }
  }

  /**
   * Makes it visible and the front window.
   */
  private void prepare() {
    if (!isVisible()) {
      setVisible(true);
    }
    if (!isActive()) {
      toFront();
    }
  }

  /**
   * Show message other than the report.
   * @param msg the message to show
   */
  public void showMessage(String msg) {
    prepare();
    final String message = msg;
    javax.swing.SwingUtilities.invokeLater(
        new Runnable() {
      public void run() {
//          pcra.setTitle(null);
        report.setText(message);
        text = message;
        report.setCaretPosition(0);
      }
    }
    );
  }

  /**
   * Returns the text behind the report.
   * @return the text behind the report
   */
  public String getText() {
    return text;
  }

  /**
   * Enable the glass pane.
   */
  public void enableGlassPane() {
    glass.setVisible(true);
  }

  /**
   * Disable the glass pane.
   */
  public void disableGlassPane() {
    glass.setVisible(false);
  }

  /**
   * Application entry point.
   * @param args a {@link String}<code>[]</code> of length 1 containing
   *        a single concept id
   */
  public static void main(String[] args) {

    try {
      ReportFrame frame = new ReportFrame("");
      frame.setVisible(true);
      frame.showNormalizedStringLookup(SwingToolkit.getUserInput(
          "Enter a string"));
      //frame.showReport(Integer.parseInt(args[0]));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

}
